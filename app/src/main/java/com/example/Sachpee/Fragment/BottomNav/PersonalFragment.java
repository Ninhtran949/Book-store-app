package com.example.Sachpee.Fragment.BottomNav;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.example.Sachpee.Activity.SignInActivity;
import com.example.Sachpee.Fragment.Profile.ProfileFragment;
import com.example.Sachpee.Fragment.Profile.ProfileViewModel;
import com.example.Sachpee.Model.Partner;
import com.example.Sachpee.Model.User;
import com.example.Sachpee.R;
import com.example.Sachpee.Service.ApiClient;
import com.example.Sachpee.Service.ApiService;
import com.example.Sachpee.databinding.FragmentPersonalBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PersonalFragment extends Fragment {
    private static final String TAG = "PersonalFragment";
    FragmentPersonalBinding binding;
    Button btn_logout_personal, btn_changepassword_personal, btn_login;
    TextView tvNumberPhoneUser, tvEdit;
    ImageView imgUser;
    List<User> listUser = new ArrayList<>();
    ProfileViewModel profileViewModel;
    CardView itemPerson;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("My_User", Context.MODE_PRIVATE);
        String user = sharedPreferences.getString("username", "");
        binding = FragmentPersonalBinding.inflate(inflater, container, false);
        itemPerson = binding.itemPerson;
        btn_logout_personal = binding.btnPersonalFragmentLogoutPersonal;
        btn_login = binding.btnPersonalFragmentLogin;
        btn_changepassword_personal = binding.btnPersonalFragmentChangePasswordPersonal;
        tvNumberPhoneUser = binding.tvPersonalFragmentNumberPhoneUser;
        tvEdit = binding.tvPersonalFragmentEditUser;
        imgUser = binding.imgPersonalFragmentImgUser;
        imgUser.setImageResource(R.drawable.ic_avatar_default);
        if (user.equals("")) {
            btn_logout_personal.setVisibility(View.GONE);
            btn_login.setVisibility(View.VISIBLE);
            itemPerson.setEnabled(false);
            imgUser.setImageResource(R.drawable.ic_avatar_default);
            btn_changepassword_personal.setVisibility(View.GONE);
        } else {
            btn_login.setVisibility(View.GONE);
            btn_logout_personal.setVisibility(View.VISIBLE);
            itemPerson.setEnabled(true);
        }
        Log.d(TAG, "onCreateView: ");
        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        btn_login.setOnClickListener(view -> {
            startActivity(new Intent(getContext(), SignInActivity.class));
        });

        btn_logout_personal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userLogout();
            }
        });

        btn_changepassword_personal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.drawer_layout, new ChangePasswordFragment()).addToBackStack(null).commit();
            }
        });

        itemPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.drawer_layout, new ProfileFragment()).addToBackStack(null).commit();
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        User user = profileViewModel.getUser().getValue();
        Partner partner = profileViewModel.getPartner().getValue();
        if (user != null) {
            profileViewModel.getUser().observe(getViewLifecycleOwner(), new Observer<User>() {
                @Override
                public void onChanged(User user) {
                    Log.d("TAG", "onChanged: ");
                    tvNumberPhoneUser.setText(user.getName());
                    Glide.with(requireActivity()).load(user.getStrUriAvatar()).error(R.drawable.ic_avatar_default).into(imgUser);
                }
            });
        } else if (partner != null) {
            profileViewModel.getPartner().observe(getViewLifecycleOwner(), new Observer<Partner>() {
                @Override
                public void onChanged(Partner partner) {
                    Log.d("TAG", "onChanged: ");
                    try {
                        byte[] decodeString = Base64.decode(partner.getImgPartner(), Base64.DEFAULT);
                        tvNumberPhoneUser.setText(partner.getUserPartner() + "\n" + partner.getNamePartner());
                        Glide.with(requireActivity())
                                .load(decodeString)
                                .signature(new ObjectKey(Long.toString(System.currentTimeMillis())))
                                .error(R.drawable.ic_avatar_default)
                                .into(imgUser);
                    } catch (Exception e) {
                        Log.e(TAG, "onChanged: ", e);
                    }
                }
            });
        }
        Log.d(TAG, "onViewCreated: " + user);
        profileViewModel.getUser().observe(requireActivity(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                Log.d(TAG, "onChanged: ");
            }
        });
    }

    public void remember() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("My_User", getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", "");
        editor.putString("password", "");
        editor.putString("role", "");
        editor.putString("id", "");
        editor.putBoolean("remember", false);
        editor.apply();
    }

    private void userLogout() {
        // Lấy refresh token từ SharedPreferences
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("My_User", Context.MODE_PRIVATE);
        String refreshToken = sharedPreferences.getString("refreshToken", "");

        // Hiển thị giá trị của refresh token
        Log.d(TAG, "Refresh token: " + refreshToken);

        // Gọi API đăng xuất
        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("refreshToken", refreshToken);
        Call<ResponseBody> call = apiService.logoutUser(requestBody);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Logout successful");
                    // Xóa thông tin người dùng và token
                    remember();
                    // Chuyển đến SignInActivity
                    Intent intent = new Intent(getActivity(), SignInActivity.class);
                    startActivity(intent);
                    getActivity().finishAffinity();
                } else {
                    Log.d(TAG, "Logout failed: " + response.message());
                    Toast.makeText(getContext(), "Đăng xuất thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Logout API call failed: " + t.getMessage());
                Toast.makeText(getContext(), "Đăng xuất thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }
}