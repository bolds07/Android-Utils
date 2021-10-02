package com.tomatedigital.androidutils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UtilActivity extends AppCompatActivity {

    final private Set<String> requestingPermissions = new HashSet<>();
    final private Map<Integer, String> permissionPendingAnswer = new HashMap<>();

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (String pem : permissions)
            this.requestingPermissions.remove("rp_" + pem.substring(Math.max(0, pem.lastIndexOf("."))));


    }

    public boolean hasPermission(@NonNull final String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final String pendingPermission = this.permissionPendingAnswer.get(requestCode);
        if (pendingPermission != null) {
            this.permissionPendingAnswer.remove(resultCode);
            this.onRequestPermissionsResult(requestCode, new String[]{pendingPermission}, new int[]{ContextCompat.checkSelfPermission(this, pendingPermission)});
        }

    }

    @MainThread
    public void requestPermission(@NonNull final String permission, final int requestorCode, @Nullable String explanationMessage) {
        final String pref = "rp_" + permission.substring(Math.max(0, permission.lastIndexOf(".")));

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED)
            this.onRequestPermissionsResult(requestorCode, new String[]{permission}, new int[]{PackageManager.PERMISSION_GRANTED});

        else if (!this.isFinishing() && this.requestingPermissions.add(pref)) {
            final SharedPreferences sp = this.getSharedPreferences("androidUtils.preference", Context.MODE_PRIVATE);

            final boolean alreadyAsked = sp.getBoolean(pref, false);
            if (!alreadyAsked) {
                sp.edit().putBoolean(pref, true).apply();
                ActivityCompat.requestPermissions(this, new String[]{permission}, requestorCode);
            } else {
                if (explanationMessage == null)
                    explanationMessage = this.getResources().getString(R.string.default_request_permission_message);


                new AlertDialog.Builder(this).setTitle(R.string.permission_required).setMessage(explanationMessage).setCancelable(false).setPositiveButton(R.string.go_to_settings, (d, which) -> {
                    this.permissionPendingAnswer.put(requestorCode, permission);
                    this.requestingPermissions.remove(pref);

                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                    intent.setData(uri);
                    this.startActivityForResult(intent, requestorCode);
                 /*   this.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                        this.onRequestPermissionsResult(requestorCode, new String[]{permission}, new int[]{ContextCompat.checkSelfPermission(this, permission)});
                    }).launch(intent);

                  */

                }).show();
            }
        }


    }

    public static interface PermissionAnswerListener {

        public void onPermissionAnswered(final int code, @NonNull final String permission, final int response);

    }

}
