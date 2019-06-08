package com.vidsing;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

/**
 * Created by darka on 31/12/2017.
 */

public class Preferences extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    public static final String prefFontSize = "txtFontSize";
    public static final String prefInicio = "txtInicio";

    @Override
    public void onCreate( Bundle savedInstaceState ){
        super.onCreate( savedInstaceState );
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        //set summary + current value
        findPreference(prefFontSize).setSummary( getString( R.string.txtfontSizePrefSum) + ( (EditTextPreference) findPreference(prefFontSize) ).getText() + "px" );
        findPreference(prefFontSize).setOnPreferenceChangeListener( this );
        findPreference( prefInicio).setSummary( getString( R.string.txtInicioSum ) + ( (EditTextPreference) findPreference(prefInicio) ).getText() );
        findPreference( prefInicio).setOnPreferenceChangeListener( this );
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if ( preference.getKey().equals( prefFontSize ) ) preference.setSummary( getString( R.string.txtFontSizeUpdated) + newValue + "px" );
        else if ( preference.getKey().equals( prefInicio) ) preference.setSummary( getString( R.string.txtInicioUpdated ) + newValue );
        return true;
    }
}
