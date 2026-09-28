package com.datapilot.server.ai.service.impl;

import com.datapilot.server.ai.service.AiModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

@Service

public class AiModelServiceImpl implements AiModelService {

    private final ChatClient chatclient;
    public AiModelServiceImpl(ChatClient.Builder chatClientBuilder){
        this.chatclient=chatClientBuilder.build();
    }

    @Override
    public String chat(String prompt){
        return chatclient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    @Override
    public <T> T chat(String systemPrompt,String userPrompt,Class<T> responseType){
        return chatclient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .entity(responseType);
    }

    @Override
    public String chat(String systemPrompt,String userPrompt){
        return chatclient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();
    }
}
