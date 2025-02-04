package com.example.Sachpee.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

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

public class SearchActivity extends AppCompatActivity {
    private List<Product> listProduct = new ArrayList<>();
    private RecyclerView rvProduct;
    private LinearLayoutManager linearLayoutManager;
    private ProductAdapter adapter;

    private ProductFragment fragment= new ProductFragment();
    private SearchView searchView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getSupportActionBar().setTitle("Tìm kiếm sản phẩm");
        initUI();
    }
    public void initUI(){
        searchView = findViewById(R.id.searchProduct);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchProduct(newText);
                return true;
            }
        });
        loadProducts();
        rvProduct = findViewById(R.id.rvSearch);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        rvProduct.setLayoutManager(linearLayoutManager);
        adapter = new ProductAdapter(listProduct,fragment, getApplicationContext());
        rvProduct.setAdapter(adapter);
        rvProduct.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));
    }
    private void searchProduct(String str) {
        List<Product> searchList = new ArrayList<>();
        for (Product product : listProduct){
            if (product.getNameProduct().toLowerCase().contains(str.toLowerCase())){
                searchList.add(product);
            }
        }
        if (searchList.isEmpty()){
            Toast.makeText(getApplicationContext(), "Không có sản phẩm", Toast.LENGTH_SHORT).show();
            adapter = new ProductAdapter(searchList, fragment, getApplicationContext());
            rvProduct.setAdapter(adapter);
        }else {
            adapter = new ProductAdapter(searchList, fragment, getApplicationContext());
            rvProduct.setAdapter(adapter);
        }
        if (str.equals("")){
            adapter = new ProductAdapter(listProduct, fragment, getApplicationContext());
            rvProduct.setAdapter(adapter);
        }
    }
    public void loadProducts() {
        // TODO: Sửa dialog khi load dữ liệu từ API lên fragment

        ApiService apiService = ApiClient.getRetrofitInstance(this).create(ApiService.class);

        // Gọi API để lấy danh sách sản phẩm
        Call<List<Product>> call = apiService.getAllProducts();
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listProduct.clear();

                    // Lấy dữ liệu từ response
                    List<Product> products = response.body();
                    Log.d("loadProducts", "Received " + products.size() + " products");

                    // Thêm các sản phẩm vào listProduct
                    listProduct.addAll(products);

                    // Cập nhật adapter để hiển thị dữ liệu lên fragment
                    adapter.notifyDataSetChanged();
                    Log.d("loadProducts", "Products loaded and adapter updated. Total: " + listProduct.size());
                } else {
                    Log.e("loadProducts", "Response not successful: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.e("loadProducts", "API call failed: " + t.getMessage());
            }
        });
    }

}