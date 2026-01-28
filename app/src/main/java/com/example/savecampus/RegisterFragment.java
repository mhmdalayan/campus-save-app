package com.example.savecampus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View view = inflater.inflate(R.layout.fragment_register, container, false);

        EditText etName = view.findViewById(R.id.etName);
        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etPassword = view.findViewById(R.id.etPassword);
        EditText etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        EditText etAdminCode = view.findViewById(R.id.etAdminCode);

        RadioGroup rgRole = view.findViewById(R.id.rgRole);
        RadioButton rbStudent = view.findViewById(R.id.rbStudent);
        RadioButton rbStaff = view.findViewById(R.id.rbStaff);

        LinearLayout adminCodeLayout = view.findViewById(R.id.adminCodeLayout);
        Button btnRegister = view.findViewById(R.id.btnRegister);

        // Show / hide admin code
        rgRole.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbStaff) {
                adminCodeLayout.setVisibility(View.VISIBLE);
            } else {
                adminCodeLayout.setVisibility(View.GONE);
                etAdminCode.setText("");
            }
        });

        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();
            String role = rbStaff.isChecked() ? "staff" : "student";
            String adminCode = etAdminCode.getText().toString().trim();

            if (name.isEmpty()) {
                etName.setError("Required");
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Invalid email");
                return;
            }

            if (password.isEmpty()) {
                etPassword.setError("Required");
                return;
            }

            if (!password.equals(confirmPassword)) {
                etConfirmPassword.setError("Passwords do not match");
                return;
            }

            if (role.equals("staff") && adminCode.isEmpty()) {
                etAdminCode.setError("Admin code required");
                return;
            }

            registerUser(name, email, password, role, adminCode);
        });
        TextView tvGoToLogin = view.findViewById(R.id.tvGoToLogin);

        tvGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            requireActivity().finish();
        });


        return view;
    }

    private void registerUser(
            String name,
            String email,
            String password,
            String role,
            String adminCode
    ) {

        String url = "http://10.0.2.2/mobileApp/register.php";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            Toast.makeText(getContext(), "Registration successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getActivity(), LoginActivity.class));
                            requireActivity().finish();
                        } else {
                            Toast.makeText(getContext(), json.getString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(getContext(), "Invalid server response", Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Network error", Toast.LENGTH_LONG).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("email", email);
                params.put("password", password);
                params.put("role", role);
                params.put("admin_code", adminCode);
                return params;
            }
        };

        queue.add(request);
    }
}
