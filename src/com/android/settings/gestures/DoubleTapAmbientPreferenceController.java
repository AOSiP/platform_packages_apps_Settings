/*
 * Copyright (C) 2019 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.settings.gestures;

import static android.provider.Settings.System.DOZE_TRIGGER_DOUBLETAP;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;

import com.android.internal.util.aosip.aosipUtils;
import com.android.settings.gestures.GesturePreferenceController;

public class DoubleTapAmbientPreferenceController extends GesturePreferenceController {

    private static final String PREF_KEY_VIDEO = "gesture_double_tap_screen_video";
    private static final String KEY_DOUBLE_TAP_SETTINGS = "double_tap";

    private final String SYSTEM_KEY = DOZE_TRIGGER_DOUBLETAP;

    static final int ON = 1;
    static final int OFF = 0;

    private Context mContext;

    public DoubleTapAmbientPreferenceController(Context context) {
        super(context, KEY_DOUBLE_TAP_SETTINGS);
        mContext = context;
    }

    @Override
    public int getAvailabilityStatus() {
        return (aosipUtils.isOlderPixelDevice())
            ? AVAILABLE
            : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    protected String getVideoPrefKey() {
        return PREF_KEY_VIDEO;
    }

    @Override
    public CharSequence getSummary() {
        return super.getSummary();
    }

    @Override
    public boolean isChecked() {
        final int enabled = Settings.System.getInt(mContext.getContentResolver(),
                DOZE_TRIGGER_DOUBLETAP, ON);
        return enabled == ON;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        return Settings.System.putInt(mContext.getContentResolver(), DOZE_TRIGGER_DOUBLETAP,
                isChecked ? ON : OFF);
    }
}
