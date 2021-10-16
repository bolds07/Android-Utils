package com.tomatedigital.androidutils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FacebookLogin extends AppCompatActivity {


    private static final FacebookLoginURLBuilder URL_BUILDER = new FacebookLoginURLBuilder("", "");
    private static FacebookLoginCallback loginCallback;
    private static AccessToken accessToken;
    private static SharedPreferences preferences;

    public static void logOut() {
        accessToken = null;
        preferences.edit().remove(Constants.DefaultSharedPreferences.FacebookLogin.TOKEN).remove(Constants.DefaultSharedPreferences.FacebookLogin.EXPIRES).remove(Constants.DefaultSharedPreferences.FacebookLogin.USER_ID).apply();
        FirebaseCrashlytics.getInstance().log("facebook credentials logout");
    }

    @Nullable
    public static AccessToken getCurrentAccessToken() {
        return accessToken;
    }

    public static void initialize(@NonNull final Context applicationContext, @NonNull final String intentFilterHost) {
        preferences = applicationContext.getSharedPreferences(Constants.DefaultSharedPreferences.PREFERENCES_FILE, MODE_PRIVATE);
        URL_BUILDER.withManifestIntent(intentFilterHost);
        final String token = preferences.getString(Constants.DefaultSharedPreferences.FacebookLogin.TOKEN, null);
        if (token != null)
            accessToken = new AccessToken(token, preferences.getLong(Constants.DefaultSharedPreferences.FacebookLogin.EXPIRES, Long.MAX_VALUE), preferences.getString(Constants.DefaultSharedPreferences.FacebookLogin.USER_ID, ""));

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (getIntent().getBooleanExtra("starting", false))
            ContextCompat.startActivity(this, new Intent(this, ClearTaskActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("starting", true).putExtra("url", URL_BUILDER.build()), null);
        else {
            final Uri data = getIntent().getData();
            if (data != null) {
                String fragment = data.getEncodedFragment();
                if (fragment != null) {
                    //granted_scopes=email%2Cads_management%2Cpublic_profile&denied_scopes=&signed_request=dfnbo-qj35smA6der_6iXSPBT-ehvkb-v3PsIXCkYQ4.eyJ1c2VyX2lkIjoiMTAwMDczMTY5MTY0MTIwIiwiY29kZSI6IkFRQ1V2NXQ4NXhPR0NIRmNYbW41S1doc19LQXZJTjRuV3d0ejNoN0dhNUZyYVRvWDZLRS1jaFdIV2VWTy1aX05mZGdtaG5rd0dmYXM2Ylg1ajZyZUF6bHdsdmdGNEJLTUluSWUySnZXOTdCREJnd0dyUUs3eWYwckR6OW84dzNKVTFUUEdDNTYwZU1sLXRFeVd0amJ4OERoRnhoSmlsYXRrak93UDN2RkN3LU02OWdIZGZta0hxQVJicGJZOUJkMDRETzZ4d0FhYURYNk1kY0I5VTVoUFN1bkhSamJ1Sk9JNHZBVVFWOFNndnJuTFNHWTZOZ2VGbHBtLXVBZjcxVXNiRXE1XzFTZVRyMEFyMllhYVR5QTRtRFNHNExUdGVVQlhqOS1kMnJfRTZmcDk3Z2szbkppZllEbm5ZR1g2M1AwODhfOGpfdXdXRURlT0ZQVk42ZVVaa0FwdHRUMllndHptak45VTY5MXpRSU1DZTR3Mm91eTZiakxremotWWxVMElPdDNjdXFpVjhUejJOOGMzVGVkMlcybCIsImFsZ29yaXRobSI6IkhNQUMtU0hBMjU2IiwiaXNzdWVkX2F0IjoxNjM0MzMxNDMwfQ&graph_domain=facebook&access_token=EAABwzLixnjYBAO4uPpo88j4ZBSPUOOqC6WhFeER6cnuWSUBKrYfjU6ETwDACjrtYqRusc4M2j8cq5uHrRhUxjgkm6XEtclae4nHuxBGD6xseNveTQgqdzTCACqPpPZAQpJLeZCqJ9ArAyavMONM8ZCKlZAny7PZAkZARJHnQBCLz6mskyRvj5yPv9X0nMmIZCs1UDeu9LeDNnHYzseYH4ioZB5t65S0ZAM0a35R66ltLFOA9xZCzHvGjuDy6usEaeEleccZD&data_access_expiration_time=0&expires_in=0
                    Map<String, String> params = new HashMap<>();
                    for (String param : fragment.split("&")) {
                        final String[] fields = param.split("=");
                        params.put(fields[0], fields.length > 1 ? URLDecoder.decode(fields[1]) : "");
                    }
                    if (params.containsKey("access_token")) {
                        saveAccessToken(params);
                        //noinspection ConstantConditions
                        loginCallback.onSuccess(params.get("acess_token"));

                    } else if (!getIntent().getBooleanExtra("canceledByUser", false))
                        loginCallback.onError(fragment);
                } else if (data.toString().equals("button://close"))
                    loginCallback.onCancel("CLOSE_BUTTON");
                else //user hit cancel button
                    loginCallback.onCancel("CANCEL_BUTTON");

                loginCallback = null;
            }

            ContextCompat.startActivity(this, new Intent(this, ClearTaskActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), null);
        }

        finish();

    }

    private void saveAccessToken(@NonNull Map<String, String> params) {
        //noinspection ConstantConditions
        accessToken = new AccessToken(params.get("access_token"), System.currentTimeMillis() + Long.parseLong(params.get("expires_in")), "");
        preferences.edit().putString(Constants.DefaultSharedPreferences.FacebookLogin.TOKEN, accessToken.getToken()).putLong(Constants.DefaultSharedPreferences.FacebookLogin.EXPIRES, accessToken.getExpires().getTime()).putString(Constants.DefaultSharedPreferences.FacebookLogin.USER_ID, accessToken.getUserId()).apply();
    }

    public static void login(@NonNull final Context context, @NonNull final FacebookLoginURLBuilder builder, @NonNull final FacebookLoginCallback loginCallback) {
        FacebookLogin.loginCallback = loginCallback;
        ContextCompat.startActivity(context, new Intent(context, FacebookLogin.class).putExtra("starting", true).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT), null);
        FirebaseCrashlytics.getInstance().log("starting login with facebook");
    }

    public static FacebookLoginURLBuilder builder(@NonNull final String clientId) {
        return URL_BUILDER.withClientId(clientId);
    }

    public static final class FacebookLoginURLBuilder {
        private String clientId;
        private String sdk;
        private String authType;
        private String manifestIntent;

        private FacebookLoginURLBuilder(@NonNull final String clientId, @NonNull final String manifestIntent) {
            this.clientId = clientId;
            this.manifestIntent = manifestIntent;
        }

        public FacebookLoginURLBuilder withManifestIntent(@NonNull final String manifestIntent) {
            this.manifestIntent = manifestIntent;
            return this;
        }

        public FacebookLoginURLBuilder withClientId(@NonNull final String clientId) {
            this.clientId = clientId;
            return this;
        }

        public FacebookLoginURLBuilder withSdk(@NonNull final String sdk) {
            this.sdk = sdk;
            return this;
        }

        /**
         * reauthentique
         *
         * @param authType
         * @return
         */
        public FacebookLoginURLBuilder withAuthType(@NonNull final String authType) {
            this.authType = authType;
            return this;
        }

        public String build() {
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
                    .append("&auth_type=").append("rerequest")
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

    public interface FacebookLoginCallback {


        public void onSuccess(@NonNull final String loginResult);


        public void onCancel(@NonNull final String cancelMessage);


        public void onError(@NonNull final String error);


    }
}


