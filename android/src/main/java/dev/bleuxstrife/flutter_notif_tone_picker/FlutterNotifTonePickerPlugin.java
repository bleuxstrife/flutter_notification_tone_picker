package dev.bleuxstrife.flutter_notif_tone_picker;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import java.io.File;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.embedding.engine.plugins.lifecycle.FlutterLifecycleAdapter;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** FlutterNotifTonePickerPlugin */
public class FlutterNotifTonePickerPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private ActivityPluginBinding activityBinding;
  private FlutterNotifTonePickerDelegate delegate;
  private Application application;
  private FlutterPluginBinding pluginBinding;

  // This is null when not using v2 embedding;
  private Lifecycle lifecycle;
  private LifeCycleObserver observer;
  private Activity activity;
  private MethodChannel channel;

  private static final String TAG = "FlutterNotifTonePicker";
  private static final String CHANNEL = "dev.bleuxstrife.flutter_notif_tone_picker";
  private static final String METHOD_CALL_RETRIEVE = "retrieve";
  private static final String METHOD_CALL_NOTIF_TONE_PICKER = "changeTone";
  private static final String EVENT_CHANNEL = "dev.bleuxstrife.flutter_notif_tone_picker_event";


  private class LifeCycleObserver
          implements Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {
    private final Activity thisActivity;

    LifeCycleObserver(final Activity activity) {
      this.thisActivity = activity;
    }

    @Override
    public void onCreate(@NonNull final LifecycleOwner owner) {
    }

    @Override
    public void onStart(@NonNull final LifecycleOwner owner) {
    }

    @Override
    public void onResume(@NonNull final LifecycleOwner owner) {
    }

    @Override
    public void onPause(@NonNull final LifecycleOwner owner) {
    }

    @Override
    public void onStop(@NonNull final LifecycleOwner owner) {
      this.onActivityStopped(this.thisActivity);
    }

    @Override
    public void onDestroy(@NonNull final LifecycleOwner owner) {
      this.onActivityDestroyed(this.thisActivity);
    }

    @Override
    public void onActivityCreated(final Activity activity, final Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(final Activity activity) {
    }

    @Override
    public void onActivityResumed(final Activity activity) {
    }

    @Override
    public void onActivityPaused(final Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(final Activity activity, final Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(final Activity activity) {
      if (this.thisActivity == activity && activity.getApplicationContext() != null) {
        ((Application) activity.getApplicationContext()).unregisterActivityLifecycleCallbacks(this); // Use getApplicationContext() to avoid casting failures
      }
    }

    @Override
    public void onActivityStopped(final Activity activity) {
    }
  }

  @SuppressWarnings("deprecation")
  public static void registerWith(final Registrar registrar) {

    if (registrar.activity() == null) {
      // If a background flutter view tries to register the plugin, there will be no activity from the registrar,
      // we stop the registering process immediately because the ImagePicker requires an activity.
      return;
    }

    final Activity activity = registrar.activity();
    Application application = null;
    if (registrar.context() != null) {
      application = (Application) (registrar.context().getApplicationContext());
    }

    final FlutterNotifTonePickerPlugin plugin = new FlutterNotifTonePickerPlugin();
    plugin.setup(registrar.messenger(), application, activity, registrar, null);

  }

  private final FlutterNotifTonePickerDelegate constructDelegate(final Activity setupActivity) {
    final NotifTonePickerCache cache = new NotifTonePickerCache(setupActivity);
    return new FlutterNotifTonePickerDelegate(activity, cache);
  }

  private void setup(
          final BinaryMessenger messenger,
          final Application application,
          final Activity activity,
          final PluginRegistry.Registrar registrar,
          final ActivityPluginBinding activityBinding) {

    this.activity = activity;
    this.application = application;
    this.delegate = constructDelegate(activity);
    this.channel = new MethodChannel(messenger, CHANNEL);
    this.channel.setMethodCallHandler(this);
    new EventChannel(messenger, EVENT_CHANNEL).setStreamHandler(new EventChannel.StreamHandler() {
      @Override
      public void onListen(final Object arguments, final EventChannel.EventSink events) {
        delegate.setEventHandler(events);
      }

      @Override
      public void onCancel(final Object arguments) {
        delegate.setEventHandler(null);
      }
    });
    this.observer = new LifeCycleObserver(activity);
    if (registrar != null) {
      // V1 embedding setup for activity listeners.
      application.registerActivityLifecycleCallbacks(this.observer);
      registrar.addActivityResultListener(this.delegate);
    } else {
      // V2 embedding setup for activity listeners.
      activityBinding.addActivityResultListener(this.delegate);
      this.lifecycle = FlutterLifecycleAdapter.getActivityLifecycle(activityBinding);
      this.lifecycle.addObserver(this.observer);
    }
  }

  private static class MethodResultWrapper implements MethodChannel.Result {
    private final MethodChannel.Result methodResult;
    private final Handler handler;

    MethodResultWrapper(final MethodChannel.Result result) {
      this.methodResult = result;
      this.handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void success(final Object result) {
      this.handler.post(
              new Runnable() {
                @Override
                public void run() {
                  MethodResultWrapper.this.methodResult.success(result);
                }
              });
    }

    @Override
    public void error(
            final String errorCode, final String errorMessage, final Object errorDetails) {
      this.handler.post(
              new Runnable() {
                @Override
                public void run() {
                  MethodResultWrapper.this.methodResult.error(errorCode, errorMessage, errorDetails);
                }
              });
    }

    @Override
    public void notImplemented() {
      this.handler.post(
              new Runnable() {
                @Override
                public void run() {
                  MethodResultWrapper.this.methodResult.notImplemented();
                }
              });
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result rawResult) {
    if (this.activity == null) {
      rawResult.error("no_activity", "Change Notification Tone plugin requires a foreground activity", null);
      return;
    }

    final MethodChannel.Result result = new MethodResultWrapper(rawResult);

    if (call.method.equals(METHOD_CALL_NOTIF_TONE_PICKER)) {
      String uriString =  call.argument("uriString");
      this.delegate.openNotificationTone(result, uriString);
    } else if(call.method.equals(METHOD_CALL_RETRIEVE)) {
      this.delegate.retrieveLostData(result);
    } else {
      result.notImplemented();
    }
  }
  

  private void tearDown() {
    this.activityBinding.removeActivityResultListener(this.delegate);
    this.activityBinding = null;
    if(this.observer != null) {
      this.lifecycle.removeObserver(this.observer);
      this.application.unregisterActivityLifecycleCallbacks(this.observer);
    }
    this.lifecycle = null;
    this.delegate.setEventHandler(null);
    this.delegate = null;
    this.channel.setMethodCallHandler(null);
    this.channel = null;
    this.application = null;
  }

  @Override
  public void onAttachedToEngine(final FlutterPluginBinding binding) {
    this.pluginBinding = binding;
  }

  @Override
  public void onDetachedFromEngine(final FlutterPluginBinding binding) {
    this.pluginBinding = null;
  }

  @Override
  public void onAttachedToActivity(final ActivityPluginBinding binding) {
    this.activityBinding = binding;
    this.setup(
            this.pluginBinding.getBinaryMessenger(),
            (Application) this.pluginBinding.getApplicationContext(),
            this.activityBinding.getActivity(),
            null,
            this.activityBinding);
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    this.onDetachedFromActivity();
  }

  @Override
  public void onReattachedToActivityForConfigChanges(final ActivityPluginBinding binding) {
    this.onAttachedToActivity(binding);
  }

  @Override
  public void onDetachedFromActivity() {
    this.tearDown();
  }
}
