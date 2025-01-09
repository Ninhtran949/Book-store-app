package com.example.Sachpee.Fragment.BottomNav;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;


import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.Sachpee.Adapter.ProductAdapter;
import com.example.Sachpee.Fragment.ProductFragments.ProductFragment;
import com.example.Sachpee.Model.Product;
import com.example.Sachpee.R;
import com.example.Sachpee.Service.ApiClient;
import com.example.Sachpee.Service.ApiService;


import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Book_Of_PartnerFragment extends Fragment {
    RecyclerView food_of_partner_recyclerView;
    LinearLayoutManager linearLayoutManager;
    private List<Product> listProduct = new ArrayList<>();
    private ProductAdapter adapter;
    private ProductFragment fragment= new ProductFragment();
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_book__of__partner, container, false);
        food_of_partner_recyclerView = view.findViewById(R.id.food_of_partner_recyclerView);

        listProduct = loadlistBook();
        linearLayoutManager = new LinearLayoutManager(getContext());
        food_of_partner_recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new ProductAdapter(listProduct,fragment,getContext());
        food_of_partner_recyclerView.setAdapter(adapter);

        food_of_partner_recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        return view;
    }

    private List<Product> loadlistBook() {
        // Lấy đối tác từ SharedPreferences
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("Partner", Context.MODE_PRIVATE);
        String partner = sharedPreferences.getString("partner", "");
        Log.d("loadlistBook", "Partner: " + partner);


        ApiService apiService = ApiClient.getRetrofitInstance(context).create(ApiService.class);

        Call<List<Product>> call = apiService.getAllProducts();  // Giả định phương thức lấy tất cả sản phẩm
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listProduct.clear();

                    // Lặp qua danh sách sản phẩm nhận được từ API
                    for (Product product : response.body()) {
                        // Kiểm tra category và đối tác
                        if (product.getCodeCategory() == 4 && partner.equals(product.getUserPartner())) {
                            listProduct.add(product);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    Log.d("loadlistBook", "Số lượng sản phẩm hiển thị: " + listProduct.size());
                } else {
                    Log.e("loadlistBook", "Phản hồi không thành công: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.e("loadlistBook", "Lỗi khi gọi API: " + t.getMessage());
            }
        });

        return listProduct;
    }

}