package com.minhui.networkcapture;


import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.minhui.vpn.ThreadProxy;
import com.minhui.vpn.VPNConstants;

import java.io.File;

/**
 * @author minhui.zhu
 *         Created by minhui.zhu on 2018/5/5.
 *         Copyright © 2017年 Oceanwing. All rights reserved.
 */

public class HistoryFragment extends BaseFragment {

    private SwipeRefreshLayout refreshContainer;
    private RecyclerView timeList;
    private String[] list;
    private Handler handler;
    private HistoryListAdapter historyListAdapter;
    private String[] rawList;

    @Override
    int getLayout() {
        return R.layout.fragment_history;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refreshContainer = view.findViewById(R.id.refresh_container);
        timeList = view.findViewById(R.id.time_list);
        timeList.setLayoutManager(new LinearLayoutManager(getActivity()));
        refreshView();
        refreshContainer.setEnabled(true);
        refreshContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshView();
            }
        });
        handler = new Handler();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        historyListAdapter = null;
    }

    private void refreshView() {
        ThreadProxy.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                File file = new File(VPNConstants.BASE_DIR);
                rawList = file.list();
                list = file.list();
                if (list != null ) {
                    for (int i = 0; i < list.length; i++) {
                        list[i] = list[i].replace('_', ' ');
                    }
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (historyListAdapter == null) {
                            historyListAdapter = new HistoryListAdapter();
                            timeList.setAdapter(historyListAdapter);
                        } else {
                            historyListAdapter.notifyDataSetChanged();
                        }
                        refreshContainer.setRefreshing(false);
                    }
                });

            }
        });
    }

    class HistoryListAdapter extends RecyclerView.Adapter {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View inflate = View.inflate(getActivity(), R.layout.item_select_date, null);
            return new CommonHolder(inflate);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            ((CommonHolder) holder).date.setText(list[position]);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String fileDir = VPNConstants.BASE_DIR + rawList[position];
                    ConnectionListActivity.openActivity(getActivity(), fileDir);
                }
            });
        }

        @Override
        public int getItemCount() {
            return list == null ? 0 : list.length;
        }

        class CommonHolder extends RecyclerView.ViewHolder {
            TextView date;

            public CommonHolder(View itemView) {
                super(itemView);
                date = itemView.findViewById(R.id.date);
            }
        }
    }


}
