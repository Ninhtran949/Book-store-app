package com.example.Sachpee.Fragment.ProductFragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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


public class TamlyFragment extends Fragment {

    private List<Product> listTamly;
    private RecyclerView rvTamly;
    
    private LinearLayoutManager linearLayoutManager;
    private ProductAdapter adapter;
    private View view;
    private ProductFragment fragment= new ProductFragment();
    private Context context;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_tamly, container, false);
        initUI();
        return view;
    }

    public void initUI(){
        listTamly = getTamlyProduct();
        rvTamly = view.findViewById(R.id.rvTamly);
        linearLayoutManager = new LinearLayoutManager(getContext());
        rvTamly.setLayoutManager(linearLayoutManager);
        adapter = new ProductAdapter(listTamly,fragment, getActivity());
        rvTamly.setAdapter(adapter);
        rvTamly.setLayoutManager(new GridLayoutManager(getContext(), 2));
    }

    public List<Product> getTamlyProduct() {
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Vui lòng đợi ...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show(); // Hiển thị ProgressDialog

        List<Product> list1 = new ArrayList<>();


        ApiService apiService = ApiClient.getRetrofitInstance(context).create(ApiService.class);
        Call<List<Product>> call = apiService.getAllProducts(); // Giả định rằng API trả về tất cả sản phẩm

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                progressDialog.dismiss(); // Ẩn ProgressDialog khi nhận được phản hồi
                if (response.isSuccessful() && response.body() != null) {
                    list1.clear(); // Xóa dữ liệu cũ

                    for (Product product : response.body()) {
                        if (product.getCodeCategory() == 3) { // Lọc theo category = 3 (tâm lý)
                            list1.add(product);
                        }
                    }

                    adapter.notifyDataSetChanged(); // Cập nhật adapter với dữ liệu mới
                    Log.d("BookFragment", "Danh sách tâm lý đã được lấy thành công: " + list1.size());
                } else {
                    Log.e("BookFragment", "Lỗi khi lấy dữ liệu từ API: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                progressDialog.dismiss(); // Ẩn ProgressDialog khi gặp lỗi
                Log.e("BookFragment", "Lỗi kết nối khi gọi API: " + t.getMessage());
            }
        });

        return list1; // Trả về danh sách sản phẩm
    }

}