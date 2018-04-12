/*
 * Copyright (C) 2018 AOSPA
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

package com.android.settings.aosip;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.aosip.buttons.ButtonBrightnessPreferenceController;
import com.android.settings.aosip.buttons.DoubleTapAppSwitchPreferenceController;
import com.android.settings.aosip.buttons.DoubleTapAssistPreferenceController;
import com.android.settings.aosip.buttons.DoubleTapBackPreferenceController;
import com.android.settings.aosip.buttons.DoubleTapCameraPreferenceController;
import com.android.settings.aosip.buttons.DoubleTapHomePreferenceController;
import com.android.settings.aosip.buttons.DoubleTapMenuPreferenceController;
import com.android.settings.aosip.buttons.LongPressAppSwitchPreferenceController;
import com.android.settings.aosip.buttons.LongPressAssistPreferenceController;
import com.android.settings.aosip.buttons.LongPressBackPreferenceController;
import com.android.settings.aosip.buttons.LongPressCameraPreferenceController;
import com.android.settings.aosip.buttons.LongPressHomePreferenceController;
import com.android.settings.aosip.buttons.LongPressMenuPreferenceController;
import com.android.settings.aosip.buttons.NavigationBarPreferenceController;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ButtonSettings extends DashboardFragment implements Indexable {

    private static final String LOG_TAG = "ButtonSettings";

    // Switches
    private static final String KEY_BUTTON_BRIGHTNESS      = "button_brightness";
    private static final String KEY_NAVIGATION_BAR         = "navigation_bar";

    // Long Press/Double Tap Actions
    private static final String KEY_HOME_LONG_PRESS        = "home_key_long_press";
    private static final String KEY_HOME_DOUBLE_TAP        = "home_key_double_tap";
    private static final String KEY_BACK_LONG_PRESS        = "back_key_long_press";
    private static final String KEY_BACK_DOUBLE_TAP        = "back_key_double_tap";
    private static final String KEY_MENU_LONG_PRESS        = "menu_key_long_press";
    private static final String KEY_MENU_DOUBLE_TAP        = "menu_key_double_tap";
    private static final String KEY_ASSIST_LONG_PRESS      = "assist_key_long_press";
    private static final String KEY_ASSIST_DOUBLE_TAP      = "assist_key_double_tap";
    private static final String KEY_APP_SWITCH_LONG_PRESS  = "app_switch_key_long_press";
    private static final String KEY_APP_SWITCH_DOUBLE_TAP  = "app_switch_key_double_tap";
    private static final String KEY_CAMERA_LONG_PRESS      = "camera_key_long_press";
    private static final String KEY_CAMERA_DOUBLE_TAP      = "camera_key_double_tap";

    // Categories
    private static final String KEY_CATEGORY_HOME          = "home_key";
    private static final String KEY_CATEGORY_BACK          = "back_key";
    private static final String KEY_CATEGORY_MENU          = "menu_key";
    private static final String KEY_CATEGORY_ASSIST        = "assist_key";
    private static final String KEY_CATEGORY_APP_SWITCH    = "app_switch_key";
    private static final String KEY_CATEGORY_CAMERA        = "camera_key";

    // Masked keys
    private static final int KEY_MASK_HOME = 0x01;
    private static final int KEY_MASK_BACK = 0x02;
    private static final int KEY_MASK_MENU = 0x04;
    private static final int KEY_MASK_ASSIST = 0x08;
    private static final int KEY_MASK_APP_SWITCH = 0x10;
    private static final int KEY_MASK_CAMERA = 0x20;

    private int mDeviceHardwareKeys;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        final Resources res = getActivity().getResources();
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        final boolean navigationBarEnabled = Settings.System.getIntForUser(resolver,
                Settings.System.NAVIGATION_BAR_ENABLED, 0, UserHandle.USER_CURRENT) != 0;

        mDeviceHardwareKeys = res.getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys);

        final boolean hasHome = (mDeviceHardwareKeys & KEY_MASK_HOME) != 0 || navigationBarEnabled;
        final boolean hasMenu = (mDeviceHardwareKeys & KEY_MASK_MENU) != 0;
        final boolean hasBack = (mDeviceHardwareKeys & KEY_MASK_BACK) != 0 || navigationBarEnabled;
        final boolean hasAssist = (mDeviceHardwareKeys & KEY_MASK_ASSIST) != 0;
        final boolean hasAppSwitch = (mDeviceHardwareKeys & KEY_MASK_APP_SWITCH) != 0 || navigationBarEnabled;
        final boolean hasCamera = (mDeviceHardwareKeys & KEY_MASK_CAMERA) != 0;

        final PreferenceCategory homeCategory =
                (PreferenceCategory) prefScreen.findPreference(KEY_CATEGORY_HOME);

        final PreferenceCategory backCategory =
                (PreferenceCategory) prefScreen.findPreference(KEY_CATEGORY_BACK);

        final PreferenceCategory menuCategory =
                (PreferenceCategory) prefScreen.findPreference(KEY_CATEGORY_MENU);

        final PreferenceCategory assistCategory =
                (PreferenceCategory) prefScreen.findPreference(KEY_CATEGORY_ASSIST);

        final PreferenceCategory appSwitchCategory =
                (PreferenceCategory) prefScreen.findPreference(KEY_CATEGORY_APP_SWITCH);

        final PreferenceCategory cameraCategory =
                (PreferenceCategory) prefScreen.findPreference(KEY_CATEGORY_CAMERA);

        if (!hasMenu && menuCategory != null) {
            prefScreen.removePreference(menuCategory);
        }

        if (!hasAssist && assistCategory != null) {
            prefScreen.removePreference(assistCategory);
        }

        if (!hasCamera && cameraCategory != null) {
            prefScreen.removePreference(cameraCategory);
        }

        mFooterPreferenceMixin.createFooterPreference().setTitle(R.string.button_settings_description);
    }

    @Override
    public int getMetricsCategory() {
        return -1;
    }

    @Override
    protected int getHelpResource() {
        return R.string.help_uri_about;
    }

    @Override
    protected String getLogTag() {
        return LOG_TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.button_settings;
    }

    @Override
    protected List<AbstractPreferenceController> getPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getActivity(), this /* fragment */,
                getLifecycle());
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context,
            Activity activity, Fragment fragment, Lifecycle lifecycle) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new ButtonBrightnessPreferenceController(context, KEY_BUTTON_BRIGHTNESS));
        controllers.add(new NavigationBarPreferenceController(context, KEY_NAVIGATION_BAR));
        /*Long Press/Double Tap Actions */
        controllers.add(new LongPressHomePreferenceController(context, KEY_HOME_LONG_PRESS));
        controllers.add(new DoubleTapHomePreferenceController(context, KEY_HOME_DOUBLE_TAP));
        controllers.add(new LongPressBackPreferenceController(context, KEY_BACK_LONG_PRESS));
        controllers.add(new DoubleTapBackPreferenceController(context, KEY_BACK_DOUBLE_TAP));
        controllers.add(new LongPressMenuPreferenceController(context, KEY_MENU_LONG_PRESS));
        controllers.add(new DoubleTapMenuPreferenceController(context, KEY_MENU_DOUBLE_TAP));
        controllers.add(new LongPressAssistPreferenceController(context, KEY_ASSIST_LONG_PRESS));
        controllers.add(new DoubleTapAssistPreferenceController(context, KEY_ASSIST_DOUBLE_TAP));
        controllers.add(new LongPressAppSwitchPreferenceController(context, KEY_APP_SWITCH_LONG_PRESS));
        controllers.add(new DoubleTapAppSwitchPreferenceController(context, KEY_APP_SWITCH_DOUBLE_TAP));
        controllers.add(new LongPressCameraPreferenceController(context, KEY_CAMERA_LONG_PRESS));
        controllers.add(new DoubleTapCameraPreferenceController(context, KEY_CAMERA_DOUBLE_TAP));
        return controllers;
    }

    /**
     * For Search.
     */
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.button_settings;
                    return Arrays.asList(sir);
                }

                @Override
                public List<AbstractPreferenceController> getPreferenceControllers(Context context) {
                    return buildPreferenceControllers(context, null /*activity */,
                            null /* fragment */, null /* lifecycle */);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
            };
}
