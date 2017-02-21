package cz.kovar.petr.homevoice.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

/**
 * Created by petr on 21.02.17.
 */

public class UtilsConverter {

    public static int dp2px(Context aContext, int aDip) {
        Resources r = aContext.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, aDip, r.getDisplayMetrics());
    }
}
