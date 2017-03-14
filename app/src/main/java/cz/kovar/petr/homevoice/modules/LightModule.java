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
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.kovar.petr.homevoice.R;
import cz.kovar.petr.homevoice.app.AppConfig;
import cz.kovar.petr.homevoice.bus.events.IntentEvent;
import cz.kovar.petr.homevoice.nlu.UserIntent;
import cz.kovar.petr.homevoice.utils.SentenceHelper;
import cz.kovar.petr.homevoice.zwave.dataModel.Device;
import cz.kovar.petr.homevoice.zwave.dataModel.DeviceType;
import cz.kovar.petr.homevoice.zwave.dataModel.Filter;
import cz.kovar.petr.homevoice.zwave.services.UpdateDeviceService;

import static cz.kovar.petr.homevoice.zwave.dataModel.DeviceType.SWITCH_BINARY;

public class LightModule extends DeviceModule {

    private static final String LOG_TAG = "LightModule";

    private static final String SET_LIGHT_INTENT = "SET_LIGHT";
    private static final String GET_LIGHT_INTENT = "GET_LIGHT";

    private static final String ENTITY_LOCATION = "location";
    private static final String LOCATION_VALUE_EVERYWHERE = "everywhere";
    private static final String ENTITY_DEVICE_NAME = "device_name";
    private static final String ENTITY_ON_OFF = "on_off";
    private static final String ENTITY_NUMBER = "number";

    public LightModule(Context aContext) {
        super(aContext);
        setSupportedIntents(new HashSet<String>() {{
            add(SET_LIGHT_INTENT);
            add(GET_LIGHT_INTENT);
        }});
        if(AppConfig.DEBUG) Log.d(LOG_TAG, "INITIALIZED");
    }

    @Override
    public void handleIntent(UserIntent aIntent) {

        updateContext(moduleContext, aIntent);
        processContext(moduleContext, new OnProcessContextListener() {

            @Override
            public void devicesNotAvailable(final Set<String> aLocations) {
                if(moduleContext.appliesActuator()) {
                    bus.post(new IntentEvent.Handled(new ArrayList<String>() {{
                            add(String.format(randomResponse(R.array.not_available_singular),
                                    "light", aLocations.isEmpty() ?
                                            "smart home" : SentenceHelper.enumeration(aLocations)));
                        }}));
                }
            }

            @Override
            public void devicesAtMultipleLocations(Set<String> aLocations) {
                List<String> query = new ArrayList<>();
                query.add(String.format(randomResponse(R.array.device_in_several_locations),
                        "light", SentenceHelper.enumeration(aLocations)));
                query.add(randomResponse(R.array.select_location));

                followup = true;
                bus.post(new IntentEvent.ContextIncomplete(LightModule.this, query));
            }

            @Override
            public void locationsNotAvailable(final Set<String> aLocations) {
                if(aLocations.size() == 1) {
                    String suggestedLocation = suggestLocation(aLocations.iterator().next());
                    List<String> query = new ArrayList<>();
                    query.add(String.format(randomResponse(R.array.not_available_singular),
                            SentenceHelper.enumeration(aLocations), "smart home"));
                    query.add("Did you mean " + suggestedLocation + "?");
                    suggestedContext = new DeviceModuleContext();
                    suggestedContext.setLocation(suggestedLocation);

                    followup = true;
                    bus.post(new IntentEvent.ContextIncomplete(LightModule.this, query));
                } else if(aLocations.size() > 1) {
                    bus.post(new IntentEvent.Handled(new ArrayList<String>() {{
                            add(String.format(randomResponse(R.array.not_available_plural),
                            SentenceHelper.enumeration(aLocations), "smart home"));
                    }}));
                }
            }

            @Override
            public void onContextReady(List<Device> aDevices) {

                provideAction(moduleContext, aDevices);

                followup = false;
                moduleContext.clear();
                suggestedContext.clear();
            }

        });

    }

    @Override
    protected void updateContext(DeviceModuleContext aContext, UserIntent aIntent) {

        if(followup) aIntent.removeEntity(ENTITY_ON_OFF);

        if(suggestionAvailable() && aIntent.hasEntity(ENTITY_YES_NO)) {
            if (aIntent.getEntity(ENTITY_YES_NO).getValue().equals(ENTITY_VALUE_YES)) {
                moduleContext.inject(suggestedContext);
                suggestedContext.clear();
            } else if (aIntent.getEntity(ENTITY_YES_NO).getValue().equals(ENTITY_VALUE_NO)) {
                suggestedContext.clear();
            }
        }

        updateContextValue(aContext, aIntent);
        updateContextIntent(aContext, aIntent);
        updateContextLocations(aContext, aIntent);
        updateContextDevice(aContext, aIntent);

    }

    @Override
    void processContext(DeviceModuleContext aContext, OnProcessContextListener aListener) {

        // check if requested intent can be handled by this module
        if(!supportsIntent(aContext.intent)) return;

        // check if all requested locations are available
        Set<String> locNotAvailable = new HashSet<>();
        for(String locationName : aContext.locations) {
            if(!containsLocation(locationName)) locNotAvailable.add(locationName);
        }
        if(!locNotAvailable.isEmpty() && aListener != null) {
            aListener.locationsNotAvailable(locNotAvailable);
            return;
        }

        // check if devices exists
        List<Device> devices = getFilteredDevices(aContext);
        if(devices.isEmpty() && aListener != null) {
            aListener.devicesNotAvailable(aContext.locations);
            return;
        }

        // check if devices appropriate (location)
        boolean locOK = true;
        List<String> availableLocations = dataContext.getLocationsNames();
        Set<String> deviceLocations = new HashSet<>();
        for(Device device : devices) {
            String location = availableLocations.get(Integer.parseInt(device.location));
            deviceLocations.add(location);
            boolean ok = false;
            for(String requestedLocation : aContext.locations)
                ok |= requestedLocation.equalsIgnoreCase(location);
            locOK &= ok;
        }
        if(!locOK && aListener != null) {
            aListener.devicesAtMultipleLocations(deviceLocations);
            return;
        }

        // notify context ready
        if(aListener != null) aListener.onContextReady(devices);
    }

    @Override
    void provideAction(DeviceModuleContext aContext, List<Device> aDevices) {

        Set<Device> binaryUpdated = new HashSet<>();
        Set<Device> binaryNotUpdated = new HashSet<>();

        Set<Device> multilevelUpdated = new HashSet<>();
        Set<Device> multilevelNotUpdated = new HashSet<>();

        for(Device device : aDevices) {
            switch(device.deviceType) {
                case SWITCH_BINARY:
                    String newState = provideBinaryValue(aContext.value);
                    String oldState = device.metrics.level;
                    if(!newState.equalsIgnoreCase(oldState)) {
                        device.metrics.level = newState;
                        UpdateDeviceService.updateDeviceState(m_context, device);
                        binaryUpdated.add(device);
                    } else {
                        binaryNotUpdated.add(device);
                    }
                    break;
                case SWITCH_MULTILEVEL:
                    String newLevel = provideMultilevelValue(aContext.value);
                    String oldLevel = device.metrics.level;
                    if(!newLevel.equalsIgnoreCase(oldLevel)) {
                        device.metrics.level = newLevel;
                        UpdateDeviceService.updateDeviceLevel(m_context, device);
                        if(newLevel.equals("0") || newLevel.equals("99"))
                            binaryUpdated.add(device);
                        else
                            multilevelUpdated.add(device);
                        break;
                    } else {
                        if(newLevel.equals("0") || newLevel.equals("99"))
                            binaryNotUpdated.add(device);
                        else
                            multilevelNotUpdated.add(device);
                        break;
                    }
            }
        }
        notifyBinaryLightUpdated(binaryUpdated, binaryNotUpdated, aContext);
        notifyMultilevelLightUpdate(multilevelUpdated, multilevelNotUpdated, aContext);

    }

    private void updateContextIntent(DeviceModuleContext aContext, UserIntent aIntent) {
        if(aIntent.hasIntent())
            aContext.setIntent(aIntent.getIntent().getValue().toString());
    }

    private void updateContextValue(DeviceModuleContext aContext, UserIntent aIntent) {
        if(aIntent.hasEntity(ENTITY_ON_OFF)){
            aContext.setDeviceTypes(new HashSet<String>() {{
                add(SWITCH_BINARY.toString());
                add(DeviceType.SWITCH_MULTILEVEL.toString());
            }});
            aContext.setValue(aIntent.getEntity(ENTITY_ON_OFF).getValue().toString());
        }

        if(aIntent.hasEntity(ENTITY_NUMBER)) {
            aContext.setDeviceType(DeviceType.SWITCH_MULTILEVEL);
            aContext.setValue(aIntent.getEntity(ENTITY_NUMBER).getValue().toString());
        }
    }

    private void updateContextLocations(DeviceModuleContext aContext, UserIntent aIntent) {
        if(aIntent.hasEntity(ENTITY_LOCATION)) {
            String location = aIntent.getEntity(ENTITY_LOCATION).getValue().toString();
            if (location.equalsIgnoreCase(LOCATION_VALUE_EVERYWHERE))
                aContext.setLocations(dataContext.getLocationsNames());
            else
                aContext.setLocation(aIntent.getEntity(ENTITY_LOCATION).getValue().toString());
        }
    }

    private void updateContextDevice(DeviceModuleContext aContext, UserIntent aIntent) {
        if(aIntent.hasEntity(ENTITY_DEVICE_NAME))
            aContext.addDeviceName(aIntent.getEntity(ENTITY_DEVICE_NAME).getValue().toString());
    }

    private String provideBinaryValue(String aValue) {
        if(aValue.equalsIgnoreCase(VALUE_ON) || aValue.equalsIgnoreCase(VALUE_OFF)) return aValue;
        if(aValue.equalsIgnoreCase("0")) return VALUE_OFF;
        return VALUE_ON;
    }

    private String provideMultilevelValue(String aValue) {
        if(aValue.equalsIgnoreCase(VALUE_ON)) return "99";
        if(aValue.equalsIgnoreCase(VALUE_OFF)) return "0";
        try {
            int value = Integer.parseInt(aValue);
            if(value < 0) return "0";
            if(value > 99) return "99";
            return aValue;
        } catch(NumberFormatException e) {
            return "0";
        }
    }

    private void notifyBinaryLightUpdated(Set<Device> aUpdatedDevices,
                                          Set<Device> aNotUpdatedDevices,
                                          DeviceModuleContext aContext) {

        if(aUpdatedDevices.isEmpty() && aNotUpdatedDevices.isEmpty()) return;

        List<String> availableLocations = dataContext.getLocationsNames();
        Set<String> updatedLocations = new HashSet<>();
        Set<String> notUpdatedLocations = new HashSet<>();

        for(Device device : aUpdatedDevices)
            updatedLocations.add(availableLocations.get(Integer.parseInt(device.location)));

        for(Device device : aNotUpdatedDevices)
            notUpdatedLocations.add(availableLocations.get(Integer.parseInt(device.location)));

        List<String> response = new ArrayList<>();
        if(!updatedLocations.isEmpty())
            response.add(String.format(randomResponse(R.array.turned_on_off),
                    "light",
                    provideBinaryValue(aContext.value),
                    SentenceHelper.enumeration(updatedLocations)));
        if(!notUpdatedLocations.isEmpty())
            response.add(String.format(randomResponse(R.array.turned_on_off_already),
                    "light",
                    provideBinaryValue(aContext.value),
                    SentenceHelper.enumeration(notUpdatedLocations)));

        bus.post(new IntentEvent.Handled(response));

    }

    private void notifyMultilevelLightUpdate(Set<Device> aUpdatedDevices,
                                             Set<Device> aNotUpdatedDevices,
                                             DeviceModuleContext aContext) {

        if(aUpdatedDevices.isEmpty() || aNotUpdatedDevices.isEmpty()) return;

        List<String> availableLocations = dataContext.getLocationsNames();
        Set<String> updatedLocations = new HashSet<>();
        Set<String> notUpdatedLocations = new HashSet<>();

        for(Device device : aUpdatedDevices)
            updatedLocations.add(availableLocations.get(Integer.parseInt(device.location)));

        for(Device device : aNotUpdatedDevices)
            notUpdatedLocations.add(availableLocations.get(Integer.parseInt(device.location)));

        List<String> response = new ArrayList<>();
        if(!updatedLocations.isEmpty())
            response.add(String.format(randomResponse(R.array.set_level),
                    "light",
                    provideMultilevelValue(aContext.value),
                    SentenceHelper.enumeration(updatedLocations)));
        if(!notUpdatedLocations.isEmpty())
            response.add(String.format(randomResponse(R.array.set_level_already),
                    "light",
                    provideMultilevelValue(aContext.value),
                    SentenceHelper.enumeration(notUpdatedLocations)));

        bus.post(new IntentEvent.Handled(response));

    }

    private List<Device> getFilteredDevices(DeviceModuleContext aContext) {
        List<Device> devices = dataContext.getDevicesWithTag(Device.TAG_LIGHT);
        Filter.filterDevices(devices, generateFilters(aContext));
        return devices;
    }

}
