package com.app.pinterest.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.app.pinterest.R;
import com.app.pinterest.fragments.HomeFragment;
import com.app.pinterest.fragments.ProfileFragment;
import com.app.pinterest.models.BoardModelClass;
import com.app.pinterest.models.PinsModelClass;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends BaseActivity {
    private static final int STORAGE_PERMISSION_CODE = 100;
    private static final int PICK_IMAGE = 102;
    public static Uri ImageUri;
    public static PinsModelClass pinsModel;
    public static BoardModelClass boardModel;
    BottomSheetDialog bottomSheetDialog;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    getFragmentManager().popBackStack();
                    selectedFragment = new HomeFragment();
                    break;
                case R.id.navigation_search:
                    getFragmentManager().popBackStack();
//                    selectedFragment = new CartFragment();
                    break;
                case R.id.navigation_create:
                    getFragmentManager().popBackStack();
                    showChoiceDialog();
                    break;
                case R.id.navigation_notification:
                    getFragmentManager().popBackStack();
//                    selectedFragment = new ProfileFragment();
                    break;
                case R.id.navigation_profile:
                    getFragmentManager().popBackStack();
                    selectedFragment = new ProfileFragment();
                    break;
            }
            if(selectedFragment!=null){
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,selectedFragment).commit();
            }
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Fragment selectedFragment = new HomeFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,selectedFragment).commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE);
            } else {
                Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            ImageUri = data.getData();
            bottomSheetDialog.dismiss();
            Intent intent = new Intent(getApplicationContext(), CreatePinActivity.class);
            intent.putExtra("key","main");
            startActivity(intent);
        }
    }
    private void showChoiceDialog() {
        bottomSheetDialog = new BottomSheetDialog(MainActivity.this , R.style.BtoonSheetDialogTheme);
        View Bottomsheetview = LayoutInflater.from(MainActivity.this)
                .inflate(R.layout.layout_botton_sheet, findViewById(R.id.bottom_sheet_container));

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
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                        return;
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    // For Android 13 (API level 32) and above
                    if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
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
                startActivity(new Intent(getApplicationContext(),CreateBoardActivity.class));
            }
        });

        bottomSheetDialog.setContentView(Bottomsheetview);
        bottomSheetDialog.show();
    }
}