package com.springai.springaiimageextision.core;

import com.springai.springaiimageextision.core.client.EnhancedImageClient;
import com.springai.springaiimageextision.core.util.ImageUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author 王玉涛
 * @version 1.0
 * @since 2025/10/1
 */
@Slf4j
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




    /**
     * 测试图像接龙生成功能
     * 
     * 该测试方法验证基于现有图片的连续图像生成功能（图像接龙）：
     * 1. 读取指定的二次元风格图片作为基础图像
     * 2. 使用 Qwen/Qwen-Image-Edit 模型基于该图像生成配色更加唯美的新图像序列
     * 3. 应用负面提示词"星球"来排除特定元素
     * 4. 设置提示词相关性因子为7.5以平衡创造性和遵循提示的程度
     * 5. 生成包含3个图像的接龙序列
     * 
     * 此外，还测试了文生图+图生图的组合接龙方式：
     * 1. 先使用 Qwen/Qwen-Image 模型根据提示生成初始二次元图片
     * 2. 再使用 Qwen/Qwen-Image-Edit 模型对该图片进行配色优化
     * 3. 同样生成包含3个图像的接龙序列
     *
     * @throws IOException 当读取图片文件失败时抛出此异常
     */
    @Test
    void testSolitaire() throws IOException {
        log.info("接龙测试: 纯图生图");
        File imageFile = ImageUtils.findImageFile("static/二次元图片.png");
        String convert = ImageUtils.convert(imageFile);

        List<String> solitaire = enhancedImageClient.param()
                .model("Qwen/Qwen-Image-Edit")
                .prompt("请让图片的配色更加唯美")
                .image(convert)
                .negativePrompt("星球")
                .cfg(7.5)  // 提示词相关性因子
                .solitaire(3);

        log.info("solitaire: {}", solitaire);
        
        log.info("接龙测试: 文生图+图生图 图像接龙");
        String output = enhancedImageClient.param()
                .model("Qwen/Qwen-Image")
                .prompt("请随机生成原神胡桃的图片")
                .negativePrompt("星球")
                .inferenceSteps(20)
                .output();
        System.out.println(output);
        File imageAsUrl = ImageUtils.createImageAsUrl(output);
        String convertAsUrl = ImageUtils.convert(imageAsUrl);
        List<String> solitaireAsUrl = enhancedImageClient.param()
                .model("Qwen/Qwen-Image-Edit")
                .prompt("请让图片的配色更加唯美")
                .image(convertAsUrl)
                .negativePrompt("星球")
                .cfg(7.5)  // 提示词相关性因子
                .solitaire(3);
        
        log.info("solitaireAsUrl: {}", solitaireAsUrl);
    }


    /**
     * 测试图像接龙生成功能（自定义提示词序列）
     * 
     * 该测试方法验证基于自定义提示词序列的图像接龙生成功能：
     * 1. 读取指定的二次元风格图片作为基础图像
     * 2. 使用 Qwen/Qwen-Image-Edit 模型基于该图像和自定义提示词序列生成新的图像序列
     * 3. 应用负面提示词"星球"来排除特定元素
     * 4. 设置提示词相关性因子为7.5以平衡创造性和遵循提示的程度
     * 5. 生成包含3个图像的接龙序列，每个步骤使用不同的提示词
     * 
     * 注意：solitaire方法的提示词参数会覆盖初始设置的prompt参数
     *
     * @throws IOException 当读取图片文件失败时抛出此异常
     */
    @Test
    void testSolitaire2() throws IOException {
        // 读取二次元风格的基础图片
        File imageFile = ImageUtils.findImageFile("static/二次元图片.png");
        String convert = ImageUtils.convert(imageFile);
        
        // 执行图像接龙生成，使用自定义提示词序列
        // 注意：solitaire的参数会覆盖prompt参数
        List<String> solitaire = enhancedImageClient.param()
                .model("Qwen/Qwen-Image-Edit")
                .prompt("请让图片的配色更加唯美")
                .image(convert)
                .negativePrompt("星球")
                .cfg(7.5)  // 提示词相关性因子
                .solitaire(3, List.of("请让图片的配色更加唯美", "请更换图片中人物的衣着"));
        
        // 输出生成的图像序列结果
        log.info("solitaire: {}", solitaire);
    }
}
