/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 05.03.2017.
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

import cz.kovar.petr.homevoice.R;
import cz.kovar.petr.homevoice.bus.events.IntentEvent;
import cz.kovar.petr.homevoice.nlu.Entity;
import cz.kovar.petr.homevoice.nlu.UserIntent;
import cz.kovar.petr.homevoice.utils.SentenceHelper;

public class RoomModule extends Module {

    private static final String ROOM_INTENT = "ROOM";
    private static final String QUERY_ENTITY = "query";
    private static final String QUERY_VALUE_LIST = "LIST";
    private static final String QUERY_VALUE_COUNT = "COUNT";

    public RoomModule(Context aContext) {
        super(aContext);
    }

    @Override
    public void handleIntent(UserIntent aIntent) {
        String intentName = (String) aIntent.getIntent().getValue();

        if(intentName.equals(ROOM_INTENT)) {
            if (aIntent.hasEntity(QUERY_ENTITY)) {
                Entity query = aIntent.getEntity(QUERY_ENTITY);
                processQuery(query.getValue().toString());
            } else {
                bus.post(new IntentEvent.Handled(new ArrayList<String>() {{
                    add(randomResponse(R.array.room_list_response)
                            + SentenceHelper.enumeration(dataContext.getLocationsNames()));
                }}));
            }
        }
    }

    private void processQuery(String aQueryValue) {
        if(aQueryValue.equals(QUERY_VALUE_COUNT)) {
            bus.post(new IntentEvent.Handled(new ArrayList<String>() {{
                add("You have " + getRoomCount() + "rooms");
            }}));
        } else if(aQueryValue.equals(QUERY_VALUE_LIST)) {
            bus.post(new IntentEvent.Handled(new ArrayList<String>() {{
                add(randomResponse(R.array.room_list_response)
                        + SentenceHelper.enumeration(dataContext.getLocationsNames()));
            }}));
        }
    }

    private int getRoomCount() {
        return dataContext.getLocationsNames().size();
    }

}
