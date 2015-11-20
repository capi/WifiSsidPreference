package cc.dont_panic.android.wifissidpreference;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Dialog preference which allows the user to select on which WiFi networks (based on SSID)
 * syncing should be allowed.
 *
 * Setting can be "All networks", or selecting individual networks.
 *
 * Saving is only allowed if either "All" or at least one WiFi has been selected, otherwise
 * the strange situation "Sync only on wifi" with none-allowed could occure, actually preventing
 * any synchronisation.
 *
 * Due to restrictions in Android, it is possible/likely, that the list of saved WiFi networks
 * cannot be retrieved if the WiFi is turned off. In this case, an explanation is shown.
 *
 * The preference is stored as Set&lt;String&gt; where <code>null</code> represents
 * "all networks allowed", and a set represents a restriction to the selected SSIDs.
 *
 * SSIDs are formatted according to the naming convention of WifiManager, i.e. they have the
 * surrounding double-quotes (") for UTF-8 names, or they are hex strings (if not quoted).
 */
public class WifiSsidPreference extends DialogPreference {

    /**
     * If all WiFi are enabled, or only selective.
     */
    private SwitchCompat mAllSwitch;
    /**
     * List containing either a notification that WiFi is disabled, or the list of saved
     * networks.
     */
    private ListView mListWifi;
    /**
     * The configured WiFi networks, or null, if WiFi is not available.
     */
    private WifiConfiguration[] mConfiguredNetworks;
    /**
     * The SSID of the networks that have been enabled (in selective mode)
     */
    private Set<String> mSelectedSsids;
    /**
     * Adapter between the data above and the ListView
     */
    private WifiListAdapter listAdapter;

    public WifiSsidPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.preference_layout);
    }

    @Override
    protected View onCreateDialogView() {
        View view = super.onCreateDialogView();
        mAllSwitch = (SwitchCompat) view.findViewById(R.id.switchAllWifi);
        mListWifi = (ListView) view.findViewById(R.id.listWifi);
        mAllSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (listAdapter != null) {
                    listAdapter.notifyDataSetChanged();
                } // if
                updateSaveButtonState();
            }
        });
        return view;
    }

    /**
     * This is where the data is actually bound.
     */
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        if (isPersistent()) {
            // currently persisted values are pre-selected in the list
            String key = getKey();
            Set<String> ssids = getSharedPreferences().getStringSet(key, null);
            mSelectedSsids = ssids != null ? new HashSet<>(ssids) : new HashSet<String>();
            // there is a difference between none-selected and all-allowed
            mAllSwitch.setChecked(ssids == null);
        } else {
            mSelectedSsids = new HashSet<>(); // if non-persistent, nothing is pre-selected
            mAllSwitch.setChecked(true); // non-persistent mode starts with all-selected
        } // if/else


        mConfiguredNetworks = loadConfiguredNetworksSorted();
        listAdapter = new WifiListAdapter();
        mListWifi.setAdapter(listAdapter);
        mAllSwitch.setEnabled(mConfiguredNetworks != null);
    }

    /**
     * Load the configured WiFi networks, sort them by SSID.
     *
     * @return a sorted array of WifiConfiguration, or null, if data cannot be retrieved
     */
    private WifiConfiguration[] loadConfiguredNetworksSorted() {
        WifiManager wifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
            // if WiFi is turned off, getConfiguredNetworks returns null on many devices
            if (configuredNetworks != null) {
                WifiConfiguration[] result = configuredNetworks.toArray(new WifiConfiguration[configuredNetworks.size()]);
                Arrays.sort(result, new Comparator<WifiConfiguration>() {
                    @Override
                    public int compare(WifiConfiguration lhs, WifiConfiguration rhs) {
                        return lhs.SSID.compareToIgnoreCase(rhs.SSID);
                    }
                });
                return result;
            } // if
        } // if
        // WiFi is turned of or device doesn't have WiFi
        return null;
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        updateSaveButtonState();
    }

    private void updateSaveButtonState() {
        Dialog dialog = getDialog();
        if (dialog instanceof AlertDialog) {
            AlertDialog alert = (AlertDialog) dialog;
            // enabled if the WiFi information is available,
            // and either all or at least one network selected
            boolean saveEnabled = mConfiguredNetworks != null &&
                    (mAllSwitch.isChecked() || !mSelectedSsids.isEmpty());
            alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(saveEnabled);
        } // if
    }

    /**
     * Responsible for persisting (if positiveResult).
     */
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult && isPersistent()) {
            // getEditor() returns new object every time, so need to store
            SharedPreferences.Editor editor = getEditor();
            String key = getKey();
            if (mAllSwitch.isChecked()) {
                // when all networks are allowed, the preference is removed (null == allow all)
                editor.remove(key);
                invokeOnPreferenceChange(null);
            } else {
                // Defensive copy of SSID names, as set is then owned by the SharedPrefernces
                Set<String> ssids = new HashSet<>();
                ssids.addAll(mSelectedSsids);
                invokeOnPreferenceChange(ssids);
                editor.putStringSet(key, ssids);
            } // if/else
            if (shouldCommit()) {
                editor.commit();
            } // if
        } // if
    }

    private void invokeOnPreferenceChange(Set<String> ssids) {
        OnPreferenceChangeListener listener = getOnPreferenceChangeListener();
        if (listener != null) {
            listener.onPreferenceChange(this, ssids);
        } // if
    }

    /**
     * Adapter between the available and selected Wifis in the preference.
     */
    class WifiListAdapter extends BaseAdapter {

        /**
         * @return either the number of configured networks, or 1 if information unavailable
         */
        @Override
        public int getCount() {
            return mConfiguredNetworks != null ? mConfiguredNetworks.length : 1;
        }

        @Override
        public Object getItem(int position) {
            return mConfiguredNetworks != null ? mConfiguredNetworks[position] : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (mConfiguredNetworks != null) {
                WifiConfiguration wifi = mConfiguredNetworks[position];
                boolean viewCreated = false;

                // Create view only if new or reuse of provided view is not possible
                if (convertView == null
                        || !(convertView.findViewById(R.id.wifiName) instanceof SwitchCompat)) {
                    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.wifi_row, parent, false);
                    viewCreated = true;
                }

                // SSIDs are formatted "name" if they represent an UTF-8 string,
                // or as 01234567890ABCDEF if hex, but displaying the '"' doesn't look good,
                // so strip them
                String displayName = wifi.SSID;
                if (displayName != null && displayName.length() > 0 && displayName.charAt(0) == '"') {
                    // If th SSID starts with '"', it is a string name, otherwise a hex string
                    if (displayName.length() >= 2) {
                        // actually, this should always be true, otherwise it's a violation of the
                        // contract of how SSID names are formatted by WifiManager
                        displayName = displayName.substring(1, displayName.length() - 1);
                    }
                }

                SwitchCompat wifiView = (SwitchCompat) convertView.findViewById(R.id.wifiName);
                wifiView.setTag(wifi.SSID); // the SSID this possibly recylced view is currently representing6
                wifiView.setText(displayName);
                wifiView.setEnabled(!mAllSwitch.isChecked());
                wifiView.setChecked(mSelectedSsids.contains(wifi.SSID));

                if (viewCreated) {
                    wifiView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            // since a view may be reused, it's not possible to capture the value
                            // of the SSID with a final variable, but it's necessary to transport
                            // it via the CompundButton's tag
                            String ssid = (String) buttonView.getTag();
                            if (isChecked) {
                                mSelectedSsids.add(ssid);
                            } else {
                                mSelectedSsids.remove(ssid);
                            } // if/else
                            updateSaveButtonState();
                        }
                    });
                }
                return convertView;
            } else {
                // WiFi is most likely turned off
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
                TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
                textView.setText(getContext().getString(R.string.turn_on_wifi));
                textView.setEnabled(false);
                return convertView;
            }
        }
    }
}