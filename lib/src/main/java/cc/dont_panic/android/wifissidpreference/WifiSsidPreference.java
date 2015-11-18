package cc.dont_panic.android.wifissidpreference;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.preference.DialogPreference;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ListView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WifiSsidPreference extends DialogPreference {

    private SwitchCompat mAllSwitch;
    private ListView mListWifi;
    private List<WifiConfiguration> mConfiguredNetworks;
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
                    listAdapter.setEnabled(!isChecked);
                } // if
            }
        });
        return view;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        if (isPersistent()) {
            String key = getKey();
            boolean exists = getSharedPreferences().contains(key);
            Set<String> ssids = getSharedPreferences().getStringSet(key, null);
            mAllSwitch.setChecked(ssids == null);
        } // if

        WifiManager wifiManager = (WifiManager) view.getContext().getSystemService(Context.WIFI_SERVICE);
        mConfiguredNetworks = wifiManager.getConfiguredNetworks();
        listAdapter = new WifiListAdapter(getContext(), mConfiguredNetworks);
        listAdapter.setEnabled(!mAllSwitch.isChecked());
        mListWifi.setAdapter(listAdapter);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult && isPersistent()) {
            SharedPreferences.Editor editor = getEditor(); // getEditor() returns new object every time
            String key = getKey();
            if (mAllSwitch.isChecked()) {
                editor.remove(key);
            } else {
                Set<String> ssids = new HashSet<>();
                editor.putStringSet(key, ssids);
            } // if/else
            if (shouldCommit()) {
                editor.commit();
            } // if
        } // if
    }

}
