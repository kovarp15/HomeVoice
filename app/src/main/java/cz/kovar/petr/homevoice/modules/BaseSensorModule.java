/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 20.03.2017.
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
import java.util.List;
import java.util.Set;

import cz.kovar.petr.homevoice.R;
import cz.kovar.petr.homevoice.utils.SentenceHelper;

abstract class BaseSensorModule extends BaseDeviceModule {

    BaseSensorModule(Context aContext) {
        super(aContext);
    }

    abstract String getQuantityTitle();

    @Override
    void onDeviceNotAvailable(Set<String> aRequestedLocations, Set<String> aSuggestedLocations) {
        if(aSuggestedLocations.isEmpty()) {
            notifyIntentHandled(String.format(SentenceHelper.randomResponse(m_context, R.array.not_available_singular),
                    getDeviceTitle(), "smart home"));
        } else if (aSuggestedLocations.size() == 1) {
            List<String> query = new ArrayList<>();
            query.add(String.format(SentenceHelper.randomResponse(m_context, R.array.not_available_singular),
                    getDeviceTitle(), SentenceHelper.enumerationOR(aRequestedLocations)));
            query.add(String.format(SentenceHelper.randomResponse(m_context, R.array.device_available_response),
                    getDeviceTitle(), SentenceHelper.enumerationAND(aSuggestedLocations)));
            query.add(String.format(SentenceHelper.randomResponse(m_context, R.array.wanna_know_value),
                    getQuantityTitle(), SentenceHelper.enumerationAND(aSuggestedLocations)));
            // TODO remove locations from context
            suggestedContext.setLocations(aSuggestedLocations);
            notifyContextNotReady(BaseSensorModule.this, query);
        } else {
            List<String> query = new ArrayList<>();
            query.add(String.format(SentenceHelper.randomResponse(m_context, R.array.not_available_singular),
                    getDeviceTitle(), SentenceHelper.enumerationOR(aRequestedLocations)));
            query.add(String.format(SentenceHelper.randomResponse(m_context, R.array.device_available_response),
                    getDeviceTitle(), SentenceHelper.enumerationAND(aSuggestedLocations)));
            query.add(SentenceHelper.randomResponse(m_context, R.array.wanna_know_value));
            notifyContextNotReady(BaseSensorModule.this, query);
        }
    }

}
