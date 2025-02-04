package com.example.Sachpee.Fragment.ProductFragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.Sachpee.Adapter.ProductAdapter;
import com.example.Sachpee.Model.Product;
import com.example.Sachpee.R;
import com.example.Sachpee.Service.ApiClient;
import com.example.Sachpee.Service.ApiService;


import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class NgoainguFragment extends Fragment {

    private List<Product> listNgoaingu = new ArrayList<>();
    private RecyclerView rvNgoaingu;
    private LinearLayoutManager linearLayoutManager;
    private ProductAdapter adapter;
    private View view;
    private ProductFragment fragment = new ProductFragment();
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_ngoaingu, container, false);
        unitUI();

        return view;
    }
    public void unitUI(){
        getNgoainguProducts();
        rvNgoaingu = view.findViewById(R.id.rvNgoaingu);
        linearLayoutManager = new LinearLayoutManager(getContext());
        rvNgoaingu.setLayoutManager(linearLayoutManager);
        adapter = new ProductAdapter(listNgoaingu,fragment,getContext());
        rvNgoaingu.setAdapter(adapter);
        rvNgoaingu.setLayoutManager(new GridLayoutManager(getContext(), 2));
    }

    public void getNgoainguProducts() {
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Vui lòng đợi ...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        ApiService apiService = ApiClient.getRetrofitInstance(context).create(ApiService.class);
        Call<List<Product>> call = apiService.getAllProducts(); //  API trả về tất cả sản phẩm

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    listNgoaingu.clear(); // Xóa dữ liệu cũ

                    for (Product product : response.body()) {
                        if (product.getCodeCategory() == 8) { // Kiểm tra sản phẩm với mã category = 8
                            listNgoaingu.add(product);
                        }
                    }

                    adapter.notifyDataSetChanged(); // Cập nhật adapter với dữ liệu mới
                    Log.d("NgoainguFragment", "Danh sách sách Ngoại ngữ đã được lấy thành công: " + listNgoaingu.size());
                } else {
                    Log.e("NgoainguFragment", "Lỗi khi lấy dữ liệu từ API: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                progressDialog.dismiss();
                Log.e("NgoainguFragment", "Lỗi kết nối khi gọi API: " + t.getMessage());
            }
        });
    }

}