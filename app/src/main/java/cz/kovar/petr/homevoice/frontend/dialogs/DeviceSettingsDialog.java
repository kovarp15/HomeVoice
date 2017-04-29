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

import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import cz.kovar.petr.homevoice.R;
import cz.kovar.petr.homevoice.app.ZWayApplication;
import cz.kovar.petr.homevoice.zwave.ApiClient;
import cz.kovar.petr.homevoice.zwave.DataContext;
import cz.kovar.petr.homevoice.zwave.dataModel.Device;
import cz.kovar.petr.homevoice.zwave.services.UpdateDeviceService;

public class DeviceSettingsDialog extends DialogFragment {

    private Device m_device;

    private EditText m_deviceTitle;
    private Spinner m_categorySpinner;
    private Spinner m_locationSpinner;
    private RadioGroup m_visibilitySwitch;

    private List<String> m_categoryList;

    @Inject
    DataContext dataContext;
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

        prepareDeviceTitle(view);
        prepareCategorySpinner(view);
        prepareLocationSpinner(view);
        prepareVisibilitySwitch(view);
        prepareCancelButton(view);
        prepareApplyButton(view);

    }

    private void prepareDeviceTitle(View aView) {
        m_deviceTitle = (EditText) aView.findViewById(R.id.deviceTitleEdit);
        m_deviceTitle.setText(m_device.metrics.title);
        m_deviceTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager imm = (InputMethodManager) getContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(m_deviceTitle.getWindowToken(), 0);
                    m_deviceTitle.clearFocus();
                }
                return false;
            }
        });
    }

    private void prepareCategorySpinner(View aView) {
        m_categorySpinner = (Spinner) aView.findViewById(R.id.deviceCategorySpinner);
        m_categoryList = Arrays.asList(getContext().getResources().getStringArray(R.array.device_categories));
        ArrayAdapter categoryAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.device_categories, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_categorySpinner.setAdapter(categoryAdapter);
        m_categorySpinner.setSelection(getCategoryByTags(m_device.tags));
    }

    private void prepareLocationSpinner(View aView) {
        m_locationSpinner = (Spinner) aView.findViewById(R.id.deviceLocationSpinner);
        List<String> locationNames = dataContext.getLocationsNames();
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, locationNames);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_locationSpinner.setAdapter(locationAdapter);
        m_locationSpinner.setSelection(m_device.location);
    }

    private void prepareVisibilitySwitch(View aView) {
        m_visibilitySwitch = (RadioGroup) aView.findViewById(R.id.visibilitySwitch);
        m_visibilitySwitch.check(getVisibilityId(m_device.visibility));
    }

    private void prepareCancelButton(View aView) {
        Button cancelButton = (Button) aView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    private void prepareApplyButton(View aView) {
        Button applyButton = (Button) aView.findViewById(R.id.applyButton);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = m_deviceTitle.getText().toString();
                if(!title.isEmpty()) {
                    m_device.metrics.title = title;
                }

                m_device.visibility = getVisibilityById(m_visibilitySwitch.getCheckedRadioButtonId());

                m_device.location = m_locationSpinner.getSelectedItemPosition();

                adjustTagsByCategory(m_device.tags, m_categoryList.get(m_categorySpinner.getSelectedItemPosition()));

                UpdateDeviceService.updateDevice(getContext(), m_device);

                dismiss();
            }
        });
    }

    private int getVisibilityId(boolean aVisible) {
        if(aVisible) {
            return R.id.visible;
        } else {
            return R.id.invisible;
        }
    }

    private boolean getVisibilityById(int aId) {
        return aId == R.id.visible;
    }

    private int getCategoryByTags(List<String> aTags) {
        for(String category : m_categoryList) {
            if(aTags.contains(category.toUpperCase())) {
                return m_categoryList.indexOf(category);
            }
        }
        return m_categoryList.size() - 1;
    }

    private void adjustTagsByCategory(List<String> aTags, String aCategory) {
        for(String category : m_categoryList) {
            if(aTags.contains(category.toUpperCase())) {
                aTags.remove(category.toUpperCase());
            }
        }
        aTags.add(aCategory.toUpperCase());
    }



}
