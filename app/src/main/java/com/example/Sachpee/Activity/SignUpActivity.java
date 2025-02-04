package com.example.Sachpee.Activity;


import static com.example.Sachpee.constant.Profile.FIELDS_EMPTY;
import static com.example.Sachpee.constant.Profile.NUMBER_PHONE_INVALID;
import static com.example.Sachpee.constant.Profile.PASSWORD_INVALID;
import static com.example.Sachpee.constant.Profile.PASSWORD_NOT_MATCH;
import static com.example.Sachpee.constant.Profile.REGEX_PHONE_NUMBER;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.Sachpee.Model.User;
import com.example.Sachpee.R;
import com.example.Sachpee.Service.ApiClient;
import com.example.Sachpee.Service.ApiService;
import com.example.Sachpee.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "SignUpActivity";
    private TextInputLayout mFormPhoneNumber,
            mFormUserName,
            mFormPassword,
            mFormConfirmPassword,
            mFormAddress;
    private Button mBtnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initUI();
        getSupportActionBar().hide(); // Ẩn actionbar
    }

    private void initUI() {
        mFormPhoneNumber = findViewById(R.id.form_SignUpActivity_phone_number);
        mFormUserName = findViewById(R.id.form_SignUpActivity_user_name);
        mFormPassword = findViewById(R.id.form_SignUpActivity_password);
        mFormConfirmPassword = findViewById(R.id.form_SignUpActivity_confirmPassword);
        mFormAddress = findViewById(R.id.form_SignUpActivity_address);
        mBtnSignUp = findViewById(R.id.btn_SignUpActivity_signUp);
        setOnclickListener();
    }

    private void setOnclickListener() {
        mBtnSignUp.setOnClickListener(this::onClick);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_SignUpActivity_signUp:
                signUp();
                break;
        }
    }

    private void signUp() {
        // Xóa lỗi hiện tại trên các form
        mFormPhoneNumber.setError(null);
        mFormPassword.setError(null);
        mFormConfirmPassword.setError(null);
        mFormUserName.setError(null);
        mFormAddress.setError(null);

        // Lấy dữ liệu từ các form input
        String strPhoneNumber = Objects.requireNonNull(mFormPhoneNumber.getEditText()).getText().toString().trim();
        String strUserName = mFormUserName.getEditText().getText().toString().trim();
        String strPassword = mFormPassword.getEditText().getText().toString().trim();
        String strConfirmPassword = mFormConfirmPassword.getEditText().getText().toString().trim();
        String strAddress = mFormAddress.getEditText().getText().toString().trim();

        try {
            // Kiểm tra tính hợp lệ của các trường nhập liệu
            validate(strPhoneNumber, strUserName, strPassword, strConfirmPassword, strAddress);

            // Tạo đối tượng User
            User user = new User();
            user.setPhoneNumber(strPhoneNumber);
            user.setName(strUserName);
            user.setPassword(strPassword);
            user.setAddress(strAddress);
            user.setStrUriAvatar("");
            user.setId(strPhoneNumber);

            // Hiển thị ProgressDialog
            ProgressDialog progressDialog = Utils.createProgressDiaglog(SignUpActivity.this);
            progressDialog.show();

            // Gọi API kiểm tra người dùng đã tồn tại hay chưa
            ApiService apiService = ApiClient.getRetrofitInstance(this).create(ApiService.class);
            Call<User> callCheckUser = apiService.getUserByPhoneNumber(strPhoneNumber);

            callCheckUser.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    progressDialog.dismiss();

                    if (response.isSuccessful() && response.body() != null) {
                        // Người dùng đã tồn tại
                        mFormPhoneNumber.setError("Số điện thoại đã tồn tại");
                    } else if (response.code() == 404) {
                        // Người dùng không tồn tại, thực hiện đăng ký
                        registerUser(user, progressDialog);
                    } else {
                        // Lỗi khác
                        Toast.makeText(SignUpActivity.this, "Có lỗi xảy ra: " + response.message(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(SignUpActivity.this, "Không thể kết nối đến server", Toast.LENGTH_LONG).show();
                }
            });

        } catch (NullPointerException e) {
            if (e.getMessage().equals(FIELDS_EMPTY)) {
                setErrorEmpty();
            } else {
                Log.e(TAG, "signUp: ", e);
            }
        } catch (IllegalArgumentException e) {
            if (e.getMessage().equals(NUMBER_PHONE_INVALID)) {
                mFormPhoneNumber.setError("Số điện thoại không hợp lệ");
            } else if (e.getMessage().equals(PASSWORD_INVALID)) {
                mFormPassword.setError("Mật khẩu không hợp lệ");
            } else if (e.getMessage().equals(PASSWORD_NOT_MATCH)) {
                mFormConfirmPassword.setError("Mật khẩu không trùng nhau");
            } else {
                Log.e(TAG, "signUp: ", e);
            }
        } catch (Exception e) {
            Log.e(TAG, "signUp: ", e);
        }
    }

    private void registerUser(User user, ProgressDialog progressDialog) {
        ApiService apiService = ApiClient.getRetrofitInstance(this).create(ApiService.class);
        Call<Void> callSignUp = apiService.signUpUser(user);

        Log.d("SignUp", "Request Data: " + new Gson().toJson(user)); // Log dữ liệu gửi đi

        callSignUp.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    Log.d("SignUp", "Response: Success");
                    Toast.makeText(SignUpActivity.this, "Tạo tài khoản thành công", Toast.LENGTH_SHORT).show();
                    remember(user.getPhoneNumber(), user.getPassword(), "user", user.getPhoneNumber());
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finishAffinity();
                } else {
                    try {
                        Log.e("`SignUp`", "Response Error: " + response.errorBody().string());
                    } catch (IOException e) {
                        Log.e("SignUp", "Error parsing errorBody", e);
                    }
                    Toast.makeText(SignUpActivity.this, "Tạo tài khoản thất bại", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressDialog.dismiss();
                Log.e("SignUp", "Failure: " + t.getMessage(), t);
                Toast.makeText(SignUpActivity.this, "Có lỗi khi tạo tài khoản", Toast.LENGTH_LONG).show();
            }
        });
    }




    private void setErrorEmpty() {
        if (mFormPhoneNumber.getEditText().getText().toString().isEmpty()) {
            mFormPhoneNumber.setError("Số điện thoại không được để trống");
        }
        if (mFormUserName.getEditText().getText().toString().isEmpty()) {
            mFormUserName.setError("Họ tên không được để trống");
        }
        if (mFormPassword.getEditText().getText().toString().isEmpty()) {
            mFormPassword.setError("Mật khẩu không được để trống");
        }
        if (mFormConfirmPassword.getEditText().getText().toString().isEmpty()) {
            mFormConfirmPassword.setError("Xác nhận mật khẩu không được để trống");
        }
        if (mFormAddress.getEditText().getText().toString().isEmpty()) {
            mFormAddress.setError("Địa chỉ không được để trống");
        }

    }

    private void validate(String strPhoneNumber,
                          String strUserName,
                          String strPassword,
                          String strConfirmPassword,
                          String strAddress) {
        if (strPhoneNumber.isEmpty()
                || strPassword.isEmpty()
                || strConfirmPassword.isEmpty()
                || strUserName.isEmpty()
                || strAddress.isEmpty())
            throw new NullPointerException(FIELDS_EMPTY);
        if (!strPhoneNumber.matches(REGEX_PHONE_NUMBER))
            throw  new IllegalArgumentException(NUMBER_PHONE_INVALID);
        if (strPassword.length() < 6)
            throw  new IllegalArgumentException(PASSWORD_INVALID);
        if (!strConfirmPassword.equals(strPassword))
            throw new IllegalArgumentException(PASSWORD_NOT_MATCH);
    }
    public void remember(String user,String password,String role, String id){
        SharedPreferences sharedPreferences = getSharedPreferences("My_User",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", user);
        editor.putString("password", password);
        editor.putString("role", role);
        editor.putString("id", id);
        editor.putBoolean("logged",true);
        editor.putBoolean("remember", true);
        editor.apply();
    }
}