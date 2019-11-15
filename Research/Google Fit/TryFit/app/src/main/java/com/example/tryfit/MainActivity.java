package com.example.tryfit;

import java.util.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DailyTotalResult;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

//import com.google.android.gms.drive.Drive;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getDateInstance;
import static java.text.DateFormat.getTimeInstance;

/*
* This is an app trying to obtain the data from google fit
* Using Google Fit API
*
* Some difficulties / achievements:
* 1. Google Fit API cannot be used directly
* Needs to setup a Google API console, apply for a OAuth CLIENT ID
* Then setup credential (Needs generate the SHA key of this app)
* Then setup an consent screen
* Then wait for google to approve
* Then can use it
*
*
* 2. After that, we successfully obtained a permission
* And get google account's information
* And successfully obtained the google fit data from
* The Google fit app in the phone, using the google account the user selects
*
* 3. Our next step would be study how to dispatch the data, and get the useful information out of it
*
*
* */


public class MainActivity extends AppCompatActivity {

    //get fitness data option
    FitnessOptions fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .build();

    int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 11;
    String LOG_TAG = "fit";

    TextView txtFit;
    GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.out.println("aapp");

        txtFit = (TextView) findViewById(R.id.txtFit);


//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .enableAutoManage(this /* FragmentActivity */,
//                        this /* OnConnectionFailedListener */)
//                .addApi(Drive.API)
//                .addScope(Drive.SCOPE_FILE)
//                .build();


        //Step 1
        //request for google signin permission
        //to get google account's fit data
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {

            GoogleSignIn.requestPermissions(
                    this, // your activity
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions);
            txtFit.setText("aaa");
        } else {
            accessGoogleFit();
            //readData();
        }

        //accessGoogleFit();
        //readData();


    }


    //step2 : on result. access google fit
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("onresult");
        //accessGoogleFit();

        //readData();
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {

                accessGoogleFit();

                //readData();
            }
        }
    }

    //step 3 : access the google fit app data
    private void accessGoogleFit() {
        System.out.println("access");
        txtFit.setText("access");
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.YEAR, -1);
        long startTime = cal.getTimeInMillis();


        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .bucketByTime(1, TimeUnit.DAYS)
                .build();



        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readData(readRequest)
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        Log.d(LOG_TAG, "onSuccess()");
                        txtFit.setText("Sucessfully got fit data\n");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(LOG_TAG, "onFailure()", e);
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<DataReadResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<DataReadResponse> task) {
                        Log.d(LOG_TAG, "onComplete()");
                    }
                });
    }

    private void readData() {
        System.out.println("readdata");
        txtFit.setText("readdata");
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        java.text.DateFormat dateFormat = getDateInstance();
        //Log.i(TAG, "Range Start: " + dateFormat.format(startTime));
        //Log.i(TAG, "Range End: " + dateFormat.format(endTime));

        DataReadRequest readRequest =
                new DataReadRequest.Builder()
                        // The data request can specify multiple data types to return, effectively
                        // combining multiple data queries into one call.
                        // In this example, it's very unlikely that the request is for several hundred
                        // datapoints each consisting of a few steps and a timestamp.  The more likely
                        // scenario is wanting to see how many steps were walked per day, for 7 days.
                        .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                        // Analogous to a "Group By" in SQL, defines how data should be aggregated.
                        // bucketByTime allows for a time span, whereas bucketBySession would allow
                        // bucketing by "sessions", which would need to be defined in code.
                        .bucketByTime(1, TimeUnit.DAYS)
                        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                        .build();

        Task<DataReadResponse> response = Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this)).readData(readRequest);
        List<DataSet> dataSets = response.getResult().getDataSets();
        System.out.println(dataSets.size());
        txtFit.setText(dataSets.size());
        dumpDataSet(dataSets.get(0));
    }

    private void dumpDataSet(DataSet dataSet) {
        String TAG = "dumpData";
        Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = getTimeInstance();

        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.i(TAG, "Data point:");
            Log.i(TAG, "\tType: " + dp.getDataType().getName());
            Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for (Field field : dp.getDataType().getFields()) {
                Log.i(TAG, "\tField: " + field.getName() + " Value: " + dp.getValue(field));
            }
            txtFit.setText("\tType: " + dp.getDataType().getName());
            System.out.println("Data point:\tType: " + dp.getDataType().getName());
        }
    }



    //saved template codes
//
//    private void displayStepDataForToday() {
//        DailyTotalResult result = Fitness.HistoryApi.readDailyTotal(mGoogleApiClient, DataType.TYPE_STEP_COUNT_DELTA ).await(1, TimeUnit.MINUTES);
//        //DailyTotalResult result = Fitness.HistoryApi.readDailyTotal(GoogleSignIn.getLastSignedInAccount(this), DataType.TYPE_STEP_COUNT_DELTA ).await(1, TimeUnit.MINUTES);
//        //DailyTotalResult result = Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this)).readData(readRequest);
//        showDataSet(result.getTotal());
//    }
//
//    private void showDataSet(DataSet dataSet) {
//        DateFormat dateFormat = getDateInstance();
//        DateFormat timeFormat = getTimeInstance();
//        for (DataPoint dp : dataSet.getDataPoints()) {
//            Log.e("History", "Data point:");
//            Log.e("History", "\tType: " + dp.getDataType().getName());
//            txtFit.setText("\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
//            Log.e("History", "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
//            for(Field field : dp.getDataType().getFields()) {
//                Log.e("History", "\tField: " + field.getName() +
//                        " Value: " + dp.getValue(field));
//            }
//        }
//    }



}

