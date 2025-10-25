package com.metimol.todoshka;

import android.content.Context;
import android.util.DisplayMetrics;

public class Utils {
    public static float dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return dp * displayMetrics.density;
    }
}
