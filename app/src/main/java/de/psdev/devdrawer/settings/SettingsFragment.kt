package de.psdev.devdrawer.settings

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.SharedPreferences
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v7.preference.ListPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.widget.Toast
import de.psdev.devdrawer.R
import de.psdev.devdrawer.appwidget.DDWidgetProvider

/**
 * Allows editing of this preference instance with a call to [apply][SharedPreferences.Editor.apply]
 * or [commit][SharedPreferences.Editor.commit] to persist the changes.
 * Default behaviour is [apply][SharedPreferences.Editor.apply].
 * ```
 * prefs.edit {
 *     putString("key", value)
 * }
 * ```
 * To [commit][SharedPreferences.Editor.commit] changes:
 * ```
 * prefs.edit(commit = true) {
 *     putString("key", value)
 * }
 * ```
 */
@SuppressLint("ApplySharedPref")
inline fun SharedPreferences.edit(
        commit: Boolean = false,
        action: SharedPreferences.Editor.() -> Unit
) {
    val editor = edit()
    action(editor)
    if (commit) {
        editor.commit()
    } else {
        editor.apply()
    }
}

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)

        findPreference<ListPreference>(R.string.pref_launch_intents).apply {
            setOnPreferenceChangeListener { preference, newValue ->
                sharedPreferences.edit {
                    putString(preference.key, newValue.toString())
                }

                preference.summary = launchIntentLabelFromValue(newValue.toString())

                return@setOnPreferenceChangeListener true
            }
        }

        findPreference<ListPreference>(R.string.pref_sort_order).apply {
            summary = sortOrderLabelFromValue(sharedPreferences.getString(getString(R.string.pref_sort_order), getString(R.string.pref_sort_order_default))!!)
            setOnPreferenceChangeListener { preference, newValue ->
                sharedPreferences.edit {
                    putString(preference.key, newValue.toString())
                }

                preference.summary = sortOrderLabelFromValue(newValue.toString())

                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, DDWidgetProvider::class.java))
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listView)

                return@setOnPreferenceChangeListener true
            }
        }

        findPreference<ListPreference>(R.string.pref_theme).apply {
            summary = sharedPreferences.getString("theme", "Light")
            setOnPreferenceChangeListener { preference, newValue ->
                sharedPreferences.edit {
                    putString(preference.key, newValue.toString())
                }

                preference.summary = newValue.toString()

                Toast.makeText(activity, "You may need to re-add the widget for this change to take effect", Toast.LENGTH_SHORT).show()

                return@setOnPreferenceChangeListener true
            }
        }
    }

    // ==========================================================================================================================
    // Private API
    // ==========================================================================================================================

    private inline fun <reified T: Preference> findPreference(@StringRes keyRes: Int): T = findPreference(getString(keyRes)) as T

    private fun sortOrderLabelFromValue(value: String): String {
        val resources = resources
        val values = resources.getStringArray(R.array.sort_order_values)
        val names = resources.getStringArray(R.array.sort_order_labels)
        return names[values.indexOfFirst { it == value }]
    }

    private fun launchIntentLabelFromValue(value: String): String {
        val resources = resources
        val values = resources.getStringArray(R.array.launch_intent_values)
        val names = resources.getStringArray(R.array.launch_intent_labels)
        return names[values.indexOfFirst { it == value }]
    }
}