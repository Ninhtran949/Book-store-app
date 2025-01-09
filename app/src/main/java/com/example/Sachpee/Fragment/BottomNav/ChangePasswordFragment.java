package com.example.Sachpee.Fragment.BottomNav;

import static com.example.Sachpee.constant.Profile.FIELDS_EMPTY;
import static com.example.Sachpee.constant.Profile.PASSWORD_INVALID;
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
import com.example.Sachpee.Model.User;
import com.example.Sachpee.Service.ApiClient;
import com.example.Sachpee.Service.ApiService;
import com.example.Sachpee.databinding.FragmentChangePasswordBinding;
import com.example.Sachpee.utils.Utils;
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
        if (mUser != null) {
            changePasswordUser();
        }
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
                validate(strOldPass, strNewPass, strConfirmPass);
                Log.d(TAG, "changePass: change password");

                // Tạo đối tượng ApiService và gọi API để cập nhật mật khẩu user
                ApiService apiService = ApiClient.getRetrofitInstance(context).create(ApiService.class);
                Map<String, String> requestBody = new HashMap<>();
                requestBody.put("oldPassword", strOldPass);
                requestBody.put("newPassword", strNewPass);

                Call<Void> call = apiService.changeUserPassword(mUser.getId(), requestBody);

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
                            Toast.makeText(requireContext(), "Lỗi phản hồi API: " + response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e(TAG, "onFailure: Lỗi khi gọi API " + t.getMessage());
                        Toast.makeText(requireContext(), "Lỗi khi gọi API: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
        mUser = mProfileFragment.getUser().getValue();
    }

    public void initUi(){
        oldPass = binding.textChangePasswordFragmentOldPass;
        newPass = binding.textChangePasswordFragmentNewPass;
        reNewPass = binding.textChangePasswordFragmentReNewPass;
        btnChangePass = binding.btnChangePasswordFragmentChange;
    }

    private void validate(String oldPasswordInput, String newPassword, String ConfirmPassword) {
        if (oldPasswordInput.isEmpty() || newPassword.isEmpty() || ConfirmPassword.isEmpty())
            throw new NullPointerException(FIELDS_EMPTY);
        if (newPassword.length() < 6) throw new IllegalArgumentException(PASSWORD_INVALID);
        if (!ConfirmPassword.equals(newPassword)) throw new IllegalArgumentException(PASSWORD_NOT_MATCH);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}