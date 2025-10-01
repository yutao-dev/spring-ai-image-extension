package com.springai.springaiimageextision.quickstart;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

/**
 * @author 王玉涛
 * @version 1.0
 * @since 2025/10/1
 */
@SpringBootTest
class OpenAiImageModelTest {

    @Resource
    private OpenAiImageModel openAiImageModel;

    @Test
    void testRun() {
        // 定义图像生成的提示词
        String prompt = "beautiful scenery";
        
        // 创建图像提示对象
        ImagePrompt imagePrompt = new ImagePrompt(prompt);
        
        // 调用OpenAI图像模型生成图像
        ImageResponse imageResponse = openAiImageModel.call(imagePrompt);
        
        // 从响应中提取生成的图像信息
        Image output = imageResponse.getResult().getOutput();
        
        // 获取图像的URL地址
        String url = output.getUrl();

        // 验证图像URL不为空
        Assert.hasLength(url, "Image URL is empty!");

        // 输出图像URL到控制台
        System.out.println(url);
    }
}
