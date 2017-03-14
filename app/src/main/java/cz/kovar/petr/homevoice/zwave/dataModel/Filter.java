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

package cz.kovar.petr.homevoice.zwave.dataModel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.kovar.petr.homevoice.R;

public enum Filter {
    LOCATION, TYPE, TAG, ID;

    public int getFilterLabelResId() {
        switch (this) {
            case LOCATION:
                return R.string.label_devices_with_location;
            case TYPE:
                return R.string.label_devices_with_type;
            case TAG:
                return R.string.label_devices_with_tag;
            case ID:
                return R.string.label_devices_with_id;
        }
        return 0;
    }

    public static void filterDevices(List<Device> aDevices, List<FilterData> aFilters) {
        Set<Device> devToRemove = new HashSet<>();
        for(Device device : aDevices) {
            boolean keep1 = true;
            for(FilterData filter : aFilters) {
                boolean keep2 = false;
                switch (filter.filter){
                    case LOCATION:
                        for(String filterValue : filter.value) {
                            keep2 |= device.location.equalsIgnoreCase(filterValue);
                        }
                        //if(!device.location.equalsIgnoreCase(filter.value)) devToRemove.add(device);
                        break;
                    case TYPE:
                        for(String filterValue : filter.value) {
                            keep2 |= device.deviceType.toString().equalsIgnoreCase(filterValue);
                        }
                        //if(!device.deviceType.toString().equalsIgnoreCase(filter.value)) devToRemove.add(device);
                        break;
                    case TAG:
                        for(String filterValue : filter.value) {
                            keep2 |= device.tags.contains(filterValue);
                        }
                        //if(!device.tags.contains(filter.value)) devToRemove.add(device);
                        break;
                    case ID:
                        for(String filterValue : filter.value) {
                            keep2 |= device.metrics.title.equalsIgnoreCase(filterValue);
                        }
                        //if(!device.id.equalsIgnoreCase(filter.value)) devToRemove.add(device);
                }
                keep1 &= keep2;
            }
            if(!keep1) devToRemove.add(device);
        }
        aDevices.removeAll(devToRemove);
    }

    public static final String DEFAULT_FILTER = "All";
}
