package dev.bleuxstrife.flutter_notif_tone_picker;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.annotation.VisibleForTesting;

import java.util.HashMap;
import java.util.Map;

public class NotifTonePickerCache {
    static final String MAP_KEY_URI_STRING = "uriString";
    static final String MAP_KEY_TONE_NAME = "toneName";
    static final String SHARED_PREFERENCE_URI_STRING = "flutter_notif_tone_picker_shared_preference_uri_string";
    static final String SHARED_PREFERENCE_TONE_NAME = "flutter_notif_tone_picker_shared_preference_tone_name";
    
    @VisibleForTesting
    static final String SHARED_PREFERENCES_NAME = "flutter_notif_tone_picker_shared_preference";

    private SharedPreferences prefs;

    NotifTonePickerCache(Context context) {
        prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    void saveUriPath(String uriPath) {
        prefs.edit().putString(SHARED_PREFERENCE_URI_STRING, uriPath).apply();
    }
    
    void saveToneName(String toneName){
        prefs.edit().putString(SHARED_PREFERENCE_TONE_NAME, toneName).apply();
    }

    void clear() {
        prefs.edit().clear().apply();
    }

    Map<String, Object> getCacheMap() {

        Map<String, Object> resultMap = new HashMap<>();
        boolean hasData = false;
        
        if(prefs.contains(SHARED_PREFERENCE_URI_STRING)){
            final String uriPath = prefs.getString(SHARED_PREFERENCE_URI_STRING, "");
            resultMap.put(MAP_KEY_URI_STRING, uriPath);
        }
        
        if(prefs.contains(SHARED_PREFERENCE_TONE_NAME)){
            final String toneName = prefs.getString(SHARED_PREFERENCE_TONE_NAME, "");
            resultMap.put(MAP_KEY_TONE_NAME, toneName);
        }
        
        return resultMap;
    }
    
}
