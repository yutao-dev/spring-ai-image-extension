package com.springai.springaiimageextision.core.application.service;

import com.springai.springaiimageextision.core.client.EnhancedImageClient;
import com.springai.springaiimageextision.core.util.ImageUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author 王玉涛
 * @version 1.0
 * @since 2025/10/1
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnhancedImageService {

    private final EnhancedImageClient enhancedImageClient;

    /**
     * 根据文本生成图像
     *
     * @param text 描述图像内容的文本提示
     * @return 生成的图像数据或URL
     */
    public String textToImage(String text) {
        log.info("textToImage: {}", text);
        return enhancedImageClient
                .param()
                .model("Qwen/Qwen-Image")
                .prompt(text)
                .output();
    }

    /**
     * 基于上传的图像文件生成新图像
     *
     * @param file   上传的图像文件
     * @param prompt 图像的URL地址（备用参数）
     * @return 处理后的图像数据或URL
     */
    @SneakyThrows
    public String imageToImage(MultipartFile file, String prompt) {
        ImageUtils.isImage(file.getOriginalFilename(), true);

        log.info("imageToImage: {}", file.getOriginalFilename());

        File imageFile = ImageUtils.convertToFile(file);

        String convert = ImageUtils.convert(imageFile);

        return enhancedImageClient
                .param()
                .model("Qwen/Qwen-Image-Edit")
                .image(convert)
                .prompt(prompt)
                .output();
    }

    /**
     * 从文本开始图像接龙生成
     * 首先根据文本生成第一张图像，然后基于上传的图像和提示列表进行连续图像生成
     *
     * @param image   基础图像文件，用于后续的图像编辑
     * @param prompt  初始文本提示，用于生成第一张图像
     * @param prompts 图像编辑提示列表，用于连续生成图像
     * @param step    接龙步数，控制生成图像的数量
     * @return 生成的图像列表，包含初始文生图和后续的图生图结果
     */
    @SneakyThrows
    public List<String> textStartSolitaire(String prompt, List<String> prompts, int step) {
        log.info("文生图开始: {}", prompt);

        // 初始化结果列表，首先添加根据文本生成的第一张图像
        List<String> arrayList = new ArrayList<>();
        arrayList.add(enhancedImageClient.param().model("Qwen/Qwen-Image").prompt(prompt).output());
        log.info("文生图结果: {}", arrayList);

        File imageAsUrl = ImageUtils.createImageAsUrl(arrayList.get(0));

        log.info("图生图接龙开始: {}", prompts);
        // 基于基础图像和提示列表进行连续图像生成，并将结果添加到列表中
        arrayList.addAll(enhancedImageClient.param()
                .model("Qwen/Qwen-Image-Edit")
                .image(ImageUtils.convert(imageAsUrl))
                .solitaire(step, prompts));
        
        return arrayList;
    }
    
    /**
     * 从图像开始图像接龙生成
     * 基于上传的图像和提示列表进行连续图像生成
     *
     * @param image   基础图像文件，用于图像编辑
     * @param prompt  备用文本提示，当提示列表为空时使用
     * @param prompts 图像编辑提示列表，用于连续生成图像
     * @param step    接龙步数，控制生成图像的数量
     * @return 生成的图像列表
     */
    @SneakyThrows
    public List<String> imageStartSolitaire(MultipartFile image, String prompt, List<String> prompts, int step) { 
        log.info("图生图开始: {}", Objects.isNull(prompts) ? prompt : prompts);
        
        // 如果提示列表为空，则使用备用提示
        if (prompts.isEmpty()) {
            prompts.add(prompt);
        }

        // 将上传的图像文件转换为文件对象
        File file = ImageUtils.convertToFile(image);

        // 基于图像和提示列表进行连续图像生成
        List<String> solitaire = enhancedImageClient.param()
                .model("Qwen/Qwen-Image-Edit")
                .image(ImageUtils.convert(file))
                .solitaire(step, prompts);
        log.info("图生图结果: {}", solitaire);

        return solitaire;
    }
}
