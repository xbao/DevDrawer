package de.psdev.devdrawer.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import de.psdev.devdrawer.R;
import de.psdev.devdrawer.appwidget.DDWidgetProvider;
import de.psdev.devdrawer.utils.Constants;

public class ClickHandlingActivity extends AppCompatActivity {
    SharedPreferences sp;

    @Override
    public void onCreate(final Bundle state) {
        super.onCreate(state);

        final String packageName = getIntent().getStringExtra(DDWidgetProvider.PACKAGE_STRING);
        final int launchType = getIntent().getIntExtra("launchType", 0);

        sp = PreferenceManager.getDefaultSharedPreferences(this);

        if (packageName != null && isAppInstalled(packageName)) {
            switch (launchType) {
                case Constants.LAUNCH_APP:
                    startApp(this, packageName);
                    break;
                case Constants.LAUNCH_APP_DETAILS:
                    startAppDetails(this, packageName);
                    break;
                case Constants.LAUNCH_UNINSTALL:
                    startUninstall(this, packageName);
                    break;
                case Constants.LAUNCH_MORE:
                    startMoreOverflowMenu(this, packageName);
                    break;
            }
        } else {
            Toast.makeText(getApplicationContext(), "Package is not installed", Toast.LENGTH_SHORT).show();

            final AlertDialog.Builder builder = new AlertDialog.Builder(ClickHandlingActivity.this);
            builder.setTitle(getResources().getString(R.string.uninstalled));
            builder.setMessage(getResources().getString(R.string.package_does_not_exist));
            builder.setPositiveButton(getResources().getString(R.string.remove), (dialogInterface, i) -> {
//                    new Database(getApplicationContext()).deleteAppFromDb(packageName);

                final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
                final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(getApplicationContext(), DDWidgetProvider.class));
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listView);

                finish();
            });
            builder.setCancelable(true);
            builder.setOnCancelListener(dialogInterface -> finish());

            final AlertDialog alert = builder.create();
            alert.show();
        }
    }

    // Method to check whether the Facebook App is installed
    private boolean isAppInstalled(final String uri) {
        final PackageManager pm = getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (final PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    public static void startApp(final Activity activity, final String packageName) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        if (sp.getBoolean("showActivityChoice", false)) {
            // Show the activity choice dialog
            final Intent intent = new Intent(activity, ChooseActivityDialog.class);
            if (sp.getString("launchingIntents", "aosp").equals("aosp")) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            } else {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
            }
            intent.putExtra("packageName", packageName);
            activity.startActivity(intent);
            activity.finish();

        } else {
            // Launch the app
            try {
                final Intent LaunchIntent = activity.getPackageManager()
                    .getLaunchIntentForPackage(packageName);
                LaunchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                if (sp.getString("launchingIntents", "aosp").equals("aosp")) {
                    LaunchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                } else {
                    LaunchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
                }

                activity.startActivity(LaunchIntent);
            } catch (final NullPointerException e) {
                Toast.makeText(activity, activity.getString(
                    R.string.no_main_activity_could_be_found), Toast.LENGTH_SHORT)
                    .show();
                final Intent intent = new Intent(activity, PrefActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TOP);
                activity.startActivity(intent);
            }
            activity.finish();
        }
    }

    public static void startAppDetails(final Activity activity, final String packageName) {
        // Launch the app details settings screen for the app
        final Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setData(Uri.parse("package:" + packageName));
        activity.startActivity(intent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            activity.finish();
        }
    }

    public static void startMoreOverflowMenu(final Activity activity, final String packageName) {
        try {
            final PackageManager pm = activity.getPackageManager();
            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            final Dialog dlg = builder.setTitle(pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)))
                .setItems(new CharSequence[]{ "View details" }, (dialog, which) -> {
                    if (which == 0) {
                        startAppDetails(activity, packageName);
                    }
                })
                .setOnCancelListener(dialog -> activity.finish())
                .create();
            dlg.show();
        } catch (final PackageManager.NameNotFoundException e) {
        }
    }

    public static void startUninstall(final Activity activity, final String packageName) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        try {
            final Uri packageUri = Uri.parse("package:" + packageName);
            final Intent uninstallIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
            uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(uninstallIntent);
            activity.finish();
        } catch (final ActivityNotFoundException e) {
            Toast.makeText(activity, "Application cannot be uninstalled / possibly system app", Toast.LENGTH_SHORT).show();
        }
    }

}
