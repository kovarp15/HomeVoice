/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 20.03.2017.
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cz.kovar.petr.homevoice.R;
import cz.kovar.petr.homevoice.app.AppConfig;
import cz.kovar.petr.homevoice.utils.SentenceHelper;
import cz.kovar.petr.homevoice.zwave.dataModel.Device;
import cz.kovar.petr.homevoice.zwave.dataModel.DeviceType;

public class BaseSensorModule extends BaseDeviceModule {

    private static final String LOG_TAG = "TemperatureModule";

    private static final String DEVICE_NAME = "thermometer";
    private static final String QUANTITY_NAME = "temperature";
    private static final String INTENT_GET_TEMPERATURE = "GET_TEMPERATURE";

    public BaseSensorModule(Context aContext) {
        super(aContext);
        if(AppConfig.DEBUG) Log.d(LOG_TAG, "INITIALIZED");
    }

    @Override
    Set<String> getSupportedIntents() {
        return new HashSet<String>() {{
            add(INTENT_GET_TEMPERATURE);
        }};
    }

    @Override
    Set<String> getDefaultDeviceTypes() {
        return new HashSet<String>() {{
            add(DeviceType.SENSOR_MULTILEVEL.toString());
        }};
    }

    @Override
    String getTag() {
        return "THERMOMETER";
    }

    @Override
    int getDeviceSpecificationRule(BaseModuleContext aContext) {
        return SPECIFY_LOCATION;
    }

    @Override
    String getDeviceTitle(BaseModuleContext aContext) {
        return "thermometer";
    }

    @Override
    void provideAction(BaseModuleContext aContext) {
        List<Device> devices = getFilteredDevices(aContext);

        List<String> availableLocations = dataContext.getLocationsNames();
        Map<String, Set<Device>> devicesInLocation = new HashMap<>();
        for(Device device : devices) {
            String location = availableLocations.get(Integer.parseInt(device.location));
            if(!devicesInLocation.containsKey(location)) {
                devicesInLocation.put(location, new HashSet<Device>());
            }
            devicesInLocation.get(location).add(device);
        }

        List<String> response = new ArrayList<>();
        for(String location : devicesInLocation.keySet()) {
            Set<Device> deviceSet = devicesInLocation.get(location);
            if(devices.size() == 1) {
                double temp = Double.parseDouble(devices.get(0).metrics.level);
                response.add(String.format(SentenceHelper.randomResponse(m_context,
                        R.array.temperature_response), temp, location));
            } else {
                double averageTemp = getAverageTemperature(deviceSet);
                response.add(String.format(SentenceHelper.randomResponse(m_context,
                        R.array.average_temperature_response), averageTemp, location));
            }
        }
        notifyIntentHandled(response);

    }

    @Override
    void onDeviceNotAvailable(Set<String> aRequestedLocations, Set<String> aSuggestedLocations) {
        if(aSuggestedLocations.isEmpty()) {
            notifyIntentHandled(String.format(SentenceHelper.randomResponse(m_context, R.array.not_available_singular),
                    DEVICE_NAME, "smart home"));
        } else if (aSuggestedLocations.size() == 1) {
            List<String> query = new ArrayList<>();
            query.add(String.format(SentenceHelper.randomResponse(m_context, R.array.not_available_singular),
                    DEVICE_NAME, SentenceHelper.enumerationOR(aRequestedLocations)));
            query.add(String.format(SentenceHelper.randomResponse(m_context, R.array.device_available_response),
                    DEVICE_NAME, SentenceHelper.enumerationAND(aSuggestedLocations)));
            query.add(String.format(SentenceHelper.randomResponse(m_context, R.array.wanna_know_value),
                    QUANTITY_NAME, SentenceHelper.enumerationAND(aSuggestedLocations)));
            notifyContextNotReady(BaseSensorModule.this, query);
        } else {
            List<String> query = new ArrayList<>();
            query.add(String.format(SentenceHelper.randomResponse(m_context, R.array.not_available_singular),
                    DEVICE_NAME, SentenceHelper.enumerationOR(aRequestedLocations)));
            query.add(String.format(SentenceHelper.randomResponse(m_context, R.array.device_available_response),
                    DEVICE_NAME, SentenceHelper.enumerationAND(aSuggestedLocations)));
            query.add(SentenceHelper.randomResponse(m_context, R.array.wanna_know_value));
            notifyContextNotReady(BaseSensorModule.this, query);
        }
    }

    private double getAverageTemperature(Set<Device> aDevices) {
        double temp = 0;
        for(Device device : aDevices) {
            temp += Double.parseDouble(device.metrics.level)/aDevices.size();
        }
        return temp;
    }

}
