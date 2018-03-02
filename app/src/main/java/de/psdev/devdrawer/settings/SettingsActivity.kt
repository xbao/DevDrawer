package de.psdev.devdrawer.settings

import android.os.Bundle
import de.psdev.devdrawer.BaseActivity
import de.psdev.devdrawer.R

class SettingsActivity: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    override fun onContentChanged() {
        super.onContentChanged()
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
        if (supportFragmentManager.findFragmentById(R.id.container_settings) == null) {
            supportFragmentManager.beginTransaction().replace(R.id.container_settings, SettingsFragment()).commit()
        }
    }
}
