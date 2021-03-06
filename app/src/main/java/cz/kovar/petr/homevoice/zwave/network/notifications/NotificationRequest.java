/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 01.02.2017.
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

package cz.kovar.petr.homevoice.zwave.network.notifications;

import retrofit2.Callback;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NotificationRequest {

    @GET("/ZAutomation/api/v1/notifications")
    void getNotifications(@Query("limit") int limit, @Query("offset") int offset,
                          @Query("pagination") boolean pagination, Callback<NotificationResponse> callback);

    @GET("/ZAutomation/api/v1/notifications?pagination=true")
    NotificationResponse getNotifications(@Query("limit") int limit, @Query("since") long updateTime);

}
