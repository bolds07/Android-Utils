package com.tomatedigital.androidutils;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.multidex.MultiDexApplication;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.google.firebase.FirebaseApp;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.crashlytics.internal.common.AppData;
import com.google.firebase.crashlytics.internal.common.CrashlyticsCore;
import com.google.firebase.crashlytics.internal.common.CrashlyticsReportDataCapture;
import com.google.firebase.crashlytics.internal.common.IdManager;
import com.google.firebase.crashlytics.internal.common.SessionReportingCoordinator;
import com.google.firebase.crashlytics.internal.log.LogFileManager;
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport;
import com.google.firebase.crashlytics.internal.stacktrace.StackTraceTrimmingStrategy;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public abstract class UtilBaseApplication extends MultiDexApplication {

    protected abstract void initializeApp();

    protected abstract void initializePrioritarySystems();

    protected abstract void firstInstallTasks();

    protected abstract boolean DEV_MODE();

    protected abstract void onAppUpdated(int previousVersion);

    protected abstract String getId();


    @Override
    public void onCreate() {

        super.onCreate();

        initializeInfraStructure();
        initializePrioritarySystems();

        loadUpdates();
        initializeApp();

        FirebaseCrashlytics.getInstance().setUserId(this.getId()); //DEVICE_ID is only set at initializeApp
        FirebaseCrashlytics.getInstance().log("androidutils baseapplication initialized");

    }

    public static String getReferrer(@NonNull final Context context) {
        return context.getSharedPreferences(Constants.DefaultSharedPreferences.PREFERENCES_FILE, MODE_PRIVATE).getString(Constants.DefaultSharedPreferences.REFERRER_ID_STRING, "not_set");
    }

    @SuppressWarnings("unused")
    @SuppressLint("HardwareIds")
    protected void detectGoogle(@NonNull GoogleDetectedCallback callback) {
        ThreadPool.run(() -> {
            if (AndroidUtils.isGoogleUsing(this)) {
                FirebaseCrashlytics.getInstance().log("google detected");
                StringBuilder sb = new StringBuilder();
                try {
                    sb.append("ip: ").append(AndroidUtils.getIpAddress()).append("\n");
                } catch (IOException ignore) {
                    sb.append("ip: null\n");
                }

                sb.append("SERIAL: ").append(Build.SERIAL).append("\n")
                        .append("MODEL: ").append(Build.MODEL).append("\n")
                        .append("ID: ").append(Build.ID).append("\n")
                        .append("Manufacture: ").append(Build.MANUFACTURER).append("\n")
                        .append("brand: ").append(Build.BRAND).append("\n")
                        .append("type: ").append(Build.TYPE).append("\n")
                        .append("user: ").append(Build.USER).append("\n")
                        .append("BASE: ").append(Build.VERSION_CODES.BASE).append("\n")
                        .append("INCREMENTAL ").append(Build.VERSION.INCREMENTAL).append("\n")
                        .append("SDK  ").append(Build.VERSION.SDK_INT).append("\n")
                        .append("BOARD: ").append(Build.BOARD).append("\n")
                        .append("BRAND ").append(Build.BRAND).append("\n")
                        .append("HOST ").append(Build.HOST).append("\n")
                        .append("FINGERPRINT: ").append(Build.FINGERPRINT).append("\n")
                        .append("Version Code: ").append(Build.VERSION.RELEASE).append("\n")
                        .append("installer: ").append(getPackageManager().getInstallerPackageName(getPackageName()));

                for (Account acc : ((AccountManager) getSystemService(ACCOUNT_SERVICE)).getAccounts())
                    sb.append("\naccount: ").append(acc.name.toLowerCase());

                sb.append("\nuptime: ").append(SystemClock.elapsedRealtime());
                FirebaseCrashlytics.getInstance().log(sb.toString());


                callback.googleDetected(sb.toString());

            }
        });
    }

    private void loadUpdates() {
        SharedPreferences sp = this.getSharedPreferences(Constants.DefaultSharedPreferences.PREFERENCES_FILE, MODE_PRIVATE);

        int previousVersion = sp.getInt(Constants.DefaultSharedPreferences.LAST_USED_VERSION_INT, -1);
        int version = 0;

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        if (previousVersion < version || DEV_MODE()) {
            SharedPreferences.Editor edit = sp.edit();

            //if first install
            if (previousVersion == -1 && PreferenceManager.getDefaultSharedPreferences(this).getInt("pluv", -2) == -2) {
                saveReferralInfo();
                firstInstallTasks();
            } else
                onAppUpdated(previousVersion);

            edit.putInt(Constants.DefaultSharedPreferences.LAST_USED_VERSION_INT, version).apply();

        }
    }


    private void initializeInfraStructure() {

        if (BuildConfig.DEBUG)
            fixCrashlytics();


        FirebaseApp.initializeApp(this);


        FirebaseCrashlytics.getInstance().log("android utils initilized infra structure");
    }


    /**
     * todo lixo crashlytics
     */
    private void fixCrashlytics() {
        try {
            Field f = FirebaseCrashlytics.class.getDeclaredField("core");
            f.setAccessible(true);


            CrashlyticsCore core = (CrashlyticsCore) f.get(FirebaseCrashlytics.getInstance());
            f = CrashlyticsCore.class.getDeclaredField("controller");
            f.setAccessible(true);

            Object controler = f.get(core);
            //noinspection ConstantConditions
            f = controler.getClass().getDeclaredField("logFileManager");
            f.setAccessible(true);


            f.set(controler, new LogFileManager(null, null) {
                @Override
                public void writeToLog(long timestamp, String msg) {
                    super.writeToLog(timestamp, msg);
                    System.out.println(msg);
                }
            });

            f = controler.getClass().getDeclaredField("reportingCoordinator");
            f.setAccessible(true);

            SessionReportingCoordinator sessionReportingCoordinator = (com.google.firebase.crashlytics.internal.common.SessionReportingCoordinator) f.get(controler);

            f = com.google.firebase.crashlytics.internal.common.SessionReportingCoordinator.class.getDeclaredField("dataCapture");
            f.setAccessible(true);
            CrashlyticsReportDataCapture original = (CrashlyticsReportDataCapture) f.get(sessionReportingCoordinator);

            f = CrashlyticsReportDataCapture.class.getDeclaredField("context");
            f.setAccessible(true);
            final Context context = (Context) f.get(original);

            f = CrashlyticsReportDataCapture.class.getDeclaredField("idManager");
            f.setAccessible(true);
            final IdManager idManager = (IdManager) f.get(original);

            f = CrashlyticsReportDataCapture.class.getDeclaredField("appData");
            f.setAccessible(true);
            final AppData appData = (AppData) f.get(original);

            f = CrashlyticsReportDataCapture.class.getDeclaredField("stackTraceTrimmingStrategy");
            f.setAccessible(true);
            final StackTraceTrimmingStrategy stackTraceTrimmingStrategy = (StackTraceTrimmingStrategy) f.get(original);

            CrashlyticsReportDataCapture enhanced = new CrashlyticsReportDataCapture(context, idManager, appData, stackTraceTrimmingStrategy) {
                @Override
                public CrashlyticsReport.Session.Event captureEventData(Throwable event, Thread eventThread, String type, long timestamp, int eventThreadImportance, int maxChainedExceptions, boolean includeAllThreads) {
                    if (event instanceof Exception)
                        event.printStackTrace();

                    return super.captureEventData(event, eventThread, type, timestamp, eventThreadImportance, maxChainedExceptions, includeAllThreads);
                }
            };
            f = SessionReportingCoordinator.class.getDeclaredField("dataCapture");
            f.setAccessible(true);

            f.set(sessionReportingCoordinator, enhanced);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void saveReferralInfo() {

        final InstallReferrerClient mReferrerClient = InstallReferrerClient.newBuilder(this).build();
        final SharedPreferences.Editor edit = getSharedPreferences(Constants.DefaultSharedPreferences.PREFERENCES_FILE, MODE_PRIVATE).edit();

        try {
            mReferrerClient.startConnection(new InstallReferrerStateListener() {
                public void onInstallReferrerSetupFinished(int responseCode) {

                    if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                        String ref;
                        String result = null;
                        try {
                            ReferrerDetails response = mReferrerClient.getInstallReferrer();
                            ref = URLDecoder.decode(response.getInstallReferrer(), "UTF-8").toLowerCase().trim();

                            if (ref.isEmpty())
                                result = "organic";

                            else {
                                String[] params = ref.split("&");
                                Map<String, String> mapParams = new HashMap<>(params.length);
                                for (String p : params) {
                                    if (p.contains("=")) {
                                        String name = p.substring(0, p.indexOf("="));
                                        String value = p.substring(name.length() + 1);
                                        mapParams.put(name, value);
                                    } else
                                        mapParams.put(p, p);
                                }
                                String[] keys = {"ref_id", "organic", "utm_campaign", "utm_source"};

                                for (String key : keys) {
                                    String v = mapParams.get(key);
                                    if (v != null) {
                                        result = v;
                                        break;
                                    }
                                }

                                if (result == null || result.isEmpty()) {
                                    ref = ref.replaceAll("[^0-9]", "");
                                    if (ref.isEmpty())
                                        result = "organic";
                                    else
                                        result = ref;
                                }
                            }
                            edit.putString(Constants.DefaultSharedPreferences.REFERRER_ID_STRING, result);


                        } catch (Exception e) {
                            //this exception happens when someone install app direct from google play (so the 'organic' will be set, but also when i do install the app from android studio or maybe some other apk ways to download the app... nothing to worry about)
                            edit.putString(Constants.DefaultSharedPreferences.REFERRER_ID_STRING, "not_set");
                        }
                        mReferrerClient.endConnection();
                    } else if (responseCode == InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED)
                        edit.putString(Constants.DefaultSharedPreferences.REFERRER_ID_STRING, "not_supported");
                    else
                        edit.putString(Constants.DefaultSharedPreferences.REFERRER_ID_STRING, "other");


                    edit.apply();
                }

                @Override
                public void onInstallReferrerServiceDisconnected() {
                }
            });
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            edit.putString(Constants.DefaultSharedPreferences.REFERRER_ID_STRING, "error_" + e.getClass().getSimpleName()).apply();
        }
    }

    public interface GoogleDetectedCallback {
        @WorkerThread
        void googleDetected(String toString);
    }
}

