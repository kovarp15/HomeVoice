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

import cz.kovar.petr.homevoice.R;

public enum Filter {
    LOCATION, TYPE, TAG;

    public int getFilterLabelResId() {
        switch (this) {
            case LOCATION:
                return R.string.label_devices_with_location;
            case TYPE:
                return R.string.label_devices_with_type;
            case TAG:
                return R.string.label_devices_with_tag;
        }
        return 0;
    }

    public static final String DEFAULT_FILTER = "All";
}
