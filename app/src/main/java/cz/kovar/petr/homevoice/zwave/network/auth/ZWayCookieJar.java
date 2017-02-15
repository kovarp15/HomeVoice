/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 31.01.2017.
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

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class ZWayCookieJar implements CookieJar {

    public static final String CLOUD_COOKIE = "ZBW_SESSID";
    public static final String ZWAY_COOKIE = "ZWaySession";

    private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        List<Cookie> ZWayCookies = new ArrayList<>();
        for(Cookie cookie : cookies) {
            if(cookie.name().compareToIgnoreCase(CLOUD_COOKIE) == 0
                    || cookie.name().compareToIgnoreCase(ZWAY_COOKIE) == 0
                    && !TextUtils.isEmpty(cookie.value())) {
                ZWayCookies.add(cookie);
            }
            cookieStore.put(url.host(), ZWayCookies);
        }
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> cookies = cookieStore.get(url.host());
        return cookies != null ? cookies : new ArrayList<Cookie>();
    }

}
