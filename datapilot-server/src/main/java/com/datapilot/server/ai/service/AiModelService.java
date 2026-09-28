package com.datapilot.server.ai.service;

public interface AiModelService {
    /**
     * 调用大语言模型
     *
     * @param prompt 提示词
     *
     * @return 模型返回内容
     */
    String chat(String prompt);

    /**
     * 结构化调用
     */
    <T> T chat(String systemPrompt,String userPrompt,Class<T> responseType);

    String chat(String systemPrompt, String userPrompt);
}
