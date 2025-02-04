package com.example.Sachpee.Adapter;

import static com.gun0912.tedpermission.provider.TedPermissionProvider.context;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Sachpee.Activity.Callback.OnCartUpdateListener;
import com.example.Sachpee.Model.Cart;
import com.example.Sachpee.R;
import com.example.Sachpee.Service.ApiClient;
import com.example.Sachpee.Service.ApiService;


import java.text.DecimalFormat;
import java.text.NumberFormat;
import android.util.Base64;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.viewHolder> {
    private List<Cart> list;
    private ApiService apiService;
    private OnCartUpdateListener cartUpdateListener;

    public CartAdapter(List<Cart> list, OnCartUpdateListener listener) {
        this.list = list;
        this.cartUpdateListener = listener;
        apiService = ApiClient.getRetrofitInstance(context).create(ApiService.class);
    }

    NumberFormat numberFormat = new DecimalFormat("#,##0");

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new viewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Cart cart = list.get(position);

        // Chuyển đổi Base64 thành bitmap
        byte[] imgByte = Base64.decode(cart.getImgProduct(), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
        holder.img_ItemCart_imgProduct.setImageBitmap(bitmap);

        // Hiển thị thông tin sản phẩm
        holder.tv_ItemCart_nameProduct.setText(String.valueOf(cart.getNameProduct()));
        holder.tv_ItemCart_priceProduct.setText("Giá: " + numberFormat.format(cart.getPriceProduct()) + " đ");
        holder.tvAmountProduct.setText(String.valueOf(cart.getNumberProduct()));
        holder.tvTotalProduct.setText("Tổng: " + numberFormat.format(cart.getNumberProduct() * cart.getPriceProduct()) + " đ");

        // Log thông tin sản phẩm
        Log.d("CartAdapter", "Product ID: " + cart.getIdCart() + ", Name: " + cart.getNameProduct());

        // Xử lý khi nhấn nút tăng số lượng sản phẩm
        holder.imgPlus.setOnClickListener(view -> {
            int amount = Integer.parseInt(holder.tvAmountProduct.getText().toString()) + 1;
            holder.tvAmountProduct.setText(String.valueOf(amount));

            Log.d("CartAdapter", "Increasing amount of product ID: " + cart.getIdCart() + " to " + amount);

            // Cập nhật số lượng sản phẩm qua API
            updateCartQuantity(String.valueOf(cart.getIdCart()), amount, cart.getPriceProduct(),position); // Chuyển đổi cart.getIdCart() thành String
        });

            // Xử lý khi nhấn nút giảm số lượng sản phẩm
        holder.imgMinus.setOnClickListener(view -> {
            int amount = Integer.parseInt(holder.tvAmountProduct.getText().toString()) - 1;

            if (amount == 0) {
                // Xóa sản phẩm khi số lượng bằng 0
                deleteProduct(String.valueOf(cart.getIdCart()),position); // Chuyển đổi cart.getIdCart() thành String
                Log.d("CartAdapter", "Product ID: " + cart.getIdCart() + " has been removed.");
            } else {
                holder.tvAmountProduct.setText(String.valueOf(amount));

                Log.d("CartAdapter", "Decreasing amount of product ID: " + cart.getIdCart() + " to " + amount);

                // Cập nhật số lượng sản phẩm qua API
                updateCartQuantity(String.valueOf(cart.getIdCart()), amount, cart.getPriceProduct(),position); // Chuyển đổi cart.getIdCart() thành String
            }
        });

        // Xử lý khi nhấn nút xóa sản phẩm
        holder.imgDelete.setOnClickListener(view -> {
            deleteProduct(String.valueOf(cart.getIdCart()),position); // Chuyển đổi cart.getIdCart() thành String
            Log.d("CartAdapter", "Product ID: " + cart.getIdCart() + " has been deleted.");
        });

    }

    @Override
    public int getItemCount() {
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        private TextView tv_ItemCart_nameProduct, tv_ItemCart_priceProduct, tvAmountProduct, tvTotalProduct;
        private ImageView img_ItemCart_imgProduct, imgPlus, imgMinus, imgDelete;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            tvAmountProduct = itemView.findViewById(R.id.tv_ItemCart_numberProduct);
            tvTotalProduct = itemView.findViewById(R.id.tv_ItemCart_totalPrice);
            tv_ItemCart_nameProduct = itemView.findViewById(R.id.tv_ItemCart_nameProduct);
            tv_ItemCart_priceProduct = itemView.findViewById(R.id.tv_ItemCart_priceProduct);
            img_ItemCart_imgProduct = itemView.findViewById(R.id.img_ItemCart_imgProduct);
            imgPlus = itemView.findViewById(R.id.btn_ItemCart_plus);
            imgMinus = itemView.findViewById(R.id.btn_ItemCart_minus);
            imgDelete = itemView.findViewById(R.id.btn_ItemCart_deleteProduct);
        }
    }


    // Phương thức cập nhật số lượng sản phẩm qua API
    private void updateCartQuantity(String idCart, int amount, int priceProduct, int position) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("numberProduct", amount);
        updates.put("totalPrice", amount * priceProduct);

        Call<Void> call = apiService.updateCartQuantity(Integer.parseInt(idCart), updates);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("CartAdapter", "Successfully updated numberProduct and totalPrice for product ID: " + idCart);

                    // Cập nhật dữ liệu cục bộ
                    Cart updatedCart = list.get(position);
                    updatedCart.setNumberProduct(amount);
                    updatedCart.setTotalPrice(amount * priceProduct);

                    // Thông báo adapter về sự thay đổi
                    notifyItemChanged(position);

                    // (Tùy chọn) Thông báo cho Fragment hoặc Activity để cập nhật tổng giỏ hàng
                    if (cartUpdateListener != null) {
                        cartUpdateListener.onCartUpdated();
                    }
                } else {
                    Log.e("CartAdapter", "Failed to update numberProduct for product ID: " + idCart +
                            ", Response Code: " + response.code() + ", Message: " + response.message());
                    // Hiển thị thông báo lỗi cho người dùng nếu cần
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("CartAdapter", "Error updating numberProduct for product ID: " + idCart, t);
                // Hiển thị thông báo lỗi cho người dùng nếu cần
            }
        });
    }




    // Phương thức xóa sản phẩm qua API

    private void deleteProduct(String idCart, int position) {
        Call<Void> call = apiService.deleteCartItem(Integer.parseInt(idCart));
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("CartAdapter", "Successfully deleted product ID: " + idCart);

                    // Xóa sản phẩm khỏi danh sách cục bộ
                    list.remove(position);

                    // Thông báo adapter về sự thay đổi
                    notifyItemRemoved(position);

                    // (Tùy chọn) Thông báo cho Fragment hoặc Activity để cập nhật tổng giỏ hàng
                    if (cartUpdateListener != null) {
                        cartUpdateListener.onCartUpdated();
                    }
                } else {
                    Log.e("CartAdapter", "Failed to delete product ID: " + idCart + ", Response Code: " + response.code() + ", Message: " + response.message());
                    // Hiển thị thông báo lỗi cho người dùng nếu cần
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("CartAdapter", "Error deleting product ID: " + idCart, t);
                // Hiển thị thông báo lỗi cho người dùng nếu cần
            }
        });
    }


}

