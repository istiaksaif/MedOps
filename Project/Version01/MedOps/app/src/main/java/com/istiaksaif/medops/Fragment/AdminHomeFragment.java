package com.istiaksaif.medops.Fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.istiaksaif.medops.R;

import java.util.HashMap;


public class AdminHomeFragment extends Fragment {

    private TextInputEditText fullName,nid,email,bmdcId;
    private MaterialAutoCompleteTextView userType;
    private Button Invite;

    private DatabaseReference doctorDatabaseRef;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user.getUid();

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        doctorDatabaseRef = FirebaseDatabase.getInstance().getReference("Doctors");

        fullName = view.findViewById(R.id.name);
        nid = view.findViewById(R.id.nid);
        email = view.findViewById(R.id.email);
        bmdcId = view.findViewById(R.id.bmdcid);
        userType = view.findViewById(R.id.type);
        TextInputLayout textInputLayoutuserType = view.findViewById(R.id.userType);
        String []optionUniName = {"Doctor","Nurse"};
        ArrayAdapter<String> arrayAdapterUni = new ArrayAdapter<>(getActivity(),R.layout.usertype_item,optionUniName);
        ((MaterialAutoCompleteTextView) textInputLayoutuserType.getEditText()).setAdapter(arrayAdapterUni);

        Invite = view.findViewById(R.id.next_button);
        Invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Info();
            }
        });
    }

    private void Info() {
        String FullName = fullName.getText().toString();
        String NID = nid.getText().toString();
        String USERTYPE = userType.getText().toString();
        String Email = email.getText().toString();
        String BMDCID = bmdcId.getText().toString();

        if (TextUtils.isEmpty(FullName)){
            Toast.makeText(getActivity(), "please enter your Name", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (TextUtils.isEmpty(USERTYPE)){
            Toast.makeText(getActivity(), "please Select User", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (TextUtils.isEmpty(NID)){
            Toast.makeText(getActivity(), "please enter NID", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (NID.length()<13 ){
            Toast.makeText(getActivity(), "Nid number minimum 13 and max 18 numbers", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (TextUtils.isEmpty(Email)){
            Toast.makeText(getActivity(), "please enter Email", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (TextUtils.isEmpty(BMDCID)){
            Toast.makeText(getActivity(), "please enter BMDCID", Toast.LENGTH_SHORT).show();
            return;
        }

        String doctorId = doctorDatabaseRef.child(uid).push().getKey();
        HashMap<String, Object> result = new HashMap<>();
        result.put("doctorName", FullName);
        result.put("nid", NID);
        result.put("bmdcID", BMDCID);
        result.put("email", Email);
        result.put("isUser", USERTYPE);
        result.put("doctorId",doctorId);
        result.put("image","https://firebasestorage.googleapis.com/v0/b/medops-covid19-detection.appspot.com/o/doctor.jpg?alt=media&token=8d6bbcc9-6afe-418f-8758-e3cfe2278437");
        result.put("status","");


        doctorDatabaseRef.child(doctorId).updateChildren(result)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        fullName.setText("");
                        nid.setText("");
                        userType.setText("");
                        email.setText("");
                        bmdcId.setText("");
                        Toast.makeText(getActivity(),"Invitation Done",Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "Error ", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_admin_home, container, false);
        return view;
    }

}