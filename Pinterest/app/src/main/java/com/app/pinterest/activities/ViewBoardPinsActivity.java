package com.app.pinterest.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.app.pinterest.R;
import com.app.pinterest.activities.BaseActivity;
import com.app.pinterest.activities.CreateBoardActivity;
import com.app.pinterest.activities.MainActivity;
import com.app.pinterest.models.PinsModelClass;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ViewBoardPinsActivity extends BaseActivity {
    List<PinsModelClass> itemsList;
    RecyclerView recyclerView;
    DatabaseReference databaseReference;
    TextView textView, tvBoard;
    ItemsListAdapter adapter;
    FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_board_pins);

        showProgressDialog("Loading...");
        databaseReference = FirebaseDatabase.getInstance().getReference("Pins");
        itemsList = new ArrayList<>();

        tvBoard = findViewById(R.id.tvBoard);
        textView = findViewById(R.id.textView);
        fabAdd = findViewById(R.id.fabAdd);

        tvBoard.setText(MainActivity.boardModel.getName()+" pins");

        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CreatePinActivity.class);
                intent.putExtra("key","board");
                startActivity(intent);
            }
        });

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemsList.clear();
                recyclerView.setAdapter(null);
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    PinsModelClass model = snapshot1.getValue(PinsModelClass.class);
                    if(MainActivity.boardModel.getPinsId().contains(model.getId())){
                        itemsList.add(model);
                    }
                }
                if(itemsList.size()>0){
                    textView.setVisibility(View.GONE);
                    adapter = new ItemsListAdapter(getApplicationContext(),itemsList);
                    recyclerView.setAdapter(adapter);
                }else {
                    textView.setVisibility(View.VISIBLE);
                }
                hideProgressDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideProgressDialog();
            }});
    }

    @Override
    public void onStart() {
        super.onStart();


    }

    public class ItemsListAdapter extends RecyclerView.Adapter<ItemsListAdapter.ImageViewHolder>{
        private Context mcontext ;
        private List<PinsModelClass> muploadList;

        public ItemsListAdapter(Context context , List<PinsModelClass> uploadList ) {
            mcontext = context ;
            muploadList = uploadList ;
        }

        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mcontext).inflate(R.layout.select_pin_list_layout, parent , false);
            return (new ImageViewHolder(v));
        }

        @Override
        public void onBindViewHolder(final ImageViewHolder holder, @SuppressLint("RecyclerView") final int position) {

            final PinsModelClass pins = muploadList.get(position);
            Picasso.with(mcontext).load(pins.getPicUrl()).placeholder(R.drawable.holder).into(holder.imgPic);

            holder.imgRemove.setVisibility(View.VISIBLE);
            holder.cbSelect.setVisibility(View.GONE);
            holder.imgRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ViewBoardPinsActivity.this);
                    builder.setTitle("Confirmation");
                    builder.setMessage("Do you want to remove this pin from board?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Boards").child(userId);
                            DatabaseReference dbRef2 = FirebaseDatabase.getInstance().getReference("Pins");

                            List<String> pinsId = MainActivity.boardModel.getPinsId();
                            pinsId.remove(pins.getId());
                            dbRef.child(MainActivity.boardModel.getId()).child("pinsId").setValue(pinsId);
                            dbRef2.child(pins.getId()).child("boardId").setValue("");
                            Toast.makeText(mcontext, "Pin removed from board", Toast.LENGTH_SHORT).show();
                            MainActivity.boardModel=null;
                            finish();
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), FullScreenImageActivity.class);
                    intent.putExtra("URI",pins.getPicUrl());
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return muploadList.size();
        }

        public class ImageViewHolder extends RecyclerView.ViewHolder{
            public ImageView imgPic;
            public ImageView imgRemove;
            public CheckBox cbSelect;

            public ImageViewHolder(View itemView) {
                super(itemView);

                imgPic = itemView.findViewById(R.id.imgPic);
                cbSelect = itemView.findViewById(R.id.cbSelect);
                imgRemove = itemView.findViewById(R.id.imgRemove);
            }
        }

    }
}