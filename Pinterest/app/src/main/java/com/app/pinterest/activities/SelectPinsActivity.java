package com.app.pinterest.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.app.pinterest.R;
import com.app.pinterest.activities.SignInActivity;
import com.app.pinterest.fragments.HomeFragment;
import com.app.pinterest.models.PinsModelClass;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SelectPinsActivity extends BaseActivity {
    List<PinsModelClass> itemsList;
    RecyclerView recyclerView;
    DatabaseReference databaseReference;
    TextView textView;
    Button btnSkip, btnDone;
    ItemsListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_pins);

        showProgressDialog("Loading...");
        databaseReference = FirebaseDatabase.getInstance().getReference("Pins");
        itemsList = new ArrayList<>();

        btnSkip = findViewById(R.id.btnSkip);
        btnDone = findViewById(R.id.btnDone);
        textView = findViewById(R.id.textView);

        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemsList.clear();
                recyclerView.setAdapter(null);
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    PinsModelClass model = snapshot1.getValue(PinsModelClass.class);
                    itemsList.add(model);
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
            }
        });
    }

    public class ItemsListAdapter extends RecyclerView.Adapter<ItemsListAdapter.ImageViewHolder>{
        private Context mcontext ;
        private List<PinsModelClass> muploadList;

        public ItemsListAdapter(Context context , List<PinsModelClass> uploadList ) {
            mcontext = context ;
            muploadList = uploadList ;
        }

        @Override
        public ItemsListAdapter.ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mcontext).inflate(R.layout.select_pin_list_layout, parent , false);
            return (new ItemsListAdapter.ImageViewHolder(v));
        }

        @Override
        public void onBindViewHolder(final ItemsListAdapter.ImageViewHolder holder, @SuppressLint("RecyclerView") final int position) {

            final PinsModelClass pins = muploadList.get(position);
            Picasso.with(mcontext).load(pins.getPicUrl()).placeholder(R.drawable.holder).into(holder.imgPic);

            holder.cbSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Boards");
                        List<String> pinsId = CreateBoardActivity.modelClass.getPinsId();
                        pinsId.add(pins.getId());
                        dbRef.child(CreateBoardActivity.modelClass.getId()).child("pinsId").setValue(pinsId);
                    }else {
                        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Boards");
                        List<String> pinsId = CreateBoardActivity.modelClass.getPinsId();
                        pinsId.remove(pins.getId());
                        dbRef.child(CreateBoardActivity.modelClass.getId()).child("pinsId").setValue(pinsId);
                    }
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

        @Override
        public int getItemCount() {
            return muploadList.size();
        }

        public class ImageViewHolder extends RecyclerView.ViewHolder{
            public ImageView imgPic;
            public CheckBox cbSelect;

            public ImageViewHolder(View itemView) {
                super(itemView);

                imgPic = itemView.findViewById(R.id.imgPic);
                cbSelect = itemView.findViewById(R.id.cbSelect);
            }
        }

    }
}