package com.example.demo.service.impl;

import com.example.demo.model.dto.ChatRequestDTO;
import com.example.demo.model.vo.ChatResponseVO;
import com.example.demo.service.ChatService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatServiceImpl implements ChatService {

    private final RestTemplate restTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final String apiKey;
    private final String model;

    public ChatServiceImpl(RestTemplate restTemplate, 
                          StringRedisTemplate stringRedisTemplate,
                          @Value("${spring.ai.dashscope.api-key}") String apiKey,
                          @Value("${spring.ai.dashscope.chat.model}") String model) {
        this.restTemplate = restTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public ChatResponseVO chat(ChatRequestDTO requestDTO) {
        String sessionId = requestDTO.getSessionId();
        String message = requestDTO.getMessage();
        String redisKey = "chat:session:" + sessionId;

        // 1. 读取历史消息
        List<String> records = stringRedisTemplate.opsForList().range(redisKey, 0, -1);
        String historyText = "";
        if (records != null && !records.isEmpty()) {
            historyText = String.join("\n", records);
        }

        // 2. 拼接上下文
        String finalPrompt;
        if (!historyText.isEmpty()) {
            finalPrompt = String.format("以下是历史对话：\n%s\n当前用户问题：\n%s", historyText, message);
        } else {
            finalPrompt = message;
        }

        // 3. 调用火山引擎 Ark API
        String answer = callArkApi(finalPrompt);

        // 4. 保存本轮记录
        String recordText = "用户：" + message + "\n助手：" + answer;
        stringRedisTemplate.opsForList().rightPush(redisKey, recordText);

        // 5. 只保留最近 3 轮
        Long size = stringRedisTemplate.opsForList().size(redisKey);
        if (size != null && size > 3) {
            stringRedisTemplate.opsForList().trim(redisKey, size - 3, size - 1);
        }

        return new ChatResponseVO(message, answer);
    }

    private String callArkApi(String message) {
        String url = "https://ark.cn-beijing.volces.com/api/v3/chat/completions";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(
            Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 1024);
        requestBody.put("temperature", 0.7);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, Object> messageObj = (Map<String, Object>) choice.get("message");
                    if (messageObj != null && messageObj.containsKey("content")) {
                        return (String) messageObj.get("content");
                    }
                }
            }
        } catch (Exception e) {
            return "API调用失败: " + e.getMessage();
        }
        
        return "抱歉，我无法回答你的问题。";
    }
}
