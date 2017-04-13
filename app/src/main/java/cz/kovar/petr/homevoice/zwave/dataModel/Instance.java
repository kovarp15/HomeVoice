/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 11.04.2017.
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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Instance implements Serializable {

    public String id;
    public String moduleId;
    public Map<String, Object> params;
    // TODO params
    public boolean active;
    public String title;
    public String description;
    public long creationTime;
    public String module;

    public Instance() {
        params = new HashMap<>();
    }

    @Override
    public String toString() {
        return "Instance{" +
                "id='" + id + '\'' +
                ", moduleId='" + moduleId + '\'' +
                ", active=" + active +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", creationTime=" + creationTime +
                ", module='" + moduleId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || ((Object)this).getClass() != o.getClass()) return false;
        Instance device = (Instance) o;
        return !(id != null ? !id.equals(device.id) : device.id != null);
    }

    public static class Init {
        public String moduleId;
        public Params params;

        public static class Params {
            public String api_key;
            public String device_id;
        }

    }

}
