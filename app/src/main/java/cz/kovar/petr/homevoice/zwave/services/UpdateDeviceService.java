/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 25.02.2017.
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

package cz.kovar.petr.homevoice.zwave.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import java.io.IOException;

import javax.inject.Inject;

import cz.kovar.petr.homevoice.app.ZWayApplication;
import cz.kovar.petr.homevoice.zwave.ApiClient;
import cz.kovar.petr.homevoice.zwave.dataModel.Device;

public class UpdateDeviceService extends IntentService {

    private static final String LOG_TAG = "UpdateDeviceService";

    private static final String ACTION_UPDATE_SWITCH_STATE = "me.z_wave.android.servises.action.UPDATE_SWITCH_STATE";
    private static final String ACTION_UPDATE_RGB = "me.z_wave.android.servises.action.UPDATE_RGB";
    private static final String ACTION_UPDATE_MODE = "me.z_wave.android.servises.action.UPDATE_MODE";
    private static final String ACTION_UPDATE_LEVEL = "me.z_wave.android.servises.action.UPDATE_LEVEL";
    private static final String ACTION_UPDATE_TOGGLE = "me.z_wave.android.servises.action.UPDATE_TOGGLE";
    private static final String ACTION_ZOOM_IN = "me.z_wave.android.servises.action.UPDATE_ZOOM_IN";
    private static final String ACTION_ZOOM_OUT = "me.z_wave.android.servises.action.UPDATE_ZOOM_OUT";
    private static final String ACTION_MOVE_CAMERA_LEFT = "me.z_wave.android.servises.action.MOVE_CAMERA_LEFT";
    private static final String ACTION_MOVE_CAMERA_RIGHT = "me.z_wave.android.servises.action.MOVE_CAMERA_RIGHT";
    private static final String ACTION_MOVE_CAMERA_UP = "me.z_wave.android.servises.action.MOVE_CAMERA_UP";
    private static final String ACTION_MOVE_CAMERA_DOWN = "me.z_wave.android.servises.action.MOVE_CAMERA_DOWN";
    private static final String ACTION_OPEN_CAMERA = "me.z_wave.android.servises.action.OPEN_CAMERA";
    private static final String ACTION_CLOSE_CAMERA = "me.z_wave.android.servises.action.CLOSE_CAMERA";

    private static final String EXTRA_DEVICE = "me.z_wave.android.servises.extra.EXTRA_DEVICE";

    @Inject
    ApiClient apiClient;

    public static void updateRgbColor(Context context, Device device) {
        Intent intent = new Intent(context, UpdateDeviceService.class);
        intent.setAction(ACTION_UPDATE_RGB);
        intent.putExtra(EXTRA_DEVICE, device);
        context.startService(intent);
    }

    public static void updateDeviceState(Context context, Device device) {
        Intent intent = new Intent(context, UpdateDeviceService.class);
        intent.setAction(ACTION_UPDATE_SWITCH_STATE);
        intent.putExtra(EXTRA_DEVICE, device);
        context.startService(intent);
    }

    public static void updateDeviceMode(Context context, Device device) {
        Intent intent = new Intent(context, UpdateDeviceService.class);
        intent.setAction(ACTION_UPDATE_MODE);
        intent.putExtra(EXTRA_DEVICE, device);
        context.startService(intent);
    }

    public static void updateDeviceLevel(Context context, Device device) {
        Intent intent = new Intent(context, UpdateDeviceService.class);
        intent.setAction(ACTION_UPDATE_LEVEL);
        intent.putExtra(EXTRA_DEVICE, device);
        context.startService(intent);
    }

    public static void updateDeviceToggle(Context context, Device device) {
        Intent intent = new Intent(context, UpdateDeviceService.class);
        intent.setAction(ACTION_UPDATE_TOGGLE);
        intent.putExtra(EXTRA_DEVICE, device);
        context.startService(intent);
    }

    public static void zoomCameraIn(Context context, Device device) {
        Intent intent = new Intent(context, UpdateDeviceService.class);
        intent.setAction(ACTION_ZOOM_IN);
        intent.putExtra(EXTRA_DEVICE, device);
        context.startService(intent);
    }

    public static void zoomCameraOut(Context context, Device device) {
        Intent intent = new Intent(context, UpdateDeviceService.class);
        intent.setAction(ACTION_ZOOM_OUT);
        intent.putExtra(EXTRA_DEVICE, device);
        context.startService(intent);
    }

    public static void moveCameraLeft(Context context, Device device) {
        Intent intent = new Intent(context, UpdateDeviceService.class);
        intent.setAction(ACTION_MOVE_CAMERA_LEFT);
        intent.putExtra(EXTRA_DEVICE, device);
        context.startService(intent);
    }

    public static void moveCameraRight(Context context, Device device) {
        Intent intent = new Intent(context, UpdateDeviceService.class);
        intent.setAction(ACTION_MOVE_CAMERA_RIGHT);
        intent.putExtra(EXTRA_DEVICE, device);
        context.startService(intent);
    }

    public static void moveCameraUp(Context context, Device device) {
        Intent intent = new Intent(context, UpdateDeviceService.class);
        intent.setAction(ACTION_MOVE_CAMERA_UP);
        intent.putExtra(EXTRA_DEVICE, device);
        context.startService(intent);
    }

    public static void moveCameraDown(Context context, Device device) {
        Intent intent = new Intent(context, UpdateDeviceService.class);
        intent.setAction(ACTION_MOVE_CAMERA_DOWN);
        intent.putExtra(EXTRA_DEVICE, device);
        context.startService(intent);
    }

    public static void openCamera(Context context, Device device) {
        Intent intent = new Intent(context, UpdateDeviceService.class);
        intent.setAction(ACTION_OPEN_CAMERA);
        intent.putExtra(EXTRA_DEVICE, device);
        context.startService(intent);
    }

    public static void closeCamera(Context context, Device device) {
        Intent intent = new Intent(context, UpdateDeviceService.class);
        intent.setAction(ACTION_CLOSE_CAMERA);
        intent.putExtra(EXTRA_DEVICE, device);
        context.startService(intent);
    }

    public UpdateDeviceService() {
        super("UpdateDeviceService");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        ((ZWayApplication) getApplicationContext()).getComponent().inject(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final Device device = (Device) intent.getSerializableExtra(EXTRA_DEVICE);
            final String action = intent.getAction();
            if (ACTION_UPDATE_RGB.equals(action)) {
                updateRgbColor(device);
            } else if(ACTION_UPDATE_SWITCH_STATE.equals(action)) {
                updateSwitchState(device);
            } else if(ACTION_UPDATE_MODE.equals(action)) {
                updateMode(device);
            } else if(ACTION_UPDATE_LEVEL.equals(action)) {
                updateLevel(device);
            } else if(ACTION_UPDATE_TOGGLE.equals(action)) {
                updateToggle(device);
            } else if(ACTION_ZOOM_IN.equals(action)) {
                zoomIn(device);
            } else if(ACTION_ZOOM_OUT.equals(action)) {
                zoomOut(device);
            } else if(ACTION_MOVE_CAMERA_LEFT.equals(action)) {
                moveCameraLeft(device);
            } else if(ACTION_MOVE_CAMERA_RIGHT.equals(action)) {
                moveCameraRight(device);
            } else if(ACTION_MOVE_CAMERA_UP.equals(action)) {
                moveCameraUp(device);
            } else if(ACTION_MOVE_CAMERA_DOWN.equals(action)) {
                moveCameraDown(device);
            } else if(ACTION_OPEN_CAMERA.equals(action)) {
                openCamera(device);
            } else if(ACTION_CLOSE_CAMERA.equals(action)) {
                closeCamera(device);
            }
        }
    }


    private void updateRgbColor(Device device) {
        try {
            apiClient.updateRGBColor(device);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to update RGB color: ", e);
        }
    }

    private void updateSwitchState(Device device) {
        try {
            apiClient.updateDevicesState(device);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to update switch state: ", e);
        }
    }

    private void updateMode(Device device) {
        try {
            apiClient.updateDevicesMode(device);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to update mode: ", e);
        }
    }

    private void updateLevel(Device device) {
        try {
            apiClient.updateDevicesLevel(device);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to update level: ", e);
        }
    }

    private void updateToggle(Device device) {
        try {
            apiClient.updateToggle(device);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to toggle: ", e);
        }
    }

    private void zoomIn(Device device) {
        try {
            apiClient.zoomCameraIn(device);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to zoom in: ", e);
        }
    }

    private void zoomOut(Device device) {
        try {
            apiClient.zoomCameraOut(device);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to zoom out: ", e);
        }
    }

    private void moveCameraLeft(Device device) {
        try {
            apiClient.moveCameraLeft(device);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to move camera left: ", e);
        }
    }

    private void moveCameraRight(Device device) {
        try {
            apiClient.moveCameraRight(device);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to move camera right: ", e);
        }
    }

    private void moveCameraUp(Device device) {
        try {
            apiClient.moveCameraUp(device);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to move camera up: ", e);
        }
    }

    private void moveCameraDown(Device device) {
        try {
            apiClient.moveCameraDown(device);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to move camera down: ", e);
        }
    }

    private void openCamera(Device device) {
        try {
            apiClient.openCamera(device);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to open camera: ", e);
        }
    }

    private void closeCamera(Device device) {
        try {
            apiClient.closeCamera(device);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to close camera: ", e);
        }
    }
}
