package com.appsheet.adityabansal.appsheet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/*
Custom Adaper for the ListView
 */
class UserAdapter extends ArrayAdapter<User> {
    public UserAdapter(Context context, ArrayList<User> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        User user = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_item, parent, false);
        }
        TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
        TextView tvAge = (TextView) convertView.findViewById(R.id.tvAge);
        TextView tvNum = (TextView) convertView.findViewById(R.id.tvNum);

        tvName.setText(user.name + " ");
        tvAge.setText(user.age + " ");
        tvNum.setText(user.number + " ");

        return convertView;
    }
}