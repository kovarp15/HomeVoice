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

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UserIntent {

    private Map<String, Entity> m_entities = new HashMap<>();

    public Entity getIntent() {
        if (hasIntent())
            return m_entities.get("intent");
        else
            return null;
    }

    public boolean hasIntent() {
        return m_entities.containsKey("intent");
    }

    public boolean hasEntity(String aName) {
        return m_entities.containsKey(aName);
    }

    public Entity getEntity(String aName) {
        return m_entities.get(aName);
    }

    public void removeEntity(String aName) {
        m_entities.remove(aName);
    }

    static UserIntent createFromJSON(JsonElement aRoot) {
        UserIntent result = new UserIntent();
        JsonObject rootobj = aRoot.getAsJsonObject();
        JsonObject entities = rootobj.getAsJsonObject("entities");
        Set<Map.Entry<String, JsonElement>> entries  =  entities.entrySet();
        for(Map.Entry<String, JsonElement> entry : entries) {
            result.m_entities.put(entry.getKey(), Entity.createFromJSON(entry));
            Log.e("WitHandler", Entity.createFromJSON(entry).toString());
        }
        return result;
    }

}
