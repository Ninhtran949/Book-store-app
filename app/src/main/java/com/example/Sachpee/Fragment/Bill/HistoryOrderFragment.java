package com.example.Sachpee.Fragment.Bill;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
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


import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HistoryOrderFragment extends Fragment {

    private RecyclerView rvBill;
    private LinearLayoutManager linearLayoutManager;
    private AdapterBill adapterBill;
    private List<Bill> listBill = new ArrayList<>();
    private Context context;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_history_order, container, false);
        initUi(view);
        return view;
    }
    public void initUi(View view ){
        getBill();
        rvBill = view.findViewById(R.id.rv_billHistory);
        linearLayoutManager = new LinearLayoutManager(getContext());
        rvBill.setLayoutManager(linearLayoutManager);
        ApiService apiService = ApiClient.getRetrofitInstance(context).create(ApiService.class);
        adapterBill = new AdapterBill(listBill,getContext(),apiService);
        rvBill.setAdapter(adapterBill);
    }
    public void getBill() {
        SharedPreferences preferences = getContext().getSharedPreferences("My_User", Context.MODE_PRIVATE);
        String user = preferences.getString("username", "");

        ApiService apiService = ApiClient.getRetrofitInstance(context).create(ApiService.class);

        // Gọi API để lấy danh sách bill
        Call<List<Bill>> call = apiService.getAllBills();
        call.enqueue(new Callback<List<Bill>>() {
            @Override
            public void onResponse(Call<List<Bill>> call, Response<List<Bill>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listBill.clear();

                    // Lấy dữ liệu từ response
                    List<Bill> bills = response.body();
                    Log.d("Bill", "Received " + bills.size() + " bills");

                    for (Bill bill : bills) {
                        if (user.equals(bill.getIdPartner()) && bill.getStatus().equals("Yes")) {
                            listBill.add(bill);
                        } else if (user.equals(bill.getIdClient()) && bill.getStatus().equals("Yes")) {
                            listBill.add(bill);
                        }
                    }
                    adapterBill.notifyDataSetChanged();
                    Log.d("Bill", "Bills filtered and added to list. Total: " + listBill.size());
                } else {
                    Log.e("Bill", "Response not successful: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Bill>> call, Throwable t) {
                Log.e("getBill", "API call failed: " + t.getMessage());
            }
        });
    }

}