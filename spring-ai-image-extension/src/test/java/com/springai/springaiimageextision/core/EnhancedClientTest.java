package com.springai.springaiimageextision.core;

import com.springai.springaiimageextision.core.client.EnhancedImageClient;
import com.springai.springaiimageextision.core.util.ImageUtils;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;

/**
 * @author 王玉涛
 * @version 1.0
 * @since 2025/10/1
 */
@SpringBootTest
class EnhancedClientTest {

    @Resource
    private EnhancedImageClient enhancedImageClient;

    /**
     * 测试图像生成功能和图像编辑功能
     * 
     * 该测试方法主要验证两个功能：
     * 1. 使用 Qwen/Qwen-Image 模型生成星空图片
     * 2. 使用 Qwen/Qwen-Image-Edit 模型对现有图片进行美化处理
     * 
     * 测试流程：
     * 1. 调用图像生成接口，生成一张星空图片，排除星球元素
     * 2. 读取本地图片文件并转换为Base64编码
     * 3. 调用图像编辑接口，对读取的图片进行美化处理
     * 4. 验证两个接口的输出均不为空
     * 
     * @throws IOException 当读取图片文件失败时抛出此异常
     */
    @Test
    void testClient() throws IOException {
        // 生成星空图片，使用负面提示词排除星球元素
        String output = enhancedImageClient.param()
                .model("Qwen/Qwen-Image")
                .prompt("请生成一张优美的星空图片")
                .negativePrompt("星球")
                .cfg(7.5)  // 设置提示词相关性因子
                .output();

        // 读取本地图片文件并转换为Base64编码，用于后续编辑
        File imageFile = ImageUtils.findImageFile("static/风景图片01.png");
        String convert = ImageUtils.convert(imageFile);
        
        // 对读取的图片进行美化处理
        String outputEdit = enhancedImageClient.param()
                .model("Qwen/Qwen-Image-Edit")
                .prompt("请你美化当前的图片")
                .cfg(8.0)  // 设置提示词相关性因子
                .image(convert)  // 提供待编辑的图片数据
                .output();
        
        // 验证生成和编辑结果均不为空
        Assertions.assertNotNull(output);
        Assertions.assertNotNull(outputEdit);
        
        // 输出结果到控制台以便查看
        System.out.println(output);
        System.out.println(outputEdit);
    }
}
