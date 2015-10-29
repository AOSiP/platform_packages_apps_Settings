package com.android.settings.aosip;

import android.content.Context;
import android.os.Bundle;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class AosipAbout extends SettingsPreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.aosip_about_info);
	getActivity().getActionBar().setTitle("About AOSiP");
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.OWLSNEST;
    }
}
