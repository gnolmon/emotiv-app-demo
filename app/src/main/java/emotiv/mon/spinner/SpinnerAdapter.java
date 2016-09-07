package emotiv.mon.spinner;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Vector;

import emotiv.mon.R;

/**
 * Created by admin on 9/7/2016.
 */
public class SpinnerAdapter extends ArrayAdapter<SpinnerModel> {
    ArrayList<SpinnerModel> data;
    Context context;
    public Vector<String> headerData;

    public SpinnerAdapter(Context context, int textViewResourceId,
                          ArrayList<SpinnerModel> model) {
        super(context, textViewResourceId, model);
        this.context = context;
        data = model;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        Activity activity = (Activity) context;
        LayoutInflater inflater = activity.getLayoutInflater();
        View row = inflater.inflate(R.layout.row, parent, false);
        TextView label = (TextView) row.findViewById(R.id.tvTrain);
        label.setTextColor(Color.parseColor("#000000"));
        label.setText(data.get(position).getTvName());
        if (headerData!= null) {
            label.setText(headerData.elementAt(position));
        }

        ImageView icon = (ImageView) row.findViewById(R.id.iconCheck);

        if (data.get(position).isChecked()) {
            icon.setVisibility(View.VISIBLE);
        } else {
            icon.setVisibility(View.INVISIBLE);
        }
        return row;
    }
}

