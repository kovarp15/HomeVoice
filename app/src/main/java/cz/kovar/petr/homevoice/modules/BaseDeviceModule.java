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

import org.apache.http.entity.StringEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.kovar.petr.homevoice.R;
import cz.kovar.petr.homevoice.nlu.UserIntent;
import cz.kovar.petr.homevoice.utils.Levenshtein;
import cz.kovar.petr.homevoice.utils.SentenceHelper;
import cz.kovar.petr.homevoice.zwave.dataModel.Device;
import cz.kovar.petr.homevoice.zwave.dataModel.DeviceType;
import cz.kovar.petr.homevoice.zwave.dataModel.Filter;
import cz.kovar.petr.homevoice.zwave.dataModel.FilterData;

import static cz.kovar.petr.homevoice.zwave.dataModel.DeviceType.SWITCH_BINARY;

abstract class BaseDeviceModule extends Module {

    private static final String ENTITY_DEVICE_NAME  = "name";
    private static final String ENTITY_LOCATION     = "location";
    private static final String LOCATION_EVERYWHERE = "everywhere";
    private static final String LOCATION_HOME       = "home";
    private static final String LOCATION_SMART_HOME = "smart home";
    private static final String ENTITY_NUMBER       = "number";
    private static final String ENTITY_COLOR        = "color";
    private static final String ENTITY_QUERY        = "query";
    private static final String QUERY_VALUE         = "value";
    private static final String QUERY_LIST          = "LIST";
    private static final String QUERY_COUNT         = "COUNT";
    private static final String ENTITY_ON_OFF       = "on_off";
    private static final String VALUE_ON            = "on";
    private static final String VALUE_OFF           = "off";

    protected static final int SPEC_NOT_NEEDED  = 0;
    protected static final int SPECIFY_LOCATION = 1;
    protected static final int SPECIFY_DEVICE   = 2;

    private BaseModuleContext moduleContext = new BaseModuleContext();
    protected BaseModuleContext suggestedContext = new BaseModuleContext();

    BaseDeviceModule(Context aContext) {
        super(aContext);
    }

    @Override
    abstract Set<String> getSupportedIntents();

    abstract Set<String> getDefaultDeviceTypes();

    abstract String getTag();

    abstract int getDeviceSpecificationRule(BaseModuleContext aContext);

    abstract String getDeviceTitle();

    abstract void provideAction(BaseModuleContext aContext);

    abstract void onDeviceNotAvailable(Set<String> aRequestedLocations, Set<String> aSuggestedLocations);

    @Override
    public void handleIntent(UserIntent aIntent) {

        updateContext(aIntent);

        if(contextReady())
            provideAction(moduleContext);

    }

    @Override
    void resetModule() {
        moduleContext.clear();
        suggestedContext.clear();
    }

    private void updateContext(UserIntent aIntent) {
        applySuggestion(aIntent);
        updateContextIntent(aIntent);
        updateContextValue(aIntent);
        updateContextQuery(aIntent);
        updateContextLocations(aIntent);
        updateContextDevice(aIntent);
    }

    private void applySuggestion(UserIntent aIntent) {
        if(!suggestedContext.isEmpty() && aIntent.hasEntity(ENTITY_YES_NO)) {
            if (aIntent.getEntity(ENTITY_YES_NO).getValue().equals(VALUE_YES)) {
                moduleContext.inject(suggestedContext);
                suggestedContext.clear();
            } else if (aIntent.getEntity(ENTITY_YES_NO).getValue().equals(VALUE_NO)) {
                suggestedContext.clear();
            }
        }
    }

    private void updateContextIntent(UserIntent aIntent) {
        if(aIntent.hasIntent())
            moduleContext.setIntent(aIntent.getIntent().getValue().toString());
    }

    private void updateContextValue(UserIntent aIntent) {
        if(aIntent.hasEntity(ENTITY_ON_OFF) && aIntent.getEntity(ENTITY_ON_OFF).getConfidence() > 0.95
                && moduleContext.intent.contains("SET_")){
            moduleContext.setDeviceTypes(new HashSet<String>() {{
                add(SWITCH_BINARY.toString());
                add(DeviceType.SWITCH_MULTILEVEL.toString());
            }});
            moduleContext.setValue(aIntent.getEntity(ENTITY_ON_OFF).getValue().toString());
        }

        if(aIntent.hasEntity(ENTITY_NUMBER)) {
            moduleContext.setDeviceType(DeviceType.SWITCH_MULTILEVEL);
            moduleContext.setValue(aIntent.getEntity(ENTITY_NUMBER).getValue().toString());
        }

        if(aIntent.hasEntity(ENTITY_COLOR)) {
            moduleContext.setDeviceType(DeviceType.SWITCH_RGBW);
            moduleContext.setValue(aIntent.getEntity(ENTITY_COLOR).getValue().toString());
            // TODO check color
        }

    }

    private void updateContextQuery(UserIntent aIntent) {
        if(aIntent.hasEntity(ENTITY_QUERY))
            moduleContext.setQuery(aIntent.getEntity(ENTITY_QUERY).getValue().toString());
    }

    private void updateContextLocations(UserIntent aIntent) {
        if(aIntent.hasEntity(ENTITY_LOCATION)) {
            String location = aIntent.getEntity(ENTITY_LOCATION).getValue().toString();
            if (location.equalsIgnoreCase(LOCATION_EVERYWHERE)
                    || location.equalsIgnoreCase(LOCATION_SMART_HOME)
                    || location.equalsIgnoreCase(LOCATION_HOME))
                moduleContext.setLocations(dataContext.getLocationsNames());
            else
                moduleContext.setLocation(aIntent.getEntity(ENTITY_LOCATION).getValue().toString());
        }
    }

    private void updateContextDevice(UserIntent aIntent) {
        if(aIntent.hasEntity(ENTITY_DEVICE_NAME))
            moduleContext.setDeviceName(aIntent.getEntity(ENTITY_DEVICE_NAME).getValue().toString());
    }

    private boolean contextReady() {

        // check if supports intent
        if(!supportsIntent(moduleContext.intent)) return false;

        // check if all requested locations are available
        Set<String> locNotAvailable = new HashSet<>();
        for (String locationName : moduleContext.locations) {
            if (!containsLocation(locationName)) locNotAvailable.add(locationName);
        }
        if (!locNotAvailable.isEmpty()) {
            moduleContext.locations.removeAll(locNotAvailable);
            notifyLocationsNotAvailable(locNotAvailable);
            return false;
        }

        // check if devices exists
        List<Device> devices = getFilteredDevices(moduleContext);
        if (devices.isEmpty()) {
            notifyDevicesNotAvailable(moduleContext);
            return false;
        }

        // check specification sufficiency
        switch (getDeviceSpecificationRule(moduleContext)) {
            case SPEC_NOT_NEEDED:
                break;
            case SPECIFY_LOCATION:
                List<String> availableLocations = dataContext.getLocationsNames();
                Set<String> deviceLocations = new HashSet<>();
                for (Device device : devices)
                    deviceLocations.add(availableLocations.get(Integer.parseInt(device.location)));
                if(!availableLocations.containsAll(deviceLocations) && deviceLocations.size() > 1) {
                    notifyDeviceNotSpecified(deviceLocations);
                    return false;
                }
                break;
            case SPECIFY_DEVICE:
                return false;
        }

        return true;

    }

    private boolean containsLocation(String aLocationName) {
        for(String location : dataContext.getLocationsNames()) {
            if(location.equalsIgnoreCase(aLocationName)) return true;
        }
        return false;
    }

    private String suggestLocation(String aLocationName) {
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

    private void notifyLocationsNotAvailable(final Set<String> aLocations) {
        if(aLocations.size() == 1) {
            String suggestedLocation = suggestLocation(aLocations.iterator().next());
            List<String> query = new ArrayList<>();
            query.add(String.format(SentenceHelper.randomResponse(m_context, R.array.not_available_singular),
                    SentenceHelper.enumerationAND(aLocations), "smart home"));
            query.add("Did you mean " + suggestedLocation + "?");
            suggestedContext.clear();
            suggestedContext.setLocation(suggestedLocation);

            notifyContextNotReady(BaseDeviceModule.this, query);
        } else if(aLocations.size() > 1) {
            notifyIntentHandled(
                    String.format(SentenceHelper.randomResponse(m_context,
                            R.array.not_available_plural),
                    SentenceHelper.enumerationAND(aLocations), "smart home"));
        }
    }

    private void notifyDevicesNotAvailable(BaseModuleContext aContext) {

        Set<String> suggestedLocations = suggestLocationsWithDevices(aContext);
        String location = aContext.locations.isEmpty() || suggestedLocations.isEmpty() ?
                "smart home" : SentenceHelper.enumerationOR(aContext.locations);

        // TODO only device names which not exists
        if(aContext.deviceNames == null || aContext.deviceNames.isEmpty()){
            onDeviceNotAvailable(aContext.locations, suggestedLocations);

            /*if(suggestedLocations.isEmpty()) {
                notifyIntentHandled(String.format(SentenceHelper.randomResponse(m_context, R.array.not_available_singular),
                        getDeviceTitle(aContext), location));
            } else {
                List<String> query = new ArrayList<>();
                query.add(String.format(SentenceHelper.randomResponse(m_context, R.array.not_available_singular),
                        getDeviceTitle(aContext), location));
                query.add(String.format(SentenceHelper.randomResponse(m_context, R.array.device_available_response),
                        getDeviceTitle(aContext), SentenceHelper.enumerationAND(suggestedLocations)));
                query.add(SentenceHelper.randomResponse(m_context, R.array.wanna_know_value));
                notifyContextNotReady(BaseDeviceModule.this, query);
            }*/
        } else {
            String names = SentenceHelper.enumerationOR(aContext.deviceNames);
            notifyIntentHandled(String.format(SentenceHelper.randomResponse(m_context, R.array.not_available_singular),
                    names, location));
        }

    }

    private void notifyDeviceNotSpecified(Set<String> aLocations) {
        List<String> query = new ArrayList<>();
        query.add(String.format(SentenceHelper.randomResponse(m_context, R.array.device_in_several_locations),
                getDeviceTitle(), SentenceHelper.enumerationAND(aLocations)));
        query.add(SentenceHelper.randomResponse(m_context, R.array.select_location));
        notifyContextNotReady(BaseDeviceModule.this, query);
    }

    private Set<String> suggestLocationsWithDevices(BaseModuleContext aContext) {
        BaseModuleContext contextWithoutLocations = new BaseModuleContext();
        contextWithoutLocations.inject(aContext);
        contextWithoutLocations.locations.clear();
        List<Device> suggestedDevices = getFilteredDevices(contextWithoutLocations);
        List<String> availableLocations = dataContext.getLocationsNames();
        Set<String> suggestedLocations = new HashSet<>();
        for(Device device : suggestedDevices)
            suggestedLocations.add(availableLocations.get(Integer.parseInt(device.location)));
        return suggestedLocations;
    }

    List<Device> getFilteredDevices(BaseModuleContext aContext) {
        List<Device> devices = dataContext.getDevicesWithTag(getTag());
        Filter.filterDevices(devices, generateFilters(aContext));
        return devices;
    }

    private List<FilterData> generateFilters(BaseModuleContext aModuleContext) {

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

    private int getLocationIndex(String aLocationName) {
        List<String> locationNames = dataContext.getLocationsNames();
        for(String location : locationNames) {
            if(location.equalsIgnoreCase(aLocationName)) return locationNames.indexOf(location);
        }
        return -1;
    }

    class BaseModuleContext extends ModuleContext {

        String value = "";
        String query = "";
        Set<String> locations = new HashSet<>();
        Set<String> deviceNames = new HashSet<>();
        Set<String> deviceTypes = new HashSet<>();

        BaseModuleContext() {
            deviceTypes = getDefaultDeviceTypes();
        }

        void inject(BaseDeviceModule.BaseModuleContext aContext) {
            super.inject(aContext);
            if(!aContext.value.isEmpty()) value = aContext.value;
            if(!aContext.query.isEmpty()) value = aContext.query;
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

        void setValue(Object aValue) {
            value = aValue.toString();
        }

        void setQuery(String aQuery) {
            query = aQuery;
        }

        void setLocation(String aLocation) {
            locations.clear();
            locations.add(aLocation);
        }

        void setLocations(Collection<String> aLocations) {
            locations.clear();
            locations.addAll(aLocations);
        }

        void setDeviceName(String aDeviceName) {
            deviceNames.clear();
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

        @Override
        void clear() {
            super.clear();
            value = "";
            query = "";
            locations = new HashSet<>();
            deviceNames = new HashSet<>();
            deviceTypes = getDefaultDeviceTypes();
        }

        @Override
        boolean isEmpty() {
            return super.isEmpty() && value.isEmpty() && locations.isEmpty()
                    && deviceNames.isEmpty() && deviceTypes.isEmpty();
        }



    }


}
