package com.example.alumno.inspect_mimetype;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import java.util.Map;

/**
 * Created by darka on 31/12/2017.
 */

public class Preferences extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    public static final String prefServer = "txtServidor";
    public static final String prefInicio = "txtInicio";

    @Override
    public void onCreate( Bundle savedInstaceState ){
        super.onCreate( savedInstaceState );
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        findPreference( prefServer ).setOnPreferenceChangeListener( this );
        findPreference( prefInicio).setOnPreferenceChangeListener( this );
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        if ( preference.getKey().equals( prefServer ) ) preference.setSummary( "Servidor actualizado!" );
        else if ( preference.getKey().equals( prefInicio) ) preference.setSummary( "Pagina actualizada!" );
        return true;
    }
}
