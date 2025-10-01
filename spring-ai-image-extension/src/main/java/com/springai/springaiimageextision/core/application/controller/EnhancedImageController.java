package com.springai.springaiimageextision.core.application.controller;

import com.springai.springaiimageextision.core.application.service.EnhancedImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

/**
 * @author 王玉涛
 * @version 1.0
 * @since 2025/10/1
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/core/images")
public class EnhancedImageController {
    
    private final EnhancedImageService enhancedImageService;
    
    /**
     * 根据文本提示生成图像，或基于上传的图像和文本提示生成新图像
     *
     * @param file   可选的上传图像文件，用于图像到图像的生成
     * @param prompt 文本提示，用于指导图像生成过程
     * @return 生成图像的URL或标识符
     */
    @PostMapping
    public String generateImage(@RequestParam(name = "file", required = false) MultipartFile file,
                                @RequestParam("prompt") String prompt) {
        return Objects.isNull(file) ? enhancedImageService.textToImage(prompt) : enhancedImageService.imageToImage(file, prompt);
    }
}
