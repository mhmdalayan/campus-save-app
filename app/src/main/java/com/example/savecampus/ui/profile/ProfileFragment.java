package com.example.savecampus.ui.profile;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.savecampus.LoginActivity;
import com.example.savecampus.R;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1001;
    private static final int CAMERA_REQUEST = 1002;
    private static final int CAMERA_PERMISSION_REQUEST = 2001;

    ImageView ivProfile;
    TextView tvUsername;
    Button btnChoosePic, btnCapturePic, btnUploadPic, btnLogout;

    String imageBase64 = null;
    private int userId = -1;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ivProfile = view.findViewById(R.id.ivProfile);
        tvUsername = view.findViewById(R.id.tvUsername);
        btnChoosePic = view.findViewById(R.id.btnChoosePic);
        btnCapturePic = view.findViewById(R.id.btnCapturePic);
        btnUploadPic = view.findViewById(R.id.btnUploadPic);
        btnLogout = view.findViewById(R.id.btnLogout);

        SharedPreferences prefs =
                requireContext().getSharedPreferences("SaveCampusPrefs", Context.MODE_PRIVATE);

        userId = prefs.getInt("user_id", -1);
        String username = prefs.getString("username", "User");
        tvUsername.setText(username);

        if (userId != -1) {
            loadProfileImage(userId);
        }

        btnChoosePic.setOnClickListener(v -> openGallery());
        btnCapturePic.setOnClickListener(v -> checkCameraPermission());
        btnUploadPic.setOnClickListener(v -> uploadProfileImage());

        // ✅ FIXED LOGOUT LOGIC
        btnLogout.setOnClickListener(v -> {

            // 1️⃣ clear session
            prefs.edit().clear().apply();

            // 2️⃣ go to login & clear back stack
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }

    /* ---------------- CAMERA PERMISSION ---------------- */

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            ActivityCompat.requestPermissions(
                    requireActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(getContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /* ---------------- OPEN GALLERY / CAMERA ---------------- */

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (resultCode != Activity.RESULT_OK || data == null) return;

            imageBase64 = null;
            btnUploadPic.setEnabled(false);

            if (requestCode == PICK_IMAGE_REQUEST) {
                Uri imageUri = data.getData();
                if (imageUri == null) return;
                ivProfile.setImageURI(imageUri);
                imageBase64 = convertUriToBase64(imageUri);

            } else if (requestCode == CAMERA_REQUEST) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                if (bitmap == null) return;
                ivProfile.setImageBitmap(bitmap);
                imageBase64 = convertBitmapToBase64(bitmap);
            }

            if (imageBase64 != null) {
                btnUploadPic.setEnabled(true);
            }

        } catch (Exception e) {
            Toast.makeText(getContext(), "Image error", Toast.LENGTH_SHORT).show();
        }
    }

    /* ---------------- LOAD PROFILE IMAGE ---------------- */

    private void loadProfileImage(int userId) {
        String url = "http://10.0.2.2/mobileApp/get_profile_pic.php?user_id=" + userId;

        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if (response.getBoolean("success") && !response.isNull("image_path")) {
                            String imageUrl = response.getString("image_path");
                            Glide.with(this)
                                    .load(imageUrl + "?v=" + System.currentTimeMillis())
                                    .placeholder(android.R.drawable.sym_def_app_icon)
                                    .into(ivProfile);
                        }
                    } catch (Exception ignored) {}
                },
                error -> {}
        );

        queue.add(request);
    }

    /* ---------------- UPLOAD PROFILE IMAGE ---------------- */

    private void uploadProfileImage() {

        if (userId == -1 || imageBase64 == null) return;

        btnUploadPic.setEnabled(false);

        String url = "http://10.0.2.2/mobileApp/upload_profile.php";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    btnUploadPic.setEnabled(true);
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                            loadProfileImage(userId);
                            imageBase64 = null;
                            btnUploadPic.setEnabled(false);
                        }
                    } catch (Exception ignored) {}
                },
                error -> btnUploadPic.setEnabled(true)
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("user_id", String.valueOf(userId));
                p.put("image_base64", imageBase64);
                return p;
            }
        };

        queue.add(request);
    }

    /* ---------------- BASE64 HELPERS ---------------- */

    private String convertUriToBase64(Uri uri) throws Exception {
        InputStream in = requireContext().getContentResolver().openInputStream(uri);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int n;
        while ((n = in.read(data)) != -1) buffer.write(data, 0, n);
        in.close();
        return Base64.encodeToString(buffer.toByteArray(), Base64.DEFAULT);
    }

    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }
}
