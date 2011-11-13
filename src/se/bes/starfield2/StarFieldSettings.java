/*
 * Created on 18 feb 2011
 */
package se.bes.starfield2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class StarFieldSettings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getPreferenceManager().setSharedPreferencesName(StarField.SHARED_PREFS_NAME);
        addPreferencesFromResource(R.xml.star_preference);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    }

}
