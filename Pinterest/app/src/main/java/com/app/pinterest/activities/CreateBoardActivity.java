package com.app.pinterest.activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.app.pinterest.R;
import com.app.pinterest.models.BoardModelClass;
import com.app.pinterest.models.PinsModelClass;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class CreateBoardActivity extends BaseActivity {

    ImageView imgCancel;
    EditText edtTitle;
    Button btnCreate;
    Switch swVisibility;
    DatabaseReference databaseReference;
    String title="", visibility="";
    public static BoardModelClass modelClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_board);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("Boards").child(userId);

        imgCancel = findViewById(R.id.imgCancel);
        swVisibility = findViewById(R.id.swVisibility);
        edtTitle = findViewById(R.id.edtTitle);
        btnCreate = findViewById(R.id.btnCreate);

        imgCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title = edtTitle.getText().toString().trim();

                if(TextUtils.isEmpty(title)){
                    edtTitle.setError("Required!");
                    edtTitle.requestFocus();
                    return;
                }
                if(swVisibility.isActivated()){
                    visibility = "visible";
                }else {
                    visibility = "invisible";
                }

                List<String> pinsId = new ArrayList<>();
                pinsId.add("pinsID");
                String id = databaseReference.push().getKey();
                modelClass = new BoardModelClass(id,title,visibility,pinsId);

                databaseReference.child(id).setValue(modelClass);

                Toast.makeText(CreateBoardActivity.this, "Board created successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(),SelectPinsActivity.class));

            }
        });
    }

}