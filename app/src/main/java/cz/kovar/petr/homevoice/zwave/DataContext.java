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
import cz.kovar.petr.homevoice.zwave.dataModel.Instance;
import cz.kovar.petr.homevoice.zwave.dataModel.Location;
import cz.kovar.petr.homevoice.zwave.dataModel.Notification;

public class DataContext {

    private static final String LOG_TAG = "DataContext";

    private List<Device> m_devices;
    private List<Location> m_locations;
    private List<Notification> m_notifications;
    private List<Instance> m_instances;

    public DataContext() {
        m_devices = new ArrayList<>();
        m_locations = new ArrayList<>();
        m_notifications = new ArrayList<>();
        m_instances = new ArrayList<>();
    }

    public void addNotifications(List<Notification> notifications) {
        Log.v(LOG_TAG, "Add " + notifications.size() + " notifications");
        if (m_notifications.isEmpty()) {
            m_notifications.addAll(notifications);
        } else {
            for (Notification notification : notifications) {
                final int i = m_notifications.indexOf(notification);
                if (i >= 0) {
                    try {
                        Log.v(LOG_TAG, "remove " + i + " of " + m_notifications.size());
                        m_notifications.remove(i);
                        m_notifications.add(i, notification);
                    } catch (IndexOutOfBoundsException e) {
                        //TODO Need to find the reason of this exception!
                        e.printStackTrace();
                    }
                } else {
                    m_notifications.add(notification);
                }
            }
        }
        Log.v(LOG_TAG, "Notifications count " + m_notifications.size());
        Log.v(LOG_TAG, "---------------------------");
    }

    public void addLocations(List<Location> locations) {
        Log.v(LOG_TAG, "Add " + locations.size() + " locations");
        if (m_locations.isEmpty()) {
            m_locations.addAll(locations);
        } else {
            for (Location location : locations) {
                final int i = m_locations.indexOf(location);
                if (i >= 0) {
                    m_locations.remove(i);
                    m_locations.add(i, location);
                } else {
                    m_locations.add(location);
                }
            }
        }
        Log.v(LOG_TAG, "Locations count " + m_locations.size());
        Log.v(LOG_TAG, "---------------------------");
    }

    public void addInstances(List<Instance> instances) {
        Log.v(LOG_TAG, "Add " + instances.size() + " instnaces");
        if (m_instances.isEmpty()) {
            m_instances.addAll(instances);
        } else {
            for (Instance instance : instances) {
                final int i = m_instances.indexOf(instance);
                if (i >= 0) {
                    m_instances.remove(i);
                    m_instances.add(i, instance);
                } else {
                    m_instances.add(instance);
                }
            }
        }
        Log.v(LOG_TAG, "Instances count " + m_locations.size());
        Log.v(LOG_TAG, "---------------------------");
    }

    public void addDevices(List<Device> devices) {
        Log.v(LOG_TAG, "Add " + devices.size() + " devices");
        for (Device device : devices) {
            if (!device.permanentlyHidden) {
                final int i = m_devices.indexOf(device);
                if (i >= 0) {
                    m_devices.remove(i);
                    m_devices.add(i, device);
                } else {
                    m_devices.add(device);
                }
            }
        }
        Log.v(LOG_TAG, "Devices count " + m_devices.size());
        Log.v(LOG_TAG, "---------------------------");
    }

    public List<String> getDeviceTypes() {
        final List<String> result = new ArrayList<String>();
        if (m_devices != null) {
            for (Device device : m_devices) {
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
        if (m_devices != null) {
            for (Device device : m_devices) {
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
        if (m_locations != null) {
            for (Location location : m_locations) {
                if (!result.contains(location.title))
                    result.add(location.title);
            }
        }
        return result;
    }

    public List<String> getLocationsNamesLowerCase() {
        final List<String> result = new ArrayList<String>();
        if (m_locations != null) {
            for (Location location : m_locations) {
                if (!result.contains(location.title))
                    result.add(location.title.toLowerCase());
            }
        }
        return result;
    }

    public List<Location> getLocations() {
        return m_locations == null ? new ArrayList<Location>() : m_locations;
    }

    public List<Instance> getInstances() {
        return m_instances == null ? new ArrayList<Instance>() : m_instances;
    }

    public List<Notification> getNotifications() {
        return m_notifications;
    }

    public List<Device> getDevicesWithType(String deviceType) {
        if (deviceType.equalsIgnoreCase(Filter.DEFAULT_FILTER))
            return m_devices;

        final ArrayList<Device> result = new ArrayList<Device>();
        for (Device device : m_devices) {
            if (device.deviceType.toString().equalsIgnoreCase(deviceType))
                result.add(device);
        }
        return result;
    }

    public List<Device> getDevicesWithTag(String deviceTag) {
        if (deviceTag.equalsIgnoreCase(Filter.DEFAULT_FILTER))
            return m_devices;

        final ArrayList<Device> result = new ArrayList<Device>();
        for (Device device : m_devices) {
            if (device.tags.contains(deviceTag))
                result.add(device);
        }
        return result;
    }

    public List<Device> getDevicesForLocation(String location) {
        if (location.equalsIgnoreCase(Filter.DEFAULT_FILTER))
            return m_devices;

        final ArrayList<Device> result = new ArrayList<Device>();
        for (Device device : m_devices) {
            if (device.location != null && device.location.equalsIgnoreCase(location))
                result.add(device);
        }
        return result;
    }

    public List<Device> getDevices() {
        return new ArrayList<>(m_devices);
    }

    public void clear() {
        Log.v(LOG_TAG, "Clear data context");
        clearList(m_devices);
        clearList(m_locations);
        clearList(m_notifications);
    }

    private void clearList(List list) {
        if (list != null)
            list.clear();
    }

}
