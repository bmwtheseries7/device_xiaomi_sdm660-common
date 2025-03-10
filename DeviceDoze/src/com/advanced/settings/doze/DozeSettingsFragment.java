/*
 * Copyright (C) 2018-2020 The Xiaomi-SDM660 Project
 *               2017-2023 The LineageOS Project
 *
 *  https://github.com/xiaomi-sdm660
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

package com.advanced.settings.doze;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreferenceCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.settingslib.widget.MainSwitchPreference;

public class DozeSettingsFragment extends PreferenceFragment
        implements OnCheckedChangeListener , OnPreferenceChangeListener {

    private MainSwitchPreference mSwitchBar;

    private SwitchPreferenceCompat mPickUpPreference;
    private SwitchPreferenceCompat mRaiseToWakePreference;
    private SwitchPreferenceCompat mHandwavePreference;
    private SwitchPreferenceCompat mPocketPreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.doze_settings);

        SharedPreferences prefs = getActivity().getSharedPreferences("doze_settings",
                Activity.MODE_PRIVATE);
        if (savedInstanceState == null && !prefs.getBoolean("first_help_shown", false)) {
            showHelp();
        }

        boolean dozeEnabled = Utils.isDozeEnabled(getActivity());

        mSwitchBar = (MainSwitchPreference) findPreference(Utils.DOZE_ENABLE);
        mSwitchBar.addOnSwitchChangeListener(this);
        mSwitchBar.setChecked(dozeEnabled);

        PreferenceCategory proximitySensorCategory =
                (PreferenceCategory) getPreferenceScreen().findPreference(Utils.CATEG_PROX_SENSOR);
                
        PreferenceCategory tiltSensorCategory =
                (PreferenceCategory) getPreferenceScreen().findPreference(Utils.CATEG_TILT_SENSOR);

        mPickUpPreference = (SwitchPreferenceCompat) findPreference(Utils.GESTURE_PICK_UP_KEY);
        mPickUpPreference.setEnabled(dozeEnabled);
        mPickUpPreference.setOnPreferenceChangeListener(this);
        
        mRaiseToWakePreference = (SwitchPreferenceCompat) findPreference(Utils.GESTURE_RAISE_TO_WAKE_KEY);
        mRaiseToWakePreference.setEnabled(dozeEnabled);
        mRaiseToWakePreference.setOnPreferenceChangeListener(this);

        mHandwavePreference = (SwitchPreferenceCompat) findPreference(Utils.GESTURE_HAND_WAVE_KEY);
        mHandwavePreference.setEnabled(dozeEnabled);
        mHandwavePreference.setOnPreferenceChangeListener(this);

        mPocketPreference = (SwitchPreferenceCompat) findPreference(Utils.GESTURE_POCKET_KEY);
        mPocketPreference.setEnabled(dozeEnabled);
        mPocketPreference.setOnPreferenceChangeListener(this);

        // Hide proximity sensor related features if the device doesn't support them
        if (!Utils.getProxCheckBeforePulse(getActivity())) {
            getPreferenceScreen().removePreference(proximitySensorCategory);
        }
        
        // Hide proximity sensor related features if the device doesn't support them
        if (!Utils.getProxCheckBeforePulse(getActivity())) {
            getPreferenceScreen().removePreference(tiltSensorCategory);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Utils.enableGesture(getActivity(), preference.getKey(), (Boolean) newValue);
        Utils.checkDozeService(getActivity());
        
        if (Utils.GESTURE_RAISE_TO_WAKE_KEY.equals(preference.getKey())) {
            Utils.setPickUp(findPreference(Utils.GESTURE_PICK_UP_KEY), (Boolean) newValue);
        }
        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Utils.enableDoze(getActivity(), isChecked);
        Utils.checkDozeService(getActivity());

        mSwitchBar.setChecked(isChecked);

        mPickUpPreference.setEnabled(isChecked);
        mRaiseToWakePreference.setEnabled(isChecked);
        mHandwavePreference.setEnabled(isChecked);
        mPocketPreference.setEnabled(isChecked);
    }

    private void showHelp() {
        AlertDialog helpDialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.doze_settings_help_title)
                .setMessage(R.string.doze_settings_help_text)
                .setPositiveButton(R.string.dialog_ok,
                        (dialog, which) -> {
                            getActivity()
                                    .getSharedPreferences("doze_settings", Activity.MODE_PRIVATE)
                                    .edit()
                                    .putBoolean("first_help_shown", true)
                                    .commit();
                            dialog.cancel();
                        })
                .create();
        helpDialog.show();
    }
}
