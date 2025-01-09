package com.example.Sachpee.Fragment.BottomNav;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ItemDecoration;

import android.graphics.Rect;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.Sachpee.Adapter.ImageSliderAdapter;
import com.example.Sachpee.Adapter.ProductAdapter;
import com.example.Sachpee.Fragment.ProductFragments.GiaokhoaFragment;
import com.example.Sachpee.Fragment.ProductFragments.HoikyFragment;
import com.example.Sachpee.Fragment.ProductFragments.KinhteFragment;
import com.example.Sachpee.Fragment.ProductFragments.NgoainguFragment;
import com.example.Sachpee.Fragment.ProductFragments.TamlyFragment;
import com.example.Sachpee.Fragment.ProductFragments.ProductFragment;
import com.example.Sachpee.Fragment.ProductFragments.ThieunhiFragment;
import com.example.Sachpee.Fragment.ProductFragments.VanhocFragment;

import com.example.Sachpee.Model.ImageSlider;
import com.example.Sachpee.Model.Product;
import com.example.Sachpee.Model.ProductTop;
import com.example.Sachpee.R;
import com.example.Sachpee.Service.ApiClient;
import com.example.Sachpee.Service.ApiService;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import androidx.viewpager.widget.ViewPager;
import me.relex.circleindicator.CircleIndicator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.facebook.shimmer.ShimmerFrameLayout;

public class HomePageFragment extends Fragment {

    private List<ProductTop> productToplistVanhoc = new ArrayList<>();
    private List<ProductTop> productToplistTamly= new ArrayList<>();
    private List<ProductTop> productToplistKinhte= new ArrayList<>();
    private List<ProductTop> productToplistBook= new ArrayList<>();
    private List<ProductTop> productToplistThieunhi= new ArrayList<>();
    private List<ProductTop> productToplistHoiky= new ArrayList<>();
    private List<ProductTop> productToplistGiaokhoa= new ArrayList<>();
    private List<ProductTop> productToplistNgoaingu= new ArrayList<>();
    private List<Product> listVanhoc = new ArrayList<>();
    private List<Product> listTamly= new ArrayList<>();
    private List<Product> listKinhte= new ArrayList<>();
    private List<Product> listBook= new ArrayList<>();
    private List<Product> listProduct = new ArrayList<>();
    private List<Product> listThieunhi= new ArrayList<>();
    private List<Product> listHoiky= new ArrayList<>();
    private List<Product> listGiaokhoa= new ArrayList<>();
    private List<Product> listNgoaingu= new ArrayList<>();

    CardView card_vanhoc_home, card_kinhte_home, card_tamly_home, card_giaoduc_home, card_thieunhi_home, card_hoiky_home, card_giaokhoa_home, card_ngoaingu_home;

    RecyclerView rv_trending_home, rv_NewBook_Home;

    ///imgview
    private List<ImageSlider> list = new ArrayList<>();
    private ViewPager viewPager;
    private ImageSliderAdapter imageSliderAdapter;
    private CircleIndicator circleIndicator;
    private Handler handler;
    private Runnable runnable;
    private int currentPage = 0;
    private final int delayMillis = 5000; // Thay đổi sau mỗi 5 giây

    private ShimmerFrameLayout mShimmerViewContainer;

    private ProductAdapter adapter;
    private ProductAdapter newBooksAdapter;
    private List<Product> newBooksList = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_page, container, false);
        
        // Khởi tạo ShimmerFrameLayout
        mShimmerViewContainer = view.findViewById(R.id.shimmer_view_container);
        
        // Khởi tạo các view cho slider
        viewPager = view.findViewById(R.id.main_slider_image);
        circleIndicator = view.findViewById(R.id.circle_indicator);

        // Khởi tạo các CardView cho navigation
        card_vanhoc_home = view.findViewById(R.id.card_vanhoc_home);
        card_kinhte_home = view.findViewById(R.id.card_kinhte_home);
        card_tamly_home = view.findViewById(R.id.card_tamly_home);
        card_giaoduc_home = view.findViewById(R.id.card_giaoduc_home);
        card_thieunhi_home = view.findViewById(R.id.card_thieunhi_home);
        card_hoiky_home = view.findViewById(R.id.card_hoiky_home);
        card_giaokhoa_home = view.findViewById(R.id.card_giaokhoa_home);
        card_ngoaingu_home = view.findViewById(R.id.card_ngoaingu_home);

        // Khởi tạo RecyclerViews
        rv_trending_home = view.findViewById(R.id.rv_trending_home);
        rv_NewBook_Home = view.findViewById(R.id.rv_NewBook_Home);

        // Setup các thành phần khác
        setupSlider();
        setupRecyclerViews();
        setupClickListeners(view);

        // Thêm gọi API để lấy dữ liệu
        getTopProduct();
        getProduct();

        return view;
    }

    private void setupClickListeners(View view) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

        // Setup click listeners cho các CardView
        card_vanhoc_home.setOnClickListener(v -> 
            fragmentManager.beginTransaction()
                .replace(R.id.frame_Home, new VanhocFragment())
                .addToBackStack(null)
                .commit());

        card_kinhte_home.setOnClickListener(v -> 
            fragmentManager.beginTransaction()
                .replace(R.id.frame_Home, new KinhteFragment())
                .addToBackStack(null)
                .commit());

        card_tamly_home.setOnClickListener(v -> 
            fragmentManager.beginTransaction()
                .replace(R.id.frame_Home, new TamlyFragment())
                .addToBackStack(null)
                .commit());

        card_giaoduc_home.setOnClickListener(v -> 
            fragmentManager.beginTransaction()
                .replace(R.id.frame_Home, new PartnerBookFragment())
                .addToBackStack(null)
                .commit());

        card_thieunhi_home.setOnClickListener(v -> 
            fragmentManager.beginTransaction()
                .replace(R.id.frame_Home, new ThieunhiFragment())
                .addToBackStack(null)
                .commit());

        card_hoiky_home.setOnClickListener(v -> 
            fragmentManager.beginTransaction()
                .replace(R.id.frame_Home, new HoikyFragment())
                .addToBackStack(null)
                .commit());

        card_giaokhoa_home.setOnClickListener(v -> 
            fragmentManager.beginTransaction()
                .replace(R.id.frame_Home, new GiaokhoaFragment())
                .addToBackStack(null)
                .commit());

        card_ngoaingu_home.setOnClickListener(v -> 
            fragmentManager.beginTransaction()
                .replace(R.id.frame_Home, new NgoainguFragment())
                .addToBackStack(null)
                .commit());

        // Click listener cho nút "Xem tất cả"
        View viewAllTrending = view.findViewById(R.id.tv_view_all_trending);
        if (viewAllTrending != null) {
            viewAllTrending.setOnClickListener(v -> {
                // Xử lý chuyển sang màn hình xem tất cả
            });
        }
    }

    private void startAutoSlide() {
        runnable = new Runnable() {
            @Override
            public void run() {
                if (currentPage == list.size()) {
                    currentPage = 0; // Quay lại trang đầu tiên nếu đến trang cuối
                }
                viewPager.setCurrentItem(currentPage++, true);
                handler.postDelayed(this, delayMillis); // Đặt lại runnable sau mỗi 5 giây
            }
        };
        handler.postDelayed(runnable, delayMillis); // Bắt đầu tự động chuyển đổi
    }
    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable); // Dừng tự động chuyển đổi khi tạm dừng fragment
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.postDelayed(runnable, delayMillis); // Tiếp tục tự động chuyển đổi khi quay lại fragment
    }
    public void getTopProduct() {
        // phương thức getTopProducts() trả về danh sách ProductTop
        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
        Call<List<ProductTop>> call = apiService.getProductTop();
        call.enqueue(new Callback<List<ProductTop>>() {
            @Override
            public void onResponse(Call<List<ProductTop>> call, Response<List<ProductTop>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Xóa sạch danh sách trước khi thêm mới
                    productToplistVanhoc.clear();
                    productToplistBook.clear();
                    productToplistKinhte.clear();
                    productToplistTamly.clear();
                    productToplistThieunhi.clear();
                    productToplistHoiky.clear();
                    productToplistGiaokhoa.clear();
                    productToplistNgoaingu.clear();

                    // Lặp qua danh sách sản phẩm top nhận được từ API
                    for (ProductTop top : response.body()) {
                        // Phân loại sản phẩm theo idCategory
                        if (top.getIdCategory() == 1) {
                            productToplistVanhoc.add(top);
                        } else if (top.getIdCategory() == 2) {
                            productToplistKinhte.add(top);
                        } else if (top.getIdCategory() == 3) {
                            productToplistTamly.add(top);
                        } else if (top.getIdCategory() == 4){
                            productToplistBook.add(top);
                        } else if (top.getIdCategory() == 5) {
                            productToplistThieunhi.add(top);
                        } else if (top.getIdCategory() == 6) {
                            productToplistHoiky.add(top);
                        } else if (top.getIdCategory() == 7) {
                            productToplistGiaokhoa.add(top);
                        } else {
                            productToplistNgoaingu.add(top);
                        }
                    }

                    // Gọi hàm getProduct() sau khi hoàn tất việc lọc
                    getProduct();
                    Log.d("getTopProduct", "Số lượng sản phẩm văn học: " + productToplistVanhoc.size() +
                            ", Kinh tế: " + productToplistKinhte.size() +
                            ", Tâm lý: " + productToplistTamly.size() +
                            ", Đồ ăn: " + productToplistBook.size() +
                            ", Thiếu nhi: " + productToplistThieunhi.size() +
                            ", Hồi ký: " + productToplistHoiky.size() +
                            ", Giáo khoa: " + productToplistGiaokhoa.size() +
                            ", Ngoại ngữ: " + productToplistNgoaingu.size());
                } else {
                    Log.e("getTopProduct", "Phản hồi không thành công: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<ProductTop>> call, Throwable t) {
                Log.e("getTopProduct", "Lỗi khi gọi API: " + t.getMessage());
            }
        });
    }

    public void getProduct() {
        mShimmerViewContainer.startShimmer();
        
        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
        
        // Gọi API lấy tất cả sản phẩm
        Call<List<Product>> call = apiService.getAllProducts();
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Xử lý dữ liệu hiện tại
                    // Xóa sạch các danh sách trước khi thêm mới
                    listTamly.clear();
                    listKinhte.clear();
                    listVanhoc.clear();
                    listBook.clear();
                    listThieunhi.clear();
                    listHoiky.clear();
                    listGiaokhoa.clear();
                    listNgoaingu.clear();
                    listProduct.clear();

                    // Thêm dữ liệu mới
                    listProduct.addAll(response.body());

                    // Sắp xếp và phân loại sản phẩm
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Collections.sort(productToplistVanhoc, Comparator.comparing(ProductTop::getAmountProduct).reversed());
                        Collections.sort(productToplistKinhte, Comparator.comparing(ProductTop::getAmountProduct).reversed());
                        Collections.sort(productToplistBook, Comparator.comparing(ProductTop::getAmountProduct).reversed());
                        Collections.sort(productToplistTamly, Comparator.comparing(ProductTop::getAmountProduct).reversed());
                        Collections.sort(productToplistThieunhi, Comparator.comparing(ProductTop::getAmountProduct).reversed());
                        Collections.sort(productToplistHoiky, Comparator.comparing(ProductTop::getAmountProduct).reversed());
                        Collections.sort(productToplistGiaokhoa, Comparator.comparing(ProductTop::getAmountProduct).reversed());
                        Collections.sort(productToplistNgoaingu, Comparator.comparing(ProductTop::getAmountProduct).reversed());
                    }

                    // Thêm sản phẩm vào danh sách tương ứng
                    add(productToplistVanhoc, listProduct, listVanhoc);
                    add(productToplistKinhte, listProduct, listKinhte);
                    add(productToplistBook, listProduct, listBook);
                    add(productToplistTamly, listProduct, listTamly);
                    add(productToplistThieunhi, listProduct, listThieunhi);
                    add(productToplistHoiky, listProduct, listHoiky);
                    add(productToplistGiaokhoa, listProduct, listGiaokhoa);
                    add(productToplistNgoaingu, listProduct, listNgoaingu);

                    // Xử lý trùng lặp
                    collections(listVanhoc);
                    collections(listKinhte);
                    collections(listBook);
                    collections(listTamly);
                    collections(listThieunhi);
                    collections(listHoiky);
                    collections(listGiaokhoa);
                    collections(listNgoaingu);

                    // Cập nhật adapter trending
                    adapter.notifyDataSetChanged();

                    // Lấy 10 sản phẩm mới nhất
                    newBooksList.clear();
                    List<Product> allProducts = response.body();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        // Sắp xếp theo mã sản phẩm giảm dần (giả sử mã lớn hơn = mới hơn)
                        Collections.sort(allProducts, (p1, p2) -> 
                            Integer.compare(p2.getCodeProduct(), p1.getCodeProduct()));
                    }
                    // Lấy 10 sản phẩm đầu tiên
                    for (int i = 0; i < Math.min(10, allProducts.size()); i++) {
                        newBooksList.add(allProducts.get(i));
                    }
                    // Cập nhật adapter sách mới
                    newBooksAdapter.notifyDataSetChanged();

                    // Ẩn shimmer và hiện RecyclerView
                    mShimmerViewContainer.stopShimmer();
                    mShimmerViewContainer.setVisibility(View.GONE);
                    rv_trending_home.setVisibility(View.VISIBLE);
                    rv_NewBook_Home.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.e("getProduct", "Lỗi khi gọi API: " + t.getMessage());
                mShimmerViewContainer.stopShimmer();
                mShimmerViewContainer.setVisibility(View.GONE);
            }
        });
    }

    public void add(List<ProductTop> listTop, List<Product> listProduct ,List<Product> listProductTop ){
        for (int i = 0; i < listTop.size(); i++) {
            for (int j = 0; j < listProduct.size(); j++) {
                if (listTop.get(i).getIdProduct() == listProduct.get(j).getCodeProduct() ){
                    listProductTop.add(listProduct.get(j));
                }
            }

        }
    }
    public void collections(List<Product> listProductTop ){
//        for (int i = 0; i < listTop.size(); i++) {
//            for (int j = 0; j < listProduct.size(); j++) {
//                if (listTop.get(i).getIdProduct() == listProduct.get(j).getCodeProduct() ){
//                    listProductTop.add(listProduct.get(j));
//                }
//            }
//
//        }
        try {
            for (int i = 0; i < listProductTop.size(); i++) {
                for (int j = 1; j < listProductTop.size(); j++) {
                    if (listProductTop.get(i).getCodeProduct() == listProductTop.get(j).getCodeProduct() ){
                        listProductTop.remove(listProductTop.get(i));
                    }
                }
            }
        }catch (Exception e){

        }

    }

    private void setupRecyclerViews() {
        // Setup trending RecyclerView với GridLayoutManager
        GridLayoutManager trendingManager = new GridLayoutManager(getContext(), 2); // 2 cột
        rv_trending_home.setLayoutManager(trendingManager);
        rv_trending_home.setHasFixedSize(true);
        rv_trending_home.setItemAnimator(new DefaultItemAnimator());
        
        // Thêm decoration cho khoảng cách giữa các item
        rv_trending_home.addItemDecoration(new ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, 
                                     @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int spacing = getResources().getDimensionPixelSize(R.dimen.item_horizontal_spacing);
                int position = parent.getChildAdapterPosition(view);
                
                outRect.left = spacing;
                outRect.right = spacing;
                outRect.bottom = spacing;
                
                // Thêm top margin cho hàng đầu tiên
                if (position < 2) {
                    outRect.top = spacing;
                }
            }
        });

        // Setup adapter cho trending
        adapter = new ProductAdapter(listVanhoc, new ProductFragment(), getContext());
        rv_trending_home.setAdapter(adapter);

        // Setup new books RecyclerView cũng với GridLayoutManager
        GridLayoutManager newBooksManager = new GridLayoutManager(getContext(), 2); // 2 cột
        rv_NewBook_Home.setLayoutManager(newBooksManager);
        rv_NewBook_Home.setHasFixedSize(true);
        rv_NewBook_Home.setItemAnimator(new DefaultItemAnimator());
        
        // Thêm decoration tương tự cho new books
        rv_NewBook_Home.addItemDecoration(new ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, 
                                     @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int spacing = getResources().getDimensionPixelSize(R.dimen.item_horizontal_spacing);
                int position = parent.getChildAdapterPosition(view);
                
                outRect.left = spacing;
                outRect.right = spacing;
                outRect.bottom = spacing;
                
                // Thêm top margin cho hàng đầu tiên
                if (position < 2) {
                    outRect.top = spacing;
                }
            }
        });

        // Setup adapter cho new books
        newBooksAdapter = new ProductAdapter(newBooksList, new ProductFragment(), getContext());
        rv_NewBook_Home.setAdapter(newBooksAdapter);
    }

    private void setupSlider() {
        list.add(new ImageSlider(R.drawable.banner06));
        list.add(new ImageSlider(R.drawable.banner02));
        list.add(new ImageSlider(R.drawable.banner08));
        list.add(new ImageSlider(R.drawable.slide1));
        list.add(new ImageSlider(R.drawable.slide2));
        list.add(new ImageSlider(R.drawable.slide3));

        imageSliderAdapter = new ImageSliderAdapter(getContext(), list);
        viewPager.setAdapter(imageSliderAdapter);
        circleIndicator.setViewPager(viewPager);

        startAutoSlide();
    }
}