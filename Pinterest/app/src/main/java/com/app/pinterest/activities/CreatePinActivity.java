package com.app.pinterest.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.app.pinterest.R;
import com.app.pinterest.models.BoardModelClass;
import com.app.pinterest.models.PinsModelClass;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import java.util.ArrayList;
import java.util.List;

public class CreatePinActivity extends BaseActivity{

    ImageView imgCancel, imgPin;
    EditText edtTitle;
    EditText edtLink;
    EditText edtDescription;
    Button btnCreate;
    TextView tvBoard;
    RelativeLayout pickABoard;
    DatabaseReference databaseReference;
    StorageReference mStorageRef ;
    private StorageTask mUploadTask;
    String Title="", Desc="", Link="", boardId="";
    List<String> pinsId = new ArrayList<>();
    boolean boardFlag = false;
    BottomSheetDialog bottomSheetDialog;
    String key="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_pin);

        Intent intent = getIntent();
        key = intent.getStringExtra("key");

        if(key.equals("main")){

        }else if(key.equals("board")){
            boardFlag = true;
            boardId = MainActivity.boardModel.getId();
            tvBoard.setText(MainActivity.boardModel.getName());
        }
        databaseReference = FirebaseDatabase.getInstance().getReference("Pins");
        mStorageRef = FirebaseStorage.getInstance().getReference("Pins/") ;

        tvBoard = findViewById(R.id.tvBoard);
        imgCancel = findViewById(R.id.imgCancel);
        imgPin = findViewById(R.id.imgPin);
        edtTitle = findViewById(R.id.edtTitle);
        edtDescription = findViewById(R.id.edtDescription);
        edtLink = findViewById(R.id.edtLink);
        pickABoard = findViewById(R.id.tvCate);
        btnCreate = findViewById(R.id.btnCreate);

        imgPin.setImageURI(MainActivity.ImageUri);

        imgCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Title = edtTitle.getText().toString().trim();
                Desc = edtDescription.getText().toString().trim();
                Link = edtLink.getText().toString().trim();

                if(TextUtils.isEmpty(Title)){
                    edtTitle.setError("Required!");
                    edtTitle.requestFocus();
                    return;
                }
                if(TextUtils.isEmpty(Desc)){
                    edtDescription.setError("Required!");
                    edtDescription.requestFocus();
                    return;
                }
                if(TextUtils.isEmpty(Link)){
                    edtLink.setError("Required!");
                    edtLink.requestFocus();
                    return;
                }

                uploadImages(MainActivity.ImageUri);

            }
        });
        tvBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(boardFlag){
                    Toast.makeText(CreatePinActivity.this, "Board already selected!", Toast.LENGTH_SHORT).show();
                }
                saveToBoard();
            }
        });
    }
    private void saveToBoard() {
        bottomSheetDialog = new BottomSheetDialog(CreatePinActivity.this , R.style.BtoonSheetDialogTheme);
        View Bottomsheetview = LayoutInflater.from(CreatePinActivity.this)
                .inflate(R.layout.layout_botton_sheet2, findViewById(R.id.bottom_sheet_container));

        ImageView imgProfile;
        TextView tvProfile;
        RecyclerView recyclerView;
        List<BoardModelClass> boardModelList = new ArrayList<>();

        tvProfile = Bottomsheetview.findViewById(R.id.tvProfile);
        imgProfile = Bottomsheetview.findViewById(R.id.imgProfile);
        recyclerView = Bottomsheetview.findViewById(R.id.recyclerView);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(),1);
        recyclerView = Bottomsheetview.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(gridLayoutManager);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Boards").child(userId);

        RecyclerView finalRecyclerView = recyclerView;
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boardModelList.clear();
                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                    BoardModelClass model = snapshot1.getValue(BoardModelClass.class);
                    boardModelList.add(model);
                }
                if(boardModelList.size()>0){
                    ItemsListAdapter adapter = new ItemsListAdapter(CreatePinActivity.this,boardModelList);
                    finalRecyclerView.setAdapter(adapter);
                }else {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boardId = "";
                boardFlag = false;
                tvBoard.setText("Profile");
            }
        });

        bottomSheetDialog.setContentView(Bottomsheetview);
        bottomSheetDialog.show();
    }
    public class ItemsListAdapter extends RecyclerView.Adapter<ItemsListAdapter.ImageViewHolder>{
        private Context mcontext ;
        private List<BoardModelClass> muploadList;

        public ItemsListAdapter(Context context , List<BoardModelClass> uploadList ) {
            mcontext = context ;
            muploadList = uploadList ;
        }

        @Override
        public ItemsListAdapter.ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mcontext).inflate(R.layout.board_list_layout, parent , false);
            return (new ItemsListAdapter.ImageViewHolder(v));
        }

        @Override
        public void onBindViewHolder(final ItemsListAdapter.ImageViewHolder holder, @SuppressLint("RecyclerView") final int position) {

            final BoardModelClass board = muploadList.get(position);
            holder.tvBoard.setText(board.getName());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boardId = board.getId();
                    pinsId = board.getPinsId();
                    tvBoard.setText(board.getName());
                    boardFlag = true;
                    bottomSheetDialog.dismiss();
                    Toast.makeText(mcontext, board.getName()+" Selected", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return muploadList.size();
        }

        public class ImageViewHolder extends RecyclerView.ViewHolder{
            public ImageView imgPic;
            public TextView tvBoard;

            public ImageViewHolder(View itemView) {
                super(itemView);

                imgPic = itemView.findViewById(R.id.imgPic);
                tvBoard = itemView.findViewById(R.id.tvBoard);
            }
        }

    }
    private  String getExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver() ;
        MimeTypeMap mime = MimeTypeMap.getSingleton() ;
        return  mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    private void uploadImages(Uri mImageUri) {
        showProgressDialog("Uploading");
            final StorageReference fileref = mStorageRef.child(System.currentTimeMillis() + "." + getExtension(mImageUri));
            mUploadTask = fileref.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            try {
                                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                String id = databaseReference.push().getKey();
                                PinsModelClass model = new PinsModelClass(id,Title,Desc,Link,uri.toString(),userId,boardId);
                                databaseReference.child(id).setValue(model);

                                if(boardFlag){
                                    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Boards");
                                    pinsId.add(id);
                                    dbRef.child(userId).child(boardId).child("pinsId").setValue(pinsId);
                                }

                                Toast.makeText(CreatePinActivity.this, "Pin created", Toast.LENGTH_SHORT).show();

                                edtTitle.setText("");
                                edtDescription.setText("");
                                edtLink.setText("");
                                hideProgressDialog();
                                finish();

                            } catch (Exception ex ){
                                Toast.makeText(getApplicationContext()  , "err" + ex.toString() , Toast.LENGTH_LONG).show();
                                hideProgressDialog();
                            }
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                    hideProgressDialog();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                }
            });
    }

}