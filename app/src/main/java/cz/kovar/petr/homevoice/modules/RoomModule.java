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
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.kovar.petr.homevoice.R;
import cz.kovar.petr.homevoice.app.AppConfig;
import cz.kovar.petr.homevoice.nlu.Entity;
import cz.kovar.petr.homevoice.nlu.UserIntent;
import cz.kovar.petr.homevoice.utils.SentenceHelper;

public class RoomModule extends Module {

    private static final String LOG_TAG = "RoomModule";

    private static final String INTENT_ROOM = "ROOM";
    private static final String ENTITY_QUERY = "query";
    private static final String QUERY_LIST = "LIST";
    private static final String QUERY_COUNT = "COUNT";

    public RoomModule(Context aContext) {
        super(aContext);
        if(AppConfig.DEBUG) Log.d(LOG_TAG, "INITIALIZED");
    }

    @Override
    Set<String> getSupportedIntents() {
        return new HashSet<String>() {{
            add(INTENT_ROOM);
        }};
    }

    @Override
    public void handleIntent(UserIntent aIntent) {

        updateContext(moduleContext, aIntent);

        if(!supportsIntent(moduleContext.intent)) return;

        switch(moduleContext.intent) {
            case INTENT_ROOM:
                if (aIntent.hasEntity(ENTITY_QUERY)) {
                    Entity query = aIntent.getEntity(ENTITY_QUERY);
                    processQuery(query.getValue().toString());
                } else {
                    notifyIntentHandled(SentenceHelper.randomResponse(m_context, R.array.room_list_response)
                            + SentenceHelper.enumerationAND(dataContext.getLocationsNames()));
                }
                break;
        }
    }

    @Override
    void resetModule() {}

    private void processQuery(String aQueryValue) {
        switch(aQueryValue) {
            case QUERY_COUNT:
                provideRoomCountResponse();
                break;
            case QUERY_LIST:
                provideRoomListResponse();
                break;
        }
    }

    private void provideRoomCountResponse() {
        notifyIntentHandled(String.format(SentenceHelper.randomResponse(m_context, R.array.room_count_response),
                getRoomCount()));
    }

    private void provideRoomListResponse() {
        List<String> response = new ArrayList<>();
        response.add(SentenceHelper.randomResponse(m_context, R.array.room_list_response));
        response.add(SentenceHelper.enumerationAND(dataContext.getLocationsNames()) + ".");
        notifyIntentHandled(response);
    }

    private int getRoomCount() {
        return dataContext.getLocationsNames().size() - 1;
    }

}
