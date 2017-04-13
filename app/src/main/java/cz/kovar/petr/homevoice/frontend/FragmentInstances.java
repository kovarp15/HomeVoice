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
package cz.kovar.petr.homevoice.frontend;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.squareup.otto.Subscribe;

import java.util.HashMap;
import java.util.List;

import cz.kovar.petr.homevoice.R;
import cz.kovar.petr.homevoice.bus.events.OnDataUpdatedEvent;
import cz.kovar.petr.homevoice.zwave.dataModel.Filter;
import cz.kovar.petr.homevoice.zwave.dataModel.Instance;

public class FragmentInstances extends FragmentInstancesBase {

    private static final String LOG_TAG = "FragmentInstances";

    public static final String FILTERS = "filters";

    private HashMap<Filter, String> m_filters = new HashMap<>();

    private GridView m_gridView;

    public static FragmentInstances newInstance() {

        return new FragmentInstances();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_instances, container, false);

        m_gridView = (GridView) v.findViewById(R.id.instancesGridView);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        prepareInstancesView();
    }

    protected void updateDevicesList(List<Instance> instances) {
        for(Instance instance : instances) {
            m_adapter.update(instance);
        }
    }

    @Subscribe
    public void onDataUpdated(OnDataUpdatedEvent event){
        /*Log.v(LOG_TAG, "Device list updated!");
        if(isCanUpdate) {
            updateDevicesList(event.devices);
            m_adapter.notifyDataSetChanged();
        }*/
    }

    private void prepareInstancesView(){
        updateDevicesList(dataContext.getInstances());
        //if(devices != null) {
        //    m_adapter.addAll(devices);
        //}
        m_gridView.setAdapter(m_adapter);
    }

}
