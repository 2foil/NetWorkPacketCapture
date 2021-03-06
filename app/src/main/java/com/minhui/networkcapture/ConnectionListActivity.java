package com.minhui.networkcapture;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.minhui.vpn.ACache;
import com.minhui.vpn.AppInfo;
import com.minhui.vpn.BaseNetConnection;
import com.minhui.vpn.ConversationData;
import com.minhui.vpn.ThreadProxy;
import com.minhui.vpn.TimeFormatUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

/**
 * @author minhui.zhu
 *         Created by minhui.zhu on 2018/5/6.
 *         Copyright © 2017年 Oceanwing. All rights reserved.
 */

public class ConnectionListActivity extends Activity {

    private RecyclerView recyclerView;
    public static final String FILE_DIRNAME = "file_dirname";
    private String fileDir;
    private ArrayList<BaseNetConnection> baseNetConnections;
    private Handler handler;
    private ConnectionAdapter connectionAdapter;
    private PackageManager packageManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_list);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        recyclerView = findViewById(R.id.recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(ConnectionListActivity.this));
        fileDir = getIntent().getStringExtra(FILE_DIRNAME);
        handler = new Handler();
        ThreadProxy.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                baseNetConnections = new ArrayList<>();
                File file = new File(fileDir + "/config");
                ACache aCache = ACache.get(file);
                String[] list = file.list();
                for (String fileName : list) {
                    BaseNetConnection netConnection = (BaseNetConnection) aCache.getAsObject(fileName);
                    baseNetConnections.add(netConnection);
                }
                Collections.sort(baseNetConnections, new BaseNetConnection.NetConnectionComparator());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        connectionAdapter = new ConnectionAdapter();
                        recyclerView.setAdapter(connectionAdapter);
                    }
                });

            }
        });
        packageManager = getPackageManager();

    }

    public static void openActivity(Activity activity, String dir) {
        Intent intent = new Intent(activity, ConnectionListActivity.class);
        intent.putExtra(FILE_DIRNAME, dir);
        activity.startActivity(intent);

    }

    class ConnectionAdapter extends RecyclerView.Adapter {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View inflate = View.inflate(ConnectionListActivity.this, R.layout.item_connection, null);
            return new ConnectionHolder(inflate);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final BaseNetConnection connection = baseNetConnections.get(position);
            ConnectionHolder connectionHolder = (ConnectionHolder) holder;
            Drawable icon;
            if (connection.appInfo != null) {
                icon = AppInfo.getIcon(getApplication(), connection.appInfo.pkgs.getAt(0));
            } else {
                icon = getDrawable(R.drawable.sym_def_app_icon);
            }

            connectionHolder.icon.setImageDrawable(icon);
            if (connection.appInfo != null) {
                connectionHolder.processName.setText(connection.appInfo.leaderAppName);
            } else {
                connectionHolder.processName.setText(getString(R.string.unknow));
            }

            connectionHolder.hostName.setVisibility(connection.url != null || connection.hostName != null ?
                    View.VISIBLE : View.GONE);
            connectionHolder.hostName.setText(connection.url != null ? connection.url : connection.hostName);
            connectionHolder.netState.setText(connection.ipAndPort);
            connectionHolder.isSSL.setVisibility(connection.isSSL ? View.VISIBLE : View.GONE);


            connectionHolder.refreshTime.setText(TimeFormatUtil.formatHHMMSSMM(connection.refreshTime));
            int sumByte = (int) (connection.sendByteNum + connection.receiveByteNum);

            String showSum;
            if (sumByte > 1000000) {
                showSum = String.valueOf((int) (sumByte / 1000000.0 + 0.5)) + "mb";
            } else if (sumByte > 1000) {
                showSum = String.valueOf((int) (sumByte / 1000.0 + 0.5)) + "kb";
            } else {
                showSum = String.valueOf(sumByte) + "b";
            }

            connectionHolder.size.setText(showSum);
            connectionHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startPacketDetailActivity(connection.getUniqueName());


                }
            });


        }

        @Override
        public int getItemCount() {
            return baseNetConnections.size();
        }

        class ConnectionHolder extends RecyclerView.ViewHolder {
            ImageView icon;
            TextView processName;
            TextView netState;
            TextView refreshTime;
            TextView size;
            TextView isSSL;
            TextView hostName;

            public ConnectionHolder(View view) {
                super(view);
                icon = (ImageView) view.findViewById(R.id.select_icon);
                refreshTime = (TextView) view.findViewById(R.id.refresh_time);
                size = (TextView) view.findViewById(R.id.net_size);
                isSSL = (TextView) view.findViewById(R.id.is_ssl);
                processName = (TextView) view.findViewById(R.id.app_name);
                netState = (TextView) view.findViewById(R.id.net_state);
                hostName = (TextView) view.findViewById(R.id.url);
            }
        }
    }

    private void startPacketDetailActivity(final String uniqueName) {
        ThreadProxy.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                File file = new File(fileDir + "/data");
                ACache aCache = ACache.get(file);
                Vector<ConversationData> conversationList = (Vector<ConversationData>) aCache.getAsObject(uniqueName);
                final ArrayList<ConversationData> conversationDataArray = new ArrayList<>();
                conversationDataArray.addAll(conversationList);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        PacketDetailActivity.startActivity(ConnectionListActivity.this, conversationDataArray);

                    }
                });
            }
        });


    }
}
