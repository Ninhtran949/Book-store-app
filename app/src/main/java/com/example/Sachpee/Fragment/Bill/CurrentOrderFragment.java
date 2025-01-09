package com.example.Sachpee.Fragment.Bill;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.Sachpee.Adapter.AdapterBill;
import com.example.Sachpee.Model.Bill;
import com.example.Sachpee.R;
import com.example.Sachpee.Service.ApiClient;
import com.example.Sachpee.Service.ApiService;
import com.example.Sachpee.databinding.FragmentBillBinding;


import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CurrentOrderFragment extends Fragment {

    private RecyclerView rvBill;
    private LinearLayoutManager linearLayoutManager;
    private AdapterBill adapterBill;
    private List<Bill> listBill = new ArrayList<>();
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view= inflater.inflate(R.layout.fragment_current_order, container, false);
        initUi(view);
        return view;
    }
    public void initUi(View view ){
        getBill();
        rvBill = view.findViewById(R.id.rv_billCurrent);
        linearLayoutManager = new LinearLayoutManager(getContext());
        rvBill.setLayoutManager(linearLayoutManager);
        ApiService apiService = ApiClient.getRetrofitInstance(context).create(ApiService.class);
        adapterBill = new AdapterBill(listBill,getContext(),apiService);
        rvBill.setAdapter(adapterBill);
    }
    public void getBill() {
        // Lấy thông tin người dùng từ SharedPreferences
        SharedPreferences preferences = getContext().getSharedPreferences("My_User", Context.MODE_PRIVATE);
        String user = preferences.getString("username", "");

        // Tạo instance của ApiService
        ApiService apiService = ApiClient.getRetrofitInstance(context).create(ApiService.class);

        // Gọi API để lấy danh sách bill
        Call<List<Bill>> call = apiService.getAllBills();
        call.enqueue(new Callback<List<Bill>>() {
            @Override
            public void onResponse(Call<List<Bill>> call, Response<List<Bill>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listBill.clear();  // Xóa danh sách cũ

                    List<Bill> bills = response.body();  // Lấy danh sách bill từ API

                    // Lọc các bill theo điều kiện
                    for (Bill bill : bills) {
                        if (user.equals(bill.getIdPartner()) && "No".equals(bill.getStatus())) {
                            listBill.add(bill);
                        } else if (user.equals(bill.getIdClient()) && "No".equals(bill.getStatus())) {
                            listBill.add(bill);
                        }
                    }

                    // Cập nhật adapter
                    adapterBill.notifyDataSetChanged();
                    Log.d("getBill", "Bills filtered and added to list. Total: " + listBill.size());
                } else {
                    Log.e("getBill", "Response not successful: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Bill>> call, Throwable t) {
                Log.e("getBill", "API call failed: " + t.getMessage());
            }
        });
    }

    public  void notification(){
        String CHANNEL_ID="1234";

        Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ getContext().getPackageName() + "/" + R.raw.sound);
        NotificationManager mNotificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        //For API 26+ you need to put some additional code like below:
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

        //General code:
        NotificationCompat.Builder status = new NotificationCompat.Builder(getContext(),CHANNEL_ID);
        status.setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                //.setOnlyAlertOnce(true)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Bạn có đơn hàng mới")
                .setDefaults(Notification.DEFAULT_LIGHTS )
                .setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE+ "://" +getContext().getPackageName()+"/"+R.raw.sound))
                .build();


        mNotificationManager.notify((int)System.currentTimeMillis(), status.build());

    }
}