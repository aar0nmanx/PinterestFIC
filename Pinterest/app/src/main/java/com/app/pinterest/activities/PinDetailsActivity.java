package com.app.pinterest.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.app.pinterest.R;
import com.app.pinterest.models.BoardModelClass;
import com.app.pinterest.models.PinsModelClass;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PinDetailsActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_CODE_WRITE = 1;
    private boolean writeGranted=false;
    PinsModelClass modelClass;
    ImageView imgPin;
    ImageView imgBack, imgShare, imgFavorite, imgUserPic;
    TextView tvUserName, tvPlantName, tvDescription;
    Button btnView, btnSave;
    DatabaseReference databaseReference;
    boolean flag;
    ScrollView child;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);
        modelClass = MainActivity.pinsModel;

        imgPin = findViewById(R.id.imgPin);
        Picasso.with(this).load(modelClass.getPicUrl()).placeholder(R.drawable.holder).into(imgPin);
        imgShare = findViewById(R.id.imgShare);
        imgBack = findViewById(R.id.imgBack);
        imgFavorite = findViewById(R.id.imgFavorite);
        imgUserPic = findViewById(R.id.imgUserPic);
        tvUserName = findViewById(R.id.tvUserName);
//        tvUserName.setText(post.getUserName());
        tvPlantName = findViewById(R.id.tvPlantName);
        tvPlantName.setText(modelClass.getTitle());
        tvDescription = findViewById(R.id.tvDescription);
        tvDescription.setText(modelClass.getDescription());

        btnView = findViewById(R.id.btnView);
        btnSave = findViewById(R.id.btnSave);
        child = findViewById(R.id.child);

        checkFavorite();

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        imgShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPic(child, child.getChildAt(0).getHeight(),child.getChildAt(0).getWidth());
            }
        });
        imgFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Favorites").child(userId);
                dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                            PinsModelClass model = snapshot.getValue(PinsModelClass.class);
                            if(modelClass.getId().equals(model.getId())){
                                flag = true;
                                break;
                            }
                        }
                        if(!flag){
                            databaseReference.child(modelClass.getId()).setValue(modelClass);
                            Toast.makeText(getApplicationContext(), "Added to favourite", Toast.LENGTH_SHORT).show();
                            imgFavorite.setImageResource(R.drawable.ic_fillheart);
                            flag = false;
                        }else {
                            databaseReference.child(modelClass.getId()).removeValue();
                            Toast.makeText(getApplicationContext(), "Removed from favourite", Toast.LENGTH_SHORT).show();
                            imgFavorite.setImageResource(R.drawable.ic_heart);
                            flag = false;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }});
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveToBoard();
            }
        });
        btnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FullScreenImageActivity.class);
                intent.putExtra("URI",modelClass.getPicUrl());
                startActivity(intent);
            }
        });
    }

    private void saveToBoard() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(PinDetailsActivity.this , R.style.BtoonSheetDialogTheme);
        View Bottomsheetview = LayoutInflater.from(PinDetailsActivity.this)
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
        databaseReference = FirebaseDatabase.getInstance().getReference("Boards").child(userId);

        RecyclerView finalRecyclerView = recyclerView;
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boardModelList.clear();
                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                    BoardModelClass model = snapshot1.getValue(BoardModelClass.class);
                    boardModelList.add(model);
                }
                if(boardModelList.size()>0){
                    ItemsListAdapter adapter = new ItemsListAdapter(PinDetailsActivity.this,boardModelList);
                    finalRecyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Pins");
                String id = databaseReference.push().getKey();
                PinsModelClass modelClass1 = new PinsModelClass(id,modelClass.getTitle(),modelClass.getDescription(),
                        modelClass.getLink(),modelClass.getPicUrl(),userId,modelClass.getBoardId());
                databaseReference.child(id).setValue(modelClass1);
                Toast.makeText(PinDetailsActivity.this, "Pin saved to profile", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            }
        });
        tvProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Pins");
                String id = databaseReference.push().getKey();
                PinsModelClass modelClass1 = new PinsModelClass(id,modelClass.getTitle(),modelClass.getDescription(),
                        modelClass.getLink(),modelClass.getPicUrl(),userId,modelClass.getBoardId());
                databaseReference.child(id).setValue(modelClass1);
                Toast.makeText(PinDetailsActivity.this, "Pin saved to profile", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
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
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Boards").child(userId);
                    List<String> pinsId = board.getPinsId();
                    pinsId.add(modelClass.getId());
                    dbRef.child(board.getId()).child("pinsId").setValue(pinsId);

                    Toast.makeText(mcontext, "Saved to board: "+board.getName(), Toast.LENGTH_SHORT).show();
                    finish();
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
    private void checkFavorite() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Favorites").child(userId);
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    PinsModelClass model = snapshot.getValue(PinsModelClass.class);
                    if(model.getId().equals(model.getId())){
                        Picasso.with(PinDetailsActivity.this).load(R.drawable.ic_fillheart).into(imgFavorite);
                        imgFavorite.setImageResource(R.drawable.ic_fillheart);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }});
    }
    public void sendPic(ScrollView img_main, int height, int width){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!writeGranted) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE_WRITE);
                    return;
                }
            }
        }
        showProgressDialog();
        File file = new File(android.os.Environment.getExternalStorageDirectory(), getResources().getString(R.string.app_name));
        if (!file.exists()) {
            file.mkdirs();
        }
        img_main.setDrawingCacheEnabled(true);

        //new code

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable bg = img_main.getBackground();
        if(bg != null){
            bg.draw(canvas);
        }else {
            canvas.drawColor(Color.WHITE);
        }
        img_main.draw(canvas);


        long ctime = Calendar.getInstance().get(Calendar.MILLISECOND);
        File f2 = new File(file.getAbsolutePath() + file.separator +  getResources().getString(R.string.app_name)
                + ctime + ".png");
        MediaScannerConnection.scanFile(PinDetailsActivity.this, new String[]{f2.getPath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
            public void onScanCompleted(String path, Uri uri) {
                // now visible in gallery
                shareImage(uri);
            }
        });
        FileOutputStream ostream = null;
        try {
            ostream = new FileOutputStream(f2);
            bitmap.compress(Bitmap.CompressFormat.PNG, 10, ostream);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
            File f = new File("file:///"+ Uri.parse(file.getAbsolutePath() + file.separator + getResources().getString(R.string.app_name)
                    + ctime + ".png"));
            try {
                f.createNewFile();
                FileOutputStream fo = new FileOutputStream(f);
                fo.write(bytes.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
            ostream.flush();
            ostream.close();
        }

        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    private void shareImage(Uri imageUri) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        //Uri uri = Uri.fromFile(imageFile);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_STREAM, imageUri);
        hideProgressDialog();
        startActivity(Intent.createChooser(intent, "Share Image"));

    }

    private ProgressDialog mProgressDialog;
    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Please wait..");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}