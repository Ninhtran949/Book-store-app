package com.example.Sachpee.Fragment.BottomNav;

import static com.example.Sachpee.constant.Profile.FIELDS_EMPTY;
import static com.example.Sachpee.constant.Profile.PASSWORD_INVALID;
import static com.example.Sachpee.constant.Profile.PASSWORD_INVALID_2;
import static com.example.Sachpee.constant.Profile.PASSWORD_INVALID_3;
import static com.example.Sachpee.constant.Profile.PASSWORD_NOT_MATCH;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.Sachpee.Fragment.Profile.ProfileViewModel;
import com.example.Sachpee.Model.Partner;
import com.example.Sachpee.Model.User;
import com.example.Sachpee.Service.ApiClient;
import com.example.Sachpee.Service.ApiService;
import com.example.Sachpee.databinding.FragmentChangePasswordBinding;
import com.example.Sachpee.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;


import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordFragment extends Fragment {
    private final String TAG = "ChangePasswordFragment";
    private FragmentChangePasswordBinding binding;
    private TextInputLayout oldPass, newPass, reNewPass;
    private Button btnChangePass;
    private Partner mPartner;
    private User mUser;
    private ProfileViewModel mProfileFragment;
    private Context context;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false);
        initUi();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViewModel();
        initListener();
    }

    private void initListener() {
        if (mPartner != null) {
            changePasswordPartner();
        } else if (mUser != null) {
            changePasswordUser();
        }

    }

    private void changePasswordPartner() {
        oldPass.setErrorEnabled(true);
        newPass.setErrorEnabled(true);
        reNewPass.setErrorEnabled(true);
        btnChangePass.setOnClickListener(view -> {
            ProgressDialog progressDialog = Utils.createProgressDiaglog(requireContext());
            progressDialog.show();
            oldPass.setError(null);
            newPass.setError(null);
            reNewPass.setError(null);
            try {
                String strOldPass = oldPass.getEditText().getText().toString();
                String strNewPass = newPass.getEditText().getText().toString();
                String strConfirmPass = reNewPass.getEditText().getText().toString();

                // Validate thông tin nhập
                validate(strOldPass, strNewPass, strConfirmPass, mPartner.getPasswordPartner());

                // Cập nhật mật khẩu mới cho đối tác
                mPartner.setPasswordPartner(strNewPass);
                Log.d(TAG, "changePass: change password");

                // Tạo đối tượng ApiService và gọi API để cập nhật mật khẩu đối tác
                ApiService apiService = ApiClient.getRetrofitInstance(context).create(ApiService.class);
                Call<Void> call = apiService.updatePartnerPassword(mPartner.getIdPartner(), mPartner);

                // Thực hiện gọi API
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "onComplete: Đổi mật khẩu đối tác thành công");
                            progressDialog.dismiss();
                            oldPass.getEditText().setText("");
                            newPass.getEditText().setText("");
                            reNewPass.getEditText().setText("");
                            mProfileFragment.setPartner(mPartner);
                            Toast.makeText(requireContext(), "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "onResponse: Lỗi phản hồi API " + response.code());
                            progressDialog.dismiss();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e(TAG, "onFailure: Lỗi khi gọi API " + t.getMessage());
                        progressDialog.dismiss();
                    }
                });

            } catch (NullPointerException e) {
                progressDialog.dismiss();
                if (e.getMessage().equals(FIELDS_EMPTY)) {
                    setErrorEmpty();
                } else {
                    Log.e(TAG, "changePasswordPartner: ", e);
                }
            } catch (IllegalArgumentException e) {
                progressDialog.dismiss();
                if (e.getMessage().equals(PASSWORD_INVALID)) {
                    newPass.setError("Mật khẩu phải từ 6 kí tự trở lên");
                } else if (e.getMessage().equals(PASSWORD_NOT_MATCH)) {
                    reNewPass.setError("Mật khẩu mới không trùng nhau");
                } else if (e.getMessage().equals(PASSWORD_INVALID_2)) {
                    newPass.setError("Mật khẩu mới không được giống mật khẩu cũ");
                } else if (e.getMessage().equals(PASSWORD_INVALID_3)) {
                    oldPass.setError("Mật khẩu cũ không đúng");
                } else {
                    Log.e(TAG, "changePasswordPartner: ", e);
                }
            } catch (Exception e) {
                progressDialog.dismiss();
                Log.e(TAG, "changePasswordPartner: ", e);
            }
        });
    }


    private void changePasswordUser() {
        oldPass.setErrorEnabled(true);
        newPass.setErrorEnabled(true);
        reNewPass.setErrorEnabled(true);
        btnChangePass.setOnClickListener(view -> {
            oldPass.setError(null);
            newPass.setError(null);
            reNewPass.setError(null);
            try {
                String strOldPass = oldPass.getEditText().getText().toString();
                String strNewPass = newPass.getEditText().getText().toString();
                String strConfirmPass = reNewPass.getEditText().getText().toString();

                // Validate thông tin mật khẩu nhập vào
                validate(strOldPass, strNewPass, strConfirmPass, mUser.getPassword());
                mUser.setPassword(strNewPass);
                Log.d(TAG, "changePass: change password");

                // Sử dụng Retrofit để cập nhật mật khẩu mới
                ApiService apiService = ApiClient.getRetrofitInstance(context).create(ApiService.class);
                Call<Void> call = apiService.updateUserPassword(mUser.getId(), mUser);

                // Thực hiện gọi API
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "onComplete: Đổi mật khẩu user thành công");
                            oldPass.getEditText().setText("");
                            newPass.getEditText().setText("");
                            reNewPass.getEditText().setText("");
                            mProfileFragment.setUser(mUser);
                            Toast.makeText(requireContext(), "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "onResponse: Lỗi phản hồi API " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e(TAG, "onFailure: Lỗi khi gọi API " + t.getMessage());
                    }
                });

            } catch (NullPointerException e) {
                if (e.getMessage().equals(FIELDS_EMPTY)) {
                    setErrorEmpty();
                } else {
                    Log.e(TAG, "changePasswordUser: ", e);
                }
            } catch (IllegalArgumentException e) {
                if (e.getMessage().equals(PASSWORD_INVALID)) {
                    newPass.setError("Mật khẩu phải từ 6 kí tự trở lên");
                } else if (e.getMessage().equals(PASSWORD_NOT_MATCH)) {
                    reNewPass.setError("Mật khẩu mới không trùng nhau");
                } else if (e.getMessage().equals(PASSWORD_INVALID_2)) {
                    newPass.setError("Mật khẩu mới không được giống mật khẩu cũ");
                } else if (e.getMessage().equals(PASSWORD_INVALID_3)) {
                    oldPass.setError("Mật khẩu cũ không đúng");
                } else {
                    Log.e(TAG, "changePasswordUser: ", e);
                }
            } catch (Exception e) {
                Log.e(TAG, "changePasswordUser: ", e);
            }
        });
    }


    private void setErrorEmpty() {
        if (oldPass.getEditText().getText().toString().isEmpty()) oldPass.setError("Không được để trống");
        if (newPass.getEditText().getText().toString().isEmpty()) newPass.setError("Không được để trống");
        if (reNewPass.getEditText().getText().toString().isEmpty()) reNewPass.setError("Không được để trống");

    }


    private void initViewModel() {
        mProfileFragment = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        mPartner = mProfileFragment.getPartner().getValue();
        mUser = mProfileFragment.getUser().getValue();
    }

    public void initUi(){
        oldPass = binding.textChangePasswordFragmentOldPass;
        newPass = binding.textChangePasswordFragmentNewPass;
        reNewPass = binding.textChangePasswordFragmentReNewPass;
        btnChangePass = binding.btnChangePasswordFragmentChange;
    }
    @Deprecated
    public boolean validate(){
        if (oldPass.getEditText().getText().toString().isEmpty() && newPass.getEditText().getText().toString().isEmpty() && reNewPass.getEditText().getText().toString().isEmpty()){

            oldPass.setError("không được để trống");
            newPass.setError("không được để trống");
            reNewPass.setError("không được để trống");
            return false;
        } else {
            oldPass.setError(null);
            newPass.setError(null);
            reNewPass.setError(null);
        }
        if (oldPass.getEditText().getText().toString().isEmpty()){
            oldPass.setError("không được để trống");
            return false;
        } else {
            oldPass.setError(null);
        }

        if (newPass.getEditText().getText().toString().isEmpty()){
            newPass.setError("không được để trống");
            return false;
        } else {
            newPass.setError(null);
        }

        if (reNewPass.getEditText().getText().toString().isEmpty()){
            reNewPass.setError("không được để trống");
            return false;
        } else if (!reNewPass.getEditText().getText().toString().equals(newPass.getEditText().getText().toString())){
            reNewPass.setError("Mật khẩu nhập lại không trùng khớp");
            return false;
        } else {
            reNewPass.setError(null);
        }
        return true;
    }
    private void validate(String oldPasswordInput,
                          String newPassword,
                          String ConfirmPassword,
                          String oldPasswordAccount) {
        if (oldPasswordInput.isEmpty() ||
                newPassword.isEmpty() ||
                ConfirmPassword.isEmpty())
            throw new NullPointerException(FIELDS_EMPTY);
        if (newPassword.length() < 6) throw  new IllegalArgumentException(PASSWORD_INVALID);
        if (!ConfirmPassword.equals(newPassword)) throw new IllegalArgumentException(PASSWORD_NOT_MATCH);
        if (newPassword.equals(oldPasswordInput)) throw new IllegalArgumentException(PASSWORD_INVALID_2);
        if (!oldPasswordInput.equals(oldPasswordAccount)) throw new IllegalArgumentException(PASSWORD_INVALID_3);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}