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

package cz.kovar.petr.homevoice.frontend;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.URLUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cz.kovar.petr.homevoice.UserData;
import cz.kovar.petr.homevoice.frontend.adapters.DevicesGridAdapter;
import cz.kovar.petr.homevoice.frontend.dialogs.CameraDialog;
import cz.kovar.petr.homevoice.utils.CameraUtils;
import cz.kovar.petr.homevoice.zwave.dataModel.Device;
import cz.kovar.petr.homevoice.zwave.services.UpdateDeviceService;

/**
 * Created by Ivan PL on 22.09.2014.
 */
public class FragmentDevicesBase extends FragmentBase
        implements DevicesGridAdapter.DeviceStateUpdatedListener {

    public static final int LIST_UPDATE_DELAY = 3000;

    public DevicesGridAdapter m_adapter;

    public Timer updateDelayTimer;
    public boolean isCanUpdate = true;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.e("FragmentDevicesBase", "onActivityCreated");
        m_adapter = new DevicesGridAdapter(getActivity(), new ArrayList<Device>(), this);
    }

    @Override
    public void onSwitchStateChanged(Device updatedDevice) {
        UpdateDeviceService.updateDeviceState(getActivity(), updatedDevice);
        startUpdateDelay();
    }

    @Override
    public void onSeekBarStateChanged(final Device updatedDevice) {
        UpdateDeviceService.updateDeviceLevel(getActivity(), updatedDevice);
        startUpdateDelay();
    }

    @Override
    public void onToggleClicked(Device updatedDevice) {
        UpdateDeviceService.updateDeviceToggle(getActivity(), updatedDevice);
        startUpdateDelay();
    }

    @Override
    public void onColorViewClicked(final Device device) {
        /*final ColorPickerDialog dialog = new ColorPickerDialog() {
            @Override
            public void onColorPicked(int color) {
                device.metrics.color.r = Color.red(color);
                device.metrics.color.g = Color.green(color);
                device.metrics.color.b = Color.blue(color);
                UpdateDeviceService.updateRgbColor(getActivity(), device);
                startUpdateDelay();
            }
        };
        dialog.setOldColor(device.metrics.color.getColorAsInt());
        bus.post(new ShowDialogEvent(dialog));*/
    }

    @Override
    public void onOpenCameraView(Device updatedDevice) {
        final String cameraUrl = CameraUtils.getCameraUrl(UserData.loadZWayProfile(getContext()),
                updatedDevice.metrics.url);
        Log.d("FragmentDevicesBase", "onOpenCameraView: " + cameraUrl);
        if(!TextUtils.isEmpty(cameraUrl)
                && URLUtil.isValidUrl(cameraUrl)) {
            CameraDialog dialog = CameraDialog.newInstance(updatedDevice);
            FragmentManager fm = getFragmentManager();
            dialog.show(fm, "camera");
            //bus.post(new StartActivityEvent(intent));
        } else {
            //bus.post(new ShowAttentionDialogEvent(getString(R.string.invalid_camera_url)));
        }
        /*final String cameraUrl = CameraUtils.getCameraUrl(profile, updatedDevice.metrics.url);
        if(!TextUtils.isEmpty(cameraUrl)
                && URLUtil.isValidUrl(cameraUrl)) {
            final Intent intent = new Intent(getActivity(), CameraActivity.class);
            intent.putExtra(CameraActivity.KEY_DEVICE, updatedDevice);
            bus.post(new StartActivityEvent(intent));
        } else {
            bus.post(new ShowAttentionDialogEvent(getString(R.string.invalid_camera_url)));
        }*/
    }

    @Override
    public void onPause() {
        super.onPause();
        isCanUpdate = true;
        if(updateDelayTimer != null) {
            updateDelayTimer.cancel();
            updateDelayTimer = null;
        }
    }

    public void startUpdateDelay() {
        if(updateDelayTimer != null) {
            updateDelayTimer.cancel();
        }

        updateDelayTimer = new Timer();
        updateDelayTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                isCanUpdate = true;
                if(isAdded()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onAfterDelayListUpdate();
                        }
                    });
                }
            }
        }, LIST_UPDATE_DELAY);
        isCanUpdate = false;
    }

    protected void updateDevicesList(List<Device> devices) {

    }

    protected void onAfterDelayListUpdate() {

    }

}
