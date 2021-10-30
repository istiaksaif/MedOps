package com.istiaksaif.medops.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;


import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.istiaksaif.medops.R;
import com.istiaksaif.medops.Utils.AgeCalculator;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class AppointmentDoctorActivity extends AppCompatActivity{

    private Toolbar toolbar;
    private TextView drName,drNickName,drStudies,drWork,drExperience;
    private ImageView drImage;
    private TextView date,hour,min,available_status,sufficient_balance,consultFee;
    private LinearLayout appoinButton,videoCallButton,confirmButton;

    private DatabaseReference databaseReference;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user.getUid();
    private Intent intent;
    private String doctorId,dateStr,time;
    private AgeCalculator age = null;
    private NumberPicker hourPicker,minPicker;
    private LottieAnimationView cross;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor);

        intent = getIntent();
        doctorId = intent.getStringExtra("doctorId");
        databaseReference = FirebaseDatabase.getInstance().getReference();

        toolbar = findViewById(R.id.drtoolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.leftarrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        drImage = findViewById(R.id.drimage);
        drName = findViewById(R.id.name);
        drNickName = findViewById(R.id.nickname);
        drStudies = findViewById(R.id.studies);
        drExperience = findViewById(R.id.post);
        drWork = findViewById(R.id.workhospital);
        videoCallButton = findViewById(R.id.videocallbtn);
        sufficient_balance = findViewById(R.id.sufficient_balance);
        consultFee = findViewById(R.id.feeamount);
        GetDataFromFirebase();
        databaseReference.child("users").orderByChild("userId").equalTo(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            try {
                                String s = snapshot.child("balanceTk").getValue().toString();
                                if(Integer.parseInt(s)>=Integer.parseInt(consultFee.getText().toString())){
                                    sufficient_balance.setVisibility(View.GONE);
                                }else {
                                    sufficient_balance.setVisibility(View.VISIBLE);
                                    appoinButton.setClickable(false);
                                    appoinButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.rectangle_33));
                                }
                            }catch (Exception e){

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        appoinButton = findViewById(R.id.takeapponbtn);
        databaseReference.child("users").child(doctorId).child("appointment").orderByChild("userId").equalTo(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            try {
                                String s = snapshot.child("status").getValue().toString();
                                if(s.equals("confirm")){
                                    appoinButton.setVisibility(View.GONE);
                                    videoCallButton.setVisibility(View.VISIBLE);
                                }
                            }catch (Exception e){

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        appoinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(AppointmentDoctorActivity.this);
                bottomSheetDialog.setContentView(R.layout.appoinment_popup);
                bottomSheetDialog.setCanceledOnTouchOutside(false);
                age=new AgeCalculator();
                date = bottomSheetDialog.findViewById(R.id.date);
                available_status = bottomSheetDialog.findViewById(R.id.available_status);
                date.setText(age.getCurrentDateOfMonthName()+"  ");
                date.setOnClickListener(new View.OnClickListener() {
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

                hour = bottomSheetDialog.findViewById(R.id.hourstore);
                min = bottomSheetDialog.findViewById(R.id.minstore);
                hourPicker = bottomSheetDialog.findViewById(R.id.hourpicker);
                minPicker = bottomSheetDialog.findViewById(R.id.minpicker);
                hourPicker.setMinValue(00);
                hourPicker.setMaxValue(23);
                minPicker.setMinValue(00);
                minPicker.setMaxValue(59);
                hourPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                        hour.setText(String.valueOf(i1));
                    }
                });
                minPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                        min.setText(String.valueOf(i1));
                        time = hour.getText().toString()+":"+min.getText().toString();
                        int minsToAdd = 10;
                        Date date = new Date();
                        date.setTime((((Integer.parseInt(time.split(":")[0]))*60 + (Integer.parseInt(time.split(":")[1])))+ date.getTimezoneOffset())*60000);
                        date.setTime(date.getTime()+ minsToAdd *60000);
                        String b = date.getHours() + ":"+date.getMinutes();
//                        date.setTime(date.getTime()- minsToAdd *60000);
//                        String a = date.getHours() + ":"+date.getMinutes();
                        databaseReference.child("users").child(doctorId).child("appointment")
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            try {
                                                String s = snapshot.child("time").getValue().toString();
                                                if(s.equals(time)){
                                                    available_status.setText(R.string.timeUnavailable);
                                                    confirmButton.setClickable(false);
                                                    confirmButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.rectangle_confirm1));
                                                }
                                                else{
                                                    available_status.setText(R.string.idealTime);
                                                    confirmButton.setClickable(true);
                                                    confirmButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.rectangle_confirm));
                                                }
                                            }catch (Exception e){

                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }
                });
                confirmButton = bottomSheetDialog.findViewById(R.id.confirm_appoin_button);
                confirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String q = databaseReference.push().getKey();
                        HashMap<String, Object> result = new HashMap<>();
                        result.put("time", time);
                        result.put("date", dateStr);
                        result.put("appointmentId", q);
                        result.put("userId", uid);
                        result.put("status", "confirm");
                        databaseReference.child("users").child(doctorId).child("appointment").child(q).updateChildren(result);
                        appoinButton.setVisibility(View.GONE);
                        videoCallButton.setVisibility(View.VISIBLE);
                        bottomSheetDialog.dismiss();
                    }
                });
                cross = (LottieAnimationView) bottomSheetDialog.findViewById(R.id.cross);
                cross.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                    }
                });
                bottomSheetDialog.show();
            }
        });
        videoCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AppointmentDoctorActivity.this,OutGoingActivity.class);
                intent.putExtra("doctorId",doctorId);
                intent.putExtra("type","video");
                startActivity(intent);
            }
        });
    }
    private void GetDataFromFirebase() {
        Query query = databaseReference.child("users").orderByChild("doctorId").equalTo(doctorId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        drName.setText(snapshot.child("doctorName").getValue().toString());
                        consultFee.setText(snapshot.child("consultFee").getValue().toString());
                        String Image = snapshot.child("image").getValue().toString();
                        try {
                            Picasso.get().load(Image).into(drImage);
                        }catch (Exception e){
                            Picasso.get().load(R.drawable.dropdown).into(drImage);
                        }
                        //doctorItem.setStatus(snapshot.child("status").getValue().toString());
                    } catch (Exception e) {

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private DatePickerDialog.OnDateSetListener datepickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            Calendar calendar = Calendar.getInstance();
            String monthname;
            calendar.set(Calendar.YEAR,year);
            calendar.set(Calendar.MONTH,month);
            monthname = calendar.getDisplayName(Calendar.MONTH,Calendar.LONG, Locale.ENGLISH);
            calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
            month = month+1;
            dateStr = monthname+"  "+dayOfMonth+", "+year;
            date.setText(dateStr+"  ");
        }
    };
//    CustomModelDownloadConditions conditions = new CustomModelDownloadConditions.Builder()
//            .requireWifi()
//            .build();
//FirebaseModelDownloader.getInstance()
//        .getModel("MedOps_Covid_Detection", DownloadType.LOCAL_MODEL, conditions)
//    .addOnSuccessListener(new OnSuccessListener<CustomModel>() {
//        @Override
//        public void onSuccess(CustomModel model) {
//            // Download complete. Depending on your app, you could enable
//            // the ML feature, or switch from the local model to the remote
//            // model, etc.
//        }
//    });
}