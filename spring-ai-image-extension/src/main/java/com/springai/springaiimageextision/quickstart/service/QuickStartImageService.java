package com.springai.springaiimageextision.quickstart.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.stereotype.Service;

/**
 * 快速开始图像服务类
 * 
 * @author 王玉涛
 * @version 1.0
 * @since 2025/10/1
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuickStartImageService {
    
    /**
     * OpenAI 图像模型客户端，用于生成图像
     */
    private final OpenAiImageModel openAiImageModel;

    /**
     * 根据提示词生成图像
     * 
     * @param prompt 图像生成的提示词
     * @return 生成图像的URL地址
     */
    public String getImage(String prompt) {
        // 创建图像提示对象
        ImagePrompt imagePrompt = new ImagePrompt(prompt);
        
        // 调用OpenAI模型生成图像
        ImageResponse imageResponse = openAiImageModel.call(imagePrompt);

        // 从响应中提取图像信息
        Image output = imageResponse.getResult().getOutput();
        String url = output.getUrl();
        
        // 记录生成的图像URL
        log.info("Image URL: {}", url);
        
        return url;
    }
}
