package com.example.Sachpee.Fragment.Profile;

import static com.example.Sachpee.Activity.MainActivity.MY_REQUEST_CODE;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.example.Sachpee.Activity.MainActivity;
import com.example.Sachpee.Model.Partner;
import com.example.Sachpee.Model.User;
import com.example.Sachpee.R;
import com.example.Sachpee.Service.ApiClient;
import com.example.Sachpee.Service.ApiService;
import com.example.Sachpee.databinding.FragmentProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "ProfileFragment";
    private ProfileViewModel profileViewModel;
    private FragmentProfileBinding binding;
    private FirebaseStorage mStorage = FirebaseStorage.getInstance();
    private StorageReference mStorageReference = mStorage.getReference();
    private User user;
    private Partner partner;

    private TextInputLayout mLayoutName, mLayoutEmail, mLayoutAddress, mLayoutPhoneNumber;
    private Button btnUpdateInfoUser;
    private ImageView ivAvatar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: start");
        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        user = profileViewModel.getUser().getValue();
        partner = profileViewModel.getPartner().getValue();
        Log.d(TAG, "onCreate: " + user + "\n" + partner);
        Log.d(TAG, "onCreate: end");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: start");
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        initUI();
//        setUserInfoToView();
        Log.d(TAG, "onCreateView: end");
        return root;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initListener();
        if (user != null) showUserInformation();
        else if (partner != null) showPartnerInformation();
//        ivAvatar.setImageBitmap(user.getBitmapAvatar());
    }

    private void showPartnerInformation() {
        Log.d(TAG, "showPartnerInformation: start");
        profileViewModel.getBitmapLiveData().observe(getViewLifecycleOwner(), new Observer<Bitmap>() {
            @Override
            public void onChanged(Bitmap bitmap) {
                Glide.with(requireActivity())
                        .load(profileViewModel.getBitmapLiveData().getValue())
                        .error(R.drawable.ic_avatar_default)
                        .signature(new ObjectKey(System.currentTimeMillis()))
                        .into(ivAvatar);
            }
        });
        profileViewModel.getPartner().observe(getViewLifecycleOwner(), new Observer<Partner>() {
            @Override
            public void onChanged(Partner partner) {
                try {
                    mLayoutName.getEditText().setText(partner.getNamePartner());
//                mLayoutEmail.getEditText().setText(String.valueOf(partner.getIdPartner()));
                    mLayoutAddress.getEditText().setText(partner.getAddressPartner());
                    mLayoutPhoneNumber.getEditText().setText(partner.getUserPartner());
                    byte[] decodeString = Base64.decode(partner.getImgPartner(), Base64.DEFAULT);
                    Glide.with(requireActivity()).load(decodeString)
                            .error(R.drawable.ic_avatar_default)
                            .signature(new ObjectKey(System.currentTimeMillis()))
                            .into(ivAvatar);
                    Log.d(TAG, "onChanged() returned: " + "ảnh được lấy từ storage về");
//                Glide.with(getActivity()).load(user.getBitmapAvatar()).error(R.drawable.ic_avatar_default).into(ivAvatar);
                    Log.d(TAG, "onChanged: ");
                    Log.d(TAG, "onChanged: " + partner.toString());
                } catch (Exception e) {
                    Log.e(TAG, "onChanged: ", e);
                }

            }
        });
        Log.d(TAG, "showPartnerInformation: end");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private void initUI() {
        ivAvatar = binding.getRoot().findViewById(R.id.iv_profile_fragment_avatar);
        btnUpdateInfoUser = binding.getRoot().findViewById(R.id.btn_profile_fragment_update);
//        mLayoutEmail = binding.getRoot().findViewById(R.id.text_input_layout_profile_fragment_email);
        mLayoutName = binding.getRoot().findViewById(R.id.text_input_layout_profile_fragment_full_name);
        mLayoutAddress = binding.getRoot().findViewById(R.id.text_input_layout_profile_fragment_address);
        mLayoutPhoneNumber = binding.getRoot().findViewById(R.id.text_input_layout_profile_fragment_phone_number);
    }
    private void initListener() {
        ivAvatar.setOnClickListener(this::onClick);
        btnUpdateInfoUser.setOnClickListener(this::onClick);
    }
    public void showUserInformation() {
        Log.d(TAG, "showUserInformation: start");
        profileViewModel.getBitmapLiveData().observe(getViewLifecycleOwner(), new Observer<Bitmap>() {
            @Override
            public void onChanged(Bitmap bitmap) {
                Glide.with(requireActivity())
                        .load(profileViewModel.getBitmapLiveData().getValue())
                        .error(R.drawable.ic_avatar_default)
                        .signature(new ObjectKey(System.currentTimeMillis()))
                        .into(ivAvatar);
            }
        });
        profileViewModel.getUser().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                mLayoutName.getEditText().setText(user.getName());
//                mLayoutEmail.getEditText().setText(user.getEmail());
                mLayoutAddress.getEditText().setText(user.getAddress());
                mLayoutPhoneNumber.getEditText().setText(user.getPhoneNumber());
                if (user.getBitmapAvatar() != null) {
                    Glide.with(requireActivity())
                            .load(user.getBitmapAvatar())
                            .error(R.drawable.ic_avatar_default)
                            .into(ivAvatar);
                    user.setBitmapAvatar(null);
                    Log.d(TAG, "onChanged() returned: " + "ảnh được chọn từ thư mục ảnh");
                } else {
                    Glide.with(requireActivity())
                            .load(user.getStrUriAvatar())
                            .error(R.drawable.ic_avatar_default)
                            .signature(new ObjectKey(Long.toString(System.currentTimeMillis())))
                            .into(ivAvatar);
                    Log.d(TAG, "onChanged() returned: " + "ảnh được lấy từ storage về");
                }
//                Glide.with(getActivity()).load(user.getBitmapAvatar()).error(R.drawable.ic_avatar_default).into(ivAvatar);
                Log.d(TAG, "onChanged: ");
                Log.d(TAG, "onChanged: " + user.toString());
            }
        });
        Log.d(TAG, "showUserInformation: end");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_profile_fragment_avatar:
                Log.d(TAG, "onClick: click imageview");
                onClickRequestPermission();
                break;
            case R.id.btn_profile_fragment_update:
                Log.d(TAG, "onClick: click btn update");
                if(user != null) updateUserInfo();
                else if (partner != null) updatePartnerInfo();
                else Log.d(TAG, "onClick: không có đối tượng để update");
                break;
        }
    }

    private void updatePartnerInfo() {
        Log.d(TAG, "updatePartnerInfo: start");

        // Cập nhật thông tin đối tác từ các trường nhập liệu
        String namePartner = mLayoutName.getEditText().getText().toString();
        String addressPartner = mLayoutAddress.getEditText().getText().toString();
        String userPartner = mLayoutPhoneNumber.getEditText().getText().toString();

        // Cập nhật các giá trị mới vào đối tượng User
        partner.setNamePartner(namePartner);
        partner.setAddressPartner(addressPartner);
        partner.setUserPartner(userPartner);

        // Xử lý hình ảnh thành Base64
        Bitmap bitmap = ((BitmapDrawable) ivAvatar.getDrawable()).getBitmap();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        byte[] imgByte = outputStream.toByteArray();

        String imgPartner = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            imgPartner = java.util.Base64.getEncoder().encodeToString(imgByte);
        }

        // Sử dụng phương thức toMap để chuyển đổi Partner thành Map
        Map<String, Object> fields = partner.toMap();

        // Tạo instance của ApiService
        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);

        // Gọi API PATCH để cập nhật thông tin Partner
        Call<Void> call = apiService.updatePartner(partner.getIdPartner(), fields);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "onComplete: Partner info updated successfully.");
                    profileViewModel.setPartner(partner);  // Cập nhật Partner vào ViewModel
                } else {
                    Log.e(TAG, "onResponse: Failed to update partner. Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "onFailure: Failed to update partner. Error: " + t.getMessage());
            }
        });
    }


    private void onClickRequestPermission() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity == null) {
            Log.d(TAG, "onClickRequestPermission: android 6.0");
            return;
        }
        //check version < android 6.0
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mainActivity.openGallery();
            Log.d(TAG, "onClickRequestPermission: android > 6.0");
            return;
        }
        //check user permission when version >= android 6.0
        if (getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            mainActivity.openGallery();
        } else {
            String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE};
            getActivity().requestPermissions(permission, MY_REQUEST_CODE);
        }
    }
    private void updateUserInfo() {
        Log.d(TAG, "updateUserInfo: start");

        // Cập nhật thông tin người dùng từ các trường nhập liệu
        String nameUser = mLayoutName.getEditText().getText().toString();
        String addressUser = mLayoutAddress.getEditText().getText().toString();
        String userUser = mLayoutPhoneNumber.getEditText().getText().toString();

        // Cập nhật các giá trị mới vào đối tượng User
        user.setName(nameUser);
        user.setAddress(addressUser);
        user.setPhoneNumber(userUser);

        // Xử lý hình ảnh thành Base64
        Bitmap bitmap = ((BitmapDrawable) ivAvatar.getDrawable()).getBitmap();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        byte[] imgByte = outputStream.toByteArray();

        String imgUser;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            imgUser = java.util.Base64.getEncoder().encodeToString(imgByte);
        } else {
            imgUser = android.util.Base64.encodeToString(imgByte, android.util.Base64.DEFAULT);
        }

        // Chuyển đổi User thành Map
        Map<String, Object> fields = user.toMap();
        fields.put("avatar", imgUser); // Cập nhật hình ảnh

        // Tạo instance của ApiService
        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);

        // Gọi API PATCH để cập nhật thông tin User
        Call<Void> call = apiService.updateUser(user.getId(), fields);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "onComplete: User info updated successfully.");
                    profileViewModel.setUser(user);  // Cập nhật User vào ViewModel
                    Toast.makeText(requireContext(), "Đổi thông tin thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "onResponse: Failed to update User. Code: " + response.code() + ", Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "onFailure: Failed to update User. Error: " + t.getMessage());
            }
        });
    }


}