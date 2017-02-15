/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 04.02.2017.
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

package cz.kovar.petr.homevoice.zwave;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cz.kovar.petr.homevoice.zwave.dataModel.Device;
import cz.kovar.petr.homevoice.zwave.dataModel.Filter;
import cz.kovar.petr.homevoice.zwave.dataModel.Location;
import cz.kovar.petr.homevoice.zwave.dataModel.Notification;

public class DataContext {

    private static final String LOG_TAG = "DataContext";

    private List<Device> mDevices;
    private List<Location> mLocation;
    private List<Notification> mNotifications;

    public DataContext() {
        mDevices = new ArrayList<Device>();
        mLocation = new ArrayList<Location>();
        mNotifications = new ArrayList<Notification>();
    }

    public void addNotifications(List<Notification> notifications) {
        Log.v(LOG_TAG, "Add " + notifications.size() + " notifications");
        if (mNotifications.isEmpty()) {
            mNotifications.addAll(notifications);
        } else {
            for (Notification notification : notifications) {
                final int i = mNotifications.indexOf(notification);
                if (i >= 0) {
                    try {
                        Log.v(LOG_TAG, "remove " + i + " of " + mNotifications.size());
                        mNotifications.remove(i);
                        mNotifications.add(i, notification);
                    } catch (IndexOutOfBoundsException e) {
                        //TODO Need to find the reason of this exception!
                        e.printStackTrace();
                    }
                } else {
                    mNotifications.add(notification);
                }
            }
        }
        Log.v(LOG_TAG, "Notifications count " + mNotifications.size());
        Log.v(LOG_TAG, "---------------------------");
    }

    public void addLocations(List<Location> locations) {
        Log.v(LOG_TAG, "Add " + locations.size() + " locations");
        if (mLocation.isEmpty()) {
            mLocation.addAll(locations);
        } else {
            for (Location location : locations) {
                final int i = mLocation.indexOf(location);
                if (i >= 0) {
                    mLocation.remove(i);
                    mLocation.add(i, location);
                } else {
                    mLocation.add(location);
                }
            }
        }
        Log.v(LOG_TAG, "Locations count " + mLocation.size());
        Log.v(LOG_TAG, "---------------------------");
    }

    public void addDevices(List<Device> devices) {
        Log.v(LOG_TAG, "Add " + devices.size() + " devices");
        for (Device device : devices) {
            if (!device.permanentlyHidden) {
                final int i = mDevices.indexOf(device);
                if (i >= 0) {
                    mDevices.remove(i);
                    mDevices.add(i, device);
                } else {
                    mDevices.add(device);
                }
            }
        }
        Log.v(LOG_TAG, "Devices count " + mDevices.size());
        Log.v(LOG_TAG, "---------------------------");
    }

    public List<String> getDeviceTypes() {
        final List<String> result = new ArrayList<String>();
        if (mDevices != null) {
            for (Device device : mDevices) {
                if (device != null && device.deviceType != null) {
                    final String deviceType = device.deviceType.toString();
                    if (!result.contains(deviceType))
                        result.add(deviceType);
                }
            }
        }
        return result;
    }

    public List<String> getDeviceTags() {
        final List<String> result = new ArrayList<String>();
        if (mDevices != null) {
            for (Device device : mDevices) {
                for (String tag : device.tags) {
                    if (!result.contains(tag))
                        result.add(tag);
                }
            }
        }
        return result;
    }

    public List<String> getLocationsNames() {
        final List<String> result = new ArrayList<String>();
        if (mLocation != null) {
            for (Location location : mLocation) {
                if (!result.contains(location.title))
                    result.add(location.title);
            }
        }
        return result;
    }

    public List<Location> getLocations() {
        return mLocation == null ? new ArrayList<Location>() : mLocation;
    }

    public List<Notification> getNotifications() {
        return mNotifications;
    }

    public List<Device> getDevicesWithType(String deviceType) {
        if (deviceType.equalsIgnoreCase(Filter.DEFAULT_FILTER))
            return mDevices;

        final ArrayList<Device> result = new ArrayList<Device>();
        for (Device device : mDevices) {
            if (device.deviceType.toString().equalsIgnoreCase(deviceType))
                result.add(device);
        }
        return result;
    }

    public List<Device> getDevicesWithTag(String deviceTag) {
        if (deviceTag.equalsIgnoreCase(Filter.DEFAULT_FILTER))
            return mDevices;

        final ArrayList<Device> result = new ArrayList<Device>();
        for (Device device : mDevices) {
            if (device.tags.contains(deviceTag))
                result.add(device);
        }
        return result;
    }

    public List<Device> getDevicesForLocation(String location) {
        if (location.equalsIgnoreCase(Filter.DEFAULT_FILTER))
            return mDevices;

        final ArrayList<Device> result = new ArrayList<Device>();
        for (Device device : mDevices) {
            if (device.location != null && device.location.equalsIgnoreCase(location))
                result.add(device);
        }
        return result;
    }

    public void clear() {
        Log.v(LOG_TAG, "Clear data context");
        clearList(mDevices);
        clearList(mLocation);
        clearList(mNotifications);
    }

    private void clearList(List list) {
        if (list != null)
            list.clear();
    }

}
