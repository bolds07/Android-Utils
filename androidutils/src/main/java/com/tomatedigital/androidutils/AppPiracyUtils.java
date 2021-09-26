package com.tomatedigital.androidutils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;

import androidx.annotation.NonNull;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppPiracyUtils {

    private static final Set<String> PIRAT_APPS = new HashSet<>(Arrays.asList("com.chelpus.lackypatch", "com.dimonvideo.luckypatcher", "com.forpda.lp", "com.android.vending.billing.InAppBillingService", "com.android.vending.billing.InAppBillingSorvice", "com.android.vendinc", "uret.jasi2169.patcher", "zone.jasi2169.uretpatcher", "p.jasi2169.al3", "cc.madkite.freedom", "cc.cz.madkite.freedom", "org.creeplays.hack", /*"com.happymod.apk",*/ "org.sbtools.gamehack", "com.zune.gamekiller", "com.aag.killer", "com.killerapp.gamekiller", "cn.lm.sq", "net.schwarzis.game_cih", "com.baseappfull.fwd", "com.github.oneminusone.disablecontentguard", "com.oneminusone.disablecontentguard"));

    @NonNull
    public static Set<String> piratAppsInstalled(@NonNull final Context c) {
        Set<String> result = new HashSet<>();
        PackageManager pm = c.getPackageManager();
        for (ApplicationInfo ai : pm.getInstalledApplications(PackageManager.GET_META_DATA))
            if (PIRAT_APPS.contains(ai.packageName))
                result.add(ai.packageName);
        for (String p : PIRAT_APPS) {
            Intent i = pm.getLaunchIntentForPackage(p);
            if (i != null && !pm.queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY).isEmpty())
                result.add(p);
        }


        return result;
    }

    public static boolean hasPiratAppInstalled(@NonNull final Context c) {

        Set<String> pirats = piratAppsInstalled(c);
        for (String p : pirats) {
            Bundle b = new Bundle();
            b.putString("app", p);
            FirebaseAnalytics.getInstance(c).logEvent("pirat_app_installed", b);
        }


        return pirats.size() > 0;
    }

    public static boolean isBeingCloned(@NonNull Context c) {
        ActivityManager am = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
        int myPid = Process.myPid();
        int myUid = Process.myUid();

        List<ActivityManager.RunningAppProcessInfo> processess = am.getRunningAppProcesses();


        if (processess == null)
            return true;

        String path = c.getFilesDir().getAbsolutePath().toLowerCase();
        String[] forbiddenPaths = {"felix.jyelves", "multi", "duel", "paralelo", "whatswebnolastseen", "mobiorca.whatsaccount", "duplicator", "trendmicro.tmas", "ludashi.superboost", "jumobile.smartapp", "hide", "paralel", "parallel", "dual", "clone"};
        for (String forbid : forbiddenPaths)
            if (path.contains(forbid))
                return true;

        try {
            ApplicationInfo appinfo = c.getPackageManager().getApplicationInfo(c.getApplicationContext().getPackageName(), 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !appinfo.dataDir.replace("user_de", "user").equals(appinfo.deviceProtectedDataDir.replace("user_de", "user")))
                return true;
        } catch (PackageManager.NameNotFoundException ignore) {
        }

        for (ActivityManager.RunningAppProcessInfo info : processess)
            if (info.uid == myUid && info.pid != myPid)
                return true;


        return false;
    }

    public static boolean checkXposed(@NonNull Context c) {
        return isXposedActive() || isXposedInstallerAvailable(c);
    }


    /**
     * Check if the Xposed framework is installed and active.
     *
     * @return {@code true} if Xposed is active on the device.
     */
    private static boolean isXposedActive() {
        StackTraceElement[] stackTraces = new Throwable().getStackTrace();
        for (StackTraceElement stackTrace : stackTraces) {
            final String clazzName = stackTrace.getClassName();
            if (clazzName != null && clazzName.contains("de.robv.android.xposed.XposedBridge")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the Xposed installer is installed and enabled on the device.
     *
     * @param context The application context
     * @return {@code true} if the package "de.robv.android.xposed.installer" is installed and enabled.
     */
    private static boolean isXposedInstallerAvailable(Context context) {
        try {
            return context.getPackageManager().getApplicationInfo("de.robv.android.xposed.installer", 0).enabled;
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return false;
    }


    public static boolean isOfficialInstaller(@NonNull final Context context) {
        // A list with valid installers package name

        //com.sec.android.easyMover samsung's app to move apk to sdk card
        //com.google.android.apps.nbu.files google's app to move apk to sd card
        //com.xiaomi.mipicks store for xiaomi phones
        Set<String> validInstallers = new HashSet<>(Arrays.asList("com.xiaomi.mipicks", "com.google.android.apps.nbu.files", "com.samsung.android.scloud", "com.sec.android.easyMover", "com.amazon.venezia", "com.sec.android.app.samsungapps", "com.huawei.appmarket", "com.android.vending", "com.google.android.feedback"));

        // The package name of the app that has installed your app
        final String installer = context.getPackageManager().getInstallerPackageName(context.getPackageName());

        if (!validInstallers.contains(installer) && !BuildConfig.DEBUG) {
            Bundle b = new Bundle();
            b.putString("pirat_installer", installer == null ? "null" : installer);
            FirebaseAnalytics.getInstance(context).logEvent("pirat_install", b);
        }


        // true if your app has been downloaded from Play Store
        return validInstallers.contains(installer);
    }

}
