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

        Set<String> ssids = null;
        if (isPersistent()) {
            String key = getKey();
            boolean exists = getSharedPreferences().contains(key);
            ssids = getSharedPreferences().getStringSet(key, null);
            mAllSwitch.setChecked(ssids == null);
        } // if

        WifiManager wifiManager = (WifiManager) view.getContext().getSystemService(Context.WIFI_SERVICE);
        mConfiguredNetworks = wifiManager.getConfiguredNetworks();
        listAdapter = new WifiListAdapter(getContext(), mConfiguredNetworks, ssids);
        listAdapter.setEnabled(!mAllSwitch.isChecked());
        mListWifi.setAdapter(listAdapter);
        mAllSwitch.setEnabled(mConfiguredNetworks != null);

    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);

        Dialog dialog = getDialog();
        if (dialog instanceof AlertDialog) {
            AlertDialog alert = (AlertDialog) dialog;
            alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(mConfiguredNetworks != null);
        } // if
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
                ssids.addAll(listAdapter.getSelectedWifiSSIDs());
                editor.putStringSet(key, ssids);
            } // if/else
            if (shouldCommit()) {
                editor.commit();
            } // if
        } // if
    }

}
