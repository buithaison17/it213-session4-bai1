package com.example.bai1;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
public class ChatModelConfig {
    @Bean
    @Primary
    @Profile("cloud")
    public ChatModel chatModel(OpenAiChatModel openAiChatModel) {
        return openAiChatModel;
    }

    @Bean
    @Primary
    @Profile("local")
    public ChatModel chatModel(OllamaChatModel ollamaChatModel) {
        return ollamaChatModel;
    }
}
