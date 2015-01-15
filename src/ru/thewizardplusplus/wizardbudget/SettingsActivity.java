package ru.thewizardplusplus.wizardbudget;

import android.preference.*;
import android.os.*;

public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        addPreferencesFromResource(R.xml.preferences);
    }
}
