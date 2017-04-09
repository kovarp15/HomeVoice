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
package cz.kovar.petr.homevoice.zwave;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import cz.kovar.petr.homevoice.app.AppConfig;
import cz.kovar.petr.homevoice.zwave.dataModel.Device;
import cz.kovar.petr.homevoice.zwave.dataModel.DeviceRgbColor;
import cz.kovar.petr.homevoice.zwave.dataModel.DeviceType;
import cz.kovar.petr.homevoice.zwave.dataModel.Notification;
import cz.kovar.petr.homevoice.zwave.network.devices.DevicesStateRequest;
import cz.kovar.petr.homevoice.zwave.network.devices.DevicesStateResponse;
import cz.kovar.petr.homevoice.zwave.network.devices.UpdateDeviceRequest;
import cz.kovar.petr.homevoice.zwave.network.locations.LocationsRequest;
import cz.kovar.petr.homevoice.zwave.network.locations.LocationsResponse;
import cz.kovar.petr.homevoice.zwave.network.notifications.NotificationDataWrapper;
import cz.kovar.petr.homevoice.zwave.network.notifications.NotificationRequest;
import cz.kovar.petr.homevoice.zwave.network.notifications.NotificationResponse;
import cz.kovar.petr.homevoice.zwave.network.notifications.UpdateNotificationRequest;
import cz.kovar.petr.homevoice.zwave.utils.BooleanTypeAdapter;
import okhttp3.Cookie;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ApiClient {

    private static final String LOG_TAG = "ApiClient";

    public static interface ApiCallback<T, K> {
        public void onSuccess(T result);

        public void onFailure(K request, boolean isNetworkError);

    }

    public static interface EmptyApiCallback<T> {
        public void onSuccess();

        public void onFailure(T request, boolean isNetworkError);
    }

    private ZWayProfile m_localProtile;
    private Retrofit m_adaptor;
    private Cookie m_cookie;

    public void init(ZWayProfile aProfile, Retrofit aAdaptor, Cookie aCookie) {
        m_localProtile = aProfile;
        m_adaptor = aAdaptor;
        m_cookie = aCookie;
    }

    public Cookie getCookie() {
        return m_cookie;
    }

    private Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Boolean.class, new BooleanTypeAdapter())
                .create();
    }

    public DevicesStateResponse getDevices(long lastUpdateTime) throws IOException {
        return m_adaptor.create(DevicesStateRequest.class).getDevices(lastUpdateTime).execute().body();
    }

    public void updateDevicesState(final Device updatedDevice) throws IOException {
        final String state = updatedDevice.deviceType == DeviceType.DOORLOCK
                ? updatedDevice.metrics.mode : updatedDevice.metrics.level;

        m_adaptor.create(UpdateDeviceRequest.class).updateDeviceSwitchState(updatedDevice.id, state).execute().body();
    }

    public void updateDevicesMode(final Device updatedDevice) throws IOException {
        m_adaptor.create(UpdateDeviceRequest.class).updateMode(updatedDevice.id,
                updatedDevice.metrics.mode).execute().body();
    }

    public void updateDevicesLevel(final Device updatedDevice) throws IOException {
        m_adaptor.create(UpdateDeviceRequest.class).updateLevel(updatedDevice.id,
                updatedDevice.metrics.level).execute().body();
    }

    public void updateToggle(final Device updatedDevice) throws IOException {
        m_adaptor.create(UpdateDeviceRequest.class).updateToggle(updatedDevice.id).execute().body();
    }

    public void updateRGBColor(final Device updatedDevice) throws IOException {
        final DeviceRgbColor color = updatedDevice.metrics.color;
        m_adaptor.create(UpdateDeviceRequest.class).updateRGB(updatedDevice.id,
                color.r, color.g, color.b).execute().body();
    }

    public void moveCameraRight(final Device updatedDevice) throws IOException {
        m_adaptor.create(UpdateDeviceRequest.class).moveCameraRight(updatedDevice.id).execute().body();
    }

    public void moveCameraLeft(final Device updatedDevice) throws IOException {
        m_adaptor.create(UpdateDeviceRequest.class).moveCameraLeft(updatedDevice.id).execute().body();
    }

    public void moveCameraUp(final Device updatedDevice) throws IOException {
        m_adaptor.create(UpdateDeviceRequest.class).movevCameraUp(updatedDevice.id).execute().body();
    }

    public void moveCameraDown(final Device updatedDevice) throws IOException {
            m_adaptor.create(UpdateDeviceRequest.class).moveCameraDown(updatedDevice.id).execute().body();
    }

    public void zoomCameraIn(final Device updatedDevice) throws IOException {
        m_adaptor.create(UpdateDeviceRequest.class).zoomCameraIn(updatedDevice.id).execute().body();
    }

    public void zoomCameraOut(final Device updatedDevice) throws IOException {
        m_adaptor.create(UpdateDeviceRequest.class).zoomCameraOut(updatedDevice.id).execute().body();
    }

    public void openCamera(final Device updatedDevice) throws IOException {
        m_adaptor.create(UpdateDeviceRequest.class).openCamera(updatedDevice.id).execute().body();
    }

    public void closeCamera(final Device updatedDevice) throws IOException {
        m_adaptor.create(UpdateDeviceRequest.class).closeCamera(updatedDevice.id).execute().body();
    }

    public LocationsResponse getLocations() throws IOException {
        return m_adaptor.create(LocationsRequest.class).getLocations().execute().body();
    }

    public NotificationResponse getNotifications(final long lastUpdateTime) {
        return m_adaptor.create(NotificationRequest.class).getNotifications(AppConfig.NOTIFICATIONS_LIMIT, lastUpdateTime);
    }

    public void updateNotifications(final Notification notification,
                                    final EmptyApiCallback<String> callback) {
        m_adaptor.create(UpdateNotificationRequest.class).updateNotification(notification.id, notification
                , new Callback<NotificationResponse>() {
                    @Override
                    public void onResponse(Call<NotificationResponse> call, Response<NotificationResponse> response) {
                        Log.v(LOG_TAG, response.toString());
                        callback.onSuccess();
                    }

                    @Override
                    public void onFailure(Call<NotificationResponse> call, Throwable t) {
                        boolean networkUnreachable = isNetworkUnreachableError(t);
                        callback.onFailure("", networkUnreachable);
                    }
                }

        );
    }

    public void getNotificationPage(int offset, final ApiCallback<NotificationDataWrapper, String> callback) {
        if(m_adaptor != null) {
            m_adaptor.create(NotificationRequest.class).getNotifications(AppConfig.NOTIFICATIONS_LIMIT,
                    offset, true, new Callback<NotificationResponse>() {
                        @Override
                        public void onResponse(Call<NotificationResponse> call, Response<NotificationResponse> response) {
                            Log.v(LOG_TAG, response.toString());
                            callback.onSuccess(response.body().data);
                        }

                        @Override
                        public void onFailure(Call<NotificationResponse> call, Throwable t) {
                            boolean networkUnreachable = isNetworkUnreachableError(t);
                            callback.onFailure("", networkUnreachable);
                        }
                    }

            );
        } else {
            callback.onFailure("", false);
        }
    }

    /*public ProfilesResponse getProfiles() {
        return m_adaptor.create(ProfilesRequest.class).getProfiles();
    }

    public void updateProfile(Profile profile, final ApiCallback<List<Profile>, String> callback) {
        if(m_adaptor != null && profile != null) {
            m_adaptor.create(UpdateProfileRequest.class).updateProfile(profile.id, profile,
                    new Callback<ProfilesResponse>() {
                        @Override
                        public void onResponse(Call<NotificationResponse> call, Response<NotificationResponse> response) {
                            Log.v(LOG_TAG, response.toString());
                            callback.onSuccess(response.body().data);
                        }

                        @Override
                        public void onFailure(Call<NotificationResponse> call, Throwable t) {
                            boolean networkUnreachable = isNetworkUnreachableError(t);
                            callback.onFailure("", networkUnreachable);
                        }
                    }
            );
        }
    }*/

    private boolean isNetworkUnreachableError(Throwable aThrowable) {

        // TODO check if network unreachable
        return false;
        //return retrofitError.isNetworkError() && (retrofitError.getResponse() == null
        //        || retrofitError.getResponse().getStatus() != 404);
    }

    public boolean isPrepared() {
        return m_adaptor != null;
    }

    public void clear() {
        m_adaptor = null;
    }

}
