package com.example.monokromcoffee;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatbotBottomSheet extends BottomSheetDialogFragment {

    // Gemini API - Gratis via Google AI Studio
    private static final String GEMINI_MODEL = "gemini-2.0-flash";
    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/"
                    + GEMINI_MODEL + ":generateContent?key=";

    private static final String SYSTEM_PROMPT =
            "Kamu adalah Barista AI dari Monokrom Coffee, sebuah coffee shop premium di Indonesia. " +
            "Tugasmu adalah membantu pelanggan dengan ramah, informatif, dan hangat. " +
            "Kamu bisa menjawab pertanyaan tentang: jenis kopi, cara brew, rekomendasi menu, " +
            "info tentang kopi (asal, rasa, proses), serta fakta menarik seputar dunia kopi. " +
            "Jawab dalam Bahasa Indonesia yang santai dan friendly. " +
            "Selalu tambahkan emoji kopi atau terkait makanan/minuman agar terasa lebih hangat. " +
            "Jika ditanya hal di luar topik kopi/minuman/Monokrom Coffee, arahkan kembali dengan sopan.";

    private RecyclerView rvMessages;
    private EditText etInput;
    private FrameLayout btnSend;
    private ChatMessageAdapter adapter;
    private final List<ChatMessage> messages = new ArrayList<>();

    // Conversation history (format Gemini: role "user"/"model" + parts)
    private final JSONArray conversationHistory = new JSONArray();

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chatbot, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvMessages = view.findViewById(R.id.rvChatMessages);
        etInput    = view.findViewById(R.id.etChatInput);
        btnSend    = view.findViewById(R.id.btnSend);

        // Setup RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        adapter = new ChatMessageAdapter(messages);
        rvMessages.setAdapter(adapter);

        // Welcome message
        addBotMessage("Halo! ☕ Selamat datang di Monokrom Coffee! Saya Barista AI kamu. " +
                "Ada yang bisa saya bantu? Mau cari rekomendasi kopi, atau punya pertanyaan seputar kopi?");

        // Send button
        btnSend.setOnClickListener(v -> sendMessage());

        // IME send action
        etInput.setOnEditorActionListener((tv, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void sendMessage() {
        String input = etInput.getText().toString().trim();
        if (input.isEmpty()) return;

        addUserMessage(input);
        etInput.setText("");
        btnSend.setEnabled(false);

        // Tampilkan loading
        int loadingIndex = messages.size();
        messages.add(new ChatMessage("", ChatMessage.TYPE_LOADING));
        adapter.notifyItemInserted(loadingIndex);
        scrollToBottom();

        callGeminiApi(input, loadingIndex);
    }

    private void callGeminiApi(String userText, int loadingIndex) {
        try {
            // Tambah pesan user ke history (format Gemini)
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            JSONArray userParts = new JSONArray();
            JSONObject userPart = new JSONObject();
            userPart.put("text", userText);
            userParts.put(userPart);
            userMsg.put("parts", userParts);
            conversationHistory.put(userMsg);

            // Bangun request body format Gemini
            JSONObject requestBody = new JSONObject();

            // System instruction
            JSONObject systemInstruction = new JSONObject();
            JSONArray systemParts = new JSONArray();
            JSONObject systemPart = new JSONObject();
            systemPart.put("text", SYSTEM_PROMPT);
            systemParts.put(systemPart);
            systemInstruction.put("parts", systemParts);
            requestBody.put("system_instruction", systemInstruction);

            // Conversation contents (seluruh history)
            requestBody.put("contents", conversationHistory);

            // Generation config
            JSONObject generationConfig = new JSONObject();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("maxOutputTokens", 1024);
            requestBody.put("generationConfig", generationConfig);

            String apiKey = BuildConfig.GEMINI_API_KEY;
            String url = GEMINI_API_URL + apiKey;

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(
                            requestBody.toString(),
                            MediaType.get("application/json; charset=utf-8")))
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    mainHandler.post(() -> {
                        removeLastFromHistory();
                        removeLoadingAndAdd(loadingIndex,
                                "😔 Maaf, gagal terhubung ke server. Periksa koneksi internet kamu ya.");
                        btnSend.setEnabled(true);
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    mainHandler.post(() -> {
                        try {
                            JSONObject json = new JSONObject(responseBody);

                            if (json.has("candidates")) {
                                // Parse Gemini response format
                                String botText = json
                                        .getJSONArray("candidates")
                                        .getJSONObject(0)
                                        .getJSONObject("content")
                                        .getJSONArray("parts")
                                        .getJSONObject(0)
                                        .getString("text");

                                // Simpan balasan model ke history (role "model" untuk Gemini)
                                JSONObject modelMsg = new JSONObject();
                                modelMsg.put("role", "model");
                                JSONArray modelParts = new JSONArray();
                                JSONObject modelPart = new JSONObject();
                                modelPart.put("text", botText);
                                modelParts.put(modelPart);
                                modelMsg.put("parts", modelParts);
                                conversationHistory.put(modelMsg);

                                removeLoadingAndAdd(loadingIndex, botText);

                            } else if (json.has("error")) {
                                removeLastFromHistory();
                                String errMsg = json.getJSONObject("error").getString("message");
                                removeLoadingAndAdd(loadingIndex, "⚠️ Error: " + errMsg);
                            } else {
                                removeLastFromHistory();
                                removeLoadingAndAdd(loadingIndex,
                                        "🤔 Maaf, saya tidak bisa memproses permintaan itu sekarang.");
                            }
                        } catch (JSONException e) {
                            removeLastFromHistory();
                            removeLoadingAndAdd(loadingIndex,
                                    "😔 Terjadi kesalahan saat memproses respons.");
                        }
                        btnSend.setEnabled(true);
                    });
                }
            });

        } catch (JSONException e) {
            removeLastFromHistory();
            removeLoadingAndAdd(loadingIndex, "😔 Terjadi kesalahan. Coba lagi ya!");
            btnSend.setEnabled(true);
        }
    }

    // Hapus pesan terakhir dari history jika request gagal
    private void removeLastFromHistory() {
        if (conversationHistory.length() > 0) {
            conversationHistory.remove(conversationHistory.length() - 1);
        }
    }

    private void addUserMessage(String text) {
        messages.add(new ChatMessage(text, ChatMessage.TYPE_USER));
        adapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
    }

    private void addBotMessage(String text) {
        messages.add(new ChatMessage(text, ChatMessage.TYPE_BOT));
        adapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
    }

    private void removeLoadingAndAdd(int loadingIndex, String botText) {
        if (loadingIndex < messages.size() &&
                messages.get(loadingIndex).getType() == ChatMessage.TYPE_LOADING) {
            messages.remove(loadingIndex);
            adapter.notifyItemRemoved(loadingIndex);
        }
        addBotMessage(botText);
    }

    private void scrollToBottom() {
        rvMessages.post(() -> {
            if (adapter.getItemCount() > 0) {
                rvMessages.smoothScrollToPosition(adapter.getItemCount() - 1);
            }
        });
    }
}
