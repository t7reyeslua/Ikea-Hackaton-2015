package com.kien.luna.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kien.luna.R;
import com.kien.luna.model.DeviceItem;

import java.util.Collections;
import java.util.List;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.DeviceRowViewHolder> {
    List<DeviceItem> data = Collections.emptyList();
    private LayoutInflater inflater;
    private Context context;


    public DeviceListAdapter(Context context, List<DeviceItem> data) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.data = data;
    }

    public void delete(int position) {
        data.remove(position);
        notifyItemRemoved(position);
    }


    @Override
    public DeviceRowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.device_row, parent, false);
        DeviceRowViewHolder holder = new DeviceRowViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(DeviceRowViewHolder holder, int position) {
        DeviceItem current = data.get(position);
        holder.mDeviceNameTextView.setText(current.getName());

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class DeviceRowViewHolder extends RecyclerView.ViewHolder {
        TextView mDeviceNameTextView;
        ImageView icon;
        TextView selectionIndicator;

        public DeviceRowViewHolder(View itemView) {
            super(itemView);
            mDeviceNameTextView = (TextView) itemView.findViewById(R.id.device_name_textview);
            selectionIndicator = (TextView) itemView.findViewById(R.id.drawer_bar);
            icon = (ImageView) itemView.findViewById(R.id.drawer_icon);
        }
    }
}
