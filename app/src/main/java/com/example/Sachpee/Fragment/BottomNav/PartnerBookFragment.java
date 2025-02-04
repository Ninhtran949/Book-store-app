package com.example.Sachpee.Fragment.BottomNav;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.Sachpee.Adapter.Partner_BookAdapter;
import com.example.Sachpee.Model.Partner;
import com.example.Sachpee.R;
import com.example.Sachpee.Service.ApiClient;
import com.example.Sachpee.Service.ApiService;
  

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PartnerBookFragment extends Fragment implements Partner_BookAdapter.ItemClickListener{
    RecyclerView recyclerView_Partner_Book;
    LinearLayoutManager linearLayoutManager;
    List<Partner> list;
    Partner_BookAdapter partner_bookAdapter;
    private Context context;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view= inflater.inflate(R.layout.fragment_partner_food, container, false);


        recyclerView_Partner_Book = view.findViewById(R.id.recyclerView_Partner_Book);

        list = getAllPartner();
        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView_Partner_Book.setLayoutManager(linearLayoutManager);

        partner_bookAdapter = new Partner_BookAdapter(list,this);
        recyclerView_Partner_Book.setAdapter(partner_bookAdapter);



        return view;
    }

    public List<Partner> getAllPartner() {
        List<Partner> list1 = new ArrayList<>();

        ApiService apiService = ApiClient.getRetrofitInstance(context).create(ApiService.class);

        // Gọi API để lấy danh sách đối tác (Partner)
        Call<List<Partner>> call = apiService.getAllPartners();
        call.enqueue(new Callback<List<Partner>>() {
            @Override
            public void onResponse(Call<List<Partner>> call, Response<List<Partner>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    list1.clear();

                    // Lấy dữ liệu từ response
                    List<Partner> partners = response.body();
                    Log.d("getAllPartner", "Received " + partners.size() + " partners");

                    // Thêm các partner vào list1
                    list1.addAll(partners);

                    // Cập nhật adapter
                    partner_bookAdapter.notifyDataSetChanged();
                    Log.d("getAllPartner", "Partners loaded and adapter updated. Total: " + list1.size());
                } else {
                    Log.e("getAllPartner", "Response not successful: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Partner>> call, Throwable t) {
                Log.e("getAllPartner", "API call failed: " + t.getMessage());
            }
        });

        return list1;
    }



    @Override
    public void onItemClick(Partner partner) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("Partner", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("partner",partner.getUserPartner());
        editor.apply();

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frame_Home, new Book_Of_PartnerFragment(),null).addToBackStack(null).commit();
    }
}