/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seyren.core.util.http;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.inject.Named;
import java.io.IOException;

/**
 * Created by ohad on 7/18/15.
 */
@Named
public class HttpUrlFetcher {

    public HttpResponse sendUrl(String url) throws IOException {
        return sendUrl(url,null,null);
    }

    public HttpResponse sendUrl(String url,String username,String password) throws IOException {
        HttpClient client = HttpClientBuilder.create().setRetryHandler(new DefaultHttpRequestRetryHandler(3,true)).build();

        HttpGet getMethod = new HttpGet(url);
        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)){
            getMethod = addAuthenticationHeader(getMethod,username,password);
        }

        return client.execute(getMethod);
    }

    private HttpGet addAuthenticationHeader(HttpGet getMethod, String username, String password) {
        String authString = username + ":" + password;
        byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
        String authStringEnc = new String(authEncBytes);
        getMethod.addHeader("Authorization","Basic " + authStringEnc);
        return getMethod;
    }
}
