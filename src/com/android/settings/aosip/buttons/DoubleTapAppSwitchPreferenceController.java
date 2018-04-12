/*
 * Copyright (C) 2018 AOSPA
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.android.settings.aosip.buttons;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;

import com.android.settings.R;
import com.android.settingslib.core.AbstractPreferenceController;

import static android.provider.Settings.System.KEY_APP_SWITCH_DOUBLE_TAP_ACTION;

public class DoubleTapAppSwitchPreferenceController extends AbstractPreferenceController implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "DoubleTapAppSwitchPref";

    private final String mDoubleTapAppSwitchKey;

    private ListPreference mDoubleTapAppSwitch;

    public DoubleTapAppSwitchPreferenceController(Context context, String key) {
        super(context);
        mDoubleTapAppSwitchKey = key;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return mDoubleTapAppSwitchKey;
    }

    @Override
    public void updateState(Preference preference) {
        final ListPreference mDoubleTapAppSwitch = (ListPreference) preference;
        final Resources res = mContext.getResources();
        if (mDoubleTapAppSwitch != null) {
            int defaultDoubleTapOnAppSwitchKeyBehavior = res.getInteger(
                    com.android.internal.R.integer.config_doubleTapOnAppSwitchKeyBehavior);
            int doubleTapOnAppSwitchKeyBehavior = Settings.System.getIntForUser(mContext.getContentResolver(),
                    Settings.System.KEY_APP_SWITCH_DOUBLE_TAP_ACTION,
                    defaultDoubleTapOnAppSwitchKeyBehavior,
                    UserHandle.USER_CURRENT);
            String appSwitchKey = String.valueOf(doubleTapOnAppSwitchKeyBehavior);
            mDoubleTapAppSwitch.setValue(appSwitchKey);
            updateDoubleTapAppSwitchSummary(mDoubleTapAppSwitch, appSwitchKey);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            String appSwitchKey = (String) newValue;
            Settings.System.putIntForUser(mContext.getContentResolver(), Settings.System.KEY_APP_SWITCH_DOUBLE_TAP_ACTION,
                    Integer.parseInt(appSwitchKey), UserHandle.USER_CURRENT);
            updateDoubleTapAppSwitchSummary((ListPreference) preference, appSwitchKey);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Could not persist screenshot mode setting", e);
        }
        return true;
    }

    private void updateDoubleTapAppSwitchSummary(Preference mDoubleTapAppSwitch, String appSwitchKey) {
        if (appSwitchKey != null) {
            String[] values = mContext.getResources().getStringArray(R.array
                    .action_values);
            final int summaryArrayResId = R.array.action_entries;
            String[] summaries = mContext.getResources().getStringArray(summaryArrayResId);
            for (int i = 0; i < values.length; i++) {
                if (appSwitchKey.equals(values[i])) {
                    if (i < summaries.length) {
                        mDoubleTapAppSwitch.setSummary(summaries[i]);
                        return;
                    }
                }
            }
        }

        mDoubleTapAppSwitch.setSummary("");
        Log.e(TAG, "Invalid double tap value: " + appSwitchKey);
    }
}
