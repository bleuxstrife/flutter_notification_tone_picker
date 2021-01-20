package dev.bleuxstrife.flutter_notif_tone_picker;

import android.app.Activity;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.VisibleForTesting;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;

public class FlutterNotifTonePickerDelegate implements PluginRegistry.ActivityResultListener {
    @VisibleForTesting private static final String TAG = "FlutterNotifTonePicker";
    @VisibleForTesting private static final int REQUEST_CODE = (FlutterNotifTonePickerPlugin.class.hashCode() + 203) & 0x0000ffff;

    private final Activity activity;
    private MethodChannel.Result pendingResult;
    private final NotifTonePickerCache cache;
    private EventChannel.EventSink eventSink;

    public FlutterNotifTonePickerDelegate(final Activity activity, final NotifTonePickerCache cache){
        this(activity, null, cache);
    }

    public void setEventHandler(final EventChannel.EventSink eventSink) {
        this.eventSink = eventSink;
    }

    @VisibleForTesting
    FlutterNotifTonePickerDelegate(final Activity activity, final MethodChannel.Result result, final NotifTonePickerCache cache) {
        this.activity = activity;
        this.pendingResult = result;
        this.cache = cache;
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if(eventSink!=null){
                eventSink.success(true);
            }
            if(data!=null){
                if (data.getExtras()!=null){
                    Uri uri = (Uri) data.getExtras().get(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    finishWithSuccess(uri);
                } else if(data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)!=null){
                    Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    finishWithSuccess(uri);
                }
            } else {
                finishWithError("uri_null", "Could not get Uri because null");
                return false;
            }
            return true;
        } else if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_CANCELED) {
            Log.i(TAG, "User cancelled the picker request");
            finishWithSuccess(null);
            return true;
        } else if (requestCode == REQUEST_CODE) {
            finishWithError("unknown_activity", "Unknown activity error, please fill an issue.");
        }
        return false;
    }

    public void openNotificationTone(MethodChannel.Result result){

        if (!this.setPendingMethodCallAndResult(result)) {
            finishWithAlreadyActiveError(result);
            return;
        }
        final Intent intent;
        intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        this.activity.startActivityForResult(intent, REQUEST_CODE);
    }

    private boolean setPendingMethodCallAndResult(final MethodChannel.Result result) {
        if (this.pendingResult != null) {
            return false;
        }
        this.pendingResult = result;
        cache.clear();
        return true;
    }

    private static void finishWithAlreadyActiveError(final MethodChannel.Result result) {
        result.error("already_active", "Change Notification Tone is already active", null);
    }

    private void finishWithSuccess(Uri data) {
        if (eventSink != null) {
            this.dispatchEventStatus(false);
        }

        if(data == null){
            finishWithError("uri_null", "Could not get Uri because null");
        }

        if (this.pendingResult != null) {
            Ringtone ringtone = RingtoneManager.getRingtone(this.activity, data);
            String title = ringtone.getTitle(this.activity);
            HashMap<String, String> mapData = new HashMap();
            cache.saveToneName(title);
            cache.saveUriPath(data);
            mapData.put("uriPath", data.getPath());
            mapData.put("toneName", title);
            this.pendingResult.success(mapData);
            this.clearPendingResult();
        }
    }

    private void finishWithError(final String errorCode, final String errorMessage) {
        if (this.pendingResult == null) {
            return;
        }

        if (eventSink != null) {
            this.dispatchEventStatus(false);
        }
        this.pendingResult.error(errorCode, errorMessage, null);
        this.clearPendingResult();
    }

    private void dispatchEventStatus(final boolean status) {
        new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(final Message message) {
                eventSink.success(status);
            }
        }.obtainMessage().sendToTarget();
    }

    public void retrieveLostData(MethodChannel.Result result) {
        Map<String, Object> resultMap = cache.getCacheMap();
        if (resultMap.isEmpty()) {
            result.success(null);
        } else {
            result.success(resultMap);
        }
        cache.clear();
    }

    private void clearPendingResult() {
        this.pendingResult = null;
    }
}
