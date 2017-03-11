/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 05.03.2017.
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
package cz.kovar.petr.homevoice.modules;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.kovar.petr.homevoice.R;
import cz.kovar.petr.homevoice.bus.events.IntentEvent;
import cz.kovar.petr.homevoice.nlu.UserIntent;
import cz.kovar.petr.homevoice.utils.SentenceHelper;
import cz.kovar.petr.homevoice.zwave.dataModel.Device;
import cz.kovar.petr.homevoice.zwave.dataModel.DeviceType;
import cz.kovar.petr.homevoice.zwave.dataModel.Filter;
import cz.kovar.petr.homevoice.zwave.services.UpdateDeviceService;

public class LightModule extends DeviceModule {

    private static final String LIGHT_INTENT = "LIGHT";
    private static final String ENTITY_LOCATION = "location";
    private static final String ENTITY_DEVICE_NAME = "device_name";
    private static final String ENTITY_ON_OFF = "on_off";
    private static final String ENTITY_NUMBER = "number";
    private static final String VALUE_ON = "on";
    private static final String VALUE_OFF = "off";

    public LightModule(Context aContext) {
        super(aContext);
    }

    @Override
    public void handleIntent(UserIntent aIntent) {

        moduleContext.clear();
        updateContext(moduleContext, aIntent);
        processContext(moduleContext, new OnProcessContextListener() {

            @Override
            public void locationsNotAvailable(Set<String> aLocations) {
                if(aLocations.size() == 1) {
                    List<String> query = new ArrayList<>();
                    query.add(randomResponse(R.array.there_is_not) + " "
                                + SentenceHelper.enumeration(aLocations) + " "
                                + randomResponse(R.array.in_smart_home));
                    query.add("Did you mean " + suggestLocation(aLocations.iterator().next()) + "?");

                    bus.post(new IntentEvent.ContextIncomplete(LightModule.this, query));
                } else if(aLocations.size() > 1) {
                    bus.post(new IntentEvent.Handled(randomResponse(R.array.there_are_not) + " "
                            + SentenceHelper.enumeration(aLocations) + " "
                            + randomResponse(R.array.in_smart_home)));
                }
            }

            @Override
            public void onContextReady() {
                List<Device> devices = getFilteredDevices();

                if (devices.isEmpty()) {
                    bus.post(new IntentEvent.Handled(randomResponse(R.array.no_such_light_response)));
                } else if (devices.size() > 1) {
                    bus.post(new IntentEvent.Handled(randomResponse(R.array.multiple_lights_response)));
                } else {
                    processAction(devices.get(0));
                    bus.post(new IntentEvent.Handled("Light done."));
                }
            }

        });

    }

    @Override
    protected void updateContext(DeviceModuleContext aContext, UserIntent aIntent) {

        if(aIntent.hasIntent())
            aContext.setIntent(aIntent.getIntent().getValue().toString());

        if(aIntent.hasEntity(ENTITY_LOCATION))
            aContext.addLocation(aIntent.getEntity(ENTITY_LOCATION).getValue().toString());

        if(aIntent.hasEntity(ENTITY_DEVICE_NAME))
            aContext.addDeviceName(aIntent.getEntity(ENTITY_DEVICE_NAME).getValue().toString());

        if(aIntent.hasEntity(ENTITY_ON_OFF)){
            aContext.addDeviceType(DeviceType.SWITCH_BINARY);
            aContext.addDeviceType(DeviceType.SWITCH_MULTILEVEL);
            aContext.setValue(aIntent.getEntity(ENTITY_ON_OFF).getValue().toString());
        }

        if(aIntent.hasEntity(ENTITY_NUMBER)) {
            aContext.addDeviceType(DeviceType.SWITCH_MULTILEVEL);
            aContext.setValue(aIntent.getEntity(ENTITY_NUMBER).getValue().toString());
        }

    }

    @Override
    void processContext(DeviceModuleContext aContext, OnProcessContextListener aListener) {

        // check if requested intent can be handled by this module
        if(!aContext.intent.equalsIgnoreCase(LIGHT_INTENT)) return;

        // check if all requested locations are available
        Set<String> locNotAvailable = new HashSet<>();
        for(String locationName : aContext.locations) {
            if(!containsLocation(locationName)) locNotAvailable.add(locationName);
        }
        if(!locNotAvailable.isEmpty() && aListener != null) {
            aListener.locationsNotAvailable(locNotAvailable);
            return;
        }

        // notify context ready
        if(aListener != null) aListener.onContextReady();
    }

    private void processAction(Device aDevice) {
            if(aDevice.deviceType == DeviceType.SWITCH_MULTILEVEL) {
                if(moduleContext.value.equalsIgnoreCase(VALUE_ON))
                    aDevice.metrics.level = String.valueOf(99);
                else if(moduleContext.value.equalsIgnoreCase(VALUE_OFF))
                    aDevice.metrics.level = String.valueOf(0);
                else
                    aDevice.metrics.level = moduleContext.value;
                UpdateDeviceService.updateDeviceLevel(m_context, aDevice);
            } else if(aDevice.deviceType == DeviceType.SWITCH_BINARY) {
                aDevice.metrics.level = moduleContext.value;
                UpdateDeviceService.updateDeviceState(m_context, aDevice);
            }
    }

    private List<Device> getFilteredDevices() {
        List<Device> devices = dataContext.getDevicesWithTag(Device.TAG_LIGHT);
        Filter.filterDevices(devices, generateFilters(moduleContext));
        return devices;
    }

}
