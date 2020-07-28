package com.tomatedigital.androidutils;

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
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.os.PowerManager;
import android.os.Process;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;

import static android.content.Context.POWER_SERVICE;

public class AndroidUtils {


    private static OkHttpClient okHttpClient;
    public static final String REGEX_EMAIL = "[a-z0-9._-]+@[a-z0-9_-]+(\\.[a-z]+)(\\.[a-z]+)?";
    public static final String REGEX_LETTER_NUMBER_ONLY = "[^a-zA-Z0-9 ]";
    public static final String REGEX_NUMBER_ONLY = "[^0-9 ]";


    protected static final Map<String, String> COUNTRY_CODES = new HashMap<>();

    static {
        COUNTRY_CODES.put("AF", "93");
        COUNTRY_CODES.put("AL", "355");
        COUNTRY_CODES.put("DZ", "213");
        COUNTRY_CODES.put("AS", "1");
        COUNTRY_CODES.put("AD", "376");
        COUNTRY_CODES.put("AO", "244");
        COUNTRY_CODES.put("AI", "1");
        COUNTRY_CODES.put("AQ", "672");
        COUNTRY_CODES.put("AR", "54");
        COUNTRY_CODES.put("AM", "374");
        COUNTRY_CODES.put("AW", "297");
        COUNTRY_CODES.put("AU", "61");
        COUNTRY_CODES.put("AT", "43");
        COUNTRY_CODES.put("AZ", "994");
        COUNTRY_CODES.put("BS", "1");
        COUNTRY_CODES.put("BH", "973");
        COUNTRY_CODES.put("BD", "880");
        COUNTRY_CODES.put("BB", "1");
        COUNTRY_CODES.put("BY", "375");
        COUNTRY_CODES.put("BE", "32");
        COUNTRY_CODES.put("BZ", "501");
        COUNTRY_CODES.put("BJ", "229");
        COUNTRY_CODES.put("BM", "1");
        COUNTRY_CODES.put("BT", "975");
        COUNTRY_CODES.put("BO", "591");
        COUNTRY_CODES.put("BA", "387");
        COUNTRY_CODES.put("BW", "267");
        COUNTRY_CODES.put("BR", "55");
        COUNTRY_CODES.put("VG", "1");
        COUNTRY_CODES.put("BN", "673");
        COUNTRY_CODES.put("BG", "359");
        COUNTRY_CODES.put("BF", "226");
        COUNTRY_CODES.put("BI", "257");
        COUNTRY_CODES.put("KH", "855");
        COUNTRY_CODES.put("CM", "237");
        COUNTRY_CODES.put("CA", "1");
        COUNTRY_CODES.put("CV", "238");
        COUNTRY_CODES.put("KY", "1");
        COUNTRY_CODES.put("CF", "236");
        COUNTRY_CODES.put("CL", "56");
        COUNTRY_CODES.put("CN", "86");
        COUNTRY_CODES.put("CO", "57");
        COUNTRY_CODES.put("KM", "269");
        COUNTRY_CODES.put("CK", "682");
        COUNTRY_CODES.put("CR", "506");
        COUNTRY_CODES.put("HR", "385");
        COUNTRY_CODES.put("CU", "53");
        COUNTRY_CODES.put("CW", "599");
        COUNTRY_CODES.put("CY", "357");
        COUNTRY_CODES.put("CZ", "420");
        COUNTRY_CODES.put("CD", "243");
        COUNTRY_CODES.put("DK", "45");
        COUNTRY_CODES.put("DJ", "253");
        COUNTRY_CODES.put("DM", "1");
        COUNTRY_CODES.put("DO", "1");
        COUNTRY_CODES.put("TL", "670");
        COUNTRY_CODES.put("EC", "593");
        COUNTRY_CODES.put("EG", "20");
        COUNTRY_CODES.put("SV", "503");
        COUNTRY_CODES.put("GQ", "240");
        COUNTRY_CODES.put("ER", "291");
        COUNTRY_CODES.put("EE", "372");
        COUNTRY_CODES.put("ET", "251");
        COUNTRY_CODES.put("FK", "500");
        COUNTRY_CODES.put("FO", "298");
        COUNTRY_CODES.put("FJ", "679");
        COUNTRY_CODES.put("FI", "358");
        COUNTRY_CODES.put("FR", "33");
        COUNTRY_CODES.put("PF", "689");
        COUNTRY_CODES.put("GA", "241");
        COUNTRY_CODES.put("GM", "220");
        COUNTRY_CODES.put("GE", "995");
        COUNTRY_CODES.put("DE", "49");
        COUNTRY_CODES.put("GH", "233");
        COUNTRY_CODES.put("GI", "350");
        COUNTRY_CODES.put("GR", "30");
        COUNTRY_CODES.put("GL", "299");
        COUNTRY_CODES.put("GP", "590");
        COUNTRY_CODES.put("GU", "1");
        COUNTRY_CODES.put("GT", "502");
        COUNTRY_CODES.put("GN", "224");
        COUNTRY_CODES.put("GW", "245");
        COUNTRY_CODES.put("GY", "592");
        COUNTRY_CODES.put("HT", "509");
        COUNTRY_CODES.put("HN", "504");
        COUNTRY_CODES.put("HK", "852");
        COUNTRY_CODES.put("HU", "36");
        COUNTRY_CODES.put("IS", "354");
        COUNTRY_CODES.put("IN", "91");
        COUNTRY_CODES.put("ID", "62");
        COUNTRY_CODES.put("IR", "98");
        COUNTRY_CODES.put("IQ", "964");
        COUNTRY_CODES.put("IE", "353");
        COUNTRY_CODES.put("IM", "44");
        COUNTRY_CODES.put("IL", "972");
        COUNTRY_CODES.put("IT", "39");
        COUNTRY_CODES.put("CI", "225");
        COUNTRY_CODES.put("JM", "1");
        COUNTRY_CODES.put("JP", "81");
        COUNTRY_CODES.put("JO", "962");
        COUNTRY_CODES.put("KZ", "7");
        COUNTRY_CODES.put("KE", "254");
        COUNTRY_CODES.put("KI", "686");
        COUNTRY_CODES.put("XK", "381");
        COUNTRY_CODES.put("KW", "965");
        COUNTRY_CODES.put("KG", "996");
        COUNTRY_CODES.put("LA", "856");
        COUNTRY_CODES.put("LV", "371");
        COUNTRY_CODES.put("LB", "961");
        COUNTRY_CODES.put("LS", "266");
        COUNTRY_CODES.put("LR", "231");
        COUNTRY_CODES.put("LY", "218");
        COUNTRY_CODES.put("LI", "423");
        COUNTRY_CODES.put("LT", "370");
        COUNTRY_CODES.put("LU", "352");
        COUNTRY_CODES.put("MO", "853");
        COUNTRY_CODES.put("MK", "389");
        COUNTRY_CODES.put("MG", "261");
        COUNTRY_CODES.put("MW", "265");
        COUNTRY_CODES.put("MY", "60");
        COUNTRY_CODES.put("MV", "960");
        COUNTRY_CODES.put("ML", "223");
        COUNTRY_CODES.put("MT", "356");
        COUNTRY_CODES.put("MH", "692");
        COUNTRY_CODES.put("MR", "222");
        COUNTRY_CODES.put("MU", "230");
        COUNTRY_CODES.put("MX", "52");
        COUNTRY_CODES.put("FM", "691");
        COUNTRY_CODES.put("MD", "373");
        COUNTRY_CODES.put("MC", "377");
        COUNTRY_CODES.put("MN", "976");
        COUNTRY_CODES.put("ME", "382");
        COUNTRY_CODES.put("MS", "1");
        COUNTRY_CODES.put("MA", "212");
        COUNTRY_CODES.put("MZ", "258");
        COUNTRY_CODES.put("MM", "95");
        COUNTRY_CODES.put("NA", "264");
        COUNTRY_CODES.put("NR", "674");
        COUNTRY_CODES.put("NP", "977");
        COUNTRY_CODES.put("NL", "31");
        COUNTRY_CODES.put("NC", "687");
        COUNTRY_CODES.put("NZ", "64");
        COUNTRY_CODES.put("NI", "505");
        COUNTRY_CODES.put("NE", "227");
        COUNTRY_CODES.put("NG", "234");
        COUNTRY_CODES.put("NU", "683");
        COUNTRY_CODES.put("NF", "672");
        COUNTRY_CODES.put("KP", "850");
        COUNTRY_CODES.put("MP", "1");
        COUNTRY_CODES.put("NO", "47");
        COUNTRY_CODES.put("OM", "968");
        COUNTRY_CODES.put("PK", "92");
        COUNTRY_CODES.put("PW", "680");
        COUNTRY_CODES.put("PA", "507");
        COUNTRY_CODES.put("PG", "675");
        COUNTRY_CODES.put("PY", "595");
        COUNTRY_CODES.put("PE", "51");
        COUNTRY_CODES.put("PH", "63");
        COUNTRY_CODES.put("PN", "870");
        COUNTRY_CODES.put("PL", "48");
        COUNTRY_CODES.put("PT", "351");
        COUNTRY_CODES.put("PR", "1");
        COUNTRY_CODES.put("QA", "974");
        COUNTRY_CODES.put("CG", "242");
        COUNTRY_CODES.put("RE", "262");
        COUNTRY_CODES.put("RO", "40");
        COUNTRY_CODES.put("RU", "7");
        COUNTRY_CODES.put("RW", "250");
        COUNTRY_CODES.put("BL", "590");
        COUNTRY_CODES.put("SH", "290");
        COUNTRY_CODES.put("KN", "1");
        COUNTRY_CODES.put("LC", "1");
        COUNTRY_CODES.put("MF", "1");
        COUNTRY_CODES.put("PM", "508");
        COUNTRY_CODES.put("VC", "1");
        COUNTRY_CODES.put("WS", "685");
        COUNTRY_CODES.put("SM", "378");
        COUNTRY_CODES.put("ST", "239");
        COUNTRY_CODES.put("SA", "966");
        COUNTRY_CODES.put("SN", "221");
        COUNTRY_CODES.put("RS", "381");
        COUNTRY_CODES.put("SC", "248");
        COUNTRY_CODES.put("SL", "232");
        COUNTRY_CODES.put("SG", "65");
        COUNTRY_CODES.put("SK", "421");
        COUNTRY_CODES.put("SI", "386");
        COUNTRY_CODES.put("SB", "677");
        COUNTRY_CODES.put("SO", "252");
        COUNTRY_CODES.put("ZA", "27");
        COUNTRY_CODES.put("KR", "82");
        COUNTRY_CODES.put("SS", "211");
        COUNTRY_CODES.put("ES", "34");
        COUNTRY_CODES.put("LK", "94");
        COUNTRY_CODES.put("SD", "249");
        COUNTRY_CODES.put("SR", "597");
        COUNTRY_CODES.put("SZ", "268");
        COUNTRY_CODES.put("SE", "46");
        COUNTRY_CODES.put("CH", "41");
        COUNTRY_CODES.put("SY", "963");
        COUNTRY_CODES.put("TW", "886");
        COUNTRY_CODES.put("TJ", "992");
        COUNTRY_CODES.put("TZ", "255");
        COUNTRY_CODES.put("TH", "66");
        COUNTRY_CODES.put("TG", "228");
        COUNTRY_CODES.put("TK", "690");
        COUNTRY_CODES.put("TT", "1");
        COUNTRY_CODES.put("TN", "216");
        COUNTRY_CODES.put("TR", "90");
        COUNTRY_CODES.put("TM", "993");
        COUNTRY_CODES.put("TV", "688");
        COUNTRY_CODES.put("UG", "256");
        COUNTRY_CODES.put("UA", "380");
        COUNTRY_CODES.put("AE", "971");
        COUNTRY_CODES.put("GB", "44");
        COUNTRY_CODES.put("US", "1");
        COUNTRY_CODES.put("UY", "598");
        COUNTRY_CODES.put("UZ", "998");
        COUNTRY_CODES.put("VU", "678");
        COUNTRY_CODES.put("VA", "39");
        COUNTRY_CODES.put("VE", "58");
        COUNTRY_CODES.put("VN", "84");
        COUNTRY_CODES.put("EH", "212");
        COUNTRY_CODES.put("YE", "967");
        COUNTRY_CODES.put("ZM", "260");
        COUNTRY_CODES.put("ZW", "263");
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
                okHttpClient.newCall(new Request.Builder().get().url(url).build()).execute();
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    @SuppressLint("WakelockTimeout")
    public static PowerManager.WakeLock acquireWakeLock(@NonNull final Context c, @NonNull final String tag) {


        PowerManager powerManager = (PowerManager) c.getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, tag);

        wakeLock.acquire();

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
    public static boolean isEmulator() {
        return !BuildConfig.DEBUG &&
                (Build.FINGERPRINT.startsWith("generic")
                        || Build.MODEL.contains("google_sdk")
                        || Build.MODEL.toLowerCase().contains("droid4x")
                        || Build.MODEL.contains("Emulator")
                        || Build.MODEL.contains("Android SDK built for x86")
                        || Build.MANUFACTURER.contains("Genymotion")
                        || Build.HARDWARE.equals("goldfish")
                        || Build.HARDWARE.equals("vbox86")
                        || Build.PRODUCT.equals("sdk")
                        || Build.PRODUCT.equals("google_sdk")
                        || Build.PRODUCT.equals("sdk_x86")
                        || Build.PRODUCT.equals("vbox86p")
                        || Build.BOARD.toLowerCase().contains("nox")
                        || Build.BOOTLOADER.toLowerCase().contains("nox")
                        || Build.HARDWARE.toLowerCase().contains("nox")
                        || Build.PRODUCT.toLowerCase().contains("nox")
                        || Build.SERIAL.toLowerCase().contains("nox")
                        || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                        || Build.FINGERPRINT.startsWith("unknown"));
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


}
