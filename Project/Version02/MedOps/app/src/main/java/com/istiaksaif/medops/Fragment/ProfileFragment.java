package com.istiaksaif.medops.Fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.istiaksaif.medops.Activity.AddMoneyActivity;
import com.istiaksaif.medops.Activity.EditPersonalInfoActivity;
import com.istiaksaif.medops.Activity.EditProfessionalInfoActivity;
import com.istiaksaif.medops.Adapter.DoctorListAdapter;
import com.istiaksaif.medops.Adapter.ProfileAdapter;
import com.istiaksaif.medops.Model.DoctorItem;
import com.istiaksaif.medops.Model.User;
import com.istiaksaif.medops.R;
import com.istiaksaif.medops.Utils.AgeCalculator;
import com.istiaksaif.medops.Utils.ImageGetHelper;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private ImageGetHelper getImageFunction;
    private ImageView imageView;
    private TextView nid,fullName,email,phone,personalinfo,DOB,BloodGroup,Height,Weight,Age,
            editAddress,balanceTk,editPhone,userAddress,addMoney;
    private LinearLayout layout;
    private TextView professionalInfo,designation,workingIn,workingExperience,BMDCId,consultHour,consultDays,consultFee;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Uri imageUri;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user.getUid();
    private ProgressDialog progressDialog,pro;

    private String profilePhoto;
    private GoogleSignInClient googleSignInClient;
    private AgeCalculator age = null;

    private RecyclerView profileRecycler;
    private ProfileAdapter profileAdapter;
    private ArrayList<User> userArrayList;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getImageFunction = new ImageGetHelper(this,null);

        age=new AgeCalculator();
        age.getCurrentDate();

        imageView = view.findViewById(R.id.profileimage);
        fullName = view.findViewById(R.id.profilefullname);
        DOB = view.findViewById(R.id.dob);
        BloodGroup = view.findViewById(R.id.bloodgroup);
        Age = view.findViewById(R.id.age);
        Height = view.findViewById(R.id.height);
        Weight = view.findViewById(R.id.weight);
        email = view.findViewById(R.id.profileemail);
        phone = view.findViewById(R.id.phonenum);
        nid = view.findViewById(R.id.nid);
        personalinfo = view.findViewById(R.id.personalinfo);
        editAddress = view.findViewById(R.id.editaddress);
        balanceTk = view.findViewById(R.id.balanceTk);
        editPhone = view.findViewById(R.id.editphone);
        userAddress = view.findViewById(R.id.address);
        addMoney = view.findViewById(R.id.addmoney);
        layout = view.findViewById(R.id.professionallayout);
        professionalInfo = view.findViewById(R.id.professionalinfo);
        designation = view.findViewById(R.id.designation);
        workingIn = view.findViewById(R.id.workingin);
        workingExperience = view.findViewById(R.id.workingExperience);
        BMDCId = view.findViewById(R.id.bmdcid);
        consultHour = view.findViewById(R.id.consulthour);
        consultDays = view.findViewById(R.id.consultdays);
        consultFee = view.findViewById(R.id.consultfee);

        addMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AddMoneyActivity.class);
                startActivity(intent);
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Update Profile Image");
                profilePhoto = "imageUrl";
                getImageFunction.pickFromGallery();
            }
        });
        personalinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditPersonalInfoActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });
        professionalInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditProfessionalInfoActivity.class);
                startActivity(intent);
            }
        });
        editAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Update Address");
                showMoreUpdating("address");
            }
        });
        editPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Update Phone Number");
                showMoreUpdating("phone");
            }
        });

        progressDialog = new ProgressDialog(getActivity());

        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        storageReference = FirebaseStorage.getInstance().getReference();
//
//        userArrayList = new ArrayList<>();
//
//        profileRecycler = view.findViewById(R.id.profileRecycler);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
//        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//        profileRecycler.setLayoutManager(layoutManager);
//        profileRecycler.setHasFixedSize(true);
//
//        GetDataFromFirebase();

        Query query = databaseReference.orderByChild("userId").equalTo(uid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot:snapshot.getChildren()) {
                    String name = ""+dataSnapshot.child("name").getValue();
                    String dob = "DOB :  "+dataSnapshot.child("dob").getValue();
                    String blood = "Blood Group :  "+dataSnapshot.child("bloodgroup").getValue();
                    String height = "Height :  "+dataSnapshot.child("height").getValue()+" cm";
                    String weight = "Weight :  "+dataSnapshot.child("weight").getValue()+" kg";
                    String retriveEmail = "   "+dataSnapshot.child("email").getValue();
                    String img = ""+dataSnapshot.child("imageUrl").getValue();
                    String receivephone = "  "+dataSnapshot.child("phone").getValue();
                    String receivenid = "NID :  "+dataSnapshot.child("nid").getValue();
                    String address = "          "+dataSnapshot.child("address").getValue();
                    String balancetk = " "+dataSnapshot.child("balanceTk").getValue()+" ";

                    String d = ""+dataSnapshot.child("dob").getValue();

                    try {
                        String str[] = d.split("/");
                        int dayOfMonth = Integer.parseInt(str[0]);
                        int month = Integer.parseInt(str[1]);
                        int year = Integer.parseInt(str[2]);
                        age.setDateOfBirth(year, month, dayOfMonth);
                        age.calcualteYear();
                        age.calcualteMonth();
                        age.calcualteDay();
                        String age1 = "Age :  " + age.getResult() + " yrs";
                        Age.setText(age1);
                    }catch (Exception e){
                    }
                    String userType = ""+dataSnapshot.child("isUser").getValue();
                    if(userType.equals("Doctor")){
                        layout.setVisibility(View.VISIBLE);
                        designation.setText(" "+dataSnapshot.child("designation").getValue()+" ");
                        workingIn.setText(" "+dataSnapshot.child("workingIn").getValue()+" ");
                        String experience = ""+dataSnapshot.child("workingExperience").getValue();
                        try {
                            String str1[] = experience.split("/");
                            int dayOfMonth1 = Integer.parseInt(str1[0]);
                            int month1 = Integer.parseInt(str1[1]);
                            int year1 = Integer.parseInt(str1[2]);
                            age.setDateOfBirth(year1, month1, dayOfMonth1);
                            age.calcualteYear();
                            age.calcualteMonth();
                            age.calcualteDay();
                            workingExperience.setText(" Experience : "+age.getResult()+" yrs");
                        }catch (Exception e){

                        }
                        BMDCId.setText(" BMDCID : "+dataSnapshot.child("bmdcID").getValue()+" ");
                        consultHour.setText(" ConsultHour : "+dataSnapshot.child("consultHour").getValue()+" to "+dataSnapshot.child("consultHourTo").getValue());
                        consultDays.setText(" ConsultDays : "+dataSnapshot.child("consultDays").getValue()+" ");
                        consultFee.setText(" ConsultFee : "+dataSnapshot.child("consultFee").getValue()+" ");
                    }
                    fullName.setText(name);
                    DOB.setText(dob);
                    BloodGroup.setText(blood);
                    email.setText(retriveEmail);
                    phone.setText(receivephone);
                    nid.setText(receivenid);
                    Height.setText(height);
                    Weight.setText(weight);
                    userAddress.setText(address);
                    balanceTk.setText(balancetk);

                    try {
                        Picasso.get().load(img).resize(320,320).into(imageView);
                    }catch (Exception e){
                        Picasso.get().load(R.drawable.dropdown).into(imageView);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(),"Some Thing Wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showMoreUpdating(String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update "+ key);
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        TextInputEditText editText = new TextInputEditText(getActivity());
        editText.setHint("Enter "+key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = editText.getText().toString().trim();
                if(!TextUtils.isEmpty(value)){
                    progressDialog.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key, value);

                    databaseReference.child(uid).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(),"Updating "+key,Toast.LENGTH_SHORT).show();
                                    getActivity().finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(),"Error ",Toast.LENGTH_SHORT).show();
                            getActivity().finish();
                        }
                    });

                }else {
                    Toast.makeText(getActivity(),"Please Enter "+key,Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == getImageFunction.IMAGE_PICK_GALLERY_CODE){
                imageUri = data.getData();
                uploadProfilePhoto(imageUri);
                imageView.setImageURI(imageUri);
            }
            if(requestCode == getImageFunction.IMAGE_PICK_CAMERA_CODE){
                try {
                    uploadProfilePhoto(getImageFunction.imageUri);
                    imageView.setImageURI(getImageFunction.imageUri);
                }catch (Exception e){
                   e.printStackTrace();
                }
            }
        }
    }

    private void uploadProfilePhoto(Uri uri) {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] fileInBytes = baos.toByteArray();

        String filePathName = profilePhoto+"_"+uid;
        StorageReference storageReference1 = storageReference.child(filePathName);

        pro = new ProgressDialog(getContext());
        pro.show();
        pro.setContentView(R.layout.progress_dialog);
        pro.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        storageReference1.putBytes(fileInBytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                Uri downloadUri = uriTask.getResult();
                if(uriTask.isSuccessful()){
                    HashMap<String, Object> results = new HashMap<>();
                    results.put(profilePhoto,downloadUri.toString());

                    databaseReference.child(uid).updateChildren(results)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();
                                    pro.dismiss();
                                    Toast.makeText(getContext(),"Image Update", Toast.LENGTH_SHORT).show();
                                    getActivity().finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(),"Error Update", Toast.LENGTH_SHORT).show();
                            getActivity().finish();
                        }
                    });
                }
                else {
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(),"Error", Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(),e.getMessage(), Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        });
    }


//    private void GetDataFromFirebase() {
//        Query query = databaseReference.child("users").orderByChild("userId").equalTo(uid);
//        query.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                ClearAll();
//                for(DataSnapshot dataSnapshot:snapshot.getChildren()) {
//                    try {
//                        User user = new User();
//                        user.setName(dataSnapshot.child("name").getValue().toString());
//                        user.setDob(dataSnapshot.child("dob").getValue().toString());
//                        user.setBloodgroup(dataSnapshot.child("bloodgroup").getValue().toString());
//                        user.setBalanceTk(dataSnapshot.child("balanceTk").getValue().toString());
//                        user.setImageUrl(dataSnapshot.child("imageUrl").getValue().toString());
//                        user.setEmail(dataSnapshot.child("email").getValue().toString());
//                        user.setPhone(dataSnapshot.child("phone").getValue().toString());
//                        user.setNid(dataSnapshot.child("nid").getValue().toString());
//
//                        userArrayList.add(user);
//                    }catch (Exception e){
//
//                    }
//                }
//                profileAdapter = new ProfileAdapter(getContext(), userArrayList);
//                profileRecycler.setAdapter(profileAdapter);
//                profileAdapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
//
//    private void ClearAll(){
//        if (userArrayList !=null){
//            userArrayList.clear();
//            if (profileAdapter !=null){
//                profileAdapter.notifyDataSetChanged();
//            }
//        }
//        userArrayList = new ArrayList<>();
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_profile, container, false);
        return view;
    }
}