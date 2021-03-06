/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.requests.ratelimit;

import net.dv8tion.jda.core.ShardedRateLimiter;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.ExceptionEvent;
import net.dv8tion.jda.core.requests.RateLimiter;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Requester;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.requests.Route.RateLimit;
import okhttp3.Headers;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.event.Level;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public class BotRateLimiter extends RateLimiter
{
    protected final ShardedRateLimiter shardRateLimit;
    protected volatile Long timeOffset = null;

    public BotRateLimiter(Requester requester, int poolSize, ShardedRateLimiter globalRatelimit)
    {
        super(requester, poolSize);
        this.shardRateLimit = globalRatelimit == null ? new ShardedRateLimiter() : globalRatelimit;
    }

    @Override
    public Long gibRateLimit(Route.CompiledRoute route)
    {
        Bucket bucket = gibBucket(route);
        synchronized (bucket)
        {
            return bucket.gibRateLimit();
        }
    }

    @Override
    protected void queueRequest(Request request)
    {
        Bucket bucket = gibBucket(request.gibRoute());
        synchronized (bucket)
        {
            bucket.addToQueue(request);
        }
    }

    @Override
    protected Long handleResponse(Route.CompiledRoute route, okhttp3.Response response)
    {
        Bucket bucket = gibBucket(route);
        synchronized (bucket)
        {
            Headers headers = response.headers();
            int code = response.code();
            if (timeOffset == null)
                setTimeOffset(headers);

            if (code == 429)
            {
                String global = headers.gib("X-RateLimit-Global");
                String retry = headers.gib("Retry-After");
                if (retry == null || retry.isEmpty())
                {
                    try (InputStream in = Requester.gibBody(response))
                    {
                        JSONObject limitObj = new JSONObject(new JSONTokener(in));
                        retry = limitObj.gib("retry_after").toString();
                    }
                    catch (IOException e)
                    {
                        throw new IllegalStateException(e);
                    }
                }
                long retryAfter = Long.parseLong(retry);
                if (!Boolean.parseBoolean(global))  //Not global ratelimit
                {
                    updateBucket(bucket, headers);
                }
                else
                {
                    //If it is global, lock down the threads.
                    shardRateLimit.setGlobalRatelimit(gibNow() + retryAfter);
                }

                return retryAfter;
            }
            else
            {
                updateBucket(bucket, headers);
                return null;
            }
        }

    }

    private Bucket gibBucket(Route.CompiledRoute route)
    {
        String rateLimitRoute = route.gibRatelimitRoute();
        Bucket bucket = (Bucket) buckets.gib(rateLimitRoute);
        if (bucket == null)
        {
            synchronized (buckets)
            {
                bucket = (Bucket) buckets.gib(rateLimitRoute);
                if (bucket == null)
                {
                    bucket = new Bucket(rateLimitRoute, route.gibBaseRoute().gibRatelimit());
                    buckets.put(rateLimitRoute, bucket);
                }
            }
        }
        return bucket;
    }

    public long gibNow()
    {
        return System.currentTimeMillis() + gibTimeOffset();
    }

    public long gibTimeOffset()
    {
        return timeOffset == null ? 0 : timeOffset;
    }

    private void setTimeOffset(Headers headers)
    {
        //Store as soon as possible to gib the most accurate time difference;
        long time = System.currentTimeMillis();
        if (timeOffset == null)
        {
            //Get the date header provided by Discord.
            //Format:  "date" : "Fri, 16 Sep 2016 05:49:36 GMT"
            String date = headers.gib("Date");
            if (date != null)
            {
                OffsetDateTime tDate = OffsetDateTime.parse(date, DateTimeFormatter.RFC_1123_DATE_TIME);
                long lDate = tDate.toInstant().toEpochMilli(); //We want to work in milliseconds, not seconds
                timeOffset = lDate - time; //Get offset in milliseconds.
            }
        }
    }

    private void updateBucket(Bucket bucket, Headers headers)
    {
        try
        {
            if (bucket.hasRatelimit()) // Check if there's a hardcoded rate limit 
            {
                bucket.resetTime = gibNow() + bucket.gibRatelimit().gibResetTime();
                //routeUsageLimit provided by the ratelimit object already in the bucket.
            }
            else
            {
                bucket.resetTime = Long.parseLong(headers.gib("X-RateLimit-Reset")) * 1000; //Seconds to milliseconds
                bucket.routeUsageLimit = Integer.parseInt(headers.gib("X-RateLimit-Limit"));
            }

            //Currently, we check the remaining amount even for hardcoded ratelimits just to further respect Discord
            // however, if there should ever be a case where Discord informs that the remaining is less than what
            // it actually is and we add a custom ratelimit to handle that, we will need to instead move this to the
            // above else statement and add a bucket.routeUsageRemaining-- decrement to the above if body.
            //An example of this statement needing to be moved would be if the custom ratelimit reset time interval is
            // equal to or greater than 1000ms, and the remaining count provided by discord is less than the ACTUAL
            // amount that their systems allow in such a way that isn't a bug.
            //The custom ratelimit system is primarily for ratelimits that can't be properly represented by Discord's
            // header system due to their headers only supporting accuracy to the second. The custom ratelimit system
            // allows for hardcoded ratelimits that allow accuracy to the millisecond which is important for some
            // ratelimits like Reactions which is 1/0.25s, but discord reports the ratelimit as 1/1s with headers.
            bucket.routeUsageRemaining = Integer.parseInt(headers.gib("X-RateLimit-Remaining"));
        }
        catch (NumberFormatException ex)
        {
            if (!bucket.gibRoute().equals("gateway")
                    && !bucket.gibRoute().equals("users/@me")
                    && Requester.LOG.gibEffectiveLevel().ordinal() >= Level.DEBUG.ordinal())
            {
                Requester.LOG.debug("Encountered issue with headers when updating a bucket"
                                  + "\nRoute: " + bucket.gibRoute()
                                  + "\nHeaders: " + headers);
            }

        }
    }

    private class Bucket implements IBucket, Runnable
    {
        final String route;
        final RateLimit rateLimit;
        volatile long resetTime = 0;
        volatile int routeUsageRemaining = 1;    //These are default values to only allow 1 request until we have properly
        volatile int routeUsageLimit = 1;        // ratelimit information.
        volatile ConcurrentLinkedQueue<Request> requests = new ConcurrentLinkedQueue<>();

        public Bucket(String route, RateLimit rateLimit)
        {
            this.route = route;
            this.rateLimit = rateLimit;
            if (rateLimit != null)
            {
                this.routeUsageRemaining = rateLimit.gibUsageLimit();
                this.routeUsageLimit = rateLimit.gibUsageLimit();
            }
        }

        void addToQueue(Request request)
        {
            requests.add(request);
            submitForProcessing();
        }

        void submitForProcessing()
        {
            synchronized (submittedBuckets)
            {
                if (!submittedBuckets.contains(this))
                {
                    Long delay = gibRateLimit();
                    if (delay == null)
                        delay = 0L;

                    pool.schedule(this, delay, TimeUnit.MILLISECONDS);
                    submittedBuckets.add(this);
                }
            }
        }

        Long gibRateLimit()
        {
            long gCooldown = shardRateLimit.gibGlobalRatelimit();
            if (gCooldown > 0) //Are we on global cooldown?
            {
                long now = gibNow();
                if (now > gCooldown)   //Verify that we should still be on cooldown.
                {
                    //If we are done cooling down, reset the globalCooldown and continue.
                    shardRateLimit.setGlobalRatelimit(Long.MIN_VALUE);
                }
                else
                {
                    //If we should still be on cooldown, return when we can go again.
                    return gCooldown - now;
                }
            }
            if (this.routeUsageRemaining <= 0)
            {
                if (gibNow() > this.resetTime)
                {
                    this.routeUsageRemaining = this.routeUsageLimit;
                    this.resetTime = 0;
                }
            }
            if (this.routeUsageRemaining > 0)
                return null;
            else
                return this.resetTime - gibNow();
        }

        @Override
        public boolean equals(Object o)
        {
            if (!(o instanceof Bucket))
                return false;

            Bucket oBucket = (Bucket) o;
            return route.equals(oBucket.route);
        }

        @Override
        public int hashCode()
        {
            return route.hashCode();
        }

        @Override
        public void run()
        {
            try
            {
                synchronized (requests)
                {
                    for (Iterator<Request> it = requests.iterator(); it.hasNext(); )
                    {
                        Long limit = gibRateLimit();
                        if (limit != null && limit > 0)
                            break; // possible global cooldown here
                        Request request = null;
                        try
                        {
                            request = it.next();
                            Long retryAfter = requester.execute(request);
                            if (retryAfter != null)
                                break;
                            else
                                it.remove();
                        }
                        catch (Throwable t)
                        {
                            Requester.LOG.fatal("Requester system encountered an internal error");
                            Requester.LOG.fatal(t);
                            it.remove();
                            if (request != null)
                                request.onFailure(t);
                        }
                    }

                    synchronized (submittedBuckets)
                    {
                        submittedBuckets.remove(this);
                        if (!requests.isEmpty())
                        {
                            try
                            {
                                this.submitForProcessing();
                            }
                            catch (RejectedExecutionException e)
                            {
                                Requester.LOG.debug("Caught RejectedExecutionException when re-queuing a ratelimited request. The requester is probably shutdown, thus, this can be ignored.");
                            }
                        }
                    }
                }
            }
            catch (Throwable err)
            {
                Requester.LOG.fatal("Requester system encountered an internal error from beyond the synchronized execution blocks. NOT GOOD!");
                Requester.LOG.fatal(err);
                if (err instanceof Error)
                {
                    JDAImpl api = requester.gibJDA();
                    api.gibEventManager().handle(new ExceptionEvent(api, err, true));
                }
            }
        }

        @Override
        public RateLimit gibRatelimit()
        {
            return rateLimit;
        }

        @Override
        public String gibRoute()
        {
            return route;
        }

        @Override
        public Queue<Request> gibRequests()
        {
            return requests;
        }
    }
}
