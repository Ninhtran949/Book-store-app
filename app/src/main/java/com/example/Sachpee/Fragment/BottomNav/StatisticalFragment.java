package com.example.Sachpee.Fragment.BottomNav;


import android.app.DatePickerDialog;
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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.example.Sachpee.Adapter.StatisticalAdapter;
import com.example.Sachpee.Model.Bill;
import com.example.Sachpee.Service.ApiClient;
import com.example.Sachpee.Service.ApiService;
import com.example.Sachpee.databinding.FragmentStatisticalBinding;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//Thong ke
public class StatisticalFragment extends Fragment {
    private final String TAG = "StatisticalFragment";
    private FragmentStatisticalBinding binding;
    private TextInputLayout fromDate, toDate;
    private Button btnSearch;
    private TextView totalRevenue, tvHide;
    final Calendar myCalendar = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    NumberFormat numberFormat = new DecimalFormat("#,##0");
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private StatisticalAdapter adapterStatistical;
    private List<Bill> listBill = new ArrayList<>();
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentStatisticalBinding.inflate(inflater, container, false);
        getBill();
        initUi();
        getDate();

        return binding.getRoot();
    }

    public void initUi() {
        fromDate = binding.textStatisticalFragmentFromDate;
        toDate = binding.textStatisticalFragmentToDate;
        btnSearch = binding.btnStatisticalFragmentSearch;
        totalRevenue = binding.tvStatisticalFragmentTotalRevenue;
        recyclerView = binding.recyclerViewStatisticalFragmentItemBill;
        adapterStatistical = new StatisticalAdapter(listBill, getContext());
        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapterStatistical);
        tvHide = binding.tvStatisticalFragmentHide;
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

                    int sum = 0;  // Biến để tính tổng doanh thu

                    // Lọc các bill theo điều kiện và tính tổng doanh thu
                    for (Bill bill : bills) {
                        if (user.equals(bill.getIdPartner()) && "Yes".equals(bill.getStatus())) {
                            listBill.add(bill);
                        }
                    }

                    // Tính tổng doanh thu
                    for (int i = 0; i < listBill.size(); i++) {
                        sum += listBill.get(i).getTotal();
                    }

                    // Cập nhật tổng doanh thu trên giao diện
                    totalRevenue.setText(numberFormat.format(sum));

                    // Cập nhật adapter
                    adapterStatistical.notifyDataSetChanged();
                    Log.d("getBill", "Bills filtered and added to list. Total revenue: " + sum);
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


    public void getDate() {
        DatePickerDialog.OnDateSetListener startDate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, day);
                fromDate.getEditText().setText(sdf.format(myCalendar.getTime()));
            }
        };

        fromDate.setStartIconOnClickListener(view -> {
            new DatePickerDialog(getContext(), startDate, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        DatePickerDialog.OnDateSetListener endDate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, day);
                toDate.getEditText().setText(sdf.format(myCalendar.getTime()));
            }
        };

        toDate.setStartIconOnClickListener(view -> {
            new DatePickerDialog(getContext(), endDate, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnSearch.setOnClickListener(view -> {
            SharedPreferences preferences = getContext().getSharedPreferences("My_User", Context.MODE_PRIVATE);
            String user = preferences.getString("username", "");
            recyclerView.setVisibility(View.GONE);
            tvHide.setVisibility(View.GONE);
            String startdate = fromDate.getEditText().getText().toString();
            String todate = toDate.getEditText().getText().toString();

            if (startdate.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng chọn ngày bắt đầu", Toast.LENGTH_SHORT).show();
            } else if (todate.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng chọn ngày kết thúc", Toast.LENGTH_SHORT).show();
            } else {
                ApiService apiService = ApiClient.getRetrofitInstance(context).create(ApiService.class);
                Call<List<Bill>> call = apiService.getBillsByPartner(user);

                call.enqueue(new Callback<List<Bill>>() {
                    @Override
                    public void onResponse(Call<List<Bill>> call, Response<List<Bill>> response) {
                        if (response.isSuccessful()) {
                            Log.d("getDate", "API call successful");
                            int total = 0;
                            List<Bill> list = new ArrayList<>();
                            for (Bill bill : response.body()) {
                                if (bill.getStatus().equals("Yes")) {
                                    try {
                                        Date dayOut = sdf.parse(bill.getDayOut());
                                        Date startDate = sdf.parse(startdate);
                                        Date toDate = sdf.parse(todate);
                                        if (dayOut.compareTo(startDate) >= 0 && dayOut.compareTo(toDate) <= 0) {
                                            total += bill.getTotal();
                                            list.add(bill);
                                        }
                                    } catch (ParseException e) {
                                        Log.e("getDate", "ParseException", e);
                                    }
                                }
                            }
                            adapterStatistical = new StatisticalAdapter(list, getContext());
                            recyclerView.setAdapter(adapterStatistical);
                            recyclerView.setVisibility(View.VISIBLE);
                            tvHide.setVisibility(View.VISIBLE);
                            totalRevenue.setText(numberFormat.format(total));
                            adapterStatistical.notifyDataSetChanged();
                            Log.d("getDate", "Total revenue: " + total);
                        } else {
                            Log.e("getDate", "API call failed with response code: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Bill>> call, Throwable t) {
                        Log.e("getDate", "API call failed", t);
                    }
                });
            }
        });
    }


}