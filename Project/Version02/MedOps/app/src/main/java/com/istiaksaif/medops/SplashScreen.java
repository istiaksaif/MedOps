package com.istiaksaif.medops;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.istiaksaif.medops.Activity.AdminManagerHomeActivity;
import com.istiaksaif.medops.Activity.DoctorHomeActivity;
import com.istiaksaif.medops.Activity.LogInActivity;
import com.istiaksaif.medops.Activity.UserHomeActivity;
import java.util.HashMap;

public class SplashScreen extends AppCompatActivity {
    public static int SPLASH_TIME_OUT = 3000;
    public int REQUEST_CODE = 100;
    public DatabaseReference databaseReference;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth mAuth;

    public void onCreate(Bundle savedInstanceState) {
        SplashScreen.super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
//        final AppUpdateManager updateManager = AppUpdateManagerFactory.create(this);
//        updateManager.getAppUpdateInfo().addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
//            public void onSuccess(AppUpdateInfo appUpdateInfo) {
//                if (appUpdateInfo.updateAvailability() == 2 && appUpdateInfo.isUpdateTypeAllowed(0)) {
//                    try {
//                        AppUpdateManager appUpdateManager = updateManager;
//                        ? r2 = SplashScreen.this;
//                        appUpdateManager.startUpdateFlowForResult(appUpdateInfo, 0, r2, r2.REQUEST_CODE);
//                    } catch (IntentSender.SendIntentException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
        this.mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase instance = FirebaseDatabase.getInstance();
        this.firebaseDatabase = instance;
        this.databaseReference = instance.getReference();
    }

    private void collectToken() {
        final FirebaseUser user = this.mAuth.getCurrentUser();
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            public void onComplete(Task<String> task) {
                if (task.isSuccessful()) {
                    final String token = (String) task.getResult();
                    SplashScreen.this.databaseReference.child("users").child(user.getUid()).addValueEventListener(new ValueEventListener() {
                        public void onDataChange(DataSnapshot snapshot) {
                            SplashScreen.this.databaseReference.child("users").child(snapshot.getKey()).child("token").setValue(token);
                        }

                        public void onCancelled(DatabaseError error) {
                        }
                    });
                }
            }
        });
    }

    private void checkUserInfo() {
        final FirebaseUser user = this.mAuth.getCurrentUser();
        collectToken();
        this.databaseReference.child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                SplashScreen.this.databaseReference.child("usersData").child(dataSnapshot.child("key").getValue().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                    public void onDataChange(DataSnapshot snapshot) {
                        if (((String) snapshot.child("nid").getValue(String.class)).equals("")) {
                            new Handler().postDelayed(new Runnable() {
                                /* JADX WARNING: type inference failed for: r1v2, types: [android.content.Context, com.istiaksaif.medops.SplashScreen] */
                                public void run() {
                                    SplashScreen.this.startActivity(new Intent(SplashScreen.this, LogInActivity.class));
                                    SplashScreen.this.finish();
                                }
                            }, (long) SplashScreen.SPLASH_TIME_OUT);
                            return;
                        }
                        if (((String) snapshot.child("isUser").getValue(String.class)).equals("User")) {
                            new Handler().postDelayed(new Runnable() {
                                /* JADX WARNING: type inference failed for: r1v2, types: [android.content.Context, com.istiaksaif.medops.SplashScreen] */
                                public void run() {
                                    SplashScreen.this.startActivity(new Intent(SplashScreen.this, UserHomeActivity.class));
                                    SplashScreen.this.finish();
                                }
                            }, (long) SplashScreen.SPLASH_TIME_OUT);
                        }
                        if (((String) snapshot.child("isUser").getValue(String.class)).equals("Doctor")) {
                            if (user.isEmailVerified()) {
                                HashMap<String, Object> result = new HashMap<>();
                                result.put("verifyStatus", "verified");
                                SplashScreen.this.databaseReference.child("usersData").child(snapshot.getKey()).updateChildren(result);
                                new Handler().postDelayed(new Runnable() {
                                    /* JADX WARNING: type inference failed for: r1v2, types: [android.content.Context, com.istiaksaif.medops.SplashScreen] */
                                    public void run() {
                                        SplashScreen.this.startActivity(new Intent(SplashScreen.this, DoctorHomeActivity.class));
                                        SplashScreen.this.finish();
                                    }
                                }, (long) SplashScreen.SPLASH_TIME_OUT);
                            } else {
                                new Handler().postDelayed(new Runnable() {
                                    /* JADX WARNING: type inference failed for: r0v5, types: [android.content.Context, com.istiaksaif.medops.SplashScreen] */
                                    public void run() {
                                        user.sendEmailVerification();
                                        Toast.makeText(SplashScreen.this, "Check your email to verify your account", 1).show();
                                        SplashScreen.this.finish();
                                    }
                                }, (long) SplashScreen.SPLASH_TIME_OUT);
                            }
                        }
                        if (((String) snapshot.child("isUser").getValue(String.class)).equals("Nurse")) {
                            new Handler().postDelayed(new Runnable() {
                                /* JADX WARNING: type inference failed for: r1v2, types: [android.content.Context, com.istiaksaif.medops.SplashScreen] */
                                public void run() {
                                    SplashScreen.this.startActivity(new Intent(SplashScreen.this, UserHomeActivity.class));
                                    SplashScreen.this.finish();
                                }
                            }, (long) SplashScreen.SPLASH_TIME_OUT);
                        }
                        if (((String) snapshot.child("isUser").getValue(String.class)).equals("Admin")) {
                            new Handler().postDelayed(new Runnable() {
                                /* JADX WARNING: type inference failed for: r1v2, types: [android.content.Context, com.istiaksaif.medops.SplashScreen] */
                                public void run() {
                                    SplashScreen.this.startActivity(new Intent(SplashScreen.this, AdminManagerHomeActivity.class));
                                    SplashScreen.this.finish();
                                }
                            }, (long) SplashScreen.SPLASH_TIME_OUT);
                        }
                    }

                    public void onCancelled(DatabaseError error) {
                    }
                });
            }

            public void onCancelled(DatabaseError error) {
            }
        });
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        SplashScreen.super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            checkUserInfo();
        } else {
            new Handler().postDelayed(new Runnable() {
                /* JADX WARNING: type inference failed for: r1v0, types: [android.content.Context, com.istiaksaif.medops.SplashScreen] */
                public void run() {
                    SplashScreen.this.startActivity(new Intent(SplashScreen.this, LogInActivity.class));
                    SplashScreen.this.finish();
                }
            }, (long) SPLASH_TIME_OUT);
        }
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        SplashScreen.super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == this.REQUEST_CODE) {
            Toast.makeText(getApplicationContext(), "Downloading...", 0).show();
        }
    }
}
