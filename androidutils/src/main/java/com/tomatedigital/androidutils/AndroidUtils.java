package com.tomatedigital.androidutils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.PowerManager;
import android.os.Process;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.ACCOUNT_SERVICE;
import static android.content.Context.POWER_SERVICE;

public class AndroidUtils {


    private static OkHttpClient okHttpClient;
    public static final String REGEX_EMAIL = "[a-z0-9._-]+@[a-z0-9_-]+(\\.[a-z]+)(\\.[a-z]+)?";
    public static final String REGEX_LETTER_NUMBER_ONLY = "[^a-zA-Z0-9 ]";


    private static final Set<String> PIRAT_APPS = new HashSet<>(Arrays.asList("com.chelpus.lackypatch", "com.dimonvideo.luckypatcher", "com.forpda.lp", "com.android.vending.billing.InAppBillingService", "com.android.vending.billing.InAppBillingSorvice", "com.android.vendinc", "uret.jasi2169.patcher", "zone.jasi2169.uretpatcher", "p.jasi2169.al3", "cc.madkite.freedom", "cc.cz.madkite.freedom", "org.creeplays.hack", /*"com.happymod.apk",*/ "org.sbtools.gamehack", "com.zune.gamekiller", "com.aag.killer", "com.killerapp.gamekiller", "cn.lm.sq", "net.schwarzis.game_cih", "com.baseappfull.fwd", "com.github.oneminusone.disablecontentguard", "com.oneminusone.disablecontentguard"));


    public static boolean isValidEmail(@Nullable String email) {

        return email != null && !email.contains("not-applicable") && !email.contains("blackhole-") && !email.trim().equals("") && !email.contains("@devnull") && email.trim().toLowerCase().matches(REGEX_EMAIL);

    }

    private static String piratAppInstalled(@NonNull final Context c) {
        PackageManager pm = c.getPackageManager();
        for (ApplicationInfo ai : pm.getInstalledApplications(PackageManager.GET_META_DATA))
            if (PIRAT_APPS.contains(ai.packageName))
                return ai.packageName;
        for (String p : PIRAT_APPS) {
            Intent i = pm.getLaunchIntentForPackage(p);
            if (i != null && !pm.queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY).isEmpty())
                return p;
        }


        return null;
    }

    public static boolean hasPiratAppInstalled(@NonNull final Context c) {
        String pirat = piratAppInstalled(c);
        if (pirat != null) {
            Bundle b = new Bundle();
            b.putString("app", pirat);
            FirebaseAnalytics.getInstance(c).logEvent("pirat_app_installed", b);
        }


        return pirat != null;
    }


    public static boolean isAppOnForeground(@NonNull final Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null)
            return false;

        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses)
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName))
                return true;


        return false;
    }

    @MainThread
    public static void copyToClipboard(@NonNull final Context c, @NonNull final String text, @NonNull final String label) {
        ClipboardManager clipboard = (ClipboardManager) c.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        //noinspection ConstantConditions
        clipboard.setPrimaryClip(clip);
    }

    public static boolean canTouch(@Nullable final OkHttpClient client, @NonNull final String url) {

        if (okHttpClient == null || client != null)
            okHttpClient = client;

        if (okHttpClient != null) {
            try {
                Response resp = okHttpClient.newCall(new Request.Builder().get().url(url).build()).execute();

                if (resp.body() != null)
                    resp.close();

                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }


    @SuppressWarnings("ConstantConditions")
    public static PowerManager.WakeLock acquireWakeLock(@NonNull final Context c, final long timeout, @NonNull final String tag) {


        PowerManager powerManager = (PowerManager) c.getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, tag);

        wakeLock.acquire(timeout);

        return wakeLock;

    }


    public static boolean isBeingCloned(@NonNull Context c) {
        ActivityManager am = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
        int myPid = Process.myPid();
        int myUid = Process.myUid();
        //noinspection ConstantConditions
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

    @SuppressLint("HardwareIds")
    public static boolean isEmulator(@NonNull final Context c) {


        Field[] tmp = Sensor.class.getDeclaredFields();
        Set<Field> fields = new HashSet<>();
        for (Field f : tmp)
            if (!Modifier.isStatic(f.getModifiers()) && f.getType() == String.class) {
                f.setAccessible(true);
                fields.add(f);
            }

        for (Sensor s : ((SensorManager) c.getSystemService(Context.SENSOR_SERVICE)).getSensorList(Sensor.TYPE_ALL)) {
            for (Field f : fields) {
                try {
                    String vendor = (String) f.get(s);
                    if (vendor != null) {
                        vendor = vendor.toLowerCase();
                        if (vendor.contains("bluestacks") || vendor.contains("tiantianvm") || vendor.contains("genymotion") || vendor.contains("goldfish") || vendor.contains("ttvm_hdragon") || vendor.contains("vbox"))
                            return true;
                    }
                } catch (IllegalAccessException ignore) {

                }
            }
        }


        final String manufacturer = Build.MANUFACTURER.toLowerCase();
        final String model = Build.MODEL.toLowerCase();
        final String hardware = Build.HARDWARE.toLowerCase();
        final String product = Build.PRODUCT.toLowerCase();
        final String brand = Build.BRAND.toLowerCase();
        final String fingerprint = Build.FINGERPRINT.toLowerCase();
        return
                model.contains("google_sdk")
                        || model.contains("droid4x")
                        || model.contains("emulator")
                        || model.contains("android sdk built for x86")
                        || model.equals("sdk")
                        || model.contains("tiantianvm")
                        || model.contains("andy")
                        || model.contains(" android sdk built for x86_64")
                        || manufacturer.contains("unknown")
                        || manufacturer.contains("genymotion")
                        || manufacturer.contains("andy")
                        || manufacturer.contains("mit")
                        || manufacturer.contains("tiantianvm")
                        || hardware.contains("goldfish")
                        || hardware.contains("vbox86")
                        || hardware.contains("nox")
                        || hardware.contains("ttvm_x86")
                        || product.contains("sdk")
                        || product.contains("andy")
                        || product.contains("ttvm_hdragon")
                        || product.contains("droid4x")
                        || product.contains("nox")
                        || product.contains("vbox86p")

                        || Build.BOARD.toLowerCase().contains("nox")
                        || Build.BOOTLOADER.toLowerCase().contains("nox")


                        || Build.SERIAL.toLowerCase().contains("nox")
                        || brand.contains("unknown")
                        || brand.contains("genymotion")
                        || brand.contains("andy")
                        || brand.contains("nox")
                        || brand.contains("mit")
                        || brand.contains("tiantianvm")
                        || Build.FINGERPRINT.startsWith("unknown")
                        || fingerprint.contains("generic")
                        || fingerprint.contains("andy")
                        || fingerprint.contains("ttvm_hdragon")
                        || fingerprint.contains("vbox86p");
    }

    public static boolean isOfficialInstaller(@NonNull final Context context) {
        // A list with valid installers package name
        Set<String> validInstallers = new HashSet<>(Arrays.asList("com.amazon.venezia", "com.sec.android.app.samsungapps", "com.huawei.appmarket", "com.android.vending", "com.google.android.feedback"));

        // The package name of the app that has installed your app
        final String installer = context.getPackageManager().getInstallerPackageName(context.getPackageName());

        if (!validInstallers.contains(installer) && !BuildConfig.DEBUG) {
            Bundle b = new Bundle();
            b.putString("installer", installer == null ? "null" : installer);
            FirebaseAnalytics.getInstance(context).logEvent("pirat_install", b);
        }


        // true if your app has been downloaded from Play Store
        return validInstallers.contains(installer);
    }

    public static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public static void openBrowser(@NonNull String page, @NonNull Context activity) {
        if (!page.startsWith("http"))
            page = "http://" + page;
        Intent openBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(page));

        openBrowser.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            openBrowser.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);

        activity.startActivity(openBrowser);

    }


    public static String readLastSms(ContentResolver res, long minTs) {

        Cursor cursor = res.query(
                Uri.parse("content://sms/inbox"),
                new String[]{"date", "body"},
                null,
                null,
                "date DESC");
        long date = 0;
        String body = null;
        if (cursor != null && cursor.moveToFirst()) { // must check the result to prevent exception

            for (int i = 0; i < cursor.getColumnCount(); i++) {
                if (cursor.getColumnName(i).equals("date"))
                    date = Long.parseLong(cursor.getString(i));
                else if (cursor.getColumnName(i).equals("body"))
                    body = cursor.getString(i);
            }
            //if WASNT received the after minTs
            if (date < minTs)
                body = null;

        }

        if (cursor != null)
            cursor.close();

        return body;
    }


    @SuppressLint("PackageManagerGetSignatures")
    public static long getCertificateValue(Context ctx) {
        try {
            Signature[] signatures = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                try {
                    signatures = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), PackageManager.GET_SIGNING_CERTIFICATES).signingInfo.getApkContentsSigners();
                } catch (Throwable ignored) {
                }
            }
            if (signatures == null) {
                signatures = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), PackageManager.GET_SIGNATURES).signatures;
            }
            long value = 1;

            for (Signature signature : signatures) {
                value *= signature.hashCode();
            }
            return value;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }


    @WorkerThread
    public static String getIpAddress() throws IOException {
        StringBuilder ip = new StringBuilder();

        HttpsURLConnection conn = (HttpsURLConnection) new URL("https://api64.ipify.org").openConnection();
        conn.getResponseCode();
        byte[] buffer = new byte[256];
        int lenght;
        while ((lenght = conn.getInputStream().read(buffer)) > 0)
            ip.append(new String(buffer, 0, lenght));

        return ip.toString();
    }

    @SuppressLint("WifiManagerPotentialLeak")
    @WorkerThread
    public static boolean isGoogleUsing(@NonNull final Context c) {


        for (Account acc : ((AccountManager) c.getSystemService(ACCOUNT_SERVICE)).getAccounts()) {
            if (acc.name.toLowerCase().endsWith("@cloudtestlabaccounts.com"))
                return true;
        }

        if (((WifiManager) c.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getSSID().startsWith("\"wl-ftl-mt"))
            return true;

        try {
            String ip = getIpAddress();
            if (ip.startsWith("108.177.1.") || ip.startsWith("108.177.2.") || ip.startsWith("108.177.3.") || ip.startsWith("108.177.4.") || ip.startsWith("108.177.5.") || ip.startsWith("108.177.6.") || ip.startsWith("108.177.7.") || ip.startsWith("108.177.8.") || ip.startsWith("108.177.9.") || ip.startsWith("108.177.10."))
                return true;
        } catch (IOException ignore) {
        }

        //google always connects to wifi
        ConnectivityManager connection = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        int i = 0;
        for (NetworkInfo net : connection.getAllNetworkInfo()) {
            if (net.getState() == NetworkInfo.State.CONNECTED && net.getType() != ConnectivityManager.TYPE_WIFI)
                return false;
        }


        return false;
    }

    private static String intToIp(int i) {

        return ((i >> 24) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                (i & 0xFF);
    }

}
