/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 11.04.2017.
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
package cz.kovar.petr.homevoice.zwave.network.instances;

import java.util.HashMap;
import java.util.Map;

import cz.kovar.petr.homevoice.zwave.dataModel.Instance;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface InstanceRequest {

    @GET("/ZAutomation/api/v1/instances")
    Call<InstanceResponse> getInstances();

    @PUT("/ZAutomation/api/v1/instances/{id}")
    Call<Instance> updateInstance(@Path("id") String id, @Body Instance instance);

    @POST("/ZAutomation/api/v1/instances")
    Call<Object> createInstance(@Body Instance aInstance);

}
