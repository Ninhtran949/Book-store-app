package com.example.Sachpee.Service;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.OkHttpClient;
import org.json.JSONObject;

public class AuthInterceptor implements Interceptor {
    private static final String TAG = "AuthInterceptor";
    private Context context;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        SharedPreferences sharedPreferences = context.getSharedPreferences("My_User", Context.MODE_PRIVATE);
        String accessToken = sharedPreferences.getString("accessToken", "");
        String refreshToken = sharedPreferences.getString("refreshToken", "");

        Request originalRequest = chain.request();
        Request.Builder builder = originalRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken);

        Request newRequest = builder.build();

        // Thêm log để kiểm tra Interceptor
        Log.d(TAG, "Interceptor called");
        Log.d(TAG, "Request URL: " + newRequest.url());
        Log.d(TAG, "Authorization Header: " + newRequest.header("Authorization"));

        Response response = chain.proceed(newRequest);

        // Kiểm tra nếu access token hết hạn
        if (response.code() == 401) {
            // Làm mới access token
            synchronized (this) {
                // Lấy access token mới
                String newAccessToken = getNewAccessToken(refreshToken);

                if (newAccessToken != null) {
                    // Lưu access token mới vào SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("accessToken", newAccessToken);
                    editor.apply();

                    // Tạo yêu cầu mới với access token mới
                    Request newRequestWithNewToken = originalRequest.newBuilder()
                            .header("Authorization", "Bearer " + newAccessToken)
                            .build();

                    // Thử lại yêu cầu ban đầu với access token mới
                    response.close();
                    return chain.proceed(newRequestWithNewToken);
                }
            }
        }

        return response;
    }

    private String getNewAccessToken(String refreshToken) {
        try {
            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(MediaType.parse("application/json"), new JSONObject().put("refreshToken", refreshToken).toString());
            Request request = new Request.Builder()
                    .url("http://192.168.28.217:8080/token") // URL endpoint để làm mới token
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                JSONObject jsonObject = new JSONObject(responseBody);
                return jsonObject.getString("accessToken");
            } else {
                Log.e(TAG, "Failed to refresh token: " + response.message());
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception while refreshing token", e);
        }
        return null;
    }
}