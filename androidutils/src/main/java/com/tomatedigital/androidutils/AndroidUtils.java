package com.tomatedigital.androidutils;

import static android.content.Context.ACCOUNT_SERVICE;
import static android.content.Context.POWER_SERVICE;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.DisplayMetrics;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

public class AndroidUtils {

    public static final String REGEX_LETTER_NUMBER_ONLY = "[^a-zA-Z0-9 ]";
    private static AlertDialog permissionDialog;

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(final float dp, @NonNull final Context context) {
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px      A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(final float px, @NonNull final Context context) {
        return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
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

        clipboard.setPrimaryClip(clip);
    }

    public static boolean isNetworkOn(@NonNull Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    public static PowerManager.WakeLock acquireWakeLock(@NonNull final Context c, final long timeout, @NonNull final String tag) {


        PowerManager powerManager = (PowerManager) c.getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, tag);

        wakeLock.acquire(timeout);

        return wakeLock;

    }


    public static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public static void openBrowser(@NonNull String page, @NonNull Context activity) {

        Intent openBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(page));

        openBrowser.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        openBrowser.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);

        if (openBrowser.resolveActivity(activity.getPackageManager()) != null)
            activity.startActivity(openBrowser);
        else
            new AlertDialog.Builder(activity).setTitle(R.string.browser_not_found).setMessage(R.string.browser_not_found_message).setPositiveButton(R.string.close, null).show();

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
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        byte[] buffer = new byte[256];
        int lenght;
        while ((lenght = conn.getInputStream().read(buffer)) > 0)
            ip.append(new String(buffer, 0, lenght));

        return ip.toString();
    }

    @SuppressLint("WifiManagerPotentialLeak")
    @WorkerThread
    public static boolean isGoogleUsing(@NonNull final Context c) {


        if ("true".equals(Settings.System.getString(c.getContentResolver(), "firebase.test.lab")))
            return true;

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
