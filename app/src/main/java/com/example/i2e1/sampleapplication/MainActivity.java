package com.example.i2e1.sampleapplication;

import android.Manifest;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Timer mTimer;
    private TextView tvDataUsed;

    private long startTxBytes;
    private long startRxBytes;

    private DecimalFormat decimalFormat;
    private RecyclerView rvDataUsage;
    private DataUsageListAdapter mAdapter;

    private ArrayList<ApplicationWithDataUsageStats> mDataSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvDataUsed = (TextView) findViewById(R.id.tvDataUsed);
        rvDataUsage = (RecyclerView) findViewById(R.id.rvDataUsage);

        mAdapter = new DataUsageListAdapter(MainActivity.this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        rvDataUsage.setLayoutManager(linearLayoutManager);
        rvDataUsage.setAdapter(mAdapter);

        decimalFormat = new DecimalFormat("#.000");

        startTxBytes = TrafficStats.getTotalTxBytes();
        startRxBytes = TrafficStats.getTotalRxBytes();

        initializeApplicationList();
        mAdapter.notifyDataSetChanged();

        mTimer = new Timer();
        mTimer.start();
    }

    private void initializeApplicationList() {

        mDataSet = new ArrayList<>();
        final PackageManager pm = getPackageManager();
        //get a list of installed apps.
        List<PackageInfo> packages = pm.getInstalledPackages(
                PackageManager.GET_PERMISSIONS);

        //loop through the list of installed packages and see if the selected
        //app is in the list
        for (PackageInfo packageInfo : packages) {
            if (packageInfo.requestedPermissions == null || !Arrays.asList(packageInfo.requestedPermissions).contains(Manifest.permission.INTERNET)) {
                continue;
            }
            System.out.println(packageInfo.requestedPermissions.length);

            ApplicationInfo applicationInfo = packageInfo.applicationInfo;

            ApplicationWithDataUsageStats applicationWithDataUsageStats = new ApplicationWithDataUsageStats();
            applicationWithDataUsageStats.uid = applicationInfo.uid;
            CharSequence name = applicationInfo.loadLabel(pm);
            applicationWithDataUsageStats.name = name == null ? "" : name.toString();
            Drawable drawable = applicationInfo.loadIcon(pm);
            if (drawable != null)
                applicationWithDataUsageStats.icon = drawable;
            applicationWithDataUsageStats.startTxBytes = TrafficStats.getUidTxBytes(applicationInfo.uid);
            applicationWithDataUsageStats.startRxBytes = TrafficStats.getUidRxBytes(applicationInfo.uid);
            applicationWithDataUsageStats.usedTxBytes = 0;
            applicationWithDataUsageStats.usedRxBytes = 0;

            mDataSet.add(applicationWithDataUsageStats);
        }
    }
    
    private void updateDataUsage() {
        long dataUsed = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes() - startTxBytes - startRxBytes;

        tvDataUsed.setText("Total data usage since app launch : " + decimalFormat.format(dataUsed / (1024.0f * 1024.0f)) + "  MB");
        if (mDataSet != null) {
            for (int i = 0; i < mDataSet.size(); i++) {
                System.out.println(mDataSet.get(i).name + "  =  " + TrafficStats.getUidTxBytes(mDataSet.get(i).uid) + "  +  " + TrafficStats.getUidRxBytes(mDataSet.get(i).uid));
                mDataSet.get(i).usedTxBytes = TrafficStats.getUidTxBytes(mDataSet.get(i).uid) - mDataSet.get(i).startTxBytes;
                mDataSet.get(i).usedRxBytes = TrafficStats.getUidRxBytes(mDataSet.get(i).uid) - mDataSet.get(i).startRxBytes;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    private class Timer extends CountDownTimer {

        public Timer() {
            super(Long.MAX_VALUE, 5000);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (MainActivity.this != null && tvDataUsed != null) {
                updateDataUsage();
            }
        }

        @Override
        public void onFinish() {
            mTimer = null;
        }
    }

    // Custom Adapter for listing available i2e1 wifi
    public class DataUsageListAdapter extends RecyclerView.Adapter<DataUsageListAdapter.ViewHolder> {
        private final DecimalFormat floatDecimalFormat;
        private final SimpleDateFormat dateFormat;

        public DataUsageListAdapter(Context context) {
            floatDecimalFormat = new DecimalFormat("0.00");
            dateFormat = new SimpleDateFormat("M/d/yyyy", Locale.US);
        }

        protected class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView tvName;
            private final ImageView ivAppIcon;
            private final TextView tvDataUsed;
            private final View root;

            public ViewHolder(View itemView) {
                super(itemView);
                tvName = (TextView) itemView.findViewById(R.id.tvName);
                tvDataUsed = (TextView) itemView.findViewById(R.id.tvDataUsed);
                ivAppIcon = (ImageView) itemView.findViewById(R.id.ivAppIcon);
                root = itemView.findViewById(R.id.root);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_data_usage_list_item, parent, false);

            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            String dataUsedText = "";
            float dataUsed = (mDataSet.get(position).usedTxBytes + mDataSet.get(position).usedRxBytes) / (1024.0f * 1024.0f);
            dataUsedText = floatDecimalFormat.format(dataUsed) + " MB";

            holder.tvDataUsed.setText(dataUsedText);
            holder.tvName.setText(mDataSet.get(position).name);

            if (mDataSet.get(position).icon != null)
                holder.ivAppIcon.setImageDrawable(mDataSet.get(position).icon);
            else
                holder.ivAppIcon.setImageResource(R.mipmap.ic_launcher);
        }

        @Override
        public int getItemCount() {
            return mDataSet != null ? mDataSet.size() : 0;
        }
    }

    private class ApplicationWithDataUsageStats {
        private int uid;
        private String name;
        UsageStats usageStats;
        private Drawable icon;
        private long startTxBytes;
        private long startRxBytes;
        private long usedRxBytes;
        private long usedTxBytes;
    }
}