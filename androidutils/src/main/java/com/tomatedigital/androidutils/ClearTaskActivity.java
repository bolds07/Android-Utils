package com.tomatedigital.androidutils;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.HashSet;
import java.util.Set;

public class ClearTaskActivity extends AppCompatActivity {

    final private ActivityResultLauncher<IntentSenderRequest> customTabLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
        if (result.getResultCode() == RESULT_CANCELED) {
            FirebaseCrashlytics.getInstance().log("facebook login canceled: " + result.getData());
         
            ContextCompat.startActivity(this, new Intent(this, FacebookLogin.class).setData(Uri.parse("button://close")), null);
        }

    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getBooleanExtra("starting", false)) {
            Intent intent = new CustomTabsIntent.Builder().setUrlBarHidingEnabled(true).setShowTitle(false).build().intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY).setData(Uri.parse(getIntent().getStringExtra("url")));
            PendingIntent pi = PendingIntent.getActivity(this, 123, intent, PendingIntent.FLAG_IMMUTABLE);

            if (hasChrome())
                intent.setPackage("com.android.chrome");

            this.customTabLauncher.launch(new IntentSenderRequest.Builder(pi).setFillInIntent(intent).build());

        } else
            finish();
    }

    private boolean hasChrome() {
        PackageManager pm = getPackageManager();
        Intent i = pm.getLaunchIntentForPackage("com.android.chrome");
        return i != null && !pm.queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY).isEmpty();
    }


}
