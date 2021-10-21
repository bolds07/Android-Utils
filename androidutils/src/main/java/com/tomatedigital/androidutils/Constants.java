package com.tomatedigital.androidutils;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;


@RestrictTo(LIBRARY)
public final class Constants {
    private Constants() {

    }

    @RestrictTo(LIBRARY)
    public static class DefaultSharedPreferences {
        public static final String PREFERENCES_FILE = "androidutils.preferences";

        public static final String LAST_USED_VERSION_INT = "luv";
        public static final String REFERRER_ID_STRING = "ri";
        public static final String DEVICE_UUID = "du";
        public static final String FACEBOOK_LOGIN_ACTIVITY_RESULT = "flar";
        public static final String FACEBOOK_LOGIN_RESULT_INTENT_TOKEN_KEY = "flritk";

        @RestrictTo(LIBRARY)
        public static class FloatingWindow {


            public static String WIDTH(@NonNull final String id) {
                return "fww_" + id;
            }

            public static String HEIGHT(@NonNull final String id) {
                return "fwh_" + id;
            }

            public static String X(@NonNull final String id) {
                return "fwx_" + id;
            }

            public static String Y(@NonNull final String id) {
                return "fwy_" + id;
            }
        }

    }


    public static class Intent {


        public static final String FACEBOOK_LOGIN_ACTIVITY_RESULT = "flar";
        public static final String FACEBOOK_LOGIN_RESULT_INTENT_TOKEN_KEY = "flirk";
        public static final String FACEBOOK_LOGIN_START_TIME = "flsts";
    }
}
