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

import android.content.Context;
import android.content.res.Resources;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;

import com.android.settings.R;
import com.android.settingslib.core.AbstractPreferenceController;

import static android.provider.Settings.System.BUTTON_BRIGHTNESS_ENABLED;

public class ButtonBrightnessPreferenceController extends AbstractPreferenceController implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "ButtonBrightnessPref";
  
    private final String mButtonBrightnessKey;

    private int mDeviceHardwareKeys;

    public ButtonBrightnessPreferenceController(Context context, String key) {
        super(context);
        mButtonBrightnessKey = key;
    }

    @Override
    public boolean isAvailable() {
        final Resources res = mContext.getResources();
        mDeviceHardwareKeys = res.getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys);
        if (mButtonBrightnessKey != null) {
            if (mDeviceHardwareKeys != 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getPreferenceKey() {
        return mButtonBrightnessKey;
    }

    @Override
    public void updateState(Preference preference) {
        int value = Settings.System.getInt(mContext.getContentResolver(), BUTTON_BRIGHTNESS_ENABLED, 1);
        ((SwitchPreference) preference).setChecked(value != 0);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final boolean enabled = (boolean) newValue;
        Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.BUTTON_BRIGHTNESS_ENABLED, enabled ? 1 : 0);
        return true;
    }
}
