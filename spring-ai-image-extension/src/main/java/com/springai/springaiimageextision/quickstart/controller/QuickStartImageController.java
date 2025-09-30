package com.springai.springaiimageextision.quickstart.controller;

import com.springai.springaiimageextision.quickstart.service.QuickStartImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 快速开始图片控制器
 * 
 * @author 王玉涛
 * @version 1.0
 * @since 2025/10/1
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quick-start/images")
public class QuickStartImageController {
    
    private final QuickStartImageService quickStartImageService;
    
    /**
     * 根据提示词生成图片
     * 
     * @param prompt 图片生成的提示词
     * @return 生成的图片URL或Base64编码
     */
    @GetMapping
    public String getImage(@RequestParam String prompt) {
        log.info("getImage: {}", prompt);
        
        return quickStartImageService.getImage(prompt);
    }
}
