package com.example.Sachpee.Service;

import com.example.Sachpee.Model.Bill;
import com.example.Sachpee.Model.Cart;
import com.example.Sachpee.Model.Partner;
import com.example.Sachpee.Model.PaymentRequest;
import com.example.Sachpee.Model.PaymentResponse;
import com.example.Sachpee.Model.Product;
import com.example.Sachpee.Model.ProductTop;
import com.example.Sachpee.Model.StatusResponse;
import com.example.Sachpee.Model.User;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @GET("/user/phone/{phoneNumber}")
    Call<User> getUserByPhoneNumber(@Path("phoneNumber") String phoneNumber);

    @POST("/user/signup")
    Call<Void> signUpUser(@Body User user);

    @POST("/user/login")
    Call<ResponseBody> loginUser(@Body Map<String, String> loginData);

    @POST("/user/logout")
    Call<ResponseBody> logoutUser(@Body Map<String, String> requestBody);

    // Phương thức để lấy thông tin đối tác bằng idPartner
    @GET("/partners/{idPartner}")
    Call<Partner> getPartnerById(@Path("idPartner") int idPartner);
    // Lấy giỏ hàng từ MongoDB theo userClient (username)
    @GET("/cart/user/{userClient}")
    Call<List<Cart>> getCartsByUser(@Path("userClient") String userClient);

    @GET("/bills/status/no/{user}")
    Call<List<Bill>> getBillsByStatusNo(@Path("user") String user);

    @POST("/bills/addBill")
    Call<Void> addBill(@Body Bill bill);

    @DELETE("cart/{idCart}")

    Call<Void> deleteCartItem(@Path("idCart") int idCart);

    @GET("/bills/")
    Call<List<Bill>> getAllBills();
    @GET("/partners/")
    Call<List<Partner>> getAllPartners(); // API lấy tất cả các đối tác
    @GET("/productTop/") // lấy tất cả productTop
    Call<List<ProductTop>> getProductTop();

    @GET("/cart/user/{userClient}")
    Call<List<Cart>> getCartProduct(@Path("userClient") String userClient);

    // Tạo mới ProductTop
    @POST("/productTop/create")
    Call<Void> createProductTop(@Body ProductTop productTop);
    // Cập nhật số lượng ProductTop
    @PUT("/update/{id}")
    Call<Void> updateProductTop(@Path("id") int id, @Body ProductTop top);

    // Cập nhật Cart theo idCart với Map
    @PATCH("/cart/idCart/{idCart}")
    Call<Void> updateCartQuantity(@Path("idCart") int idCart, @Body Map<String, Object> updates);

    //Product ATV
    @GET("/products/")
    Call<List<Product>> getAllProducts();

    @POST("/products/")
    Call<Void> addProduct(@Body Product product);

    @PATCH("/products/{codeProduct}")
    Call<Void> updateProduct(@Path("codeProduct") int codeProduct, @Body Product product);

    @DELETE("/products/{codeProduct}")
    Call<Void> deleteProduct(@Path("codeProduct") int codeProduct);

    @POST("/cart/")
    Call<Cart> addCart(@Body Cart cart);

    @GET("/user/")
    Call<List<User>> getAllUsers();

    @POST("/")
    Call<Void> addPartner(@Body Partner Partner);
    @DELETE("/partners/{idPartner}")
    Call<Void> deletePartner(@Path("idPartner") int idPartner);

    @GET("/bills/{idBill}/cart")
    Call<List<Cart>> getCartsByBillId(@Path("idBill") int idBill);

    @GET("/user/id/{Id}")
    Call<User> getUserById(@Path("userId") String userId);


    @PATCH("/bills/{idBill}/status")
    Call<Void> updateBillStatus(@Path("idBill") String idBill, @Body Map<String, String> status);


    @PATCH("/partners/{idPartner}")
    Call<Void> updatePartner(@Path("id") int idPartner, @Body Map<String, Object> fields);
    @PATCH("/user/id/{id}")
    Call<Void> updateUser(@Path("id") String id, @Body Map<String, Object> fields);

    @PATCH("/partners/{idPartner}")
    Call<Void> updatePartnerPassword(@Path("idPartner") int idPartner, @Body Partner partner);
    @PATCH("/user/id/{id}")
    Call<Void> updateUserPassword(@Path("id") String id, @Body User user);

    @GET("/bills/range")
    Call<List<Bill>> getBillsInRange(@Query("user") String user, @Query("startDate") String startDate, @Query("endDate") String endDate);

    // Phương thức lấy danh sách Bill theo idPartner (user) va status "yes"
    @GET("/bills/{idPartner}")
    Call<List<Bill>> getBillsByPartner(@Path("idPartner") String idPartner);


    //payment
    @POST("/zalopay/payment")
    Call<PaymentResponse> createPayment(@Body PaymentRequest paymentRequest);

    @GET("/zalopay/check-status-order/{idBill}")
    Call<StatusResponse> checkOrderStatus(@Path("idBill") int idBill);
}
