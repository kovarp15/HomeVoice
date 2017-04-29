/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 11.04.2017.
 * Copyright (c) 2017 Petr Kovář
 *
 * All rights reserved
 * kovarp15@fel.cvut.cz
 * HomeVoice for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * HomeVoice for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HomeVoice for Android.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.kovar.petr.homevoice.frontend.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

import cz.kovar.petr.homevoice.R;
import cz.kovar.petr.homevoice.zwave.dataModel.Instance;

public class InstancesGridAdapter extends BaseAdapter {

    public interface InstanceUpdatedListener{
        void onActiveClicked(Instance updatedInstance);
    }

    private final InstanceUpdatedListener m_listener;
    private List<Instance> m_instances;
    private Context m_context;
    private LayoutInflater m_inflater;


    public InstancesGridAdapter(Context context, List<Instance> objects,
                                InstanceUpdatedListener listener) {
        m_inflater = LayoutInflater.from(context);
        m_listener = listener;
        m_instances = objects;
        m_context = context;
    }

    @Override
    public int getCount() {
        return m_instances.size();
    }

    @Override
    public Object getItem(int position) {
        return m_instances.size() > position ? m_instances.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = m_inflater.inflate(R.layout.grid_item_instance, parent, false);
            convertView.setTag(new ViewHolder(convertView));
        }

        final ViewHolder holder = (ViewHolder) convertView.getTag();
        final Instance instance = (Instance) getItem(position);

        holder.name.setText(instance.title);
        holder.module.setText(instance.module);
        setDeviceIcon(holder, instance);
        prepareSwitch(holder, instance);
        return convertView;
    }

    public void update(Instance instance) {
        final int instancePosition = m_instances.indexOf(instance);
        if(instancePosition >= 0) {
            m_instances.remove(instance);
            m_instances.add(instancePosition, instance);
        } else {
            m_instances.add(instance);
        }

    }

    public void remove(Instance instance) {
        m_instances.remove(instance);
    }

    public void addAll(List<Instance> instances) {
        for(Instance instance: instances) {
            update(instance);
        }
    }

    private void setDeviceIcon(ViewHolder holder, Instance instance) {
        /*if(device.isIconLink()){
            //Picasso.with(m_context).load(device.metrics.icon).into(holder.icon);
        } else {
            if(device.getIconId() == 0){
                holder.icon.setImageResource(R.drawable.ic_device_unknown);
            } else {
                holder.icon.setImageResource(device.getIconId());
            }
        }*/
    }

    private void prepareSwitch(ViewHolder holder, final Instance instance){
        holder.active.setChecked(instance.active);
        holder.active.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_listener.onActiveClicked(instance);
            }
        });
    }

    private class ViewHolder{
        public View parent;
        public ImageView icon;
        public TextView name;
        public TextView module;
        public Switch active;

        private ViewHolder(View parent) {
            this.parent = parent;
            icon = (ImageView) parent.findViewById(R.id.instance_item_icon);
            name = (TextView) parent.findViewById(R.id.instance_item_title);
            module = (TextView) parent.findViewById(R.id.instance_item_module);
            active = (Switch) parent.findViewById(R.id.instance_item_switch);
        }
    }

}
