package bu.cs591.mosso.ui.history;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import bu.cs591.mosso.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import bu.cs591.mosso.entity.CurrentUser;
import bu.cs591.mosso.entity.RunningRecord;
import bu.cs591.mosso.utils.DateHelper;

public class HistoryFragment extends Fragment {

    private HistoryViewModel historyViewModel;
    private RecyclerView recyclerView;
    private static final String TAG = "testo";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        historyViewModel =
                ViewModelProviders.of(this).get(HistoryViewModel.class);


        Log.d(TAG, CurrentUser.getInstance().toString());

        View root = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = root.findViewById(R.id.history_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager((getActivity().getApplicationContext())));

        historyViewModel.getLiveUsers().observe(this, new Observer<List<RunningRecord>>() {
            @Override
            public void onChanged(List<RunningRecord> runningRecords) {
                recyclerView.setAdapter(new RunningRecordAdapter(runningRecords));
            }
        });

        return root;
    }

    private class RecordHolder extends RecyclerView.ViewHolder{

        //ImageView imgEpisode;
        TextView tvDuration;
        TextView tvDate;
        TextView tvWeekNo;
        TextView tvDistance;
        TextView tvSpeed;

        public RecordHolder(View itemView) {
            super(itemView);
            //imgEpisode = itemView.findViewById(R.id.record);
            tvDate = itemView.findViewById(R.id.tvHistoryTitle);
            tvDuration = itemView.findViewById(R.id.tvHistoryDuration);
            tvSpeed = itemView.findViewById(R.id.tvHistorySpeed);
            tvWeekNo = itemView.findViewById(R.id.tvHistorySubtitle);
            tvDistance = itemView.findViewById(R.id.tvHistoryDistance);
        }

        public void bindRecord(final RunningRecord runningRecord) {
            //imgEpisode.setImageBitmap(runningRecord.getRunningRoute());
            tvDate.setText(DateHelper.getDateInfo(runningRecord.getDate()));
            tvWeekNo.setText(runningRecord.getDayInWeek());
            tvDistance.setText(runningRecord.getDistance());
            tvSpeed.setText(runningRecord.getSpeed());
            tvDuration.setText(runningRecord.getDuration());
        }
    }

    private class RunningRecordAdapter extends RecyclerView.Adapter<RecordHolder> {

        private List<RunningRecord> runningRecords;

        public RunningRecordAdapter(List<RunningRecord> runningRecords) {
            super();
            this.runningRecords = runningRecords;
        }

        @NonNull
        @Override
        public RecordHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity().getApplicationContext());
            View view = layoutInflater.inflate(R.layout.row_history_item, viewGroup , false);
            return new RecordHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecordHolder recordHolder, int i) {
            recordHolder.bindRecord(runningRecords.get(i));
        }

        @Override
        public int getItemCount() {
            return runningRecords.size();
        }
    }
}