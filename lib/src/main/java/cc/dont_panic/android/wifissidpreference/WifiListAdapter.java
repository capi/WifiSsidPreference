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
        mWifis = new WifiConfiguration[wifis.size()];
        mWifis = wifis.toArray(mWifis);
    }

    @Override
    public int getCount() {
        return mWifis.length;
    }

    @Override
    public Object getItem(int position) {
        return mWifis[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        WifiConfiguration wifi = mWifis[position];

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        } // if
        TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
        textView.setText(wifi.SSID);
        textView.setEnabled(enabled);

        return convertView;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            this.notifyDataSetChanged();
        } // if
    }
}
