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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import cz.kovar.petr.homevoice.R;
import cz.kovar.petr.homevoice.bus.events.OnDataUpdatedEvent;
import cz.kovar.petr.homevoice.zwave.dataModel.Device;
import cz.kovar.petr.homevoice.zwave.dataModel.Filter;

public class FragmentDevices extends FragmentDevicesBase {

    private static final String LOG_TAG = "FragmentDevices";

    public static final String FILTER_KEY = "filter_key";
    public static final String FILTER_NAME_KEY = "filter_name_key";

    private Filter m_filter;
    private String m_filterValue;

    private GridView m_gridView;

    public static FragmentDevices newInstance(Filter filter, String filterValue) {

        FragmentDevices f = new FragmentDevices();
        Bundle b = new Bundle();
        b.putInt(FILTER_KEY, filter.ordinal());
        b.putString(FILTER_NAME_KEY, filterValue);

        f.setArguments(b);

        return f;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_devices, container, false);

        m_gridView = (GridView) v.findViewById(R.id.devicesGridView);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        prepareDevicesView();
    }

    protected void updateDevicesList(List<Device> devices) {
        for(Device device : devices) {
            if(isAppropriateDevice(device)) {
                m_adapter.update(device);
            } else {
                m_adapter.remove(device);
            }
        }
    }

    @Subscribe
    public void onDataUpdated(OnDataUpdatedEvent event){
        Log.v(LOG_TAG, "Device list updated!");
        if(isCanUpdate) {
            updateDevicesList(event.devices);
            m_adapter.notifyDataSetChanged();
        }
    }

    private void prepareDevicesView(){
        final List<Device> devices = getFilteredDeviceList();
        if(devices != null) {
            m_adapter.addAll(devices);
        }
        m_gridView.setAdapter(m_adapter);
    }

    private List<Device> getFilteredDeviceList(){
        m_filter = Filter.values()[getArguments().getInt(FILTER_KEY, 0)];
        m_filterValue = getArguments().getString(FILTER_NAME_KEY, Filter.DEFAULT_FILTER);
        switch (m_filter){
            case LOCATION:
                return dataContext.getDevicesForLocation(m_filterValue);
            case TYPE:
                return dataContext.getDevicesWithType(m_filterValue);
            case TAG:
                return dataContext.getDevicesWithTag(m_filterValue);
        }
        return new ArrayList<>();
    }

    private boolean isAppropriateDevice(Device device) {
        if(m_filterValue.equalsIgnoreCase(Filter.DEFAULT_FILTER))
            return true;

        switch (m_filter){
            case LOCATION:
                return device.location != null &&  device.location.equalsIgnoreCase(m_filterValue);
            case TYPE:
                return device.deviceType.toString().equalsIgnoreCase(m_filterValue);
            case TAG:
                return device.tags.contains(m_filterValue);
        }

        return true;
    }

}
