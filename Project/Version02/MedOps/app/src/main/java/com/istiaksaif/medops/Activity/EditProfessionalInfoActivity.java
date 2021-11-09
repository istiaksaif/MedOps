package com.istiaksaif.medops.Activity;

import static com.istiaksaif.medops.Utils.DayListHelper.optionDaysList;
import static com.istiaksaif.medops.Utils.TimeListHelper.optionTimeList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.istiaksaif.medops.Adapter.DayAdapter;
import com.istiaksaif.medops.Adapter.TimeAdapter;
import com.istiaksaif.medops.Model.DaySelectModel;
import com.istiaksaif.medops.Model.TimeModel;
import com.istiaksaif.medops.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class EditProfessionalInfoActivity extends AppCompatActivity {

    private TextInputEditText BMDCId,Designation,workingIn,workingExperience,consultFee,Degrees;
    private MaterialAutoCompleteTextView bloodGroup;
    private TextView startTimeStore,endTimeStore;
    private Button nextButton;
    private String date;
    private Toolbar toolBar;

    private RecyclerView timeRecyclerView,timeRecyclerView1,dayRecyclerView;
    private TimeAdapter timeAdapter,timeAdapter1;
    private List<TimeModel> timeModelList;

    private ArrayList<DaySelectModel> dayModelList;
    private DayAdapter dayAdapter;


    private DatabaseReference databaseReference;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user.getUid();
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_professional_info);
        toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.leftarrow);
        toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        progressDialog = new ProgressDialog(this);
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        Designation = findViewById(R.id.designation);
        workingExperience = findViewById(R.id.workingExperience);
        workingExperience.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int nYear = calendar.get(Calendar.YEAR);
                int nMonth = calendar.get(Calendar.MONTH);
                int nDay = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(v.getContext(), android.R.style.Theme_Holo_Light_Dialog_MinWidth, datepickerListener, nYear, nMonth, nDay);
                datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                datePickerDialog.show();
            }
        });
        BMDCId = findViewById(R.id.bmdcid);
        workingIn = findViewById(R.id.workingin);
        consultFee = findViewById(R.id.consultfee);
        Degrees = findViewById(R.id.degrees);
//        bloodGroup = findViewById(R.id.bloodgroup);
//        TextInputLayout textInputLayoutblood = findViewById(R.id.bloodgrouplayout);
//        String []optionUniName = {"A+","A-","B+","B-","AB+"
//                ,"AB-","O+","O-"};
//        ArrayAdapter<String> arrayAdapterUni = new ArrayAdapter<>(this,R.layout.usertype_item,optionUniName);
//        ((MaterialAutoCompleteTextView) textInputLayoutblood.getEditText()).setAdapter(arrayAdapterUni);
        startTimeStore = findViewById(R.id.starttimestore);
        endTimeStore = findViewById(R.id.endtimestore);

        timeRecyclerView = findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(EditProfessionalInfoActivity.this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        timeRecyclerView.setLayoutManager(layoutManager);
        timeRecyclerView1 = findViewById(R.id.endrecyclerview);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(EditProfessionalInfoActivity.this);
        layoutManager1.setOrientation(LinearLayoutManager.HORIZONTAL);
        timeRecyclerView1.setLayoutManager(layoutManager1);

        timeModelList = new ArrayList<>();
        for(int i=0;i<=47;i++){
            TimeModel time = new TimeModel(optionTimeList[i]);
            timeModelList.add(time);
        }
        timeAdapter = new TimeAdapter(EditProfessionalInfoActivity.this,timeModelList);
        timeRecyclerView.setAdapter(timeAdapter);
        timeAdapter.notifyDataSetChanged();
        timeAdapter1 = new TimeAdapter(EditProfessionalInfoActivity.this,timeModelList);
        timeRecyclerView1.setAdapter(timeAdapter1);
        timeAdapter1.notifyDataSetChanged();
        timeAdapter.setOnItemClickListner(new TimeAdapter.onItemClickListner() {
            @Override
            public void onClick(String str) {
                startTimeStore.setText(str);
            }
        });
        timeAdapter1.setOnItemClickListner(new TimeAdapter.onItemClickListner() {
            @Override
            public void onClick(String str) {
                endTimeStore.setText(str);
            }
        });

        dayRecyclerView = findViewById(R.id.daysrecyclerview);
        LinearLayoutManager layoutManagerDay = new LinearLayoutManager(EditProfessionalInfoActivity.this);
        layoutManagerDay.setOrientation(LinearLayoutManager.HORIZONTAL);
        dayRecyclerView.setLayoutManager(layoutManagerDay);

        dayModelList = new ArrayList<>();
        for(int i=0;i<7;i++){
            DaySelectModel day = new DaySelectModel(optionDaysList[i]);
            dayModelList.add(day);
        }
        dayAdapter = new DayAdapter(EditProfessionalInfoActivity.this,dayModelList);
        dayRecyclerView.setAdapter(dayAdapter);

        nextButton = findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < dayAdapter.getSelected().size(); i++) {
                    stringBuilder.append(dayAdapter.getSelected().get(i).getText());
                    stringBuilder.append(", ");
                }
                showToast(stringBuilder.toString().trim());
                Info(stringBuilder.toString().trim());
            }
        });
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void Info(String msg) {
        String DESIGNATION = Designation.getText().toString();
        String WORKING_EXPERIENCE = workingExperience.getText().toString();
        String WORKING_IN = workingIn.getText().toString();
        String BMDCID = BMDCId.getText().toString();
        String CONSULT_FEE = consultFee.getText().toString();
        String Dr_Degrees = Degrees.getText().toString();

        if (TextUtils.isEmpty(DESIGNATION)){
            Toast.makeText(EditProfessionalInfoActivity.this, "please enter your Designation", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (TextUtils.isEmpty(WORKING_IN)){
            Toast.makeText(EditProfessionalInfoActivity.this, "please enter workingIn", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();
        HashMap<String, Object> result = new HashMap<>();
        result.put("designation", DESIGNATION);
        result.put("bmdcID", BMDCID);
        result.put("workingIn", WORKING_IN);
        result.put("workingExperience", WORKING_EXPERIENCE);
        result.put("consultFee", CONSULT_FEE);
        result.put("consultHour", startTimeStore.getText().toString());
        result.put("consultHourTo", endTimeStore.getText().toString());
        result.put("consultDays", msg);
        result.put("degrees", Dr_Degrees);

        databaseReference.child(uid).updateChildren(result)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(EditProfessionalInfoActivity.this, "Error ", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }
    private DatePickerDialog.OnDateSetListener datepickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR,year);
            calendar.set(Calendar.MONTH,month);
            calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
            month = month+1;
            date = dayOfMonth+"/"+month+"/"+year;
            workingExperience.setText(date);
        }
    };
}