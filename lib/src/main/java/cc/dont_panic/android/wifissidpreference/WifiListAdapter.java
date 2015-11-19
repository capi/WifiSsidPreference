package cc.dont_panic.android.wifissidpreference;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class WifiListAdapter extends BaseAdapter {

    private Context mContext;
    private WifiConfiguration[] mWifis;
    private boolean enabled;

    public WifiListAdapter(Context context, List<WifiConfiguration> wifis) {
        mContext = context;
        if (wifis != null) {
            mWifis = new WifiConfiguration[wifis.size()];
            mWifis = wifis.toArray(mWifis);
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
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            } // if
            TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
            textView.setText(wifi.SSID);
            textView.setEnabled(enabled);
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
}
