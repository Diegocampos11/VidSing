package com.example.alumno.inspect_mimetype;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
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
        findPreference(prefFontSize).setSummary( "Tamaño de letra en TV. Valor actual: " + ( (EditTextPreference) findPreference(prefFontSize) ).getText() + "px" );
        findPreference(prefFontSize).setOnPreferenceChangeListener( this );
        findPreference( prefInicio).setSummary( "Pagina de inicio. Valor actual: " + ( (EditTextPreference) findPreference(prefInicio) ).getText() );
        findPreference( prefInicio).setOnPreferenceChangeListener( this );
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if ( preference.getKey().equals(prefFontSize) ) preference.setSummary( "Tamaño de letra actualizada! " + newValue + "px" );
        else if ( preference.getKey().equals( prefInicio) ) preference.setSummary( "Pagina actualizada! " + newValue );
        return true;
    }
}
