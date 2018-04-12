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

import static android.provider.Settings.System.KEY_CAMERA_LONG_PRESS_ACTION;

public class LongPressCameraPreferenceController extends AbstractPreferenceController implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "LongPressCameraPref";

    private final String mLongPressCameraKey;

    private ListPreference mLongPressCamera;

    public LongPressCameraPreferenceController(Context context, String key) {
        super(context);
        mLongPressCameraKey = key;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return mLongPressCameraKey;
    }

    @Override
    public void updateState(Preference preference) {
        final ListPreference mLongPressCamera = (ListPreference) preference;
        final Resources res = mContext.getResources();
        if (mLongPressCamera != null) {
            int defaultLongPressOnCameraKeyBehavior = res.getInteger(
                    com.android.internal.R.integer.config_longPressOnCameraKeyBehavior);
            int longPressOnCameraKeyBehavior = Settings.System.getIntForUser(mContext.getContentResolver(),
                    Settings.System.KEY_CAMERA_LONG_PRESS_ACTION,
                    defaultLongPressOnCameraKeyBehavior,
                    UserHandle.USER_CURRENT);
            String cameraKey = String.valueOf(longPressOnCameraKeyBehavior);
            mLongPressCamera.setValue(cameraKey);
            updateLongPressCameraSummary(mLongPressCamera, cameraKey);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            String cameraKey = (String) newValue;
            Settings.System.putIntForUser(mContext.getContentResolver(), Settings.System.KEY_CAMERA_LONG_PRESS_ACTION,
                    Integer.parseInt(cameraKey), UserHandle.USER_CURRENT);
            updateLongPressCameraSummary((ListPreference) preference, cameraKey);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Could not persist screenshot mode setting", e);
        }
        return true;
    }

    private void updateLongPressCameraSummary(Preference mLongPressCamera, String cameraKey) {
        if (cameraKey != null) {
            String[] values = mContext.getResources().getStringArray(R.array
                    .action_values);
            final int summaryArrayResId = R.array.action_entries;
            String[] summaries = mContext.getResources().getStringArray(summaryArrayResId);
            for (int i = 0; i < values.length; i++) {
                if (cameraKey.equals(values[i])) {
                    if (i < summaries.length) {
                        mLongPressCamera.setSummary(summaries[i]);
                        return;
                    }
                }
            }
        }

        mLongPressCamera.setSummary("");
        Log.e(TAG, "Invalid long press value: " + cameraKey);
    }
}
