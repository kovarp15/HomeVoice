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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.kovar.petr.homevoice.R;
import cz.kovar.petr.homevoice.bus.events.OnDataUpdatedEvent;
import cz.kovar.petr.homevoice.zwave.dataModel.Device;
import cz.kovar.petr.homevoice.zwave.dataModel.Filter;

public class FragmentDevices extends FragmentDevicesBase {

    private static final String LOG_TAG = "FragmentDevices";

    public static final String FILTERS = "filters";
    public static final String SHOW_HIDDEN = "show_hidden";

    private HashMap<Filter, String> m_filters = new HashMap<>();
    private boolean m_showHidden = false;

    private GridView m_gridView;

    public static FragmentDevices newInstance(HashMap<Filter, String> aFilters) {

        FragmentDevices f = new FragmentDevices();
        Bundle b = new Bundle();
        b.putSerializable(FILTERS, aFilters);
        b.putBoolean(SHOW_HIDDEN, true);

        f.setArguments(b);

        return f;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_devices, container, false);

        m_gridView = (GridView) v.findViewById(R.id.devicesGridView);
        m_showHidden = savedInstanceState.getBoolean(SHOW_HIDDEN);

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
        updateDevicesList(devices);
        //if(devices != null) {
        //    m_adapter.addAll(devices);
        //}
        m_gridView.setAdapter(m_adapter);
    }

    private List<Device> getFilteredDeviceList(){
        m_filters = (HashMap<Filter, String>) getArguments().getSerializable(FILTERS);
        List<Device> devices = dataContext.getDevices();

        for(Filter filter : m_filters.keySet()) {
            filterDevices(devices, filter, m_filters.get(filter));
        }

        return devices;
    }

    private void filterDevices(List<Device> aDevices, Filter aFilter, String aFilterValue) {
        Set<Device> devToRemove = new HashSet<>();
        for(Device device : aDevices) {
            switch (aFilter){
                case LOCATION:
                    if(!device.location.equalsIgnoreCase(aFilterValue)) devToRemove.add(device);
                    break;
                case TYPE:
                    if(!device.deviceType.toString().equalsIgnoreCase(aFilterValue)) devToRemove.add(device);
                    break;
                case TAG:
                    if(!device.tags.contains(aFilterValue)) devToRemove.add(device);
                    break;
            }
        }
        aDevices.removeAll(devToRemove);
    }

    private boolean isAppropriateDevice(Device device) {
        for(Filter filter : m_filters.keySet()) {
            String filterValue = m_filters.get(filter);
            switch (filter) {
                case LOCATION:
                    if(!device.location.equalsIgnoreCase(filterValue)) return false;
                    break;
                case TYPE:
                    if(device.deviceType != null && !device.deviceType.toString().equalsIgnoreCase(filterValue)) return false;
                    break;
                case TAG:
                    if(!device.tags.contains(filterValue)) return false;
                    break;
            }
        }
        return device.visibility;
    }

}
