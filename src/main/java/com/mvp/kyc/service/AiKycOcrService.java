package com.mvp.kyc.service;

import com.mvp.kyc.model.KycResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class AiKycOcrService {

    private final ObjectMapper mapper = new ObjectMapper();

    public KycResult analyze(byte[] imageBytes) {
        try {
            String localText = callLocalOcr(imageBytes);
            String apiText = callOcrApi(imageBytes);
            String merged = (localText + " " + apiText).trim();
            String json = callOllamaMistral(merged);
            return new KycResult(true, json);
        } catch (Exception e) {
            return new KycResult(false, "Ошибка анализа: " + e.getMessage());
        }
    }

    private String callLocalOcr(byte[] imageBytes) throws Exception {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "passport.jpg",
                        RequestBody.create(imageBytes, MediaType.parse("image/jpeg")))
                .build();
        Request request = new Request.Builder()
                .url("http://localhost:5000/ocr")
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            String res = response.body().string();
            JsonNode json = mapper.readTree(res);
            return json.path("text").asText("");
        }
    }

    private String callOcrApi(byte[] imageBytes) throws Exception {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("apikey", "YOUR_OCRSPACE_KEY")
                .addFormDataPart("language", "rus")
                .addFormDataPart("file", "image.jpg",
                        RequestBody.create(imageBytes, MediaType.parse("image/jpeg")))
                .build();
        Request request = new Request.Builder()
                .url("https://api.ocr.space/parse/image")
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            String res = response.body().string();
            JsonNode node = mapper.readTree(res);
            return node.path("ParsedResults").get(0).path("ParsedText").asText("");
        }
    }

    private String callOllamaMistral(String mergedText) throws Exception {
        OkHttpClient client = new OkHttpClient();
        String prompt = "Извлеки из текста паспорта РФ строго JSON формата: {\n" +
                "  \"ФИО\": \"...\",\n" +
                "  \"Серия\": \"...\",\n" +
                "  \"Номер\": \"...\",\n" +
                "  \"ДатаВыдачи\": \"...\",\n" +
                "  \"КодПодразделения\": \"...\"\n" +
                "} Не добавляй комментариев. Текст:\n" + mergedText;
        String json = mapper.writeValueAsString(Map.of(
                "model", "mistral",
                "prompt", prompt,
                "stream", false
        ));
        Request request = new Request.Builder()
                .url("http://localhost:11434/api/generate")
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
}
