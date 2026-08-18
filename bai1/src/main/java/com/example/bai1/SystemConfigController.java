package com.example.bai1;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/incident/config")
public class SystemConfigController {

    @Value("${spring.ai.ollama.chat.model:}")
    private String ollamaModel;

    @Value("${spring.ai.openai.chat.model:}")
    private String openAiModel;

    @GetMapping
    public String getCurrentModel() {

        if (!ollamaModel.isBlank()) {
            return ollamaModel;
        }

        if (!openAiModel.isBlank()) {
            return openAiModel;
        }

        return "Unknown";
    }
}