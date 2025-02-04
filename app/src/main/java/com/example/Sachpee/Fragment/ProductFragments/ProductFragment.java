package com.example.Sachpee.Fragment.ProductFragments;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.Sachpee.Adapter.ProductAdapter_tabLayout;
import com.example.Sachpee.Model.Product;
import com.example.Sachpee.R;
import com.example.Sachpee.Service.ApiClient;
import com.example.Sachpee.Service.ApiService;
import com.example.Sachpee.databinding.FragmentProductBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputLayout;
  
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import android.util.Base64;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductFragment extends Fragment {

    private FragmentProductBinding binding;
    private TabLayout tabLayoutProduct;
    private ViewPager2 viewPagerProduct;
    private ProductAdapter_tabLayout adapter_tabLayout;
    private FloatingActionButton fab_addProduct;
    private List<Product> listProduct = new ArrayList<>();
    private TextInputLayout til_nameProduct,til_priceProduct;
    private TextView tvErrorImg;
    private ImageView img_Product,img_addImageCamera,img_addImageDevice;
    private String nameProduct,imgProduct,userPartner,priceProduct,role;
    private int codeCategory;
    private Button btn_addBook,btn_cancelBook;
    private Spinner sp_nameCategory;
    private String[] arr = {"Văn học","Kinh tế","Tâm lý"};
    private ArrayAdapter<String> adapterSpiner;
    private SharedPreferences sharedPreferences;

    private Context context;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProductBinding.inflate(inflater, container, false);
        tabLayout();
        initUI();
        fab_addProduct.setOnClickListener(view1 -> {
            dialogProduct(new Product(),0,getContext());
        });
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    public void tabLayout(){
        tabLayoutProduct = binding.tabProductFragment;
        viewPagerProduct = binding.viewPagerProductFragment;
        adapter_tabLayout = new ProductAdapter_tabLayout(this);
        viewPagerProduct.setAdapter(adapter_tabLayout);
        new TabLayoutMediator(tabLayoutProduct, viewPagerProduct, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position){
                    case 0: tab.setText("Văn học");
                        break;
                    case 1: tab.setText("Kinh tế");
                        break;
                    case 2:tab.setText("Tâm lý");
                        break;
                    case 3:tab.setText("Giáo dục ");
                        break;
                    case 4: tab.setText("Thiếu nhi");
                        break;
                    case 5: tab.setText("Hồi ký");
                        break;
                    case 6:tab.setText("Giáo khoa");
                        break;
                    case 7:tab.setText("Ngoại ngữ ");
                        break;
                }
            }
        }).attach();
    }
    public void initUI(){
        getAllProducts();
        fab_addProduct = binding.fabAddProductFragment;


    }

    public void dialogProduct(Product product,int type,Context context) {
        sharedPreferences = context.getSharedPreferences("My_User",Context.MODE_PRIVATE);
        role = sharedPreferences.getString("role","");
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Thêm sản phẩm");
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_product,null);
        builder.setView(view);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        initUiDialog(view,context);
        if (type ==0) {
            img_addImageCamera.setOnClickListener(view1 -> {
                requestPermissionCamera();
            });
            img_addImageDevice.setOnClickListener(view1 -> {
                requestPermissionDevice();
            });
            btn_addBook.setOnClickListener(view1 -> {
                getData(context);
                validate(0);
            });
            btn_cancelBook.setOnClickListener(view1 -> {
                alertDialog.dismiss();
            });
        }
        if (type==1){
            setData(product);
            img_addImageCamera.setVisibility(View.GONE);

            img_addImageDevice.setVisibility(View.GONE);
            btn_addBook.setOnClickListener(view1 -> {
                getData(context);
                if (validate(1)==1){
                    product.setNameProduct(nameProduct);
                    product.setPriceProduct(Integer.parseInt(priceProduct));
                    product.setCodeCategory(codeCategory);
                    updateProduct(product);
                }
                alertDialog.dismiss();
            });
            btn_cancelBook.setOnClickListener(view1 -> {
                alertDialog.dismiss();
            });
        }
    }
    public void initUiDialog(View view,Context context){
        tvErrorImg = view.findViewById(R.id.error_img);
        img_Product = view.findViewById(R.id.imgProduct_dialog);
        img_addImageCamera = view.findViewById(R.id.img_addImageCamera_dialog);
        img_addImageDevice = view.findViewById(R.id.img_addImageDevice_dialog);
        til_nameProduct =  view.findViewById(R.id.til_NameProduct_dialog);
        til_priceProduct =  view.findViewById(R.id.til_PriceProduct_dialog);
        btn_addBook =  view.findViewById(R.id.btn_addBook_dialog);
        btn_cancelBook =  view.findViewById(R.id.btn_cancelBook_dialog);
        sp_nameCategory = view.findViewById(R.id.sp_nameCategory);
        adapterSpiner = new ArrayAdapter<>(context,android.R.layout.simple_spinner_dropdown_item,arr);
        sp_nameCategory.setAdapter(adapterSpiner);
        if (!role.equals("admin")){
            sp_nameCategory.setVisibility(View.GONE);
        }
    }

    public void setData(Product product){

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            byte[] imgByte = Base64.decode(product.getImgProduct(),Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imgByte,0,imgByte.length);
            img_Product.setImageBitmap(bitmap);
            til_nameProduct.getEditText().setText(product.getNameProduct());
            til_priceProduct.getEditText().setText(String.valueOf(product.getPriceProduct()));
        }

    }

    public void getData(Context context){
        nameProduct = til_nameProduct.getEditText().getText().toString();
        try {
            Bitmap bitmap = ((BitmapDrawable)img_Product.getDrawable()).getBitmap();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
            byte[] imgByte = outputStream.toByteArray();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                imgProduct = Base64.encodeToString(imgByte,Base64.DEFAULT);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences("My_User", Context.MODE_PRIVATE);
        userPartner = sharedPreferences.getString("username","");

                String category = sp_nameCategory.getSelectedItem().toString();
                if (category.equals("Văn học")){
                    codeCategory = 1;
                }else if (category.equals("Kinh tế")){
                    codeCategory = 2;
                }else  if (category.equals("Tâm lý")){
                    codeCategory = 3;
                }
        priceProduct = til_priceProduct.getEditText().getText().toString();

        }

    public void setDataProduct(){
        Product product = new Product();
        product.setUserPartner(userPartner);
        product.setCodeCategory(codeCategory);
        product.setNameProduct(nameProduct);
        product.setPriceProduct(Integer.parseInt(priceProduct));
        product.setImgProduct(imgProduct);
        addProduct(product);
    }

    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
       this.startActivityForResult(intent, 10);
    }
    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        this.startActivityForResult(gallery, 100);


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10) {
            if (resultCode == RESULT_OK) {
                Bitmap bp = (Bitmap) data.getExtras().get("data");
                this. img_Product.setImageBitmap(bp);

                Uri imageUri = data.getData();
                img_Product.setImageURI(imageUri);
            } else if (resultCode == RESULT_CANCELED) {

            } else if (data!=null){

            }
        }
        if (requestCode == 100) {
            if (resultCode == RESULT_OK ) {
                Uri imageUri = data.getData();
                this.img_Product.setImageURI(imageUri);
            }
        }

    }

    public void getAllProducts() {
        ApiService apiService = ApiClient.getRetrofitInstance(context).create(ApiService.class);

        Call<List<Product>> call = apiService.getAllProducts();

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listProduct.clear();
                    listProduct.addAll(response.body());

                    // Log thành công khi lấy dữ liệu
                    Log.d("ProductFragment", "Dữ liệu sản phẩm đã được lấy thành công. Số lượng sản phẩm: " + listProduct.size());
                } else {
                    // Log lỗi khi không có dữ liệu hợp lệ
                    Log.e("ProductFragment", "Lỗi khi lấy dữ liệu sản phẩm: Không có dữ liệu hợp lệ.");
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                // Log lỗi khi truy vấn thất bại
                Log.e("ProductFragment", "Lỗi khi truy vấn dữ liệu sản phẩm: " + t.getMessage());
            }
        });
    }


    public void addProduct(Product product) {
        ApiService apiService = ApiClient.getRetrofitInstance(context).create(ApiService.class);

        if (listProduct.size() == 0) {
            product.setCodeProduct(1);
            Call<Void> call = apiService.addProduct(product);

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.d("ProductFragment", "Sản phẩm đã được thêm thành công với mã sản phẩm: 1");
                    } else {
                        Log.e("ProductFragment", "Lỗi khi thêm sản phẩm: Không thành công");
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("ProductFragment", "Lỗi khi thêm sản phẩm: " + t.getMessage());
                }
            });
        } else {
            int i = listProduct.size() - 1;
            int id = listProduct.get(i).getCodeProduct() + 1;
            product.setCodeProduct(id);

            Call<Void> call = apiService.addProduct(product);

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.d("ProductFragment", "Sản phẩm đã được thêm thành công với mã sản phẩm: " + id);
                    } else {
                        Log.e("ProductFragment", "Lỗi khi thêm sản phẩm: Không thành công");
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("ProductFragment", "Lỗi khi thêm sản phẩm: " + t.getMessage());
                }
            });
        }
    }


    public void updateProduct(Product product) {
        ApiService apiService = ApiClient.getRetrofitInstance(context).create(ApiService.class);

        // Gửi yêu cầu cập nhật sản phẩm qua API
        Call<Void> call = apiService.updateProduct(product.getCodeProduct(), product);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("ProductFragment", "Sản phẩm đã được cập nhật thành công: " + product.getNameProduct());
                } else {
                    Log.e("ProductFragment", "Lỗi khi cập nhật sản phẩm: Phản hồi không thành công");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("ProductFragment", "Lỗi khi cập nhật sản phẩm: " + t.getMessage());
            }
        });
    }


    public void deleteProduct(Product product) {
        ApiService apiService = ApiClient.getRetrofitInstance(context).create(ApiService.class);

        // Gửi yêu cầu xóa sản phẩm qua API
        Call<Void> call = apiService.deleteProduct(product.getCodeProduct());

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("ProductFragment", "Sản phẩm đã được xóa thành công với mã sản phẩm: " + product.getCodeProduct());
                } else {
                    Log.e("ProductFragment", "Lỗi khi xóa sản phẩm: Phản hồi không thành công");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("ProductFragment", "Lỗi khi xóa sản phẩm: " + t.getMessage());
            }
        });
    }


    public boolean isEmptys(String str,TextInputLayout til){
        if (str.isEmpty()){
            til.setError("Không được để trống");
            Log.d("ProductFragment","lỗi");
            return false;
        }else til.setError(null);
        return true;
    }
    public boolean errorImg(String str, TextView tv){
        if (str != null){
            tv.setText("");
            return true;
        }else {
            tv.setText("Ảnh không được để trống");
            return false;
        }
    }
    public int validate(int type){
        if (type == 0) {
            if (isEmptys(nameProduct, til_nameProduct) && isEmptys(priceProduct, til_priceProduct) && errorImg(imgProduct, tvErrorImg)) {
                setDataProduct();
                removeAll();
                return 0;
            }
        }else if (type==1){
            if (isEmptys(nameProduct, til_nameProduct) && isEmptys(priceProduct, til_priceProduct) && errorImg(imgProduct, tvErrorImg)) {
                return 1;
            }
        }
        return 2;
    }
    public void removeAll(){
        til_nameProduct.getEditText().setText("");
        til_priceProduct.getEditText().setText("");
        img_Product.setImageResource(R.drawable.ic_menu_camera1);
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

}