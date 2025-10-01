package com.springai.springaiimageextision.core.application.service;

import com.springai.springaiimageextision.core.client.EnhancedImageClient;
import com.springai.springaiimageextision.core.util.ImageUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

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
}
