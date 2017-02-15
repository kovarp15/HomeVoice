/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 24.01.2017.
 * Copyright (c) 2017 Petr Kovář
 *
 * All rights reserved
 * kovarp15@fel.cvut.cz
 * HomeVoice for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * HomeVoice for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HomeVoice for Android.  If not, see <http://www.gnu.org/licenses/>.
 */

package cz.kovar.petr.homevoice.zwave.network.auth;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import okhttp3.OkHttpClient;

/**
 * HttpClientHelper creates new HTTPS client
 */
public class HttpClientHelper {

    private static final String LOG_TAG = "HttpClientHelper";

    public static final int DEFAULT_DELAY = 30000;

    public static OkHttpClient createHttpsClient() {
        return createHttpsClient(DEFAULT_DELAY);
    }

    public static OkHttpClient createHttpsClient(int delay) {
        final int actualDelay = delay < 0 ? DEFAULT_DELAY : delay;
        try {
            //TODO set default port to 8083
            final OkHttpClient.Builder client = createHttpClientBuilder(actualDelay);

            client.hostnameVerifier(new ZWaveHostnameVerifier());

            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new ZWaveTrustManager()}, null);
            client.sslSocketFactory(sslContext.getSocketFactory(), new ZWaveTrustManager());

            client.cookieJar(new ZWayCookieJar());

            return client.build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static OkHttpClient.Builder createHttpClientBuilder(int delay) {
        Log.v(LOG_TAG, "Delay - " + delay);
        return new OkHttpClient().newBuilder()
                .connectTimeout(delay, TimeUnit.MILLISECONDS)
                .readTimeout(delay, TimeUnit.MILLISECONDS);
    }

}
