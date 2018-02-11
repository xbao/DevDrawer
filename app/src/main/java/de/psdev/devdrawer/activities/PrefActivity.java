package de.psdev.devdrawer.activities;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;
import de.psdev.devdrawer.R;
import de.psdev.devdrawer.appwidget.DDWidgetProvider;
import de.psdev.devdrawer.utils.Constants;

public class PrefActivity extends PreferenceActivity {
    SharedPreferences sp;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        sp = getPreferenceManager().getSharedPreferences();

        final ListPreference activityChoicePref = (ListPreference) findPreference("widgetSorting");
        final ListPreference themePref = (ListPreference) findPreference("theme");
        final ListPreference intentsPref = (ListPreference) findPreference("launchingIntents");

        activityChoicePref.setSummary(nameFromValue(sp.getString(Constants.PREF_SORT_ORDER, Constants.ORDER_INSTALLED), activityChoicePref));
        themePref.setSummary(sp.getString("theme", "Light"));
        intentsPref.setSummary(intentNameFromValue(sp.getString("launchingIntents", "aosp"), intentsPref));

        activityChoicePref.setOnPreferenceChangeListener((preference, newValue) -> {
            final SharedPreferences.Editor editor = sp.edit();
            editor.putString(preference.getKey(), newValue.toString());
            editor.apply();

            preference.setSummary(nameFromValue(newValue.toString(), preference));

            final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
            final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(getApplicationContext(), DDWidgetProvider.class));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listView);

            return false;
        });

        themePref.setOnPreferenceChangeListener((preference, newValue) -> {
            final SharedPreferences.Editor editor = sp.edit();
            editor.putString(preference.getKey(), newValue.toString());
            editor.apply();

            preference.setSummary(newValue.toString());

            Toast.makeText(this, "You may need to re-add the widget for this change to take effect", Toast.LENGTH_SHORT).show();

            return false;
        });

        intentsPref.setOnPreferenceChangeListener((preference, newValue) -> {
            final SharedPreferences.Editor editor = sp.edit();
            editor.putString(preference.getKey(), newValue.toString());
            editor.commit();

            preference.setSummary(intentNameFromValue(newValue.toString(), preference));

            return false;
        });
    }

    String nameFromValue(final String value, final Preference preference) {
        String ofTheSpaceCowboy = "";

        final String[] values = getResources().getStringArray(R.array.sorting_options_values);
        final String[] names = getResources().getStringArray(R.array.sorting_options);

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
