package com.photoframesample;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int REQUEST_IMAGE = 100;

    @BindView(R.id.img_profile)
    ImageView imgProfile;
    @BindView(R.id.img_frame)
    ImageView imgFrame;
    @BindView(R.id.tv_name)
    TextView tvName;

    @BindView(R.id.img_plus)
    ImageView imgPlus;
    @BindView(R.id.img_save)
    ImageView imgSave;

    @BindView(R.id.frame_layout)
    FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setTitle(null);

        loadProfileDefault();

        // Clearing older images from cache directory
        // don't call this line if you want to choose multiple images in the same activity
        // call this once the bitmap(s) usage is over
        ImagePickerActivity.clearCache(this);
    }

    private void loadProfile(String url) {
        Log.d(TAG, "Image cache path: " + url);

//        imgPlus.setVisibility(View.GONE);
        GlideApp.with(this)
                .load(url)
                .into(imgProfile);
        imgProfile.setColorFilter(ContextCompat.getColor(this, android.R.color.transparent));
//        frameLayout.setVisibility(View.VISIBLE);
    }

    private void loadProfileDefault() {
        GlideApp.with(this).load(R.drawable.frame)
                .into(imgFrame);
    }

    @OnClick({R.id.img_plus})
    void onProfileImageClick() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            showImagePickerOptions();
                        }

                        if (report.isAnyPermissionPermanentlyDenied()) {
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void showImagePickerOptions() {
        ImagePickerActivity.showImagePickerOptions(this, new ImagePickerActivity.PickerOptionListener() {
            @Override
            public void onTakeCameraSelected() {
                launchCameraIntent();
            }

            @Override
            public void onChooseGallerySelected() {
                launchGalleryIntent();
            }
        });
    }

    private void launchCameraIntent() {
        Intent intent = new Intent(MainActivity.this, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_IMAGE_CAPTURE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);

        // setting maximum bitmap width and height
        intent.putExtra(ImagePickerActivity.INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, true);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_WIDTH, 1000);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_HEIGHT, 1000);

        startActivityForResult(intent, REQUEST_IMAGE);
    }

    private void launchGalleryIntent() {
        Intent intent = new Intent(MainActivity.this, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_GALLERY_IMAGE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = null;
                if (data != null) {
                    uri = data.getParcelableExtra("path");

                    // You can update this bitmap to your server
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);

                    // loading profile image from local cache
                    if (uri != null) {
                        loadProfile(uri.toString());
                    }
                } else {
                    Toast.makeText(this, "Something somewhere..", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @OnClick(R.id.tv_name)
    void setName(){
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(this);
        View view = layoutInflaterAndroid.inflate(R.layout.dialog_name, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(this);
        alertDialogBuilderUserInput.setView(view);

        EditText etName = view.findViewById(R.id.et_name);
        etName.requestFocus();

        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {
                        String name = etName.getText().toString();
                        tvName.setText(name);
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.dismiss();
                            }
                        });

        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();
    }

    @OnClick({R.id.img_save})
    void saveImage(){
        Calendar calendar = Calendar.getInstance();
        String fileName = String.valueOf(calendar.getTimeInMillis());

        Log.d(TAG, "saveImage Path: " + Environment.getExternalStorageDirectory());

        frameLayout.setDrawingCacheEnabled(true);
        frameLayout.buildDrawingCache();
        Bitmap cache = frameLayout.getDrawingCache();
        try {
            FileOutputStream fileOutputStream = new FileOutputStream( Environment.getExternalStorageDirectory() + "/" + fileName + ".png");
            cache.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            Toast.makeText(this, "Image saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // TODO: handle exception
            Toast.makeText(this, "Something somewhere", Toast.LENGTH_SHORT).show();
        } finally {
            frameLayout.destroyDrawingCache();
        }
    }

    /**
     * Showing Alert Dialog with Settings option
     * Navigates user to app settings
     * NOTE: Keep proper title and message depending on your app
     */
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getString(R.string.dialog_permission_title));
        builder.setMessage(getString(R.string.dialog_permission_message));
        builder.setPositiveButton(getString(R.string.go_to_settings), (dialog, which) -> {
            dialog.cancel();
            openSettings();
        });
        builder.setNegativeButton(getString(android.R.string.cancel), (dialog, which) -> dialog.cancel());
        builder.show();

    }

    // navigating user to app settings
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }
}
