package com.example.tryfit;

import java.util.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInOptionsExtension;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DailyTotalResult;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.tasks.Tasks;

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
    static final String TAG = "Tag";

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
                //.aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
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
                        Log.d("TAG_F", "onSuccess: 2 " + dataReadResponse.toString());
                        Log.d("TAG_F", "onSuccess: 2 " + dataReadResponse.getStatus());
                        Log.d("TAG_F", "onSuccess: 2calotry " + dataReadResponse.getDataSet(DataType.TYPE_STEP_COUNT_DELTA));
                        Log.d("TAG_F", "onSuccess: 2step " + dataReadResponse.getDataSet(DataType.TYPE_STEP_COUNT_DELTA).getDataPoints());
                        //Log.d("TAG_F", "onSuccess: step " + dataReadResponse.getDataSet(DataType.TYPE_STEP_COUNT_DELTA).getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt());
                        Log.d("TAG_F", "onSuccess: 2 " + dataReadResponse.getBuckets().get(0));
                        Log.d("TAG_F", "onSuccess: 2 " + dataReadResponse.getBuckets().get(0).getDataSets().size());

                        for (Bucket bucket : dataReadResponse.getBuckets()) {
                            List<DataSet> dataSets = bucket.getDataSets();
                            for (DataSet dataSet : dataSets) {
                                DateFormat dateFormat = getTimeInstance();

                                for (DataPoint dp : dataSet.getDataPoints()) {
                                    Log.d("TAG_F", "Data point:");
                                    Log.d("TAG_F", "\tType: " + dp.getDataType().getName());
                                    Log.d("TAG_F", "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
                                    Log.d("TAG_F", "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
                                    for (Field field : dp.getDataType().getFields()) {
                                        Log.i("TAG_F", "\tField: " + field.getName() + " Value: " + dp.getValue(field));
                                        txtFit.setText("step:" + dp.getValue(field));
                                    }
                                }
                            }
                        }

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
        read3();
        readHistoryData();
        //read4();
    }

    private void read3() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
//                .read(DataType.TYPE_STEP_COUNT_DELTA)
                //.aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .bucketByTime(8, TimeUnit.DAYS)
                .enableServerQueries()
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        Fitness.getHistoryClient(
                this,
                GoogleSignIn.getLastSignedInAccount(this))
                .readData(readRequest)
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        Log.d("TAG_F", "onSuccess: 1 " + dataReadResponse.toString());
                        Log.d("TAG_F", "onSuccess: 1 " + dataReadResponse.getStatus());
                        Log.d("TAG_F", "onSuccess: calotry " + dataReadResponse.getDataSet(DataType.TYPE_STEP_COUNT_DELTA));
                        Log.d("TAG_F", "onSuccess: step " + dataReadResponse.getDataSet(DataType.TYPE_STEP_COUNT_DELTA).getDataPoints());
                        //Log.d("TAG_F", "onSuccess: step " + dataReadResponse.getDataSet(DataType.TYPE_STEP_COUNT_DELTA).getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt());
                        Log.d("TAG_F", "onSuccess: 1 " + dataReadResponse.getBuckets().get(0));
                        Log.d("TAG_F", "onSuccess: 1 " + dataReadResponse.getBuckets().get(0).getDataSets().size());




                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("TAG_F", "onFailure: 1 " + e.getMessage());
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<DataReadResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<DataReadResponse> task) {
                        Log.d("TAG_F", "onComplete: 1 ");
                    }
                });
    }









    private Task<DataReadResponse> readHistoryData() {
        // Begin by creating the query.
        DataReadRequest readRequest = queryFitnessData();

        // Invoke the History API to fetch the data with the query
        return Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readData(readRequest)
                .addOnSuccessListener(
                        new OnSuccessListener<DataReadResponse>() {
                            @Override
                            public void onSuccess(DataReadResponse dataReadResponse) {
                                // For the sake of the sample, we'll print the data so we can see what we just
                                // added. In general, logging fitness information should be avoided for privacy
                                // reasons.
                                printData(dataReadResponse);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "There was a problem reading the data.", e);
                            }
                        });
    }


    public static DataReadRequest queryFitnessData() {
        // [START build_read_data_request]
        // Setting a start and end date using a range of 1 week before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        java.text.DateFormat dateFormat = getDateInstance();
        Log.i(TAG, "Range Start: " + dateFormat.format(startTime));
        Log.i(TAG, "Range End: " + dateFormat.format(endTime));

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
        // [END build_read_data_request]

        return readRequest;
    }

    /**
     * Logs a record of the query result. It's possible to get more constrained data sets by
     * specifying a data source or data type, but for demonstrative purposes here's how one would dump
     * all the data. In this sample, logging also prints to the device screen, so we can see what the
     * query returns, but your app should not log fitness information as a privacy consideration. A
     * better option would be to dump the data you receive to a local data directory to avoid exposing
     * it to other applications.
     */
    public static void printData(DataReadResponse dataReadResult) {
        // [START parse_read_data_result]
        // If the DataReadRequest object specified aggregated data, dataReadResult will be returned
        // as buckets containing DataSets, instead of just DataSets.
        if (dataReadResult.getBuckets().size() > 0) {
            Log.i(
                    TAG, "Number of returned buckets of DataSets is: " + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    dumpDataSet(dataSet);
                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            Log.i(TAG, "Number of returned DataSets is: " + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                dumpDataSet(dataSet);
            }
        }
        // [END parse_read_data_result]
    }

    // [START parse_dataset]
    private static void dumpDataSet(DataSet dataSet) {
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
        }
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

//    private void dumpDataSet(DataSet dataSet) {
//        String TAG = "dumpData";
//        Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
//        DateFormat dateFormat = getTimeInstance();
//
//        for (DataPoint dp : dataSet.getDataPoints()) {
//            Log.i(TAG, "Data point:");
//            Log.i(TAG, "\tType: " + dp.getDataType().getName());
//            Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
//            Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
//            for (Field field : dp.getDataType().getFields()) {
//                Log.i(TAG, "\tField: " + field.getName() + " Value: " + dp.getValue(field));
//            }
//            txtFit.setText("\tType: " + dp.getDataType().getName());
//            System.out.println("Data point:\tType: " + dp.getDataType().getName());
//        }
//    }

//    protected void read4() {
//
//        long total = 0;
//
//        PendingResult<DailyTotalResult> result = Fitness.HistoryApi.readDailyTotal(mClient, DataType.TYPE_STEP_COUNT_DELTA);
//        DailyTotalResult totalResult = result.await(30, TimeUnit.SECONDS);
//        if (totalResult.getStatus().isSuccess()) {
//            DataSet totalSet = totalResult.getTotal();
//            total = totalSet.isEmpty()
//                    ? 0
//                    : totalSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
//        } else {
//            Log.w(TAG, "There was a problem getting the step count.");
//        }
//
//        Log.i(TAG, "Total steps: " + total);
//
//        return;
//    }










//    public void read2() {
//
//        Calendar cal = Calendar.getInstance();
//        Date now = new Date();
//        cal.setTime(now);
//        long endTime = cal.getTimeInMillis();
//        cal.add(Calendar.WEEK_OF_YEAR, -1);
//        long startTime = cal.getTimeInMillis();
//        GoogleSignInOptionsExtension fitnessOptions =
//                FitnessOptions.builder()
//                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
//                        .build();
//
//        GoogleSignInAccount googleSignInAccount =
//                GoogleSignIn.getAccountForExtension(this, fitnessOptions);
//
//        Task<DataReadResponse> response = Fitness.getHistoryClient(this, googleSignInAccount)
//                .readData(new DataReadRequest.Builder()
//                        .read(DataType.TYPE_STEP_COUNT_DELTA)
//                        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
//                        .build());
//
//        DataReadResult readDataResult = Tasks.await(response);
//        DataSet dataSet = readDataResult.getDataSet(DataType.TYPE_STEP_COUNT_DELTA);
//
//    }

//    public void subscribe() {
//        // To create a subscription, invoke the Recording API. As soon as the subscription is
//        // active, fitness data will start recording.
//
//        Fitness.RecordingApi.subscribe(mClient, DataType.TYPE_STEP_COUNT_CUMULATIVE)
//                .setResultCallback(new ResultCallback<Status>() {
//                    @Override
//                    public void onResult(Status status) {
//                        String TAG1 = "subscrib";
//                        if (status.isSuccess()) {
//                            if (status.getStatusCode()
//                                    == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
//                                Log.i(TAG1, "Existing subscription for activity detected.");
//                            } else {
//                                Log.i(TAG1, "Successfully subscribed!");
//                            }
//                        } else {
//                            Log.w(TAG1, "There was a problem subscribing.");
//                        }
//                    }
//                });
//    }
//
//    private class VerifyDataTask extends AsyncTask<Void, Void, Void> {
//        protected Void doInBackground(Void... params) {
//
//            long total = 0;
//
//            PendingResult<DailyTotalResult> result = Fitness.HistoryApi.readDailyTotal(mClient, DataType.TYPE_STEP_COUNT_DELTA);
//            DailyTotalResult totalResult = result.await(30, TimeUnit.SECONDS);
//            if (totalResult.getStatus().isSuccess()) {
//                DataSet totalSet = totalResult.getTotal();
//                total = totalSet.isEmpty()
//                        ? 0
//                        : totalSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
//            } else {
//                Log.w(TAG, "There was a problem getting the step count.");
//            }
//
//            Log.i(TAG, "Total steps: " + total);
//
//            return null;
//        }
//    }


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


