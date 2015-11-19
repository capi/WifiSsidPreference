package cc.dont_panic.android.wifissidpreference;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WifiListAdapter extends BaseAdapter {

    private Context mContext;
    private WifiConfiguration[] mWifis;
    private Set<Integer> mCheckedIndexes = new HashSet<>();
    private boolean enabled;
    private Collection<? extends String> selectedWifiNames;

    public WifiListAdapter(Context context, List<WifiConfiguration> wifis, Set<String> selectedSsids) {
        mContext = context;
        if (wifis != null) {
            Collections.sort(wifis, new Comparator<WifiConfiguration>() {
                @Override
                public int compare(WifiConfiguration lhs, WifiConfiguration rhs) {
                    return lhs.SSID.compareTo(rhs.SSID);
                }
            });
            mWifis = new WifiConfiguration[wifis.size()];
            mWifis = wifis.toArray(mWifis);

            if (selectedSsids != null) {
                for (int i = 0; i < mWifis.length; i++) {
                    WifiConfiguration wifi = mWifis[i];
                    if (selectedSsids.contains(wifi.SSID)) {
                        mCheckedIndexes.add(i);
                    } // if
                } // for
            } // if
        } // if
    }

    @Override
    public int getCount() {
        return mWifis != null ? mWifis.length : 1;
    }

    @Override
    public Object getItem(int position) {
        return mWifis != null ? mWifis[position] : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (mWifis != null) {
            WifiConfiguration wifi = mWifis[position];
            boolean viewCreated = false;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.wifi_row, parent, false);
                viewCreated = true;
            } // if

            String displayName = wifi.SSID;
            if (displayName != null && displayName.length() > 0 && displayName.charAt(0) == '"') {
                // If th SSID starts with '"', it is a string name, otherwise a hex string
                if (displayName.length() >= 2) {
                    // actually, this should always be true, otherwise it's a violation of the
                    // contract of how SSID names are formatted by WifiManager
                    displayName = displayName.substring(1, displayName.length() - 1);
                } // if
            } // if

            SwitchCompat wifiView = (SwitchCompat) convertView.findViewById(R.id.wifiName);
            wifiView.setTag(position); // position is needed in the event listener, cannot capture due to reuse of component
            wifiView.setText(displayName);
            wifiView.setEnabled(enabled);
            wifiView.setChecked(mCheckedIndexes.contains(position));

            if (viewCreated) {
                wifiView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        int position = (int) buttonView.getTag();
                        if (isChecked) {
                            mCheckedIndexes.add(position);
                        } else {
                            mCheckedIndexes.remove(position);
                        } // if/else
                    }
                });
            } // if

            return convertView;
        } else {
            // WiFi is most likely turned off
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
            textView.setText("Please turn on WiFi to select networks.");
            textView.setEnabled(false);
            return convertView;
        } // if/else
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            this.notifyDataSetChanged();
        } // if
    }

    /**
     * @return a new Set with the currently selected SSIDs
     */
    public Set<String> getSelectedWifiSSIDs() {
        HashSet<String> wifiNames = new HashSet<>();
        for (int idx : mCheckedIndexes) {
            wifiNames.add(mWifis[idx].SSID);
        } // for
        return wifiNames;
    }
}
