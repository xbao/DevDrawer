package de.psdev.devdrawer.activities;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;
import de.psdev.devdrawer.R;
import de.psdev.devdrawer.appwidget.DDWidgetProvider;

public class PrefActivity extends PreferenceActivity {
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        sharedPreferences = getPreferenceManager().getSharedPreferences();

        final ListPreference sortOrderPref = (ListPreference) findPreference(getString(R.string.pref_sort_order));
        final ListPreference themePref = (ListPreference) findPreference("theme");
        final ListPreference intentsPref = (ListPreference) findPreference("launchingIntents");

        sortOrderPref.setSummary(
            nameFromValue(sharedPreferences.getString(getString(R.string.pref_sort_order), getString(R.string.pref_sort_order_default)), sortOrderPref));
        themePref.setSummary(sharedPreferences.getString("theme", "Light"));
        intentsPref.setSummary(intentNameFromValue(sharedPreferences.getString("launchingIntents", "aosp"), intentsPref));

        sortOrderPref.setOnPreferenceChangeListener((preference, newValue) -> {
            final SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(preference.getKey(), newValue.toString());
            editor.apply();

            preference.setSummary(nameFromValue(newValue.toString(), preference));

            final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
            final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(getApplicationContext(), DDWidgetProvider.class));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listView);

            return true;
        });

        themePref.setOnPreferenceChangeListener((preference, newValue) -> {
            final SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(preference.getKey(), newValue.toString());
            editor.apply();

            preference.setSummary(newValue.toString());

            Toast.makeText(this, "You may need to re-add the widget for this change to take effect", Toast.LENGTH_SHORT).show();

            return true;
        });

        intentsPref.setOnPreferenceChangeListener((preference, newValue) -> {
            final SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(preference.getKey(), newValue.toString());
            editor.apply();

            preference.setSummary(intentNameFromValue(newValue.toString(), preference));

            return true;
        });
    }

    String nameFromValue(final String value, final Preference preference) {
        String ofTheSpaceCowboy = "";

        final Resources resources = getResources();
        final String[] values = resources.getStringArray(R.array.sorting_options_values);
        final String[] names = resources.getStringArray(R.array.sorting_options);

        for (int i = 0; i < names.length; i++) {
            if (value.equals(values[i])) {
                ofTheSpaceCowboy = names[i];
            }
        }

        return ofTheSpaceCowboy;
    }

    private String intentNameFromValue(final String value, final Preference preference) {
        String ofTheSpaceCowboy = "";

        final String[] values = getResources().getStringArray(R.array.launching_intents_values);
        final String[] names = getResources().getStringArray(R.array.launching_intents);

        for (int i = 0; i < names.length; i++) {
            if (value.equals(values[i])) {
                ofTheSpaceCowboy = names[i];
            }
        }

        return ofTheSpaceCowboy;
    }
}
