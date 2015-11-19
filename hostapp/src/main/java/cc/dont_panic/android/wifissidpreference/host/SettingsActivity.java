package cc.dont_panic.android.wifissidpreference.host;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import cc.dont_panic.android.wifissidpreference.WifiSsidPreference;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragement()).commit();
    }


    public static class PrefsFragement extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        public static final String WHITELISTED_WIFI_SSIDS = "whitelisted_wifi_ssids";

        private WifiSsidPreference ssidPreference;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);

            ssidPreference = (WifiSsidPreference) findPreference("whitelisted_wifi_ssids");
            updateSummary();
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        private void updateSummary() {
            SharedPreferences sp = getPreferenceManager().getSharedPreferences();
            Set<String> wifis = sp.getStringSet(WHITELISTED_WIFI_SSIDS, null);

            if (ssidPreference != null) {
                if (wifis != null) {
                    SortedSet<String> sortedWifis = new TreeSet<>(wifis);
                    ssidPreference.setSummary("Only allow: " + sortedWifis);
                } else {
                    ssidPreference.setSummary("All WiFi allowed.");
                } // if/else
            } // if
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            updateSummary();
        }
    }

}
