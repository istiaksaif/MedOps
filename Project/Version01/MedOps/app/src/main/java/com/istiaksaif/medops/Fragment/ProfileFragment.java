package com.istiaksaif.medops.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.istiaksaif.medops.R;
import com.istiaksaif.medops.Utils.ImageGetHelper;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private ImageGetHelper getImageFunction;
    private ImageView logoutButton,imageView;
    private TextView nid,fullName,email,phone,personalinfo,DOB,BloodGroup;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Uri imageUri;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user.getUid();
    private ProgressDialog progressDialog,pro;

    private String profilePhoto;
    private GoogleSignInClient googleSignInClient;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getImageFunction = new ImageGetHelper(this,null);

        logoutButton = view.findViewById(R.id.logout);
        imageView = view.findViewById(R.id.profileimage);
        fullName = view.findViewById(R.id.profilefullname);
        DOB = view.findViewById(R.id.dob);
        BloodGroup = view.findViewById(R.id.bloodgroup);
        email = view.findViewById(R.id.profileemail);
        phone = view.findViewById(R.id.phonenum);
        nid = view.findViewById(R.id.nid);
        personalinfo = view.findViewById(R.id.personalinfo);


        progressDialog = new ProgressDialog(getActivity());

        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        storageReference = FirebaseStorage.getInstance().getReference();

        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot:snapshot.getChildren()) {
                    String name = ""+dataSnapshot.child("name").getValue();
                    String dob = "DOB :  "+dataSnapshot.child("dob").getValue();
                    String blood = "   "+dataSnapshot.child("bloodgroup").getValue();
                    String retriveEmail = "   "+dataSnapshot.child("email").getValue();
                    String img = ""+dataSnapshot.child("imageUrl").getValue();
                    String receivephone = ""+dataSnapshot.child("phone").getValue();
                    String receivenid = "NID :   "+dataSnapshot.child("nid").getValue();
                    
                    fullName.setText(name);
                    DOB.setText(dob);
                    BloodGroup.setText(blood);
                    email.setText(retriveEmail);
                    phone.setText(receivephone);
                    nid.setText(receivenid);

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

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Update Profile Image");
                profilePhoto = "imageUrl";
                getImageFunction.showImagePicDialog();
            }
        });

        personalinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(getActivity(), EditPersonalInfoActivity.class);
//                startActivity(intent);
            }
        });
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
                    uploadProfilePhoto(imageUri);
                    imageView.setImageURI(imageUri);
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
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(),"Error Update", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(),"Error", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(),e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_profile, container, false);
        return view;
    }

}