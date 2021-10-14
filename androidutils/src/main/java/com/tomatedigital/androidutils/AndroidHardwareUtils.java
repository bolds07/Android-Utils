package com.tomatedigital.androidutils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public class AndroidHardwareUtils {


    @SuppressLint("HardwareIds")
    public static String getUniqueDeviceUUID(@NonNull Context context) {

        final SharedPreferences sp = context.getSharedPreferences(Constants.DefaultSharedPreferences.PREFERENCES_FILE, Context.MODE_PRIVATE);
        String uuid = sp.getString(Constants.DefaultSharedPreferences.DEVICE_UUID, null);
        if (uuid == null) {
            uuid = UUID.nameUUIDFromBytes(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID).getBytes()).toString();
            sp.edit().putString(Constants.DefaultSharedPreferences.DEVICE_UUID, uuid).apply();
        }
        return uuid;
    }

    public static int getProcessorCoresCount() {
        if (Build.VERSION.SDK_INT >= 17) {
            return Runtime.getRuntime().availableProcessors();
        } else {
            // Use saurabh64's answer
            return getNumCoresOldPhones();
        }
    }

    /**
     * Gets the number of cores available in this device, across all processors.
     * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
     *
     * @return The number of cores, or 1 if failed to get result
     */
    private static int getNumCoresOldPhones() {
        //Private Class to display only CPU devices in the directory listing
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                //Check if filename is "cpu", followed by a single digit number
                return Pattern.matches("cpu[0-9]+", pathname.getName());
            }
        }

        try {
            //Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            //Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            //Return the number of cores (virtual CPU devices)
            return files.length;
        } catch (Exception e) {
            //Default to return 1 core
            return 1;
        }
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

}
