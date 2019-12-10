package bu.cs591.mosso;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import bu.cs591.mosso.db.RunningRepo;
import bu.cs591.mosso.entity.CurrentUser;
import bu.cs591.mosso.entity.MarkerInfo;
import bu.cs591.mosso.entity.RunningParam;
import bu.cs591.mosso.lambda.LambdaClient;
import bu.cs591.mosso.lambda.RequestClass;
import bu.cs591.mosso.lambda.ResponseClass;
import bu.cs591.mosso.ui.map.MapViewModel;

import static java.text.DateFormat.getTimeInstance;

public class LocationUpdatesService extends Service {
    private static final String PACKAGE_NAME = "bu.cs591.mosso";

    private static final String TAG = LocationUpdatesService.class.getSimpleName();

    /**
     * The name of the channel for notifications.
     */
    private static final String CHANNEL_ID = "channel_01";

    public static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";

    public static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME +
            ".started_from_notification";

    private final IBinder mBinder = new LocalBinder();

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * The identifier for the notification displayed for the foreground service.
     */
    private static final int NOTIFICATION_ID = 12345678;

    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */
    private boolean mChangingConfiguration = false;

    private NotificationManager mNotificationManager;

    /**
     * Contains parameters used by {@link com.google.android.gms.location.FusedLocationProviderApi}.
     */
    private LocationRequest mLocationRequest;

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Callback for changes in location.
     */
    private LocationCallback mLocationCallback;

    private Handler mServiceHandler;

    /**
     * The current location.
     */
    private Location mLocation;

    // the singleton instance of Running Repo
    private RunningRepo myRepo;

    public LocationUpdatesService() {
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "on Create()");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };

        myRepo = RunningRepo.getInstance();
        createLocationRequest();
        getLastLocation();

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service started");
        boolean startedFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION,
                false);

        // We got here because the user decided to remove location updates from the notification.
        if (startedFromNotification) {
            removeLocationUpdates();
            stopSelf();
        }
        // Tells the system to not try to recreate the service after it has been killed.
        return START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) comes to the foreground
        // and binds with this service. The service should cease to be a foreground service
        // when that happens.
        Log.i(TAG, "in onBind()");
        stopForeground(true);
        mChangingConfiguration = false;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground
        // service when that happens.
        Log.i(TAG, "in onRebind()");
        stopForeground(true);
        mChangingConfiguration = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Last client unbound from service");

        // Called when the last client (MainActivity in case of this sample) unbinds from this
        // service. If this method is called due to a configuration change in MainActivity, we
        // do nothing. Otherwise, we make this service a foreground service.
        if (!mChangingConfiguration && Utils.requestingLocationUpdates(this)) {
            Log.i(TAG, "Starting foreground service");

            startForeground(NOTIFICATION_ID, getNotification());
        }
        return true; // Ensures onRebind() is called when a client re-binds.
    }

    @Override
    public void onDestroy() {
        mServiceHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Makes a request for location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    public void requestLocationUpdates() {
        Log.i(TAG, "Requesting location updates");
        Utils.setRequestingLocationUpdates(this, true);
        startService(new Intent(getApplicationContext(), LocationUpdatesService.class));
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper());
        } catch (SecurityException unlikely) {
            Utils.setRequestingLocationUpdates(this, false);
            Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
        }
    }

    /**
     * Removes location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    public void removeLocationUpdates() {
        Log.i(TAG, "Removing location updates");
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            Utils.setRequestingLocationUpdates(this, false);
            stopSelf();
        } catch (SecurityException unlikely) {
            Utils.setRequestingLocationUpdates(this, true);
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
    }

    /**
     * Returns the {@link NotificationCompat} used as part of the foreground service.
     */
    private Notification getNotification() {
        Intent intent = new Intent(this, LocationUpdatesService.class);

        CharSequence text = Utils.getLocationText(mLocation);

        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);

        // The PendingIntent that leads to a call to onStartCommand() in this service.
        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // The PendingIntent to launch activity.
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        // TO DO: add drawable icon for notification....
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .addAction(R.mipmap.ic_launcher,getString(R.string.launch_activity),
                        activityPendingIntent)
                .addAction(R.mipmap.ic_launcher, getString(R.string.stop_running),
                        servicePendingIntent)
                .setContentText(text)
                .setContentTitle(Utils.getLocationTitle(this))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(text)
                .setWhen(System.currentTimeMillis());

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }

        return builder.build();
    }

    private void getLastLocation() {
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                mLocation = task.getResult();
                                myRepo.addNewLocation(mLocation);
                            } else {
                                Log.w(TAG, "Failed to get location.");
                            }
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission." + unlikely);
        }
    }

    private void onNewLocation(Location location) {
        Log.i(TAG, "New location: " + location);

        final RunningParam runningParam = RunningParam.getInstance();
        runningParam.setCurrLocation(location);
        runningParam.setSelfPrevSteps(runningParam.getSelfCurSteps());
        accessGoogleFit();
        runningParam.setSelfCurSteps(FitData.getStep());
        runningParam.getSelfPoints().add(new LatLng(location.getLatitude(), location.getLongitude()));


        RequestClass requestClass = new RequestClass(CurrentUser.getInstance().getEmail(),
                                                    location.getLatitude() + "",
                                                    location.getLongitude() + "",
                                                    "1",
                                                    RunningParam.getInstance().getSelfPrevSteps() + "",
                                                    RunningParam.getInstance().getSelfCurSteps() + "",
                                                    CurrentUser.getInstance().getTeam());
        Log.d("testo", requestClass.toString());

        new AsyncTask<RequestClass, Void, ResponseClass>() {
            @Override
            protected ResponseClass doInBackground(RequestClass... params) {
                // invoke "echo" method. In case it fails, it will throw a
                // LambdaFunctionException.
                try {
                    return LambdaClient.getInstance().mossoLocationUpdation(params[0]);
                } catch (LambdaFunctionException lfe) {
                    Log.e("testo", "Failed to invoke echo", lfe);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(ResponseClass result) {
                Log.d("testo", "result: " + result.toString());
                Log.d("testo", "result: " + result.toString());
                Map<String, LatLng> locationsInfo = new HashMap();
                Map<String, Integer> stepsInfo = new HashMap();
                if (result.getBasicInfo() != "") {
                    for (String s : result.getBasicInfo().split(" ")) {
                        String[] totalInfo = s.split(":");
                        String email = totalInfo[0];
                        String[] detailInfo = totalInfo[1].split(",");
                        locationsInfo.put(email, new LatLng(Double.valueOf(detailInfo[0]), Double.valueOf(detailInfo[1])));
                        stepsInfo.put(email, Integer.valueOf(detailInfo[2]));
                    }
                }
                Map<String, MarkerInfo> markerInfo = runningParam.getMarkersInfo();
                for (Map.Entry<String, MarkerInfo> entry : markerInfo.entrySet()) {
                    if (locationsInfo.containsKey(entry.getKey())) {
                        entry.getValue().setLatLng(locationsInfo.get(entry.getKey()));
                        entry.getValue().setSteps(stepsInfo.get(entry.getKey()));
                        if (entry.getValue().getState() == -1) entry.getValue().setState(1);
                    } else {
                        entry.getValue().setState(-1);
                    }
                }
                String[] teamInfo = result.getAdditionalInfo().split(" ");
                runningParam.setRed(Integer.valueOf(teamInfo[0]));
                runningParam.setBlue(Integer.valueOf(teamInfo[1]));
                runningParam.setState(1);

                MapViewModel.getRunningParamMutableLiveData().setValue(runningParam);
            }
        }.execute(requestClass);



        // Notify anyone listening for broadcasts about the new location.
        // just for test, will be removed in the future
        Intent intent = new Intent(ACTION_BROADCAST);
        intent.putExtra(EXTRA_LOCATION, location);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

        // Update notification content if running as a foreground service.
        if (serviceIsRunningInForeground(this)) {
            mNotificationManager.notify(NOTIFICATION_ID, getNotification());
        }
    }

    /**
     * Sets the location request parameters.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public LocationUpdatesService getService() {
            return LocationUpdatesService.this;
        }
    }

    /**
     * Returns true if this is a foreground service.
     *
     * @param context The {@link Context}.
     */
    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }



    public void accessGoogleFit() {


        final String TAG_F = "ServiceFit";
        //sensorStep = FitData.getSensorStep();
        //sensorCalory = FitData.getSensorCalory();

        System.out.println("access");
        //txtFit.setText("access");
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        //cal.add(Calendar.YEAR, -1);
        long startTime = cal.getTimeInMillis();

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

        DateFormat dateFormat = getTimeInstance(DateFormat.LONG);


        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                //.aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .bucketByTime(1, TimeUnit.DAYS)
                .build();



        //Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
        Fitness.getHistoryClient(getApplicationContext(), GoogleSignIn.getLastSignedInAccount(getApplicationContext()))
                .readData(readRequest)
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {

                        String TAG_F = "UtilFit";
                        int totalStep = 0;//read from fit api every time
                        int caloryToday = 0;

                        Log.d(TAG_F, "fit onSuccess in service()");
//                        //txtFit.setText("Sucessfully got fit data\n");
//                        Log.d(TAG_F, "onSuccess: 2 " + dataReadResponse.toString());
//                        Log.d(TAG_F, "onSuccess: 2 " + dataReadResponse.getStatus());
//                        Log.d(TAG_F, "onSuccess: 2calotry " + dataReadResponse.getDataSet(DataType.TYPE_STEP_COUNT_DELTA));
//                        Log.d(TAG_F, "onSuccess: 2step " + dataReadResponse.getDataSet(DataType.TYPE_STEP_COUNT_DELTA).getDataPoints());
//                        //Log.d("TAG_F", "onSuccess: step " + dataReadResponse.getDataSet(DataType.TYPE_STEP_COUNT_DELTA).getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt());
//                        Log.d(TAG_F, "onSuccess: 2 " + dataReadResponse.getBuckets().get(0));
//                        Log.d(TAG_F, "onSuccess: 2 " + dataReadResponse.getBuckets().get(0).getDataSets().size());


                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

                        for (Bucket bucket : dataReadResponse.getBuckets()) {
                            List<DataSet> dataSets = bucket.getDataSets();
                            for (DataSet dataSet : dataSets) {
                                DateFormat dateFormat = getTimeInstance(DateFormat.LONG);

                                for (DataPoint dp : dataSet.getDataPoints()) {
                                    Log.d(TAG_F, "Data point:");
                                    Log.d(TAG_F, "\tType: " + dp.getDataType().getName());
                                    Log.d(TAG_F, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)) + " " + formatter.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
                                    Log.d(TAG_F, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)) + " " + formatter.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
                                    for (Field field : dp.getDataType().getFields()) {
                                        Log.i(TAG_F, "\tField: " + field.getName() + " Value: " + dp.getValue(field));
                                        //txtFit.setText("step:" + dp.getValue(field));
                                        totalStep += dp.getValue(field).asInt();
                                        FitData.setFitStep(totalStep);

                                    }
                                }
                            }
                        }

                        Log.i(TAG_F, "Total step is " + String.valueOf(totalStep));
                        //setTotalSteps();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG_F, "onFailure()", e);
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<DataReadResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<DataReadResponse> task) {
                        Log.d(TAG_F, "onComplete()");
                    }
                });






    }
}
