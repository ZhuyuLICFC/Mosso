package bu.cs591.mosso.ui.statistic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.renderscript.Element;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;


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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.txusballesteros.widgets.FitChart;

import java.text.SimpleDateFormat;
import java.util.*;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getDateInstance;
import static java.text.DateFormat.getTimeInstance;


import bu.cs591.mosso.R;

public class StatisticFragment extends Fragment {

    private StatisticViewModel statisticViewModel;
    TextView totalSteps;

    //get fitness data option
    FitnessOptions fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .build();
    int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 11;
    String LOG_TAG = "fit";
    String TAG_F = "GoogleFitData";

    public int totalStep;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        statisticViewModel =
                ViewModelProviders.of(this).get(StatisticViewModel.class);
        View root = inflater.inflate(R.layout.fragment_statistic, container, false);

//        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
//
//            GoogleSignIn.requestPermissions(
//                    this, // your activity
//                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
//                    GoogleSignIn.getLastSignedInAccount(this),
//                    fitnessOptions);
//        } else {
//            accessGoogleFit();
//            //readData();
//        }
        totalStep = 0;
        totalSteps = (TextView) root.findViewById(R.id.totalSteps);
        accessGoogleFit();


        return root;
    }

    //step2 : on result. access google fit
   // @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        System.out.println("onresult");
//        //accessGoogleFit();
//
//        //readData();
//        if (resultCode == Activity.RESULT_OK) {
//            if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
//
//                accessGoogleFit();
//
//                //readData();
//            }
//        }
//    }

    //step 3 : access the google fit app data
    public void accessGoogleFit() {
        System.out.println("access");
        //txtFit.setText("access");
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.MONTH, -1);
        //cal.add(Calendar.YEAR, -1);
        long startTime = cal.getTimeInMillis();

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

        DateFormat dateFormat = getTimeInstance(DateFormat.LONG);
        Log.d(TAG_F, "start Time " + startTime + " " + dateFormat.format(startTime) + " " + formatter.format(startTime));
        Log.d(TAG_F, "end time " + endTime + " " + dateFormat.format(endTime) + " " + formatter.format(endTime));


        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                //.aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .bucketByTime(1, TimeUnit.DAYS)
                .build();



       //Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
        Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .readData(readRequest)
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        Log.d(LOG_TAG, "onSuccess()");
                        //txtFit.setText("Sucessfully got fit data\n");
                        Log.d(TAG_F, "onSuccess: 2 " + dataReadResponse.toString());
                        Log.d(TAG_F, "onSuccess: 2 " + dataReadResponse.getStatus());
                        Log.d(TAG_F, "onSuccess: 2calotry " + dataReadResponse.getDataSet(DataType.TYPE_STEP_COUNT_DELTA));
                        Log.d(TAG_F, "onSuccess: 2step " + dataReadResponse.getDataSet(DataType.TYPE_STEP_COUNT_DELTA).getDataPoints());
                        //Log.d("TAG_F", "onSuccess: step " + dataReadResponse.getDataSet(DataType.TYPE_STEP_COUNT_DELTA).getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt());
                        Log.d(TAG_F, "onSuccess: 2 " + dataReadResponse.getBuckets().get(0));
                        Log.d(TAG_F, "onSuccess: 2 " + dataReadResponse.getBuckets().get(0).getDataSets().size());


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

                                    }
                                }
                            }
                        }

                        Log.i(TAG_F, "Total step is " + String.valueOf(totalStep));
                        setTotalSteps();

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

    private void setTotalSteps(){
        Toast.makeText(getActivity(),"Successfully get fit data", Toast.LENGTH_LONG ).show();
        totalSteps.setText(totalStep + " steps today");
        final FitChart fitChart = (FitChart)getActivity().findViewById(R.id.fitChart);
        fitChart.setMinValue(0f);
        fitChart.setMaxValue(10000f);
        fitChart.setValue((float)totalStep);
    }
}