package com.example.savecampus.ui.profile;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.savecampus.LoginActivity;
import com.example.savecampus.R;
import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response; // Added missing import

import static androidx.core.content.ContextCompat.checkSelfPermission;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_READ_EXTERNAL = 2;
    // Changed to "SaveCampusPrefs" to match your LoginActivity
    private static final String PREFS_NAME = "SaveCampusPrefs";
    private static final String KEY_USERNAME = "username";

    private ImageView ivProfile;
    private TextView tvUsername;
    private Button btnLogout, btnChoosePic, btnUploadPic;
    private Uri selectedImageUri;

    private static final String UPLOAD_URL = "http://10.0.2.2/mobileApp/upload_profile.php";

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ivProfile = view.findViewById(R.id.ivProfile);
        tvUsername = view.findViewById(R.id.tvUsername);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnChoosePic = view.findViewById(R.id.btnChoosePic);
        btnUploadPic = view.findViewById(R.id.btnUploadPic);

        // Get Username from SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String username = prefs.getString(KEY_USERNAME, "User");
        tvUsername.setText(username);

        // Load existing profile picture
        loadProfileImage(username);

        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            requireActivity().finish();
        });

        btnChoosePic.setOnClickListener(v -> {
            if (checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_READ_EXTERNAL);
            } else {
                openImagePicker();
            }
        });

        btnUploadPic.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                uploadProfileImage(username, selectedImageUri);
            } else {
                Toast.makeText(getContext(), "Please select an image first.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_READ_EXTERNAL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(getContext(), "Permission denied. Cannot select image.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadProfileImage(String username, Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imageUri);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
            byte[] byteArray = stream.toByteArray();

            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("username", username)
                    .addFormDataPart("profile_pic", "profile.jpg",
                            RequestBody.create(byteArray, MediaType.parse("image/jpeg")))
                    .build();

            Request request = new Request.Builder().url(UPLOAD_URL).post(requestBody).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> Toast.makeText(getContext(), "Server Error", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    // Use try-with-resources or close the response body
                    try (response) {
                        if (!response.isSuccessful()) {
                            runOnUiThread(() -> Toast.makeText(getContext(), "Upload Failed", Toast.LENGTH_SHORT).show());
                            return;
                        }

                        final String myResponse = response.body().string();
                        runOnUiThread(() -> {
                            try {
                                JSONObject json = new JSONObject(myResponse);
                                if (json.getBoolean("success")) {
                                    Toast.makeText(getContext(), "Upload Success!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), json.getString("message"), Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                Toast.makeText(getContext(), "Response error", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Image processing failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void runOnUiThread(Runnable action) {
        if (getActivity() != null) getActivity().runOnUiThread(action);
    }

    private void loadProfileImage(String username) {
        // Updated to use the correct local server IP
        String url = "http://10.0.2.2/mobileApp/get_profile_pic.php?username=" + username;
        if (getContext() != null) {
            Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.ic_menu_camera) // Make sure this exists or change to a valid icon
                    .error(android.R.drawable.sym_def_app_icon)
                    .into(ivProfile);
        }
    }
}