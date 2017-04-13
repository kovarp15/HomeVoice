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

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cz.kovar.petr.homevoice.frontend.adapters.InstancesGridAdapter;
import cz.kovar.petr.homevoice.zwave.dataModel.Instance;

public class FragmentInstancesBase extends FragmentBase
        implements InstancesGridAdapter.InstanceUpdatedListener {

    public static final int LIST_UPDATE_DELAY = 3000;

    public InstancesGridAdapter m_adapter;

    public Timer updateDelayTimer;
    public boolean isCanUpdate = true;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        m_adapter = new InstancesGridAdapter(getActivity(), new ArrayList<Instance>(), this);
    }

    @Override
    public void onActiveClicked(Instance updatedInstance) {
        startUpdateDelay();
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

    protected void updateInstancesList(List<Instance> instances) {

    }

    protected void onAfterDelayListUpdate() {

    }
}
