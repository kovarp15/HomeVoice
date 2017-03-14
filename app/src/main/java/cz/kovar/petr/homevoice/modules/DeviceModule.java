/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 10.03.2017.
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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.kovar.petr.homevoice.nlu.UserIntent;
import cz.kovar.petr.homevoice.utils.Levenshtein;
import cz.kovar.petr.homevoice.zwave.dataModel.Device;
import cz.kovar.petr.homevoice.zwave.dataModel.DeviceType;
import cz.kovar.petr.homevoice.zwave.dataModel.Filter;
import cz.kovar.petr.homevoice.zwave.dataModel.FilterData;

public abstract class DeviceModule extends Module {

    static final String VALUE_ON = "on";
    static final String VALUE_OFF = "off";

    DeviceModuleContext suggestedContext = new DeviceModuleContext();
    DeviceModuleContext moduleContext = new DeviceModuleContext();

    DeviceModule(Context aContext) {
        super(aContext);
    }

    protected abstract void updateContext(DeviceModuleContext aContext, UserIntent aIntent);

    abstract void processContext(DeviceModuleContext aContext, OnProcessContextListener aListener);

    abstract void provideAction(DeviceModuleContext aContext, List<Device> aDevices);

    boolean containsLocation(String aLocationName) {
        for(String location : dataContext.getLocationsNames()) {
            if(location.equalsIgnoreCase(aLocationName)) return true;
        }
        return false;
    }

    private int getLocationIndex(String aLocationName) {
        List<String> locationNames = dataContext.getLocationsNames();
        for(String location : locationNames) {
            if(location.equalsIgnoreCase(aLocationName)) return locationNames.indexOf(location);
        }
        return -1;
    }

    String suggestLocation(String aLocationName) {
        List<String> locationNames = dataContext.getLocationsNames();
        String suggestedName = "";
        double similarityScore = Integer.MAX_VALUE;
        for(String location : locationNames) {
            double score = Levenshtein.distance(aLocationName, location);
            if(score < similarityScore) {
                similarityScore = score;
                suggestedName = location;
            }
        }
        return suggestedName;
    }

    boolean suggestionAvailable() {
        return !suggestedContext.isEmpty();
    }

    List<FilterData> generateFilters(DeviceModuleContext aModuleContext) {

        List<FilterData> filters = new ArrayList<>();

        if(!aModuleContext.locations.isEmpty()) {
            Set<String> locationIndexes = new HashSet<>();
            for(String locationName : aModuleContext.locations) {
                locationIndexes.add(String.valueOf(getLocationIndex(locationName)));
            }
            filters.add(new FilterData(Filter.LOCATION, locationIndexes));
        }

        if(!aModuleContext.deviceNames.isEmpty()) {
            filters.add(new FilterData(Filter.ID, aModuleContext.deviceNames));
        }

        if(!aModuleContext.deviceTypes.isEmpty()) {
            filters.add(new FilterData(Filter.TYPE, aModuleContext.deviceTypes));
        }

        return filters;

    }

    class DeviceModuleContext {

        String intent = "";
        String value = "";
        Set<String> locations = new HashSet<>();
        Set<String> deviceNames = new HashSet<>();
        Set<String> deviceTypes = new HashSet<>();

        void inject(DeviceModuleContext aContext) {
            if(!aContext.intent.isEmpty()) intent = aContext.intent;
            if(!aContext.value.isEmpty()) value = aContext.value;
            if(!aContext.locations.isEmpty()) {
                locations.clear();
                locations.addAll(aContext.locations);
            }
            if(!aContext.deviceNames.isEmpty()) {
                deviceNames.clear();
                deviceNames.addAll(aContext.deviceNames);
            }
            if(!aContext.deviceTypes.isEmpty()) {
                deviceTypes.clear();
                deviceTypes.addAll(aContext.deviceTypes);
            }
        }

        boolean appliesActuator() {
            return deviceTypes.contains(DeviceType.SWITCH_BINARY.toString())
                    || deviceTypes.contains(DeviceType.SWITCH_MULTILEVEL.toString())
                    || deviceTypes.contains(DeviceType.SWITCH_RGBW.toString())
                    || deviceTypes.contains(DeviceType.SWITCH_CONTROLL.toString());
        }

        void setIntent(String aIntent) {
            intent = aIntent;
        }

        void setValue(Object aValue) {
            value = aValue.toString();
        }

        void setLocation(String aLocation) {
            locations.clear();
            locations.add(aLocation);
        }

        void setLocations(Collection<String> aLocations) {
            locations.clear();
            locations.addAll(aLocations);
        }

        void addDeviceName(String aDeviceName) {
            deviceNames.add(aDeviceName);
        }

        void setDeviceType(DeviceType aDeviceType) {
            deviceTypes.clear();
            deviceTypes.add(aDeviceType.toString());
        }

        void setDeviceTypes(Collection<String> aDeviceTypes) {
            deviceTypes.clear();
            deviceTypes.addAll(aDeviceTypes);
        }

        void clear() {
            value = "";
            locations = new HashSet<>();
            deviceNames = new HashSet<>();
            deviceTypes = new HashSet<>();
        }

        boolean isEmpty() {
            return intent.isEmpty() && value.isEmpty() && locations.isEmpty()
                    && deviceNames.isEmpty() && deviceTypes.isEmpty();
        }

    }

    interface OnProcessContextListener {

        void devicesNotAvailable(Set<String> aLocation);

        void devicesAtMultipleLocations(Set<String> aLocation);

        void locationsNotAvailable(Set<String> aLocation);

        void onContextReady(List<Device> aDevices);

    }

}
