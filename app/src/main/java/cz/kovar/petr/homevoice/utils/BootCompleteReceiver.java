/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 21.01.2017.
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

package cz.kovar.petr.homevoice.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cz.kovar.petr.homevoice.KeywordSpotting;

/**
 * Receives intents caused by boot complete event.
 */
public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // start keyword spotting service once the boot is complete
        Intent keywordService = new Intent(context, KeywordSpotting.class);
        context.startService(keywordService);

    }

}
