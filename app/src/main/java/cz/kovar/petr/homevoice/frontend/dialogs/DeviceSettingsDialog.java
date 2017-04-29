/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 29.04.2017.
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
package cz.kovar.petr.homevoice.frontend.dialogs;

import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import javax.inject.Inject;

import cz.kovar.petr.homevoice.R;
import cz.kovar.petr.homevoice.app.ZWayApplication;
import cz.kovar.petr.homevoice.zwave.ApiClient;
import cz.kovar.petr.homevoice.zwave.dataModel.Device;

public class DeviceSettingsDialog extends DialogFragment {

    private Device m_device;

    private EditText m_deviceTitle;
    private Switch m_deviceVisibility;

    @Inject
    ApiClient apiClient;

    public DeviceSettingsDialog() {}

    public static DeviceSettingsDialog newInstance(Device aDevice) {
        DeviceSettingsDialog frag = new DeviceSettingsDialog();
        Bundle args = new Bundle();
        args.putSerializable("device", aDevice);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_device_settings, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((ZWayApplication) getActivity().getApplicationContext()).getComponent().inject(this);

        m_device = (Device) getArguments().getSerializable("device");

        m_deviceTitle = (EditText) view.findViewById(R.id.deviceTitleEdit);
        m_deviceTitle.setText(m_device.metrics.title);

        m_deviceVisibility = (Switch) view.findViewById(R.id.deviceVisibilitySwitch);
        m_deviceVisibility.setChecked(m_device.visibility);

        Button cancelButton = (Button) view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeviceSettingsDialog.this.dismiss();
            }
        });

    }

}
