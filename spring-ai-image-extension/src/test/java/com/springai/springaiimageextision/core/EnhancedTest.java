package com.springai.springaiimageextision.core;

import com.springai.springaiimageextision.core.custom.api.EnhancedImageApi;
import com.springai.springaiimageextision.core.custom.option.EnhancedImageOptions;
import com.springai.springaiimageextision.core.util.ImageUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author 王玉涛
 * @version 1.0
 * @since 2025/10/1
 */
@SpringBootTest
class EnhancedTest {

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
     * 测试EnhancedImageApi和EnhancedImageOptions功能
     * 包括文生图和图生图两种模式的测试
     * 
     * @throws IOException 当读取资源文件失败时抛出
     */
    @Test
    void testApiAndOptions() throws IOException {
        // 构建EnhancedImageApi实例，设置API密钥和基础URL
        EnhancedImageApi imageApi = EnhancedImageApi
                .builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .build();

        // 配置文生图选项：指定模型、推理步数和提示词
        EnhancedImageOptions imageOptions = EnhancedImageOptions.builder()
                .model("Qwen/Qwen-Image")
                .inferenceSteps(30)
                .prompt("请你生成小狗图片")
                .build();

        // 加载测试用的原始图片资源

        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("static/风景图片01.png");
        Assert.notNull(resource, "没有找到图片");
        String filePath = java.net.URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8);
        File file = new File(filePath);
        
        // 配置图生图选项：指定模型、输入图片和提示词
        EnhancedImageOptions imageOptionsEdit = EnhancedImageOptions.builder()
                .model("Qwen/Qwen-Image-Edit")
                .image(ImageUtils.convert(file))
                .prompt("请你将图片的天空改为星空")
                .build();

        // 调用API生成图片，分别获取文生图和图生图的结果
        EnhancedImageApi.OpenAiImageResponse aiImageResponse = imageApi.createImage(imageOptions).getBody();
        EnhancedImageApi.OpenAiImageResponse aiImageResponseEdit = imageApi.createImage(imageOptionsEdit).getBody();

        // 验证API响应不为空
        Assert.notNull(aiImageResponse, "没有获取到图片");
        Assert.notNull(aiImageResponseEdit, "没有获取到图片");

        // 获取响应数据列表
        List<EnhancedImageApi.Data> dataList = aiImageResponse.data();
        List<EnhancedImageApi.Data> dataListEdit = aiImageResponseEdit.data();

        // 验证数据列表不为空
        Assert.notEmpty(dataList, "没有获取到图片");
        Assert.notEmpty(dataListEdit, "没有获取到图片");

        // 获取第一张生成的图片数据
        EnhancedImageApi.Data data = dataList.get(0);
        EnhancedImageApi.Data dataEdit = dataListEdit.get(0);

        // 提取图片URL
        String url = data.url();
        String urlEdit = dataEdit.url();
        Assert.hasText(url, "没有获取到图片地址");
        Assert.hasText(urlEdit, "没有获取到图片地址");

        // 输出生成的图片URL
        System.out.println("文生图地址: " + url);
        System.out.println("图生图地址: " + urlEdit);
    }
}
