package com.example.Sachpee.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.Sachpee.Activity.SignInActivity;
import com.example.Sachpee.Fragment.ProductFragments.ProductFragment;
import com.example.Sachpee.Model.Cart;
import com.example.Sachpee.Model.Product;
import com.example.Sachpee.R;
import com.example.Sachpee.Service.ApiClient;
import com.example.Sachpee.Service.ApiService;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import android.util.Base64;
import android.widget.Toast;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
// TODO BUG Payload Too Large for img
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private List<Product> list;
    private List<Cart> listCart = new ArrayList<>(); // Khởi tạo danh sách giỏ hàng
    private ProductFragment fragment;
    private Context context;

    public ProductAdapter(List<Product> list, ProductFragment fragment, Context context) {
        this.list = list;
        this.fragment = fragment;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SharedPreferences preferences = context.getSharedPreferences("My_User", Context.MODE_PRIVATE);
        String user = preferences.getString("username", "");
        String role = preferences.getString("role", "");

        Product product = list.get(position);
        NumberFormat numberFormat = new DecimalFormat("#,##0");

        // Gọi getAllCart() để lấy danh sách giỏ hàng
        getAllCart(user); // Cập nhật giỏ hàng trước khi thực hiện các thao tác

        try {
            // Log để debug
            Log.d("ProductAdapter", String.format(
                    "Loading image for product: %s, Image type: %s",
                    product.getNameProduct(),
                    product.isBase64Image() ? "Base64" :
                            product.isUrlImage() ? "URL" : "Unknown"
            ));

            if (product.isBase64Image()) {
                // Xử lý ảnh base64
                String base64Image = product.getImgProduct();
                if (base64Image.startsWith("/9j/")) {
                    base64Image = "data:image/jpeg;base64," + base64Image;
                }
                byte[] imgByte = Base64.decode(base64Image.split(",")[1], Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
                holder.imgProduct.setImageBitmap(bitmap);
            } else if (product.isUrlImage()) {
                // Xử lý ảnh URL bằng Glide
                RequestOptions requestOptions = new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_baseline_image_24)
                        .error(R.drawable.ic_baseline_image_24);

                Glide.with(context)
                        .load(product.getImgProduct())
                        .apply(requestOptions)
                        .into(holder.imgProduct);

                Log.d("ProductAdapter", "Loading URL image: " + product.getImgProduct());
            } else {
                // Unknown format, use placeholder
                holder.imgProduct.setImageResource(R.drawable.ic_baseline_image_24);
                Log.w("ProductAdapter", "Unknown image format for product: " + product.getNameProduct());
            }
        } catch (Exception e) {
            // Log error and use placeholder
            Log.e("ProductAdapter", "Error loading image for product: " + product.getNameProduct(), e);
            holder.imgProduct.setImageResource(R.drawable.ic_baseline_image_24);
        }

        holder.tvNameProduct.setText(String.valueOf(product.getNameProduct()));
        holder.tvPriceProduct.setText(numberFormat.format(product.getPriceProduct()) + " đ");
        holder.cardProduct.setOnClickListener(view -> {
            if (role.equals("admin") || role.equals("partner")) {
                if (holder.btnUpdateProduct.getVisibility() == View.VISIBLE || holder.btnDeleteProduct.getVisibility() == View.VISIBLE) {
                    holder.btnUpdateProduct.setVisibility(View.GONE);
                    holder.btnDeleteProduct.setVisibility(View.GONE);
                } else {
                    holder.btnUpdateProduct.setVisibility(View.VISIBLE);
                    holder.btnDeleteProduct.setVisibility(View.VISIBLE);
                }
            }
        });
        holder.btnUpdateProduct.setOnClickListener(view -> {
            fragment.dialogProduct(product, 1, context);
        });
        holder.btnDeleteProduct.setOnClickListener(view -> {
            showDialogDelete(product);
        });

        holder.btn_addCart.setOnClickListener(view -> {
            if (!user.equals("")) {
                StringBuilder str = new StringBuilder();
                Cart cart = new Cart();
                cart.setUserClient(user);
                cart.setIdCategory(product.getCodeCategory());
                cart.setIdProduct(product.getCodeProduct());
                cart.setImgProduct(product.getImgProduct());
                cart.setNameProduct(product.getNameProduct());
                cart.setPriceProduct(product.getPriceProduct());
                cart.setNumberProduct(1);
                cart.setIdPartner(product.getUserPartner());
                cart.setTotalPrice(cart.getPriceProduct() * cart.getNumberProduct());
                for (int i = 0; i < listCart.size(); i++) {
                    if (!listCart.get(i).getIdPartner().equals(cart.getIdPartner())) {
                        str.append("1");
                    }
                }
                if (str.length() != 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Nếu bạn thêm sản phẩm ở cửa hàng này, sản phẩm ở cửa hàng khác sẽ bị xóa");
                    builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            for (int j = 0; j < listCart.size(); j++) {
                                if (user.equals(listCart.get(j).getUserClient())) {
                                    deleteCart(listCart.get(j));
                                }
                            }
                            addProductCart(cart);
                        }
                    });
                    builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                } else {
                    addProductCart(cart);
                }
            }else {
                showDialogLogin();
            }
        });



    }




    @Override
    public int getItemCount() {
        if (list!=null){
            return list.size();
        }
        return 0;
    }

    public void updateData(List<Product> products) {
        this.list.clear();
        this.list.addAll(products);
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNameProduct, tvPriceProduct;
        private ImageView imgProduct;
        private CardView cardProduct;
        private Button btnUpdateProduct, btnDeleteProduct, btn_addCart;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNameProduct = itemView.findViewById(R.id.tvNameProduct_item);
            tvPriceProduct = itemView.findViewById(R.id.tvPriceProduct_item);
            imgProduct = itemView.findViewById(R.id.imgProduct_item);
            cardProduct = itemView.findViewById(R.id.cardProduct);
            btnUpdateProduct = itemView.findViewById(R.id.btn_updateProduct_item);
            btnDeleteProduct = itemView.findViewById(R.id.btn_deleteProduct_item);
            btn_addCart = itemView.findViewById(R.id.btn_addCart_item);
        }
    }

    private void deleteCart(Cart cart) {
        ApiService apiService = ApiClient.getRetrofitInstance(context).create(ApiService.class);

        Call<Void> call = apiService.deleteCartItem(cart.getIdCart());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("ProductAdapter", "Sản phẩm trong giỏ hàng đã được xóa thành công với mã giỏ hàng: " + cart.getIdCart());
                } else {
                    Log.e("ProductAdapter", "Lỗi khi xóa sản phẩm: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("ProductAdapter", "Lỗi khi xóa sản phẩm: " + t.getMessage());
            }
        });
    }

    public void addProductCart(Cart cart) {
        ApiService apiService = ApiClient.getRetrofitInstance(context).create(ApiService.class);

        if (listCart.size() == 0) {
            cart.setIdCart(1);
        } else {
            int i = listCart.size() - 1;
            int id = listCart.get(i).getIdCart() + 1;
            cart.setIdCart(id);
        }

        Call<Cart> call = apiService.addCart(cart);
        call.enqueue(new Callback<Cart>() {
            @Override
            public void onResponse(Call<Cart> call, Response<Cart> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Bạn đã thêm sản phẩm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("ProductAdapter", "Lỗi khi thêm sản phẩm: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Cart> call, Throwable t) {
                Log.e("ProductAdapter", "Lỗi khi thêm sản phẩm: " + t.getMessage());
            }
        });
    }

    private void getAllCart(String user) {
        // API Call để lấy giỏ hàng
        ApiService apiService = ApiClient.getRetrofitInstance(context).create(ApiService.class);
        apiService.getCartsByUser(user).enqueue(new Callback<List<Cart>>() {
            @Override
            public void onResponse(Call<List<Cart>> call, Response<List<Cart>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listCart.clear();
                    listCart.addAll(response.body());
                    Log.d("ProductAdapter", "Data retrieved successfully: " + listCart.size() + " items.");
                } else {
                    Log.e("ProductAdapter", "Error: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Cart>> call, Throwable t) {
                Log.e("ProductAdapter", "Error: " + t.getMessage());
            }
        });
    }

    private void showDialogDelete(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Bạn có chắc muốn xóa sản phẩm");
        builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                fragment.deleteProduct(product);
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

    public void showDialogLogin() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Bạn phải đăng nhập");
        builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(context, SignInActivity.class);
                context.startActivity(intent);
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

}
