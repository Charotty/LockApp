package com.example.lockapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.lockapp.User;
import java.util.List;

public class UserAdapter extends BaseAdapter {
    private Context context;
    private List<User> userList;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public Object getItem(int position) {
        return userList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return userList.get(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.user_row, parent, false);
        }
        User user = userList.get(position);
        ((TextView) convertView.findViewById(R.id.textId)).setText(String.valueOf(user.id));
        ((TextView) convertView.findViewById(R.id.textFio)).setText(user.fio);
        ((TextView) convertView.findViewById(R.id.textPosition)).setText(user.position);
        ((TextView) convertView.findViewById(R.id.textPassword)).setText(user.password);
        ((TextView) convertView.findViewById(R.id.textRfid)).setText(user.rfidStr());
        ((TextView) convertView.findViewById(R.id.textFingerprint)).setText(user.fingerprintStr());
        ((TextView) convertView.findViewById(R.id.textIvRfid)).setText(user.ivRfidStr());
        ((TextView) convertView.findViewById(R.id.textIvFingerprint)).setText(user.ivFingerprintStr());
        return convertView;
    }
}
