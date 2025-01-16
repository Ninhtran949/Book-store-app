package com.example.Sachpee.Fragment.BottomNav;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.Sachpee.Adapter.ChatMessageAdapter;
import com.example.Sachpee.R;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {
    private WebSocket webSocket;
    private EditText messageInput;
    private EditText nameInput;
    private RecyclerView chatRecyclerView;
    private ChatMessageAdapter chatMessageAdapter;
    private List<String> chatMessages = new ArrayList<>();
    private String userName = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        messageInput = view.findViewById(R.id.messageInput);
        nameInput = view.findViewById(R.id.nameInput);
        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);
        Button sendButton = view.findViewById(R.id.sendButton);
        Button submitNameButton = view.findViewById(R.id.submitNameButton);

        chatMessageAdapter = new ChatMessageAdapter(chatMessages);
        chatRecyclerView.setAdapter(chatMessageAdapter);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        submitNameButton.setOnClickListener(v -> {
            userName = nameInput.getText().toString().trim();
            if (!userName.isEmpty()) {
                initiateWebSocket();
                nameInput.setVisibility(View.GONE);
                submitNameButton.setVisibility(View.GONE);
            }
        });

        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString();
            if (!message.isEmpty() && !userName.isEmpty()) {
                try {
                    JSONObject jsonMessage = new JSONObject();
                    jsonMessage.put("type", "chat");
                    jsonMessage.put("text", message);
                    webSocket.send(jsonMessage.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                messageInput.setText("");
            } else if (userName.isEmpty()) {
                Toast.makeText(getActivity(), "Please enter your name first.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void initiateWebSocket() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("ws://192.168.28.217:8080").build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            private final WeakReference<ChatFragment> fragmentRef = new WeakReference<>(ChatFragment.this);

            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull okhttp3.Response response) {
                ChatFragment fragment = fragmentRef.get();
                if (fragment != null && fragment.isAdded()) {
                    fragment.getActivity().runOnUiThread(() -> {
                        //chatMessages.add("Connected to server as " + fragment.userName);
                        chatMessageAdapter.notifyDataSetChanged();
                    });
                    webSocket.send("{\"type\":\"setName\", \"name\":\"" + fragment.userName + "\"}");
                }
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                ChatFragment fragment = fragmentRef.get();
                if (fragment != null && fragment.isAdded()) {
                    fragment.getActivity().runOnUiThread(() -> {
                        try {
                            JSONObject message = new JSONObject(text);
                            String type = message.getString("type");

                            switch (type) {
                                case "success":
                                    break;
                                case "error":
                                    String errorMessage = message.getString("message");
                                    chatMessages.add("Error: " + errorMessage);
                                    break;
                                case "serverMessage":
                                    String serverMessage = message.getString("message");
                                    if (message.has("name")) {
                                        String name = message.getString("name");
                                        chatMessages.add(name + ": " + serverMessage);
                                    } else {
                                        chatMessages.add(serverMessage);
                                    }
                                    break;
                                case "chat":
                                    String sender = message.getString("sender");
                                    String chatMessage = message.getString("message");
                                    chatMessages.add(sender + ": " + chatMessage);
                                    break;
                                default:
                                    chatMessages.add("Unknown message type: " + type);
                                    break;
                            }
                            chatMessageAdapter.notifyDataSetChanged();
                            chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                        } catch (JSONException e) {
                            chatMessages.add("Error parsing message: " + e.getMessage());
                        } catch (Exception e) {
                            chatMessages.add("Unexpected error: " + e.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                ChatFragment fragment = fragmentRef.get();
                if (fragment != null && fragment.isAdded()) {
                    webSocket.close(1000, null);
                    fragment.getActivity().runOnUiThread(() -> {
                        chatMessages.add("Closing: " + reason);
                        chatMessageAdapter.notifyDataSetChanged();
                    });
                }
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable okhttp3.Response response) {
                ChatFragment fragment = fragmentRef.get();
                if (fragment != null && fragment.isAdded()) {
                    fragment.getActivity().runOnUiThread(() -> {
                        chatMessages.add("Error: " + t.getMessage());
                        chatMessageAdapter.notifyDataSetChanged();
                    });
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webSocket != null) {
            webSocket.close(1000, "App is closing");
            webSocket = null;
        }
    }
}
