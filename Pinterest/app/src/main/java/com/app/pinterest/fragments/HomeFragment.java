package com.app.pinterest.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.app.pinterest.R;
import com.app.pinterest.activities.MainActivity;
import com.app.pinterest.activities.PinDetailsActivity;
import com.app.pinterest.models.PinsModelClass;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private ProgressDialog mProgressDialog;
    View view;
    Context context;
    List<PinsModelClass> itemsList;
    RecyclerView recyclerView;
    DatabaseReference databaseReference;
    TextView textView;
    ItemsListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        context = container.getContext();

        showProgressDialog("Loading data..");

        databaseReference = FirebaseDatabase.getInstance().getReference("Pins");
        itemsList = new ArrayList<>();

        textView = view.findViewById(R.id.textView);

        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemsList.clear();
                recyclerView.setAdapter(null);
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    PinsModelClass model = snapshot1.getValue(PinsModelClass.class);
                    itemsList.add(model);
                    if(!userId.equals(model.getUserId())){

                    }
                }
                if(itemsList.size()>0){
                    textView.setVisibility(View.GONE);
                    adapter = new ItemsListAdapter(context,itemsList);
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

    public void showProgressDialog(String msg) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setMessage(msg);
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
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
            View v = LayoutInflater.from(mcontext).inflate(R.layout.pins_list_layout, parent , false);
            return (new ImageViewHolder(v));
        }

        @Override
        public void onBindViewHolder(final ImageViewHolder holder, @SuppressLint("RecyclerView") final int position) {

            final PinsModelClass pins = muploadList.get(position);
            Picasso.with(mcontext).load(pins.getPicUrl()).placeholder(R.drawable.holder).into(holder.imgPic);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.pinsModel = pins;
                    startActivity(new Intent(context, PinDetailsActivity.class));
                }
            });
        }

        @Override
        public int getItemCount() {
            return muploadList.size();
        }

        public class ImageViewHolder extends RecyclerView.ViewHolder{
            public ImageView imgPic;
            public ImageView imgMore;

            public ImageViewHolder(View itemView) {
                super(itemView);

                imgPic = itemView.findViewById(R.id.imgPic);
                imgMore = itemView.findViewById(R.id.imgMore);
            }
        }

    }
}
