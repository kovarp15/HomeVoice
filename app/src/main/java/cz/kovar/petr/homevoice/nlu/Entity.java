/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 18.10.2016.
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
package cz.kovar.petr.homevoice.nlu;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;

public class Entity {

    private String m_name = null;
    private double m_confidence = 0;
    private Object m_value = null;

    public String getName() {
        return m_name;
    }

    public Object getValue() {
        return m_value;
    }

    static Entity createFromJSON(Map.Entry<String, JsonElement> aData) {
        Entity result = new Entity();
        result.m_name = aData.getKey();
        JsonElement root = aData.getValue();
        JsonArray array = root.getAsJsonArray();
        for(JsonElement elem : array) {
            JsonObject value = elem.getAsJsonObject();
            result.m_confidence = value.get("confidence").getAsDouble();
            result.m_value = value.get("value").getAsString();
        }
        return result;
    }

    @Override
    public String toString() {
        return "{\n" +
                "\t name: " + m_name + ",\n" +
                "\t value: " + m_value + ",\n" +
                "\t conf: " + m_confidence + "\n" +
                "}";
    }
}
