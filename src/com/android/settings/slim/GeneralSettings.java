/*
 * Copyright (C) 2013 SlimRoms
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

package com.android.settings.slim;

import android.app.Activity;
import android.content.ContentResolver; 
import android.content.res.Resources;
import android.app.ActivityManagerNative;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.provider.Settings;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem; 
import android.view.Window;
import android.widget.AdapterView.AdapterContextMenuInfo;


import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeneralSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "GeneralSettings";

    private static final String KEY_CHRONUS = "chronus";
    private static final String KEY_LOW_BATTERY_WARNING_POLICY = "pref_low_battery_warning_policy";
private static final String KEY_VIBRATION_MULTIPLIER = "vibrator_multiplier"; 

    private ListPreference mLowBatteryWarning;
private ListPreference mVibrationMultiplier; 

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.slim_general_settings);

updateVibMulti();

        mLowBatteryWarning = (ListPreference) findPreference(KEY_LOW_BATTERY_WARNING_POLICY);
        mLowBatteryWarning.setOnPreferenceChangeListener(this);
        int lowBatteryWarning = Settings.System.getInt(getActivity().getContentResolver(),
                                    Settings.System.POWER_UI_LOW_BATTERY_WARNING_POLICY, 0);
        mLowBatteryWarning.setValue(String.valueOf(lowBatteryWarning));
        mLowBatteryWarning.setSummary(mLowBatteryWarning.getEntry());

        removePreferenceIfPackageNotInstalled(findPreference(KEY_CHRONUS));
    }

    @Override
    public void onResume() {
        super.onResume();
updateVibMulti();
    }

    @Override
    public void onPause() {
        super.onPause();
updateVibMulti();
    }

private void updateVibMulti() {
        	
	/* Globally Change the Vibration Multiplier */
         mVibrationMultiplier = (ListPreference) findPreference(KEY_VIBRATION_MULTIPLIER);
         
	 if(mVibrationMultiplier != null) {
            mVibrationMultiplier.setOnPreferenceChangeListener(this);
            String currentValue = Float.toString(Settings.System.getFloat(getActivity()
                     .getContentResolver(), Settings.System.VIBRATION_MULTIPLIER, 1)); 
            mVibrationMultiplier.setValue(currentValue);
            mVibrationMultiplier.setSummary(currentValue);
        }
    }


    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mLowBatteryWarning) {
            int lowBatteryWarning = Integer.valueOf((String) newValue);
            int index = mLowBatteryWarning.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWER_UI_LOW_BATTERY_WARNING_POLICY,
                    lowBatteryWarning);
            mLowBatteryWarning.setSummary(mLowBatteryWarning.getEntries()[index]);
            return true;
        } else  if (preference == mVibrationMultiplier) {
            String currentValue = (String) newValue;
            float val = Float.parseFloat(currentValue);
            Settings.System.putFloat(getActivity().getContentResolver(),
                     Settings.System.VIBRATION_MULTIPLIER, val); 
            mVibrationMultiplier.setSummary(currentValue);
            return true;
        } 

        return false;
    }

    private boolean removePreferenceIfPackageNotInstalled(Preference preference) {
        String intentUri = ((PreferenceScreen) preference).getIntent().toUri(1);
        Pattern pattern = Pattern.compile("component=([^/]+)/");
        Matcher matcher = pattern.matcher(intentUri);

        String packageName = matcher.find() ? matcher.group(1) : null;
        if (packageName != null) {
            try {
                getPackageManager().getPackageInfo(packageName, 0);
            } catch (NameNotFoundException e) {
                Log.e(TAG, "package " + packageName + " not installed, hiding preference.");
                getPreferenceScreen().removePreference(preference);
                return true;
            }
        }
        return false;
    }
}
