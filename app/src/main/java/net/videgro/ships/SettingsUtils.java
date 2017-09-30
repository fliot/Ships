package net.videgro.ships;

//import net.videgro.ships.domain.TrackingSettings;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public final class SettingsUtils {
	private static final String TAG = "SettingsUtils";

	// Declare the same values as in res/xml/preferences.xml!
	public static final String KEY_PREF_RTL_SDR_PPM = "pref_rtlSdrPpm";
    private static final String KEY_PREF_SHIP_SCALE_FACTOR = "pref_shipScaleFactor";
    private static final String KEY_PREF_OWN_LOCATION_ICON = "pref_ownLocationIcon";
//	private static final String KEY_PREF_RTL_SDR_FORCE_ROOT = "pref_rtlSdrForceRoot";

	private static final String KEY_PREF_LOGGING_VERBOSE = "pref_loggingVerbose";
	private static final String KEY_PREF_MAP_ZOOM_TO_EXTEND = "pref_mapZoomToExtend";
	private static final String KEY_PREF_MAP_CACHE_ZOOM_LOWER_LEVELS = "pref_mapFetchLowerZoomLevels";
	private static final String KEY_PREF_MAP_CACHE_DISK_USAGE_MAX = "pref_mapCacheMaxDiskUsage";
	private static final String KEY_PREF_AIS_MESSAGES_DESTINATION_HOST = "pref_aisMessagesDestinationHost";
	private static final String KEY_PREF_AIS_MESSAGES_DESTINATION_PORT = "pref_aisMessagesDestinationPort";

	private static final boolean DEFAULT_LOGGING_VERBOSE = true;
	private static final boolean DEFAULT_MAP_ZOOM_TO_EXTEND = true;
	private static final int DEFAULT_MAP_CACHE_DISK_USAGE_MAX = 5;
	private static final boolean DEFAULT_MAP_CACHE_ZOOM_LOWER_LEVELS = true;
	private static final String DEFAULT_AIS_MESSAGES_DESTINATION_HOST = "127.0.0.1";
	private static final int DEFAULT_AIS_MESSAGES_DESTINATION_PORT = 10110;

//	private static final boolean DEFAULT_RTL_SDR_FORCE_ROOT = false;
	private static final int DEFAULT_RTL_SDR_PPM = Integer.MAX_VALUE;

	private static final int RTL_SDR_PPM_VALID_OFFSET = 150;
    private static final int DEFAULT_SHIP_SCALE_FACTOR = 8;

	/* Same as  res/values/strings.xml pref_ownLocationIcon_default */
    private static final String DEFAULT_OWN_LOCATION_ICON = "antenna.png";

    private static SettingsUtils instance=null;

    private SharedPreferences sharedPreferences=null;


    private SettingsUtils() {
		// Singleton, no public constructor
	}

	public static SettingsUtils getInstance(){
        if (instance==null){
            instance=new SettingsUtils();
        }
        return instance;
    }

	public void init(final Context context){
		if (context==null){
			throw new IllegalArgumentException("No context set.");
		}
		sharedPreferences=PreferenceManager.getDefaultSharedPreferences(context);
	}

	private void validateSharedPreferences(){
		if (sharedPreferences==null){
			throw new IllegalArgumentException("No SharedPreferences set. Please init class before using it.");
		}
	}

	public int parseFromPreferencesRtlSdrPpm() {
		final String tag="parseFromPreferencesRtlSdrPpm - ";
		validateSharedPreferences();

		int result = DEFAULT_RTL_SDR_PPM;

		try {
			result = Integer.valueOf(sharedPreferences.getString(KEY_PREF_RTL_SDR_PPM, Integer.toString(DEFAULT_RTL_SDR_PPM)));
		} catch (ClassCastException | NumberFormatException e) {
			Log.e(TAG,tag, e);
		}

		return result;
	}

	public static boolean isValidPpm(final int ppm) {
		return (ppm > (SettingsUtils.RTL_SDR_PPM_VALID_OFFSET * -1) && ppm < SettingsUtils.RTL_SDR_PPM_VALID_OFFSET);
	}

	public void setToPreferencesPpm(final int ppm) {
		validateSharedPreferences();

		final SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(KEY_PREF_RTL_SDR_PPM, Integer.toString(ppm));
		editor.commit();
	}

	public boolean parseFromPreferencesLoggingVerbose() {
		validateSharedPreferences();
		return sharedPreferences.getBoolean(KEY_PREF_LOGGING_VERBOSE, DEFAULT_LOGGING_VERBOSE);
	}

	public boolean parseFromPreferencesMapZoomToExtend() {
		validateSharedPreferences();
        return sharedPreferences.getBoolean(KEY_PREF_MAP_ZOOM_TO_EXTEND, DEFAULT_MAP_ZOOM_TO_EXTEND);
	}

	public boolean parseFromPreferencesMapCacheLowerZoomlevels() {
		validateSharedPreferences();
		return sharedPreferences.getBoolean(KEY_PREF_MAP_CACHE_ZOOM_LOWER_LEVELS, DEFAULT_MAP_CACHE_ZOOM_LOWER_LEVELS);
	}

	public long parseFromPreferencesMapCacheDiskUsageMax() {
		final String tag="parseFromPreferencesMapCacheDiskUsageMax";
		validateSharedPreferences();

		int result = DEFAULT_MAP_CACHE_DISK_USAGE_MAX;

        try {
            result = Integer.valueOf(sharedPreferences.getString(KEY_PREF_MAP_CACHE_DISK_USAGE_MAX, Integer.toString(DEFAULT_MAP_CACHE_DISK_USAGE_MAX)));
        } catch (ClassCastException | NumberFormatException e) {
            Log.e(TAG,tag,e);
        }

		// Return in bytes
		return result * 1024 * 1024L;
	}

	public String parseFromPreferencesAisMessagesDestinationHost() {
		validateSharedPreferences();
		return sharedPreferences.getString(KEY_PREF_AIS_MESSAGES_DESTINATION_HOST, DEFAULT_AIS_MESSAGES_DESTINATION_HOST);
	}

	public int parseFromPreferencesAisMessagesDestinationPort() {
		final String tag="parseFromPreferencesAisMessagesDestinationPort - ";
		validateSharedPreferences();

		int result = DEFAULT_AIS_MESSAGES_DESTINATION_PORT;
        try {
            result = Integer.valueOf(sharedPreferences.getString(KEY_PREF_AIS_MESSAGES_DESTINATION_PORT, Integer.toString(DEFAULT_AIS_MESSAGES_DESTINATION_PORT)));
        } catch (ClassCastException | NumberFormatException e) {
            Log.e(TAG,tag,e);
		}
		return result;
	}

	public int parseFromPreferencesShipScaleFactor() {
		final String tag="parseFromPreferencesShipScaleFactor - ";
		validateSharedPreferences();

		int result = DEFAULT_SHIP_SCALE_FACTOR;

        try {
            result = Integer.valueOf(sharedPreferences.getString(KEY_PREF_SHIP_SCALE_FACTOR, Integer.toString(DEFAULT_SHIP_SCALE_FACTOR)));
        } catch (ClassCastException | NumberFormatException e) {
            Log.e(TAG,tag, e);
        }

		return result;
	}

    public String parseFromPreferencesOwnLocationIcon() {
		validateSharedPreferences();
		return sharedPreferences.getString(KEY_PREF_OWN_LOCATION_ICON, DEFAULT_OWN_LOCATION_ICON);
    }
}

