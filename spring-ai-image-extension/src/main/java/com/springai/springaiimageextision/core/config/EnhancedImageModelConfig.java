package com.springai.springaiimageextision.core.config;

import com.springai.springaiimageextision.core.client.EnhancedImageClient;
import com.springai.springaiimageextision.core.custom.api.EnhancedImageApi;
import com.springai.springaiimageextision.core.custom.bean.ImageOptionsProperties;
import com.springai.springaiimageextision.core.custom.model.EnhancedImageModel;
import com.springai.springaiimageextision.core.custom.option.EnhancedImageOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 王玉涛
 * @version 1.0
 * @since 2025/10/1
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class EnhancedImageModelConfig {

    /**
     * 从配置文件中读取OpenAI API密钥
     */
    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    /**
     * 从配置文件中读取OpenAI API基础URL
     */
    @Value("${spring.ai.openai.base-url}")
    private String baseUrl; 
    
    /**
     * 配置文生图选项：指定模型、推理步数和提示词
     */
    private final ImageOptionsProperties properties;
    
    /**
     * 创建EnhancedImageApi实例
     * 用于与图像生成API进行通信
     * 
     * @return EnhancedImageApi 实例
     */
    @Bean
    public EnhancedImageApi enhancedImageApi() {
        log.info("Initializing EnhancedImageApi with baseUrl: {}", baseUrl);
        return EnhancedImageApi.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .build();
    }
    
    /**
     * 创建EnhancedImageOptions实例
     * 包含图像生成的各种配置参数
     * 
     * @return EnhancedImageOptions 实例
     */
    @Bean
    public EnhancedImageOptions enhancedImageOptions() {
        log.info("Building EnhancedImageOptions with model: {} and prompt: {}", 
                properties.getModel(), properties.getPrompt());
        
        return EnhancedImageOptions.builder()
                .seed(properties.getSeed())
                .model(properties.getModel())
                .inferenceSteps(properties.getInferenceSteps())
                .prompt(properties.getPrompt())
                .negativePrompt(properties.getNegativePrompt())
                .guidanceScale(properties.getGuidanceScale())
                .cfg(properties.getCfg())
                .width(properties.getWidth())
                .height(properties.getHeight())
                .style(properties.getStyle())
                .size(properties.getSize())
                .quality(properties.getQuality())
                .responseFormat(properties.getResponseFormat())
                .user(properties.getUser())
                .image(properties.getImage())
                .n(properties.getN())
                .build();
    }
    
    /**
     * 创建EnhancedImageModel实例
     * 整合API客户端和配置选项，提供完整的图像生成能力
     * 
     * @return EnhancedImageModel 实例
     */
    @Bean
    public EnhancedImageModel enhancedImageModel() {
        log.info("Creating EnhancedImageModel with configured API and options");
        return new EnhancedImageModel(enhancedImageApi(), enhancedImageOptions(), RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }

    /**
     * 创建EnhancedImageClient实例
     * 提供图像生成API的访问入口
     *
     * @return EnhancedImageClient 实例
     */
    @Bean
    public EnhancedImageClient enhancedImageClient() {
        return new EnhancedImageClient(enhancedImageModel());
    }
}
