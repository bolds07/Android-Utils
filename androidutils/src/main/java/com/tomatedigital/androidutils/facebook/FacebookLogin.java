package com.tomatedigital.androidutils.facebook;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.tomatedigital.androidutils.Constants;

import java.io.Serializable;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FacebookLogin extends AppCompatActivity {

    private static final String TOKEN = "flt";
    private static final String EXPIRES = "fle";
    private static final String USER_ID = "fluid";
    private static final String PREFERENCES_FILE = "facebooklogin.preferences";

    private static final FacebookLoginBuilder URL_BUILDER = new FacebookLoginBuilder("", "");

    private static AccessToken accessToken;
    private static SharedPreferences preferences;


    private long creationTs;
    private String resultIntentTokenKey;
    private Class<? extends Activity> resultActivity;

    final private ActivityResultLauncher<IntentSenderRequest> loginResultHandler = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
        ContextCompat.startActivity(this, new Intent(this, FacebookLogin.class).setData(Uri.parse("button://close")).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), null);
        FirebaseCrashlytics.getInstance().log("custom tab close button pressed");
    });


    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.creationTs = savedInstanceState.getLong("creationTs", 0L);
        //noinspection unchecked
        this.resultActivity = (Class<Activity>) savedInstanceState.getSerializable("resultActivity");
        this.resultIntentTokenKey = savedInstanceState.getString("resultIntentTokenKey");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putLong("creationTs", this.creationTs);
        outState.putSerializable("resultActivity", this.resultActivity);
        outState.putString("resultIntentTokenKey", this.resultIntentTokenKey);
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);


        this.restoreLoginStateFields();


        final Uri data = intent.getData();
        final Map<String, String> params = new HashMap<>();
        final FacebookLoginCallback loginCallback = new DefaultFacebookLoginCallback(this.resultActivity, this.resultIntentTokenKey);
        if (data != null) {
            String fragment = data.getEncodedFragment();
            if (fragment != null) {
                //granted_scopes=email%2Cads_management%2Cpublic_profile&denied_scopes=&signed_request=dfnbo-qj35smA6der_6iXSPBT-ehvkb-v3PsIXCkYQ4.eyJ1c2VyX2lkIjoiMTAwMDczMTY5MTY0MTIwIiwiY29kZSI6IkFRQ1V2NXQ4NXhPR0NIRmNYbW41S1doc19LQXZJTjRuV3d0ejNoN0dhNUZyYVRvWDZLRS1jaFdIV2VWTy1aX05mZGdtaG5rd0dmYXM2Ylg1ajZyZUF6bHdsdmdGNEJLTUluSWUySnZXOTdCREJnd0dyUUs3eWYwckR6OW84dzNKVTFUUEdDNTYwZU1sLXRFeVd0amJ4OERoRnhoSmlsYXRrak93UDN2RkN3LU02OWdIZGZta0hxQVJicGJZOUJkMDRETzZ4d0FhYURYNk1kY0I5VTVoUFN1bkhSamJ1Sk9JNHZBVVFWOFNndnJuTFNHWTZOZ2VGbHBtLXVBZjcxVXNiRXE1XzFTZVRyMEFyMllhYVR5QTRtRFNHNExUdGVVQlhqOS1kMnJfRTZmcDk3Z2szbkppZllEbm5ZR1g2M1AwODhfOGpfdXdXRURlT0ZQVk42ZVVaa0FwdHRUMllndHptak45VTY5MXpRSU1DZTR3Mm91eTZiakxremotWWxVMElPdDNjdXFpVjhUejJOOGMzVGVkMlcybCIsImFsZ29yaXRobSI6IkhNQUMtU0hBMjU2IiwiaXNzdWVkX2F0IjoxNjM0MzMxNDMwfQ&graph_domain=facebook&access_token=EAABwzLixnjYBAO4uPpo88j4ZBSPUOOqC6WhFeER6cnuWSUBKrYfjU6ETwDACjrtYqRusc4M2j8cq5uHrRhUxjgkm6XEtclae4nHuxBGD6xseNveTQgqdzTCACqPpPZAQpJLeZCqJ9ArAyavMONM8ZCKlZAny7PZAkZARJHnQBCLz6mskyRvj5yPv9X0nMmIZCs1UDeu9LeDNnHYzseYH4ioZB5t65S0ZAM0a35R66ltLFOA9xZCzHvGjuDy6usEaeEleccZD&data_access_expiration_time=0&expires_in=0
                for (String param : fragment.split("&")) {
                    final String[] fields = param.split("=");
                    params.put(fields[0], fields.length > 1 ? URLDecoder.decode(fields[1]) : "");
                }
                if (params.containsKey("access_token")) {

                    saveAccessToken(params);
                    //noinspection ConstantConditions
                    loginCallback.onSuccess(params.get("access_token"), this);

                } else if (!intent.getBooleanExtra("canceledByUser", false))
                    loginCallback.onError(fragment, this);
            } else if (data.toString().equals("button://close"))
                loginCallback.onCancel("CLOSE_BUTTON", this);
            else //user hit cancel button
                loginCallback.onCancel("CANCEL_BUTTON", this);

        }


        finish();

    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    private void restoreLoginStateFields() {
        if (this.resultIntentTokenKey == null || this.resultActivity == null) {
            FirebaseCrashlytics.getInstance().log("this.resultIntentTokenKey == null || this.resultActivity == null");
            final SharedPreferences sp = this.getSharedPreferences(Constants.DefaultSharedPreferences.PREFERENCES_FILE, MODE_PRIVATE);
            this.resultIntentTokenKey = sp.getString(Constants.DefaultSharedPreferences.FACEBOOK_LOGIN_RESULT_INTENT_TOKEN_KEY, null);
            final String className = sp.getString(Constants.DefaultSharedPreferences.FACEBOOK_LOGIN_ACTIVITY_RESULT, null);
            if (className != null) {
                try {
                    this.resultActivity = (Class<? extends Activity>) Class.forName(className);
                } catch (Exception e) {
                    FirebaseCrashlytics.getInstance().log(e.getMessage());
                }
            }
            if (this.resultIntentTokenKey == null || this.resultActivity == null) {
                FirebaseCrashlytics.getInstance().recordException(new RuntimeException("this.resultIntentTokenKey == null || this.resultActivity == null"));
                finish();
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.creationTs = System.currentTimeMillis();

        if (this.getIntent().getData() == null) {
            //           getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE).edit().putString("caller", this.caller = getIntent().getStringExtra("caller")).apply();

            this.resultIntentTokenKey = getIntent().getStringExtra(Constants.Intent.FACEBOOK_LOGIN_RESULT_INTENT_TOKEN_KEY);
            this.resultActivity = (Class<? extends Activity>) getIntent().getSerializableExtra(Constants.Intent.FACEBOOK_LOGIN_ACTIVITY_RESULT);

            this.getSharedPreferences(Constants.DefaultSharedPreferences.PREFERENCES_FILE, MODE_PRIVATE).edit().putString(Constants.DefaultSharedPreferences.FACEBOOK_LOGIN_RESULT_INTENT_TOKEN_KEY, this.resultIntentTokenKey).putString(Constants.DefaultSharedPreferences.FACEBOOK_LOGIN_ACTIVITY_RESULT, this.resultActivity.getName()).apply();

            //intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY) breaks
            Intent intent = new CustomTabsIntent.Builder().setShowTitle(false).setUrlBarHidingEnabled(true).build().intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT).setData(Uri.parse(URL_BUILDER.buildLoginUrl()));


            if (hasChrome())
                intent.setPackage("com.android.chrome");


            this.loginResultHandler.launch(new IntentSenderRequest.Builder(PendingIntent.getActivity(this, 123, intent, Build.VERSION.SDK_INT < Build.VERSION_CODES.M ? 0 : PendingIntent.FLAG_IMMUTABLE)).build());
            //ContextCompat.startActivity(this, intent, null);
            FirebaseCrashlytics.getInstance().log("opened custom tab");
        } else
            onNewIntent(getIntent());
    }

    private boolean hasChrome() {
        PackageManager pm = getPackageManager();
        Intent i = pm.getLaunchIntentForPackage("com.android.chrome");
        return i != null && !pm.queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY).isEmpty();
    }

    public static void logOut() {
        accessToken = null;
        preferences.edit().remove(FacebookLogin.TOKEN).remove(FacebookLogin.EXPIRES).remove(FacebookLogin.USER_ID).apply();
        FirebaseCrashlytics.getInstance().log("facebook credentials logout");
    }

    @Nullable
    public static AccessToken getCurrentAccessToken() {
        return accessToken;
    }

    public static void initialize(@NonNull final Context applicationContext, @NonNull final String intentFilterHost) {
        preferences = applicationContext.getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
        URL_BUILDER.withManifestIntent(intentFilterHost);
        final String token = preferences.getString(FacebookLogin.TOKEN, null);
        if (token != null)
            accessToken = new AccessToken(token, preferences.getLong(FacebookLogin.EXPIRES, Long.MAX_VALUE), preferences.getString(FacebookLogin.USER_ID, ""));

    }

    private void saveAccessToken(@NonNull Map<String, String> params) {
        //noinspection ConstantConditions
        accessToken = new AccessToken(params.get("access_token"), System.currentTimeMillis() + Long.parseLong(params.get("expires_in")), "");
        preferences.edit().putString(FacebookLogin.TOKEN, accessToken.getToken()).putLong(FacebookLogin.EXPIRES, accessToken.getExpires().getTime()).putString(FacebookLogin.USER_ID, accessToken.getUserId()).apply();
    }


    public static FacebookLoginBuilder builder(@NonNull final String clientId) {
        return URL_BUILDER.withClientId(clientId);
    }

    public static final class FacebookLoginBuilder {
        private String clientId;
        private String sdk;
        private String authType;
        private String manifestIntent;

        private FacebookLoginBuilder(@NonNull final String clientId, @NonNull final String manifestIntent) {
            this.clientId = clientId;
            this.manifestIntent = manifestIntent;
            this.authType = "rerequest";
        }

        public FacebookLoginBuilder withManifestIntent(@NonNull final String manifestIntent) {
            this.manifestIntent = manifestIntent;
            return this;
        }

        public FacebookLoginBuilder withClientId(@NonNull final String clientId) {
            this.clientId = clientId;
            return this;
        }

        public FacebookLoginBuilder withSdk(@NonNull final String sdk) {
            this.sdk = sdk;
            return this;
        }

        /**
         * reauthentique
         *
         * @param authType
         * @return
         */
        public FacebookLoginBuilder withAuthType(@NonNull final String authType) {
            this.authType = authType;
            return this;
        }

        public void build(@NonNull final Context context, @NonNull final Class<? extends Activity> activityResult, @NonNull final String intentTokenKey) {
            ContextCompat.startActivity(context, new Intent(context, FacebookLogin.class).putExtra(Constants.Intent.FACEBOOK_LOGIN_ACTIVITY_RESULT, activityResult).putExtra(Constants.Intent.FACEBOOK_LOGIN_RESULT_INTENT_TOKEN_KEY, intentTokenKey).putExtra(Constants.Intent.FACEBOOK_LOGIN_START_TIME, System.currentTimeMillis()), null);
            FirebaseCrashlytics.getInstance().log("starting login with facebook");
        }

        private String buildLoginUrl() {
//            return "https://www.infomoney.com.br";?
            return new StringBuilder(700).append("https://m.facebook.com/v12.0/dialog/oauth?")
                    .append("cct_prefetching=").append(0)
                    .append("&client_id=").append(this.clientId)
                    .append("&cbt=").append(System.currentTimeMillis())
                    .append("&e2e=").append("%7B%22init%22%3A").append(System.currentTimeMillis()).append("%7D")
                    .append("&ies=").append(1)
                    .append("&sdk=").append(this.sdk == null ? "android-12.0.0" : this.sdk)
                    .append("&sso=").append("chrome_custom_tab")
                    .append("&nonce=").append(UUID.randomUUID().toString())
                    .append("&scope=").append("openid")
                    //.append("&state=").append("%7B%220_auth_logger_id%22%3A%22e79995c7-1a8d-4880-93ec-7589c5de3c11%22%2C%223_method%22%3A%22custom_tab%22%2C%227_challenge%22%3A%22ldq0e38jmrsjlvt3fjir%22%7D")
                    .append("&default_audience=").append("friends")
                    .append("&login_behavior=").append("WEB_ONLY")
                    .append("&redirect_uri=").append("fbconnect%3A%2F%2F").append(this.manifestIntent)
                    .append("&auth_type=").append(this.authType)
                    .append("&response_type=").append("id_token%2Ctoken%2Csigned_request%2Cgraph_domain")
                    .append("&return_scopes=").append("true").toString();

        }

    }

    public static final class AccessToken {

        private final String token;
        private final Date expiration;
        private final String userId;

        public AccessToken(@NonNull String token, final long expiration, @NonNull final String userid) {
            this.token = token;
            this.expiration = new Date(expiration);
            this.userId = userid;
        }

        @NonNull
        public String getToken() {
            return this.token;
        }


        @NonNull
        public Date getExpires() {
            return this.expiration;
        }

        @NonNull
        public String getUserId() {
            return this.userId;
        }
    }

    public interface FacebookLoginCallback extends Serializable {


        public void onSuccess(@NonNull final String loginResult, @NonNull final Context context);


        public void onCancel(@NonNull final String cancelMessage, @NonNull final Context context);


        public void onError(@NonNull final String error, @NonNull final Context context);


    }

    public static final class DefaultFacebookLoginCallback implements FacebookLoginCallback {


        private final Class<?> activityClass;
        private final String intentTokenKey;

        public DefaultFacebookLoginCallback(@NonNull final Class<?> activityClass, @NonNull final String intentTokenKey) {

            this.activityClass = activityClass;
            this.intentTokenKey = intentTokenKey;
        }

        @Override
        public void onSuccess(@NonNull String loginResult, @NonNull final Context context) {
            FirebaseCrashlytics.getInstance().log("on sucess");
            ContextCompat.startActivity(context, new Intent(context, this.activityClass).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK).putExtra(this.intentTokenKey, loginResult), null);
        }

        @Override
        public void onCancel(@NonNull String cancelMessage, @NonNull final Context context) {
            //  builder.withAuthType("reauthenticate"); this only forces users to retype password for same account
            FacebookLogin.logOut();
            FirebaseCrashlytics.getInstance().log("on cancel");
            ContextCompat.startActivity(context, new Intent(context, this.activityClass).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK), null);

        }

        @Override
        public void onError(@NonNull String error, @NonNull final Context context) {
            //    builder.withAuthType("reauthenticate");
            FacebookLogin.logOut();
            FirebaseCrashlytics.getInstance().log("on error");
            ContextCompat.startActivity(context, new Intent(context, this.activityClass).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK), null);

        }
    }
}


