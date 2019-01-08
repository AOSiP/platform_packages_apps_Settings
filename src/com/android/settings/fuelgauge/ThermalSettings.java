/**
 * Copyright (C) 2015-2016 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.fuelgauge;

import android.util.Log;
import android.annotation.Nullable;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.android.settingslib.applications.ApplicationsState;
import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThermalSettings extends SettingsPreferenceFragment
        implements AdapterView.OnItemClickListener, ApplicationsState.Callbacks {

    private static final String THERMAL_PREFERENCE_TAG = "thermal_prefs";

    private static final int STATE_DEFAULT = 0;
    private static final int STATE_BENCHMARK = 1;
    private static final int STATE_BROWSER = 2;
    private static final int STATE_CAMERA = 3;
    private static final int STATE_DIALER = 4;
    private static final int STATE_GAMING = 5;
    private static final int STATE_YOUTUBE = 6;

    private static final String THERMAL_BENCHMARK = "thermal.benchmark=";
    private static final String THERMAL_BROWSER = "thermal.browser=";
    private static final String THERMAL_CAMERA = "thermal.camera=";
    private static final String THERMAL_DIALER = "thermal.dialer=";
    private static final String THERMAL_GAMING = "thermal.gaming=";
    private static final String THERMAL_YOUTUBE = "thermal.youtube=";

    private AllPackagesAdapter mAllPackagesAdapter;
    private ApplicationsState mApplicationsState;
    private ApplicationsState.Session mSession;
    private ActivityFilter mActivityFilter;
    private Map<String, ApplicationsState.AppEntry> mEntryMap =
            new HashMap<String, ApplicationsState.AppEntry>();

    private ListView mUserListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mApplicationsState = ApplicationsState.getInstance(getActivity().getApplication());
        mSession = mApplicationsState.newSession(this);
        mSession.onResume();
        mActivityFilter = new ActivityFilter(getActivity().getPackageManager());

        mAllPackagesAdapter = new AllPackagesAdapter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.thermal_layout, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mUserListView = (ListView) view.findViewById(R.id.thermal_list_view);
        mUserListView.setAdapter(mAllPackagesAdapter);
        mUserListView.setOnItemClickListener(this);
    }


    @Override
    public void onResume() {
        super.onResume();
        rebuild();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSession.onPause();
        mSession.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.mode.performClick();
    }

    @Override
    public void onPackageListChanged() {
        mActivityFilter.updateLauncherInfoList();
        rebuild();
    }

    @Override
    public void onRebuildComplete(ArrayList<ApplicationsState.AppEntry> entries) {
        if (entries != null) {
            handleAppEntries(entries);
            mAllPackagesAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoadEntriesCompleted() {
        rebuild();
    }

    @Override
    public void onAllSizesComputed() {}

    @Override
    public void onLauncherInfoChanged() {}

    @Override
    public void onPackageIconChanged() {}

    @Override
    public void onPackageSizeChanged(String packageName) {}

    @Override
    public void onRunningStateChanged(boolean running) {}

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.OWLSNEST;
    }

    private void writePackage(String packageName, int mode) {
        String value = getValue();
        value = value.replace(packageName, "");
        String[] modes = value.split(":");
        String finalString; 

        switch (mode) {
            case STATE_BENCHMARK:
                modes[0] = modes[0] + packageName;
                break;
            case STATE_BROWSER:
                modes[1] = modes[1] + packageName;
                break;
            case STATE_CAMERA:
                modes[2] = modes[2] + packageName;
                break;
            case STATE_DIALER:
                modes[3] = modes[3] + packageName;
                break;
            case STATE_GAMING:
                modes[4] = modes[4] + packageName;
                break;
            case STATE_YOUTUBE:
                modes[5] = modes[5] + packageName;
                break;
        }

        finalString = modes[0] + ":" + modes[1] + ":" + modes[2] + ":" + modes[3] + ":" +
                modes[4] + ":" + modes[5];
        writeValue(finalString);
    }

    private  void writeValue(String packageName) {
        Settings.Global.putString(getContentResolver(), Settings.Global.THERMAL_CONTROL, packageName);
    }

    private String getValue() {
        String value = Settings.Global.getStringForUser(getActivity().getContentResolver(),
                Settings.Global.THERMAL_CONTROL,
                UserHandle.USER_CURRENT);

        if (value == null || value.isEmpty()) {
            value = THERMAL_BENCHMARK + ":" + THERMAL_BROWSER + ":" + THERMAL_CAMERA + ":" +
                    THERMAL_DIALER + ":" + THERMAL_GAMING + ":" + THERMAL_YOUTUBE;
            writeValue(value);
        }
        return value;
    }

    private int getStateForPackage(String packageName) {
        String value = getValue();
        String[] modes = value.split(":");        
        int state = STATE_DEFAULT;
        if (modes[0].contains(packageName)) {
            state = STATE_BENCHMARK;
        } else if (modes[1].contains(packageName)) {
            state = STATE_BROWSER;
        } else if (modes[2].contains(packageName)) {
            state = STATE_CAMERA;
        } else if (modes[3].contains(packageName)) {
            state = STATE_DIALER;
        } else if (modes[4].contains(packageName)) {
            state = STATE_GAMING;
        } else if (modes[5].contains(packageName)) {
            state = STATE_YOUTUBE;
        }

        return state;
    }

    private void handleAppEntries(List<ApplicationsState.AppEntry> entries) {
        final ArrayList<String> sections = new ArrayList<String>();
        final ArrayList<Integer> positions = new ArrayList<Integer>();
        final PackageManager pm = getPackageManager();
        String lastSectionIndex = null;
        int offset = 0;

        for (int i = 0; i < entries.size(); i++) {
            final ApplicationInfo info = entries.get(i).info;
            final String label = (String) info.loadLabel(pm);
            final String sectionIndex;

            if (!info.enabled) {
                sectionIndex = "--"; // XXX
            } else if (TextUtils.isEmpty(label)) {
                sectionIndex = "";
            } else {
                sectionIndex = label.substring(0, 1).toUpperCase();
            }

            if (lastSectionIndex == null ||
                    !TextUtils.equals(sectionIndex, lastSectionIndex)) {
                sections.add(sectionIndex);
                positions.add(offset);
                lastSectionIndex = sectionIndex;
            }

            offset++;
        }

        mAllPackagesAdapter.setEntries(entries, sections, positions);
        mEntryMap.clear();
        for (ApplicationsState.AppEntry e : entries) {
            mEntryMap.put(e.info.packageName, e);
        }
    }

    private void rebuild() {
        mSession.rebuild(mActivityFilter, ApplicationsState.ALPHA_COMPARATOR);
    }

    private int getStateDrawable(int state) {
        switch (state) {
            case STATE_BENCHMARK:
                return R.drawable.ic_thermal_benchmark;
            case STATE_BROWSER:
                return R.drawable.ic_thermal_browser;
            case STATE_CAMERA:
                return R.drawable.ic_thermal_camera;
            case STATE_DIALER:
                return R.drawable.ic_thermal_dialer;
            case STATE_GAMING:
                return R.drawable.ic_thermal_gaming;
             case STATE_YOUTUBE:
                return R.drawable.ic_thermal_youtube;
           case STATE_DEFAULT:
            default:
                return R.drawable.ic_thermal_default;
        }
    }

    private class AllPackagesAdapter extends BaseAdapter
            implements AdapterView.OnItemSelectedListener, SectionIndexer {

        private final LayoutInflater mInflater;
        private final ModeAdapter mModesAdapter;
        private List<ApplicationsState.AppEntry> mEntries = new ArrayList<>();
        private String[] mSections;
        private int[] mPositions;

        public AllPackagesAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
            mModesAdapter = new ModeAdapter(context);
            mActivityFilter = new ActivityFilter(context.getPackageManager());
        }

        @Override
        public int getCount() {
            return mEntries.size();
        }

        @Override
        public Object getItem(int position) {
            return mEntries.get(position);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public long getItemId(int position) {
            return mEntries.get(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder(mInflater.inflate(
                        R.layout.thermal_list_item, parent, false));
                holder.mode.setAdapter(mModesAdapter);
                holder.mode.setOnItemSelectedListener(this);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ApplicationsState.AppEntry entry = mEntries.get(position);

            if (entry == null) {
                return holder.rootView;
            }

            holder.title.setText(entry.label);
            mApplicationsState.ensureIcon(entry);
            holder.icon.setImageDrawable(entry.icon);
            holder.mode.setSelection(getStateForPackage(entry.info.packageName), false);
            holder.mode.setTag(entry);
            holder.stateIcon.setImageResource(getStateDrawable(
                    getStateForPackage(entry.info.packageName)));
            return holder.rootView;
        }

        private void setEntries(List<ApplicationsState.AppEntry> entries,
                List<String> sections, List<Integer> positions) {
            mEntries = entries;
            mSections = sections.toArray(new String[sections.size()]);
            mPositions = new int[positions.size()];
            for (int i = 0; i < positions.size(); i++) {
                mPositions[i] = positions.get(i);
            }
            notifyDataSetChanged();
        }


        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            final ApplicationsState.AppEntry entry = (ApplicationsState.AppEntry) parent.getTag();
            switch (position) {
                case STATE_DEFAULT:
                    writePackage(entry.info.packageName, STATE_DEFAULT);
                    break;
                case STATE_BENCHMARK:
                    writePackage(entry.info.packageName, STATE_BENCHMARK);
                    break;
                case STATE_BROWSER:
                    writePackage(entry.info.packageName, STATE_BROWSER);
                    break;
                case STATE_CAMERA:
                    writePackage(entry.info.packageName, STATE_CAMERA);
                    break;
                case STATE_DIALER:
                    writePackage(entry.info.packageName, STATE_DIALER);
                    break;
                case STATE_GAMING:
                    writePackage(entry.info.packageName, STATE_GAMING);
                    break;
                case STATE_YOUTUBE:
                    writePackage(entry.info.packageName, STATE_YOUTUBE);
                    break;
            }
            notifyDataSetChanged();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}

        @Override
        public int getPositionForSection(int section) {
            if (section < 0 || section >= mSections.length) {
                return -1;
            }

            return mPositions[section];
        }

        @Override
        public int getSectionForPosition(int position) {
            if (position < 0 || position >= getCount()) {
                return -1;
            }

            final int index = Arrays.binarySearch(mPositions, position);

            /*
             * Consider this example: section positions are 0, 3, 5; the supplied
             * position is 4. The section corresponding to position 4 starts at
             * position 3, so the expected return value is 1. Binary search will not
             * find 4 in the array and thus will return -insertPosition-1, i.e. -3.
             * To get from that number to the expected value of 1 we need to negate
             * and subtract 2.
             */
            return index >= 0 ? index : -index - 2;
        }

        @Override
        public Object[] getSections() {
            return mSections;
        }
    }

    private static class ViewHolder {
        private TextView title;
        private Spinner mode;
        private ImageView icon;
        private View rootView;
        private ImageView stateIcon;

        private ViewHolder(View view) {
            this.title = (TextView) view.findViewById(R.id.app_name);
            this.mode = (Spinner) view.findViewById(R.id.app_mode);
            this.icon = (ImageView) view.findViewById(R.id.app_icon);
            this.stateIcon = (ImageView) view.findViewById(R.id.state);
            this.rootView = view;

            view.setTag(this);
        }
    }

    private static class ModeAdapter extends BaseAdapter {

        private final LayoutInflater inflater;
        private final TypedValue textColorSecondary;
        private final int textColor;
        private final int[] items = {
                R.string.thermal_default,
                R.string.thermal_benchmark,
                R.string.thermal_browser,
                R.string.thermal_camera,
                R.string.thermal_dialer,
                R.string.thermal_gaming,
                R.string.thermal_youtube
        };

        private ModeAdapter(Context context) {
            inflater = LayoutInflater.from(context);

            textColorSecondary = new TypedValue();
            context.getTheme().resolveAttribute(com.android.internal.R.attr.textColorSecondary,
                    textColorSecondary, true);
            textColor = context.getColor(textColorSecondary.resourceId);
        }

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int position) {
            return items[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view;
            if (convertView != null) {
                view = (TextView) convertView;
            } else {
                view = (TextView) inflater.inflate(android.R.layout.simple_spinner_dropdown_item,
                        parent, false);
            }

            view.setText(items[position]);
            view.setTextColor(textColor);
            view.setTextSize(14f);

            return view;
        }
    }

    private class ActivityFilter implements ApplicationsState.AppFilter {

        private final PackageManager mPackageManager;
        private final List<String> mLauncherResolveInfoList = new ArrayList<String>();

        private ActivityFilter(PackageManager packageManager) {
            this.mPackageManager = packageManager;

            updateLauncherInfoList();
        }

        public void updateLauncherInfoList() {
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> resolveInfoList = mPackageManager.queryIntentActivities(i, 0);

            synchronized (mLauncherResolveInfoList) {
                mLauncherResolveInfoList.clear();
                for (ResolveInfo ri : resolveInfoList) {
                    mLauncherResolveInfoList.add(ri.activityInfo.packageName);
                }
            }
        }

        @Override
        public void init() {}

        @Override
        public boolean filterApp(ApplicationsState.AppEntry entry) {
            boolean show = !mAllPackagesAdapter.mEntries.contains(entry.info.packageName);
            if (show) {
                synchronized (mLauncherResolveInfoList) {
                    show = mLauncherResolveInfoList.contains(entry.info.packageName);
                }
            }
            return show;
        }
    }
}
