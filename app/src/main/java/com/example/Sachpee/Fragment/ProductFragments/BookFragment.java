package com.example.Sachpee.Fragment.ProductFragments;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;


import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.Sachpee.Activity.Callback.ProductPartnerCallback;
import com.example.Sachpee.Adapter.ProductAdapter;
import com.example.Sachpee.Model.Partner;
import com.example.Sachpee.Model.Product;
import com.example.Sachpee.R;
import com.example.Sachpee.Service.ApiClient;
import com.example.Sachpee.Service.ApiService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class BookFragment extends Fragment {
    private Context context;
    private List<Product> listBook;
    private RecyclerView rvBook;
    private LinearLayoutManager linearLayoutManager;
    private ProductAdapter adapter;
    private View view;
    private SharedPreferences sharedPreferences;
    private String user;

    private Partner partner = new Partner();
    private FloatingActionButton fab_addProduct;
    private List<Product> listProduct;
    private TextInputLayout til_nameProduct,til_priceProduct;
    private ImageView img_Product,img_addImageCamera,img_addImageDevice;
    private String nameProduct,imgProduct,userPartner,priceProduct;
    private int codeCategory;
    private Button btn_addBook,btn_cancelBook;
    private static final int REQUEST_ID_IMAGE_CAPTURE =10;
    private static final int PICK_IMAGE =100;
    private ProductFragment fragment = new ProductFragment();
    private TextView tvErrorImg;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_book, container, false);
        initUI();
        rvBook.setLayoutManager(new GridLayoutManager(getContext(), 2));

        sharedPreferences = getContext().getSharedPreferences("My_User", Context.MODE_PRIVATE);
        user = sharedPreferences.getString("username","");
        if(user.equals("admin")){
            view.findViewById(R.id.fab_addBook_fragment).setVisibility(View.GONE);
        }else {
            view.findViewById(R.id.fab_addBook_fragment).setVisibility(View.VISIBLE);
        }
        fab_addProduct = view.findViewById(R.id.fab_addBook_fragment);
        fab_addProduct.setOnClickListener(view1 -> {
            dialogProduct();
        });

        return view;
    }
    public void initUI() {
        listProduct = getAllProduct();
        rvBook = view.findViewById(R.id.rvBook);
        linearLayoutManager = new LinearLayoutManager(getContext());
        rvBook.setLayoutManager(linearLayoutManager);

        // Gọi getProductPartner và cung cấp callback
        getProductPartner(new ProductPartnerCallback() {
            @Override
            public void onProductsLoaded(List<Product> products) {
                listBook = products; // Cập nhật danh sách sản phẩm
                adapter = new ProductAdapter(listBook, fragment, getContext()); // Khởi tạo adapter với danh sách mới
                rvBook.setAdapter(adapter); // Đặt adapter cho RecyclerView
            }
        });
    }

    public List<Product> getAllProduct() {
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Vui lòng đợi ...");
        progressDialog.setCanceledOnTouchOutside(false);
        List<Product> list1 = new ArrayList<>();
        progressDialog.show();


        ApiService apiService = ApiClient.getRetrofitInstance(context).create(ApiService.class);

        Call<List<Product>> call = apiService.getAllProducts();

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    list1.clear();
                    list1.addAll(response.body());
                    Log.d("BookFragment", "Sản phẩm đã được lấy thành công: " + list1.size());
                } else {
                    Log.e("BookFragment", "Lỗi khi lấy dữ liệu: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                progressDialog.dismiss();
                Log.e("BookFragment", "Lỗi kết nối: " + t.getMessage());
            }
        });

        return list1; //  phương thức này sẽ trả về danh sách trống, vì gọi API là bất đồng bộ
    }

    public void getProductPartner(ProductPartnerCallback callback) {
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Vui lòng đợi ...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        ApiService apiService = ApiClient.getRetrofitInstance(context).create(ApiService.class);
        Call<List<Product>> call = apiService.getAllProducts();

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> list1 = new ArrayList<>();
                    for (Product product : response.body()) {
                        if (user.equals("admin") && product.getCodeCategory() == 4) {
                            list1.add(product);
                        } else if (product.getUserPartner().equals(user) && product.getCodeCategory() == 4) {
                            list1.add(product);
                        }
                    }
                    callback.onProductsLoaded(list1); // Gọi callback để trả về list sản phẩm
                    Log.d("BookFragment", "Sản phẩm đối tác đã được lấy thành công: " + list1.size());
                } else {
                    Log.e("BookFragment", "Lỗi khi lấy dữ liệu: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                progressDialog.dismiss();
                Log.e("BookFragment", "Lỗi kết nối: " + t.getMessage());
            }
        });
    }


    public void addProduct(Product product) {
        ApiService apiService = ApiClient.getRetrofitInstance(context).create(ApiService.class);

        // Gọi API để lấy tất cả sản phẩm trước khi thêm sản phẩm mới
        Call<List<Product>> call = apiService.getAllProducts();

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> listProduct = response.body();
                    int newCodeProduct;

                    // Nếu danh sách sản phẩm rỗng, thiết lập codeProduct là 1
                    if (listProduct.isEmpty()) {
                        newCodeProduct = 1;
                    } else {
                        int lastIndex = listProduct.size() - 1;
                        newCodeProduct = listProduct.get(lastIndex).getCodeProduct() + 1; // Lấy ID mới
                    }

                    product.setCodeProduct(newCodeProduct);

                    // Gọi API để thêm sản phẩm
                    Call<Void> addProductCall = apiService.addProduct(product);
                    addProductCall.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Log.d("BookFragment", "Sản phẩm đã được thêm thành công!");
                            } else {
                                Log.e("BookFragment", "Lỗi khi thêm sản phẩm: " + response.message());
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Log.e("BookFragment", "Lỗi kết nối: " + t.getMessage());
                        }
                    });
                } else {
                    Log.e("BookFragment", "Lỗi khi lấy danh sách sản phẩm: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.e("BookFragment", "Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void dialogProduct() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Thêm sản phẩm");
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_product,null);
        builder.setView(view);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        initUiDialog(view);
        view.findViewById(R.id.sp_nameCategory).setVisibility(View.GONE);
        img_addImageCamera.setOnClickListener(view1 -> {
            requestPermissionCamera();
        });
        img_addImageDevice.setOnClickListener(view1 -> {
            requestPermissionDevice();
        });
        btn_addBook.setOnClickListener(view1 -> {
            getData();
            validate();

        });
        btn_cancelBook.setOnClickListener(view1 -> {
            alertDialog.dismiss();
        });
    }
    public void initUiDialog(View view){
        tvErrorImg = view.findViewById(R.id.error_img);
        img_Product = view.findViewById(R.id.imgProduct_dialog);
        img_addImageCamera = view.findViewById(R.id.img_addImageCamera_dialog);
        img_addImageDevice = view.findViewById(R.id.img_addImageDevice_dialog);
        til_nameProduct =  view.findViewById(R.id.til_NameProduct_dialog);
        til_priceProduct =  view.findViewById(R.id.til_PriceProduct_dialog);
        btn_addBook =  view.findViewById(R.id.btn_addBook_dialog);
        btn_cancelBook =  view.findViewById(R.id.btn_cancelBook_dialog);
    }
    public void requestPermissionCamera(){
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                captureImage();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                requestPermissionCamera();
            }
        };
        TedPermission.create()
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("Nếu bạn không cấp quyền,bạn sẽ không thể tải ảnh lên\n\nVui lòng vào [Cài đặt] > [Quyền] và cấp quyền để sử dụng")
                .setPermissions(Manifest.permission.CAMERA)
                .check();
    }
    public void requestPermissionDevice(){
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                openGallery();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                requestPermissionDevice();
            }
        };
        TedPermission.create()
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("Nếu bạn không cấp quyền,bạn sẽ không thể tải ảnh lên\n\nVui lòng vào [Cài đặt] > [Quyền] và cấp quyền để sử dụng" )
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();
    }
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        this.startActivityForResult(intent, REQUEST_ID_IMAGE_CAPTURE);
    }
    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ID_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                Bitmap bp = (Bitmap) data.getExtras().get("data");
                this.img_Product.setImageBitmap(bp);

                Uri imageUri = data.getData();
                img_Product.setImageURI(imageUri);
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getContext(), "Bạn chưa thêm ảnh", Toast.LENGTH_LONG).show();
            } else if (data!=null){
                Toast.makeText(getContext(), "Lỗi", Toast.LENGTH_LONG).show();

            }
        }
        if (requestCode == PICK_IMAGE) {
            if (resultCode == RESULT_OK ) {
                Uri imageUri = data.getData();
                this.img_Product.setImageURI(imageUri);
            }
        }

    }
    public void getData(){
        nameProduct = til_nameProduct.getEditText().getText().toString();
        try {
            Bitmap bitmap = ((BitmapDrawable)img_Product.getDrawable()).getBitmap();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
            byte[] imgByte = outputStream.toByteArray();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                imgProduct = Base64.getEncoder().encodeToString(imgByte);
                Log.d("BookFragment", "Image converted to Base64 successfully");
            }
        }catch (Exception e){
            Log.e("BookFragment", "Error converting image to Base64: " + e.getMessage());
            imgProduct = ""; // Set empty string if conversion fails
        }

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("My_User", Context.MODE_PRIVATE);
        userPartner = sharedPreferences.getString("username","");
        codeCategory = 4;
        priceProduct = til_priceProduct.getEditText().getText().toString();
    }
    public boolean isEmptys(String str,TextInputLayout til){
        if (str.isEmpty()){
            til.setError("Không được để trống");
            return false;
        }else{
            til.setError("");
            return true;
        }

    }
    public boolean errorImg(String str, TextView tv){
        if (str != null && !str.isEmpty()){
            tv.setText("");
            return true;
        }else {
            tv.setText("Ảnh không được để trống");
            return false;
        }
    }
    public void validate(){
        if (isEmptys(nameProduct, til_nameProduct) && isEmptys(priceProduct, til_priceProduct) && errorImg(imgProduct, tvErrorImg)) {
            Log.d("BookFragment", "Validation passed for product: " + nameProduct);
            setDataProduct();
            removeAll();
        } else {
            Log.d("BookFragment", "Validation failed for product: " + nameProduct);
        }
    }
    public void removeAll(){
        til_nameProduct.getEditText().setText("");
        til_priceProduct.getEditText().setText("");
        img_Product.setImageResource(R.drawable.ic_baseline_image_24);
    }
    public void setDataProduct(){
        Product product = new Product();
        product.setUserPartner(userPartner);
        product.setCodeCategory(codeCategory);
        product.setNameProduct(nameProduct);
        product.setPriceProduct(Integer.parseInt(priceProduct));

        // Ensure image is properly set
        if (imgProduct != null && !imgProduct.isEmpty()) {
            product.setImgProduct(imgProduct);
            Log.d("BookFragment", "Setting image for product: " + nameProduct);
        } else {
            product.setImgProduct("");
            Log.w("BookFragment", "No image set for product: " + nameProduct);
        }

        addProduct(product);
    }

}