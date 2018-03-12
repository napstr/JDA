/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.requests;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.requests.ratelimit.BotRateLimiter;
import net.dv8tion.jda.core.requests.ratelimit.ClientRateLimiter;
import net.dv8tion.jda.core.utils.JDALogger;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.internal.http.HttpMethod;
import org.slf4j.Logger;

import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class SyncRequester implements Requester
{
    public static final Logger LOG = JDALogger.getLog(SyncRequester.class);

    protected final JDAImpl api;
    private final RateLimiter rateLimiter;

    private final OkHttpClient httpClient;

    private volatile boolean retryOnTimeout = false;

    public SyncRequester(JDA api)
    {
        this(api, api.getAccountType());
    }

    public SyncRequester(JDA api, AccountType accountType)
    {
        if (accountType == null)
            throw new NullPointerException("Provided accountType was null!");

        this.api = (JDAImpl) api;
        if (accountType == AccountType.BOT)
            rateLimiter = new BotRateLimiter(this, 5);
        else
            rateLimiter = new ClientRateLimiter(this, 5);

        this.httpClient = this.api.getHttpClientBuilder().build();
    }

    @Override
    public JDAImpl getJDA()
    {
        return api;
    }

    @Override
    public Logger getLog()
    {
        return LOG;
    }

    @Override
    public Long execute(Request<?> apiRequest, boolean handleOnRateLimit)
    {
        return execute(apiRequest, false, handleOnRateLimit);
    }

    public Long execute(Request<?> apiRequest, boolean retried, boolean handleOnRatelimit)
    {
        Route.CompiledRoute route = apiRequest.getRoute();
        Long retryAfter = rateLimiter.getRateLimit(route);
        if (retryAfter != null)
        {
            if (handleOnRatelimit)
                apiRequest.handleResponse(new Response(retryAfter, Collections.emptySet()));
            return retryAfter;
        }

        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();

        String url = DISCORD_API_PREFIX + route.getCompiledRoute();
        builder.url(url);

        String method = apiRequest.getRoute().getMethod().toString();
        RequestBody body = apiRequest.getBody();

        if (body == null && HttpMethod.requiresRequestBody(method))
            body = EMPTY_BODY;

        builder.method(method, body)
            .header("user-agent", USER_AGENT)
            .header("accept-encoding", "gzip");

        //adding token to all requests to the discord api or cdn pages
        //we can check for startsWith(DISCORD_API_PREFIX) because the cdn endpoints don't need any kind of authorization
        if (url.startsWith(DISCORD_API_PREFIX) && api.getToken() != null)
            builder.header("authorization", api.getToken());

        // Apply custom headers like X-Audit-Log-Reason
        // If customHeaders is null this does nothing
        if (apiRequest.getHeaders() != null)
        {
            for (Entry<String, String> header : apiRequest.getHeaders().entrySet())
                builder.addHeader(header.getKey(), header.getValue());
        }

        okhttp3.Request request = builder.build();

        Set<String> rays = new LinkedHashSet<>();
        okhttp3.Response[] responses = new okhttp3.Response[4];
        // we have an array of all responses to later close them all at once
        //the response below this comment is used as the first successful response from the server
        okhttp3.Response firstSuccess = null;
        try
        {
            int attempt = 0;
            do
            {
                //If the request has been canceled via the Future, don't execute.
                //if (apiRequest.isCanceled())
                //    return null;
                Call call = httpClient.newCall(request);
                firstSuccess = call.execute();
                responses[attempt] = firstSuccess;
                String cfRay = firstSuccess.header("CF-RAY");
                if (cfRay != null)
                    rays.add(cfRay);

                if (firstSuccess.code() < 500)
                    break; // break loop, got a successful response!

                attempt++;
                LOG.debug("Requesting {} -> {} returned status {}... retrying (attempt {})",
                    apiRequest.getRoute().getMethod(),
                    url, firstSuccess.code(), attempt);
                try
                {
                    Thread.sleep(50 * attempt);
                } catch (InterruptedException ignored)
                {
                }
            }
            while (attempt < 3 && firstSuccess.code() >= 500);

            if (firstSuccess.code() >= 500)
            {
                //Epic failure from other end. Attempted 4 times.
                return null;
            }

            retryAfter = rateLimiter.handleResponse(route, firstSuccess);
            if (!rays.isEmpty())
                LOG.debug("Received response with following cf-rays: {}", rays);

            if (retryAfter == null)
                apiRequest.handleResponse(new Response(firstSuccess, -1, rays));
            else if (handleOnRatelimit)
                apiRequest.handleResponse(new Response(firstSuccess, retryAfter, rays));

            return retryAfter;
        } catch (SocketTimeoutException e)
        {
            if (retryOnTimeout && !retried)
                return execute(apiRequest, true, handleOnRatelimit);
            LOG.error("Requester timed out while executing a request", e);
            apiRequest.handleResponse(new Response(firstSuccess, e, rays));
            return null;
        } catch (Exception e)
        {
            LOG.error("There was an exception while executing a REST request", e); //This originally only printed on DEBUG in 2.x
            apiRequest.handleResponse(new Response(firstSuccess, e, rays));
            return null;
        } finally
        {
            for (okhttp3.Response r : responses)
            {
                if (r == null)
                    break;
                r.close();
            }
        }
    }

    @Override
    public OkHttpClient getHttpClient()
    {
        return this.httpClient;
    }

    @Override
    public RateLimiter getRateLimiter()
    {
        return rateLimiter;
    }

    @Override
    public void setRetryOnTimeout(boolean retryOnTimeout)
    {
        this.retryOnTimeout = retryOnTimeout;
    }

    @Override
    public void shutdown(long time, TimeUnit unit)
    {
        rateLimiter.shutdown(time, unit);
    }

    @Override
    public void shutdownNow()
    {
        rateLimiter.forceShutdown();
    }

}
