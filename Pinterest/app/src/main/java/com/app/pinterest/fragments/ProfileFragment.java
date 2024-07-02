package com.app.pinterest.fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.app.pinterest.R;
import com.app.pinterest.activities.CreateBoardActivity;
import com.app.pinterest.activities.CreatePinActivity;
import com.app.pinterest.activities.MainActivity;
import com.app.pinterest.activities.PinDetailsActivity;
import com.app.pinterest.activities.SignInActivity;
import com.app.pinterest.activities.ViewBoardPinsActivity;
import com.app.pinterest.models.BoardModelClass;
import com.app.pinterest.models.PinsModelClass;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {
    private static final int STORAGE_PERMISSION_CODE = 100;
    private static final int PICK_IMAGE = 102;
    private ProgressDialog mProgressDialog;
    View view;
    Context context;
    List<PinsModelClass> itemsList;
    List<BoardModelClass> boardModelList = new ArrayList<>();
    RecyclerView recyclerView;
    DatabaseReference databaseReference;
    TextView tvEmail, tvPins, tvBoards, tvSearch;
    Button btnLogout;
    View line1, line2;
    ImageView imgAdd;
    ItemsListAdapter adapter;
    int count = 0;
    String url="";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        context = container.getContext();

        databaseReference = FirebaseDatabase.getInstance().getReference("Pins");
        itemsList = new ArrayList<>();

        tvEmail = view.findViewById(R.id.tvEmail);
        tvPins = view.findViewById(R.id.tvPins);
        tvBoards = view.findViewById(R.id.tvBoards);
        tvBoards = view.findViewById(R.id.tvBoards);
        line1 = view.findViewById(R.id.line1);
        line2 = view.findViewById(R.id.line2);
        tvSearch = view.findViewById(R.id.tvSearch);
        imgAdd = view.findViewById(R.id.imgAdd);
        btnLogout = view.findViewById(R.id.btnLogout);

        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        tvEmail.setText(email);

        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);

        loadPinsData();
        tvPins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                line1.setVisibility(View.VISIBLE);
                line2.setVisibility(View.GONE);

                StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
                recyclerView = view.findViewById(R.id.recyclerView);
                recyclerView.setLayoutManager(staggeredGridLayoutManager);

                loadPinsData();
            }
        });
        tvBoards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                line1.setVisibility(View.GONE);
                line2.setVisibility(View.VISIBLE);

                GridLayoutManager gridLayoutManager = new GridLayoutManager(context,2);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(gridLayoutManager);

                loadBoardsData();
            }
        });
        imgAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChoiceDialog();
            }
        });
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Confirmation");
                builder.setMessage("Do you want to logout?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(context, SignInActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        getActivity().finish();
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
        return view;
    }

    private void loadBoardsData() {
        showProgressDialog("Loading data..");
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("Boards").child(userId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boardModelList.clear();
                recyclerView.setAdapter(null);
                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                    BoardModelClass model = snapshot1.getValue(BoardModelClass.class);
                    boardModelList.add(model);
                }
                if(boardModelList.size()>0){
                    BoardsListAdapter adapter = new BoardsListAdapter(context,boardModelList);
                    recyclerView.setAdapter(adapter);
                }
                hideProgressDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void loadPinsData() {
        showProgressDialog("Loading data..");
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Pins");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemsList.clear();
                recyclerView.setAdapter(null);
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    PinsModelClass model = snapshot1.getValue(PinsModelClass.class);
                    itemsList.add(model);
                    if(userId.equals(model.getUserId())){

                    }
                }
                if(itemsList.size()>0){
                    adapter = new ItemsListAdapter(context,itemsList);
                    recyclerView.setAdapter(adapter);
                }
                hideProgressDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideProgressDialog();
            }
        });
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
            holder.imgMore.setVisibility(View.GONE);
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
    public class BoardsListAdapter extends RecyclerView.Adapter<BoardsListAdapter.ImageViewHolder>{
        private Context mcontext ;
        private List<BoardModelClass> muploadList;

        public BoardsListAdapter(Context context , List<BoardModelClass> uploadList ) {
            mcontext = context ;
            muploadList = uploadList ;
        }

        @Override
        public BoardsListAdapter.ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mcontext).inflate(R.layout.board_list_layout2, parent , false);
            return (new BoardsListAdapter.ImageViewHolder(v));
        }

        @Override
        public void onBindViewHolder(final BoardsListAdapter.ImageViewHolder holder, @SuppressLint("RecyclerView") final int position) {

            final BoardModelClass board = muploadList.get(position);
            holder.tvBoard.setText(board.getName());

            checkPinsCount(board.getId(),holder.tvNumberPins,holder.imgPic,board.getPinsId());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.boardModel = board;
                    startActivity(new Intent(context, ViewBoardPinsActivity.class));
                }
            });
        }

        private void checkPinsCount(String boardId, TextView tvNumberPins, ImageView imgPic, List<String> pinsId) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Pins");
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    count = 0;
                    url="";
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        PinsModelClass model = snapshot1.getValue(PinsModelClass.class);
                        if(userId.equals(model.getUserId()) && pinsId.contains(model.getId())){
                            count++;
                            if(count==1){
                                url = model.getPicUrl();
                            }
                        }
                    }
                    if(!url.isEmpty()){
                        Picasso.with(context).load(url).placeholder(R.drawable.holder).into(imgPic);
                    }
                    tvNumberPins.setText(count+" Pins");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    hideProgressDialog();
                }});
        }

        @Override
        public int getItemCount() {
            return muploadList.size();
        }

        public class ImageViewHolder extends RecyclerView.ViewHolder{
            public ImageView imgPic;
            public TextView tvBoard;
            public TextView tvNumberPins;

            public ImageViewHolder(View itemView) {
                super(itemView);

                imgPic = itemView.findViewById(R.id.imgPic);
                tvBoard = itemView.findViewById(R.id.tvBoard);
                tvNumberPins = itemView.findViewById(R.id.tvNumberPins);
            }
        }

    }
    private void showChoiceDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context , R.style.BtoonSheetDialogTheme);
        View Bottomsheetview = LayoutInflater.from(context)
                .inflate(R.layout.layout_botton_sheet, view.findViewById(R.id.bottom_sheet_container));

        ImageView imgPin;
        ImageView imgBoard;

        imgPin = Bottomsheetview.findViewById(R.id.imgPin);
        imgBoard = Bottomsheetview.findViewById(R.id.imgBoard);

        imgPin.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                    // For Android 6.0 (API level 23) to Android 12 (API level 31)
                    if (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                        return;
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    // For Android 13 (API level 32) and above
                    if (context.checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION_CODE);
                        return;
                    }
                }

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE);
            }
        });
        imgBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, CreateBoardActivity.class));
            }
        });

        bottomSheetDialog.setContentView(Bottomsheetview);
        bottomSheetDialog.show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE);
            } else {
                Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            MainActivity.ImageUri = data.getData();
            Intent intent = new Intent(context,CreatePinActivity.class);
            intent.putExtra("key","main");
            startActivity(intent);
        }
    }
}
