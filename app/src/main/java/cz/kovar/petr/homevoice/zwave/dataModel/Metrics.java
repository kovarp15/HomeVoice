/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 01.02.2017.
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

import android.text.TextUtils;

import java.io.Serializable;

public class Metrics implements Serializable {

    public String probeTitle;
    public String scaleTitle;
    public String level;
    public String title;
    public String iconBase;
    public String icon;
    public String mode;
    public String min = "0";
    public String max = "100";
    public DeviceRgbColor color;

    //Camera metrics
    public String url;
    public Boolean hasZoomIn;
    public Boolean hasZoomOut;
    public Boolean hasLeft;
    public Boolean hasRight;
    public Boolean hasUp;
    public Boolean hasDown;
    public Boolean hasOpen;
    public Boolean hasClose;

    public String getScaleTitle() {
        return !TextUtils.isEmpty(scaleTitle) && !scaleTitle.equals("null") ? scaleTitle : "";
    }

}
