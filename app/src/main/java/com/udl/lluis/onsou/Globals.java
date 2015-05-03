package com.udl.lluis.onsou;

/**
 * Created by Lluís on 03/05/2015.
 */
public class Globals {

    public static String TAG = "ONSOU:::::::::::";

    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_APP_VERSION = "appVersion";
    public static final String PROPERTY_EXPIRATION_TIME = "onServerExpirationTimeMs";
    public static final String PROPERTY_USER = "user";

    public static final int EXPIRATION_TIME_MS = 1000 * 3600 * 24 * 7;
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public static final String SENDER_ID = "930427914417";

    public static final String ARG_SECTION_NUMBER = "SECTION_TAG";

    public static String URL_BARS = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%s&radius=%s&types=%s&sensor=true&key=%s";

    public static final boolean ALWAYS_SIMPLE_PREFS = false;

}
