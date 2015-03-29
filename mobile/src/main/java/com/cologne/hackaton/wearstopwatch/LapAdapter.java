package com.cologne.hackaton.wearstopwatch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.cologne.hackaton.wearstopwatch.timelib.model.Lap;
import com.cologne.hackaton.wearstopwatch.timelib.utils.StringUtils;

import java.util.List;

/**
 * Provides adapter class for the list of Lap
 *
 * @author Dmytro Khmelenko, Gabriel Schlatter
 */
public class LapAdapter extends ArrayAdapter<Lap> {

    private Context mContext;

    public LapAdapter(Context context, int resource, List<Lap> objects) {
        super(context, resource, objects);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.lap_list_item, null);
            LapHolder holder = new LapHolder();
            holder.mText = (TextView) convertView.findViewById(R.id.tv_text);
            convertView.setTag(holder);
        }

        LapHolder holder = (LapHolder) convertView.getTag();
        Lap lap = getItem(position);
        String lapString = "# " + lap.getLapNumber() + " "
                + StringUtils.formatString(lap.getLapTime()) + " - "
                + StringUtils.formatString(lap.getTimeSum());
        holder.mText.setText(lapString);
        return convertView;
    }

    private class LapHolder {
        private TextView mText;
    }
}