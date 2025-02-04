package com.example.Sachpee.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Sachpee.Fragment.PartnerFragment;
import com.example.Sachpee.Model.Partner;
import com.example.Sachpee.R;
import com.example.Sachpee.Service.ApiClient;
import com.example.Sachpee.Service.ApiService;


import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PartnerAdapter extends RecyclerView.Adapter<PartnerAdapter.viewHolder> {
    List<Partner> list;
    PartnerFragment fragment;
    private Context context;

    public PartnerAdapter(List<Partner> list, PartnerFragment fragment) {
        this.list = list;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_partner,parent,false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Partner partner = list.get(position);
        holder.tvCodePartner_adapter.setText("Mã ĐT: "+partner.getIdPartner());
        holder.tvNamePartner_adapter.setText("Tên ĐT: "+partner.getNamePartner());
        holder.tvAddressPartner_adapter.setText("Địa chỉ: "+partner.getAddressPartner());
        holder.tvUserPartner_adapter.setText("Tài khoản: "+partner.getUserPartner());
        holder.tvPasswordPartnre_adapter.setText("Mật khẩu: "+partner.getPasswordPartner());
        holder.itemPartner.setOnClickListener(view -> {
            if (holder.btn_UpdatePartner.getVisibility()==View.VISIBLE || holder.btn_DeletePartner.getVisibility()==View.VISIBLE){
                holder.btn_DeletePartner.setVisibility(View.GONE);
                holder.btn_UpdatePartner.setVisibility(View.GONE);
            }else {
                holder.btn_DeletePartner.setVisibility(View.VISIBLE);
                holder.btn_UpdatePartner.setVisibility(View.VISIBLE);
            }
        });
        holder.btn_DeletePartner.setOnClickListener(view -> {
            showDialog(partner);
        });

    }

    @Override
    public int getItemCount() {
        if (list!=null){
            return list.size();
        }
        return 0;
    }

    public class viewHolder extends RecyclerView.ViewHolder{
        private TextView tvCodePartner_adapter,tvNamePartner_adapter,tvAddressPartner_adapter,
        tvUserPartner_adapter,tvPasswordPartnre_adapter;
        private Button btn_UpdatePartner,btn_DeletePartner;
        private CardView itemPartner;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            tvCodePartner_adapter = itemView.findViewById(R.id.tv_CodePartner_item);
            tvNamePartner_adapter = itemView.findViewById(R.id.tv_NamePartner_item);
            tvAddressPartner_adapter = itemView.findViewById(R.id.tv_AddressPartner_item);
            tvUserPartner_adapter = itemView.findViewById(R.id.tv_UserPartner_item);
            tvPasswordPartnre_adapter = itemView.findViewById(R.id.tv_PasswordPartner_item);
            btn_DeletePartner = itemView.findViewById(R.id.btn_DeletePartner);
            btn_UpdatePartner = itemView.findViewById(R.id.btn_UpdatePartner);
            itemPartner = itemView.findViewById(R.id.itemPartner);

        }

    }
    private void showDialog(Partner partner) {
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getContext());
        builder.setMessage("Bạn có chắc muốn xóa đối tác");
        builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deletePartner(partner);
            }
        });
        builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void deletePartner(Partner partner) {
        ApiService apiService = ApiClient.getRetrofitInstance(context).create(ApiService.class);

        Call<Void> call = apiService.deletePartner(partner.getIdPartner());

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("BookFragment", "Đã xóa đối tác thành công.");
                    // Bạn có thể cập nhật giao diện nếu cần
                } else {
                    Log.e("BookFragment", "Xóa đối tác thất bại: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("BookFragment", "Lỗi kết nối: " + t.getMessage());
            }
        });
    }


}
