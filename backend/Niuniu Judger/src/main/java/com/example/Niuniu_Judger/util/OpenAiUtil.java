package com.example.Niuniu_Judger.util;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.ArrayList;

@Component
public class OpenAiUtil {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    private final String baseUrl = "https://chatapi.midjourney-vip.cn/v1/chat/completions";
    private final OkHttpClient client;

    public OpenAiUtil() {
        this.client = new OkHttpClient.Builder()
                .callTimeout(Duration.ofSeconds(60))
                .build();
    }

    /**
     * 分析开发者的个人介绍，提取领域信息
     */
    public String analyzeDeveloperProfile(String text) {
        // 构建 ChatGPT 请求的消息体
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("system", "你是一个助手，用于从开发者的个人简介中提取主要开发领域。"));
        messages.add(new Message("user", "请从以下开发者的自我介绍中提取主要的开发领域或方向。你的回复仅包含具体的研究领域的名称逗号隔开,无法推理则回复N/A\n" + text));

        // 将消息转换为 JSON 格式
        String requestBody = String.format("{\"model\": \"gpt-3.5-turbo\", \"messages\": %s, \"max_tokens\": 150, \"temperature\": 0.7}",
                toJson(messages));

        // 创建请求
        Request request = new Request.Builder()
                .url(baseUrl)
                .header("Authorization", "Bearer " + openAiApiKey)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(requestBody, MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            // 解析响应体
            String responseBody = response.body().string();
            String domain = parseContentFromResult(responseBody);
            return domain;

        } catch (IOException e) {
            e.printStackTrace();
            return "N/A";
        }
    }

    /**
     * 分析开发者的个人介绍
     */
    public String PJDeveloperProfile(String text) {
        // 构建 ChatGPT 请求的消息体
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("system", "你是一个助手，用于从开发者的简介和开发的网页内容或博客对开发者进行评估。"));
        messages.add(new Message("user", "请根据以下内容生成该开发者的评估信息，字数为145：" + text ));

        // 将消息转换为 JSON 格式
        String requestBody = String.format("{\"model\": \"gpt-3.5-turbo\", \"messages\": %s, \"max_tokens\": 150, \"temperature\": 0.7}",
                toJson(messages));
        System.out.println(requestBody);
        // 创建请求
        Request request = new Request.Builder()
                .url(baseUrl)
                .header("Authorization", "Bearer " + openAiApiKey)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(requestBody, MediaType.get("application/json")))
                .build();
        int MAX_TRY_TIME = 5;
        while (MAX_TRY_TIME >= 1) {
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    if (MAX_TRY_TIME >= 2) {
                        MAX_TRY_TIME = MAX_TRY_TIME - 1;
                        continue;
                    } else {
                        throw new IOException("Unexpected code " + response);
                    }
                }

                // 解析响应体
                String responseBody = response.body().string();
                String comment = parseContentFromResult(responseBody);
                return comment;

            } catch (IOException e) {
                e.printStackTrace();
                return "N/A";
            }
        }
        throw new RuntimeException("Maximum try time reach, request stop");
    }

    /**
     * 推断开发者的国家/地区
     */
    public String inferNation(String bio, String location, String email) {
        // 构建 ChatGPT 请求的消息体
        StringBuilder content = new StringBuilder();
        content.append("[请根据以下信息推断开发者的国家或地区。方括号中的文字不构成推断的因素]");
        if (bio != null && !bio.isEmpty()) {
            content.append("[个人简介: ]").append(bio);
        }
        if (location != null && !location.isEmpty()) {
            content.append("[位置: ]").append(location);
        }
        if (email != null && !email.isEmpty()) {
            content.append("[电子邮件: ]").append(email);
        }
        content.append("[你的回复应仅包含国家名称。]");

        // 构建消息
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("assistant", "你是一个助手，用于根据个人信息推断开发者的国家。你的回答应该只有一个国家的名称，无法推理则回复N/A"));
        messages.add(new Message("user", content.toString()));

        // 将消息转换为 JSON 格式
        String requestBody = String.format("{\"model\": \"gpt-3.5-turbo\", \"messages\": %s, \"max_tokens\": 200, \"temperature\": 0.0}",
                toJson(messages));

        // 创建请求
        Request request = new Request.Builder()
                .url(baseUrl)
                .header("Authorization", "Bearer " + openAiApiKey)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(requestBody, MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            // 解析响应体
            String responseBody = response.body().string();
            String nation = parseContentFromResult(responseBody);
            return nation;

        } catch (IOException e) {
            e.printStackTrace();
            return "N/A";
        }
    }

    /**
     * 将消息列表转换为 JSON 字符串格式
     */
    private String toJson(List<Message> messages) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            json.append(String.format("{\"role\": \"%s\", \"content\": \"%s\"}", message.role, message.content.replace("\"", "\\\"")));
            if (i < messages.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }

    /**
     * 解析 OpenAI 返回的结果，提取内容
     */
    private String parseContentFromResult(String result) {
        try {
            String contentKey = "\"content\":";
            int contentIndex = result.indexOf(contentKey);

            if (contentIndex == -1) {
                return "N/A";
            }
            int start = contentIndex + contentKey.length();
            int firstQuoteIndex = result.indexOf("\"", start);
            int secondQuoteIndex = result.indexOf("\"", firstQuoteIndex + 1);

            if (firstQuoteIndex == -1 || secondQuoteIndex == -1) {
                return "N/A";
            }

            return result.substring(firstQuoteIndex + 1, secondQuoteIndex);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "N/A";
    }

    /**
     * 内部消息类，用于构建请求体
     */
    private static class Message {
        String role;
        String content;

        public Message(String role, String content) {
            this.role = role.replace("\n", "").replace("\r", "");
            this.content = content.replace("\n", "").replace("\r", "");
        }
    }

    /**
     * GPT回复通用接口
     */
    public String getGPTResponse(String message) {
        // 构建 ChatGPT 请求的消息体
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("user", message));

        // 将消息转换为 JSON 格式
        String requestBody = String.format(
                "{\"model\": \"gpt-3.5-turbo\", \"messages\": %s, \"max_tokens\": 200, \"temperature\": 0.7}",
                toJson(messages)
        );

        // 创建请求
        Request request = new Request.Builder()
                .url(baseUrl)
                .header("Authorization", "Bearer " + openAiApiKey)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(requestBody, MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            String responseBody = response.body().string();
            String reply = parseContentFromResult(responseBody);
            return reply;

        } catch (IOException e) {
            e.printStackTrace();
            return "N/A";
        }
    }

}
