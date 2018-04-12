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

import static android.provider.Settings.System.KEY_ASSIST_DOUBLE_TAP_ACTION;

public class DoubleTapAssistPreferenceController extends AbstractPreferenceController implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "DoubleTapAssistPref";

    private final String mDoubleTapAssistKey;

    private ListPreference mDoubleTapAssist;

    public DoubleTapAssistPreferenceController(Context context, String key) {
        super(context);
        mDoubleTapAssistKey = key;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return mDoubleTapAssistKey;
    }

    @Override
    public void updateState(Preference preference) {
        final ListPreference mDoubleTapAssist = (ListPreference) preference;
        final Resources res = mContext.getResources();
        if (mDoubleTapAssist != null) {
            int defaultDoubleTapOnAssistKeyBehavior = res.getInteger(
                    com.android.internal.R.integer.config_doubleTapOnAssistKeyBehavior);
            int doubleTapOnAssistKeyBehavior = Settings.System.getIntForUser(mContext.getContentResolver(),
                    Settings.System.KEY_ASSIST_DOUBLE_TAP_ACTION,
                    defaultDoubleTapOnAssistKeyBehavior,
                    UserHandle.USER_CURRENT);
            String AssistKey = String.valueOf(doubleTapOnAssistKeyBehavior);
            mDoubleTapAssist.setValue(AssistKey);
            updateDoubleTapAssistSummary(mDoubleTapAssist, AssistKey);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            String AssistKey = (String) newValue;
            Settings.System.putIntForUser(mContext.getContentResolver(), Settings.System.KEY_ASSIST_DOUBLE_TAP_ACTION,
                    Integer.parseInt(AssistKey), UserHandle.USER_CURRENT);
            updateDoubleTapAssistSummary((ListPreference) preference, AssistKey);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Could not persist screenshot mode setting", e);
        }
        return true;
    }

    private void updateDoubleTapAssistSummary(Preference mDoubleTapAssist, String AssistKey) {
        if (AssistKey != null) {
            String[] values = mContext.getResources().getStringArray(R.array
                    .action_values);
            final int summaryArrayResId = R.array.action_entries;
            String[] summaries = mContext.getResources().getStringArray(summaryArrayResId);
            for (int i = 0; i < values.length; i++) {
                if (AssistKey.equals(values[i])) {
                    if (i < summaries.length) {
                        mDoubleTapAssist.setSummary(summaries[i]);
                        return;
                    }
                }
            }
        }

        mDoubleTapAssist.setSummary("");
        Log.e(TAG, "Invalid double tap value: " + AssistKey);
    }
}
