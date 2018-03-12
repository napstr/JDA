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

import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.internal.http.HttpMethod;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

public interface Requester
{
    String DISCORD_API_PREFIX = String.format("https://discordapp.com/api/v%d/", JDAInfo.DISCORD_REST_VERSION);
    String USER_AGENT = "DiscordBot (" + JDAInfo.GITHUB + ", " + JDAInfo.VERSION + ")";
    MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    RequestBody EMPTY_BODY = RequestBody.create(null, new byte[]{});

    JDAImpl getJDA();

    Logger getLog();

    OkHttpClient getHttpClient();

    RateLimiter getRateLimiter();

    void setRetryOnTimeout(boolean retryOnTimeout);

    void shutdown(long time, TimeUnit unit);

    void shutdownNow();

    default <T> void request(Request<T> apiRequest)
    {
        if (getRateLimiter().isShutdown)
            throw new IllegalStateException("The Requester has been shutdown! No new requests can be requested!");

        if (apiRequest.shouldQueue())
            getRateLimiter().queueRequest(apiRequest);
        else
            execute(apiRequest, true);
    }

    default Long execute(Request<?> apiRequest)
    {
        return execute(apiRequest, false);
    }

    /**
     * Used to execute a Request. Processes request related to provided bucket.
     *
     * @param apiRequest        The API request that needs to be sent
     * @param handleOnRateLimit Whether to forward rate-limits, false if rate limit handling should take over
     * @return Non-null if the request was ratelimited. Returns a Long containing retry_after milliseconds until
     * the request can be made again. This could either be for the Per-Route ratelimit or the Global ratelimit.
     * <br>Check if globalCooldown is {@code null} to determine if it was Per-Route or Global.
     */
    Long execute(Request<?> apiRequest, boolean handleOnRateLimit);

    /**
     * Retrieves an {@link java.io.InputStream InputStream} for the provided {@link okhttp3.Response Response}.
     * <br>When the header for {@code content-encoding} is set with {@code gzip} this will wrap the body
     * in a {@link java.util.zip.GZIPInputStream GZIPInputStream} which decodes the data.
     * <p>
     * <p>This is used to make usage of encoded responses more user-friendly in various parts of JDA.
     *
     * @param response The not-null Response object
     * @return InputStream representing the body of this response
     * @throws IOException If a GZIP format error has occurred or the compression method used is unsupported
     */
    static InputStream getBody(okhttp3.Response response) throws IOException
    {
        String encoding = response.header("content-encoding", "");
        if (encoding.equals("gzip"))
            return new GZIPInputStream(response.body().byteStream());
        return response.body().byteStream();
    }

    /**
     * Builds an OkHttp request from JDAs internal {@link Request} against the Discord API, including figuring out the
     * http method and url, setting appropriate headers like authorization, gzip, user agent and any custom headers,
     * and setting a body if necessary.
     *
     * @param apiRequest the JDA request to be built into an okhttp request.
     * @return A request of the Okhttp lib against the Discord API ready to be sent off.
     */
    static okhttp3.Request buildOkHttpRequest(Request<?> apiRequest)
    {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();

        builder.url(DISCORD_API_PREFIX + apiRequest.getRoute().getCompiledRoute());

        String method = apiRequest.getRoute().getMethod().toString();
        RequestBody body = apiRequest.getBody();

        if (body == null && HttpMethod.requiresRequestBody(method))
            body = EMPTY_BODY;

        builder.method(method, body)
            .header("user-agent", USER_AGENT)
            .header("accept-encoding", "gzip");

        if (apiRequest.getJDA().getToken() != null)
            builder.header("authorization", apiRequest.getJDA().getToken());

        // Apply custom headers like X-Audit-Log-Reason
        // If customHeaders is null this does nothing
        if (apiRequest.getHeaders() != null)
        {
            for (Map.Entry<String, String> header : apiRequest.getHeaders().entrySet())
                builder.addHeader(header.getKey(), header.getValue());
        }

        return builder.build();
    }
}
