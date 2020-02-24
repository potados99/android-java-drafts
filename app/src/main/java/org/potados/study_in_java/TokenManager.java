package org.potados.study_in_java;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.Nullable;

public class TokenManager {

    private static final String PREFERENCE_NAME = "YEAH";
    private static final String TOKEN_KEY = "MY_AWESOME_KEY";

    private Context mContext = null;

    public TokenManager(Context context) {
        this.mContext = context;
    }

    private SharedPreferences getPreference(Context context) {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Put token into shared preferences.
     * Existing token will be overwritten.
     * @param string token to put in.
     */
    public void putToken(@Nullable String string) {
        SharedPreferences prefs = getPreference(mContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(TOKEN_KEY, string);
        editor.apply();
    }

    /**
     * Get token from shared preferences.
     * @return token if exists, otherwise null.
     */
    @Nullable
    public String getToken() {
        SharedPreferences prefs = getPreference(mContext);
        return prefs.getString(TOKEN_KEY, null);
    }
}
