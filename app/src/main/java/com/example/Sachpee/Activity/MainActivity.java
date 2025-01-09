package com.example.Sachpee.Activity;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.example.Sachpee.Fragment.Profile.ProfileViewModel;
import com.example.Sachpee.Model.Bill;
import com.example.Sachpee.Model.Cart;
import com.example.Sachpee.Model.Partner;
import com.example.Sachpee.Model.User;
import com.example.Sachpee.R;
import com.example.Sachpee.Service.ApiClient;
import com.example.Sachpee.Service.ApiService;
import com.example.Sachpee.Service.ConnectionReceiver;
import com.example.Sachpee.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

import retrofit2.Callback;
import retrofit2.Response;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONObject;
// TODO Done
public class MainActivity extends AppCompatActivity{
    public static final String TAG = "MainActivity";
    public static final int MY_REQUEST_CODE = 10;

    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private AppBarConfiguration mAppBarConfiguration;

    private ActivityMainBinding binding;

    private ImageView ivAvatar;
    private TextView tvUserName;
    private TextView tvUserEmail;

    private TextView cartBadgeTextView; // Khai báo biến thành viên

    private User user;
    private Partner partner;

    private ProfileViewModel profileViewModel;
    private List<Bill> listBill = new ArrayList<>();


    ConnectionReceiver connectionReceiver = new ConnectionReceiver();

    // Khởi tạo Socket.io client trong Activity
    Socket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.appBarMain.toolbar);
        initUI();

        // Kiểm tra nếu Activity được mở qua Intent với dữ liệu từ URL
        Intent intent = getIntent();
        Uri data = intent.getData();

        if (data != null) {
            handlePaymentResult(data);
        }


        // setContentView(R.layout.activity_main);

        initViewModel();
        checkUser();
        SharedPreferences preferences1 = getSharedPreferences("Number", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences1.edit();
        String number = "0";
        editor.putString("number", "" + number);
        editor.apply();
        getBill();
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home,
                R.id.nav_Product,
                R.id.nav_Bill,
                R.id.nav_Partner,
                R.id.nav_F)
                .setOpenableLayout(mDrawerLayout)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(mNavigationView, navController);

        // Khởi tạo Socket.io client
        try {
            // Địa chỉ IP hoặc tên miền thực tế của server
            mSocket = IO.socket("http://192.168.0.4:8080"); // Ví dụ: sử dụng địa chỉ IP cục bộ
            mSocket.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Lắng nghe sự kiện cập nhật giỏ hàng
        mSocket.on("cartUpdated", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d(TAG, "cartUpdated event received"); // Thêm log để kiểm tra
                JSONObject data = (JSONObject) args[0];

                // Cập nhật giao diện khi giỏ hàng thay đổi
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Cập nhật Badge giỏ hàng
                        updateCartBadge();
                    }
                });
            }
        });
    }



    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Khi Activity đã tồn tại và nhận được Intent mới
        Uri data = intent.getData();
        if (data != null) {
            handlePaymentResult(data);
        }
    }

    private void handlePaymentResult(Uri data) {
        String status = data.getQueryParameter("status");
        String appTransId = data.getQueryParameter("apptransid");

        Log.d(TAG, "Payment Status: " + status);
        Log.d(TAG, "Transaction ID: " + appTransId);

        if ("1".equals(status)) {
            // Thanh toán thành công
            Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_LONG).show();
            // Chuyển đến Fragment hoặc cập nhật giao diện
            navigateToFragment();
        } else {
            // Thanh toán thất bại hoặc hủy
            Toast.makeText(this, "Thanh toán thất bại hoặc đã hủy.", Toast.LENGTH_LONG).show();
            // Chuyển đến Fragment hoặc cập nhật giao diện
            navigateToFragment();
        }
    }

    private void navigateToFragment() {
        //  Navigation Component để chuyển đến Fragment
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        navController.navigate(R.id.nav_home);
    }


    // Phương thức cập nhật Badge giỏ hàng
    private void updateCartBadge() {
        SharedPreferences preferences = getSharedPreferences("My_User", MODE_PRIVATE);
        String user = preferences.getString("username", "");

        // Sử dụng Retrofit để lấy dữ liệu từ MongoDB
        ApiService apiService = ApiClient.getRetrofitInstance(this).create(ApiService.class);
        Call<List<Cart>> call = apiService.getCartsByUser(user);
        call.enqueue(new Callback<List<Cart>>() {
            @Override
            public void onResponse(Call<List<Cart>> call, Response<List<Cart>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Cart> carts = response.body();
                    cartBadgeTextView.setText(String.valueOf(carts.size()));
                    cartBadgeTextView.setVisibility(carts.size() > 0 ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<Cart>> call, Throwable t) {
                // Xử lý lỗi
                Log.e("API_ERROR", t.getMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        final MenuItem menuItem = menu.findItem(R.id.btn_Actionbar_cart);
        View actionView = menuItem.getActionView();
        MenuItem menuItem1 = menu.findItem(R.id.searchProduct);
        menuItem1.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
                return true;
            }
        });


        cartBadgeTextView = actionView.findViewById(R.id.tv_CartActionItem_cart_badge); // Khởi tạo biến thành viên
        cartBadgeTextView.setVisibility(View.GONE);                       //shape tăng số lượng khi thêm sản phẩm vào cart chưa hoạt động

        SharedPreferences preferences = getSharedPreferences("My_User", MODE_PRIVATE);
        String user = preferences.getString("username", "");

        // Tạo danh sách tạm thời để lưu trữ giỏ hàng
        List<Cart> list1 = new ArrayList<>();

        // Sử dụng Retrofit để lấy dữ liệu từ MongoDB
        ApiService apiService = ApiClient.getRetrofitInstance(this).create(ApiService.class);
        Call<List<Cart>> call = apiService.getCartsByUser(user);
        call.enqueue(new Callback<List<Cart>>() {
            @Override
            public void onResponse(Call<List<Cart>> call, Response<List<Cart>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    list1.clear();
                    list1.addAll(response.body()); // Lưu trữ giỏ hàng vào list1
                    cartBadgeTextView.setText(String.valueOf(list1.size()));
                    cartBadgeTextView.setVisibility(list1.size() > 0 ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<Cart>> call, Throwable t) {
                // Xử lý lỗi
                Log.e("API_ERROR", t.getMessage());
            }
        });

        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CartActivity.class));
            }
        });

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSocket != null) {
            mSocket.disconnect();
            mSocket.off("cartUpdated");
        }
    }

    //TODO: thử chuyển method sang ProfileFragment
    private final ActivityResultLauncher<Intent> mActivityResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult()
                    , new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == RESULT_OK) {
                                Intent intent = result.getData();
                                if (intent != null && intent.getData() != null) {
                                    Uri uriImage = intent.getData();
                                    Bitmap selectedImageBitmap = null;
                                    try {
                                        selectedImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriImage);
                                        Log.d(TAG, "onActivityResult: " + selectedImageBitmap.toString());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    Log.d(TAG, "onActivityResult: ");
                                    profileViewModel.setBitmapImageAvatar(selectedImageBitmap);
                                }
                            }
                        }
                    });
    public void checkUser() {
        SharedPreferences sharedPreferences = getSharedPreferences("My_User", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");
        String userRule = sharedPreferences.getString("role", "");
        String userId = sharedPreferences.getString("id", ""); // userId vẫn là String lúc lấy ra từ SharedPreferences se k loi

        if (userRule.equals("admin")) {
            Log.d(TAG, "checkUser: admin");
            mNavigationView.setVisibility(View.VISIBLE);
            mNavigationView.getMenu().findItem(R.id.nav_F).setVisible(false);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        } else if (userRule.equals("partner")) {
            Log.d(TAG, "checkUser: partner");
            // Chuyển đổi userId từ String sang int
            int idPartner = Integer.parseInt(userId);
            loadPartnerInfoById(idPartner); // truyền idPartner kiểu int
            try {
                setPartnerViewModelObserver();
            } catch (Exception e) {
                Log.e(TAG, "checkUser: ", e);
            }

        } else if (userRule.equals("user")) {
            loadUserInfoById(username);
            setUserViewModelObserver();
            mNavigationView.setVisibility(View.GONE);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            Log.d(TAG, "checkUser: user");
        } else {
            mNavigationView.setVisibility(View.GONE);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }


    private void setPartnerViewModelObserver() throws Exception {
        final Observer<Partner> partnerObserver = new Observer<Partner>() {
            @Override
            public void onChanged(Partner partner) {
                Log.d(TAG, "onChanged: change user information");
                tvUserEmail.setText(partner.getUserPartner());
                tvUserName.setText(partner.getNamePartner());
                SharedPreferences sharedPreferences = getSharedPreferences("My_User",MODE_PRIVATE);
                String password = sharedPreferences.getString("password","");
                if (!partner.getPasswordPartner().equals(password)) {
                    sharedPreferences.edit().putString("password", partner.getPasswordPartner()).commit();
                }
                byte[] decodeString = Base64.decode(partner.getImgPartner(), Base64.DEFAULT);
                Glide.with(MainActivity.this)
                        .load(decodeString)
                        .error(R.drawable.ic_avatar_default)
                        .signature(new ObjectKey(Long.toString(System.currentTimeMillis())))
                        .into(ivAvatar);
                Log.d(TAG, "onChanged: " + partner.toString());
            }

        };
        profileViewModel.getPartner().observe(this, partnerObserver);
    }
    public void loadUserInfoById(String phoneNumber) {
        Log.d(TAG, "loadUserInfoById: ");
        Log.d(TAG, "loadUserInfoById: " + phoneNumber);

        // Khởi tạo Retrofit
        ApiService apiService = ApiClient.getRetrofitInstance(this).create(ApiService.class);

        // Gọi API để lấy thông tin người dùng
        Call<User> call = apiService.getUserByPhoneNumber(phoneNumber);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    User user = response.body();
                    Log.d(TAG, "onResponse: " + user);
                    if (user != null) {
                        MainActivity.this.user = user;
                        profileViewModel.setUser(user);
                    }
                } else {
                    Log.e(TAG, "onResponse: Failed to get user");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "onFailure: ", t);
            }
        });
    }
    private void loadPartnerInfoById(int idPartner) {
        partner = new Partner();
        Log.d(TAG, "loadPartnerInfoById: " + idPartner);

        ApiService apiService = ApiClient.getRetrofitInstance(this).create(ApiService.class);
        // Gọi API để lấy thông tin partner

        Call<Partner> call = apiService.getPartnerById(idPartner);

        call.enqueue(new Callback<Partner>() {
            @Override
            public void onResponse(Call<Partner> call, Response<Partner> response) {
                if (response.isSuccessful() && response.body() != null) {
                    partner = response.body();
                    Log.d(TAG, "onResponse: " + partner);
                    try {
                        showPartnerInformation();
                    } catch (Exception e) {
                        Log.e(TAG, "onResponse: ", e);
                    }
                } else {
                    Log.e(TAG, "onResponse: Không tìm thấy partner với id " + idPartner);
                }
            }

            @Override
            public void onFailure(Call<Partner> call, Throwable t) {
                Log.e(TAG, "onFailure: ", t);
            }
        });
    }

    private void showPartnerInformation() throws Exception {
        profileViewModel.setPartner(partner);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        mNavigationView.setVisibility(View.VISIBLE);
        mNavigationView.getMenu().findItem(R.id.nav_Product).setVisible(false);
        mNavigationView.getMenu().findItem(R.id.nav_Partner).setVisible(false);
    }


    @Deprecated
    private void setUserViewModelObserver() {
        final Observer<User> userObserver = new Observer<User>() {
            @Override
            public void onChanged(User user1) {
                Log.d(TAG, "onChanged: change user information");
                tvUserEmail.setText(user1.getPhoneNumber());
                tvUserName.setText(user1.getName());
                SharedPreferences sharedPreferences = getSharedPreferences("My_User",MODE_PRIVATE);
                String password = sharedPreferences.getString("password","");
                if (!user1.getPassword().equals(password)) {
                    sharedPreferences.edit().putString("password", user1.getPassword()).commit();
                }
                Glide.with(MainActivity.this)
                        .load(user1.getStrUriAvatar())
                        .error(R.drawable.ic_avatar_default)
                        .signature(new ObjectKey(Long.toString(System.currentTimeMillis())))
                        .into(ivAvatar);
                Log.d(TAG, "onChanged: " + user1.toString());
            }

        };
        profileViewModel.getUser().observe(this, userObserver);
    }

    private void initViewModel() {
        this.profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

    }

    private void initUI() {
        mDrawerLayout = binding.drawerLayout;
        mNavigationView = binding.navView;
        ivAvatar = mNavigationView.getHeaderView(0).findViewById(R.id.iv_MainActivity_avatar);
        tvUserName = mNavigationView.getHeaderView(0).findViewById(R.id.tv_MainActivity_username);
        tvUserEmail = mNavigationView.getHeaderView(0).findViewById(R.id.tv_MainActivity_userEmail);
    }

 //3


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }



    private void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences("My_User",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("logged", false);
        editor.commit();
        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        startActivity(intent);
        finishAffinity();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Log.d(TAG, "onRequestPermissionsResult: Please enable read external permission !");
                Toast.makeText(this, "Please enable read external permission !", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mActivityResultLauncher.launch(Intent.createChooser(intent, "Select picture"));
        Log.d(TAG, "openGallery: openGallery method");
    }
    public void getBill() {
        SharedPreferences preferences = getSharedPreferences("My_User", Context.MODE_PRIVATE);
        String user = preferences.getString("username", "");

        ApiService apiService = ApiClient.getRetrofitInstance(this).create(ApiService.class);

        Call<List<Bill>> call = apiService.getBillsByStatusNo(user);
        call.enqueue(new Callback<List<Bill>>() {
            @Override
            public void onResponse(Call<List<Bill>> call, Response<List<Bill>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listBill.clear();
                    listBill.addAll(response.body());

                    SharedPreferences preferences1 = getSharedPreferences("Number", MODE_PRIVATE);
                    int number = Integer.parseInt(preferences1.getString("number", ""));
                    Log.d("CartBadge", "Current number from preferences: " + number);

                    if (listBill.size() > number) {
                        Log.d("CartBadge", "New bill count is greater, updating badge");
                        notification();
                        SharedPreferences.Editor editor = preferences1.edit();
                        editor.putString("number", String.valueOf(listBill.size()));
                        editor.apply();
                        Log.d("CartBadge", "Updated number: " + listBill.size());
                    } else {
                        Log.d("CartBadge", "Bill count is not greater, no update needed");
                        SharedPreferences.Editor editor = preferences1.edit();
                        editor.putString("number", String.valueOf(listBill.size()));
                        editor.apply();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Bill>> call, Throwable t) {
                Log.e("getBill", "onFailure: " + t.getMessage());
            }
        });
    }


    public  void notification(){
        String CHANNEL_ID="1234";

        Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ getPackageName() + "/" + R.raw.sound);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel mChannel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(CHANNEL_ID, "Thông báo", NotificationManager.IMPORTANCE_HIGH);
            mChannel.setLightColor(Color.GRAY);
            mChannel.enableLights(true);
            mChannel.setDescription("Chuông thông báo");
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            mChannel.setSound(soundUri, audioAttributes);
            mChannel.setVibrationPattern( new long []{ 100 , 200 , 300 , 400 , 500 , 400 , 300 , 200 , 400 }) ;

            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel( mChannel );
            }
        }

        NotificationCompat.Builder status = new NotificationCompat.Builder(this,CHANNEL_ID);
        status.setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Bạn có đơn hàng mới")
                .setDefaults(Notification.DEFAULT_LIGHTS )
                .setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE+ "://" +getPackageName()+"/"+R.raw.sound))
                .build();


        mNotificationManager.notify((int)System.currentTimeMillis(), status.build());

    }

    @Override
    protected void onStart() {
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectionReceiver, intentFilter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(connectionReceiver);
        super.onStop();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}