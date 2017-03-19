/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 11.03.2017.
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

import android.content.Context;

import java.util.Collection;
import java.util.Random;

public class SentenceHelper {

    public static String enumerationAND(Collection<String> aStringSet) {
        String ret = "";
        int counter = 0;
        for(String location : aStringSet) {
            if(counter == 0) {
                ret += location;
            } else if(counter == aStringSet.size() - 1) {
                ret += " and " + location;
            } else {
                ret += ", " + location;
            }
            counter++;
        }
        return ret;
    }

    public static String enumerationOR(Collection<String> aStringSet) {
        String ret = "";
        int counter = 0;
        for(String location : aStringSet) {
            if(counter == 0) {
                ret += location;
            } else if(counter == aStringSet.size() - 1) {
                ret += " or " + location;
            } else {
                ret += ", " + location;
            }
            counter++;
        }
        return ret;
    }

    public static String randomResponse(Context aContext, int aStringArrayID) {
        String[] responseArray = aContext.getResources().getStringArray(aStringArrayID);
        return responseArray[new Random().nextInt(responseArray.length)];
    }

}
