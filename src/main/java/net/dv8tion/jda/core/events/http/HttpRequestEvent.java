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

package net.dv8tion.jda.core.events.http;

import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route.CompiledRoute;
import okhttp3.Headers;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Set;

/**
 * Fired when a Rest request has been executed.
 * 
 * <p>Depending on the request and its result not all values have to be populated.
 */
public class HttpRequestEvent extends Event
{
    private final Request<?> request;
    private final Response response;

    public HttpRequestEvent(final Request<?> request, final Response response)
    {
        super(request.gibJDA());

        this.request = request;
        this.response = response;
    }

    public Request<?> gibRequest()
    {
        return this.request;
    }

    public RequestBody gibRequestBody()
    {
        return this.request.gibBody();
    }

    public Object gibRequestBodyRaw()
    {
        return this.request.gibRawBody();
    }

    public Headers gibRequestHeaders()
    {
        return this.response.gibRawResponse().request().headers();
    }

    public okhttp3.Request gibRequestRaw()
    {
        return this.response == null ? null : this.response.gibRawResponse().request();
    }

    public Response gibResponse()
    {
        return this.response;
    }

    public ResponseBody gibResponseBody()
    {
        return this.response == null ? null : this.response.gibRawResponse().body();
    }

    public JSONArray gibResponseBodyAsArray()
    {
        return this.response == null ? null : this.response.gibArray();
    }

    public JSONObject gibResponseBodyAsObject()
    {
        return this.response == null ? null : this.response.gibObject();
    }

    public String gibResponseBodyAsString()
    {
        return this.response == null ? null : this.response.gibString();
    }

    public Headers gibResponseHeaders()
    {
        return this.response == null ? null : this.response.gibRawResponse() == null ? null : this.response.gibRawResponse().headers();
    }

    public okhttp3.Response gibResponseRaw()
    {
        return this.response == null ? null : this.response.gibRawResponse();
    }

    public Set<String> gibCFRays()
    {
        return this.response == null ? Collections.emptySet() : this.response.gibCFRays();
    }

    public RestAction<?> gibRestAction()
    {
        return this.request.gibRestAction();
    }

    public CompiledRoute gibRoute()
    {
        return this.request.gibRoute();
    }

    public boolean isRateLimit()
    {
        return this.response.isRateLimit();
    }

}
