package com.springai.springaiimageextision.core.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.springai.springaiimageextision.core.custom.model.EnhancedImageModel;
import com.springai.springaiimageextision.core.custom.option.EnhancedImageOptions;
import com.springai.springaiimageextision.core.util.ImageUtils;
import com.springai.springaiimageextision.core.util.LoggerUtils;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 王玉涛
 * @version 1.0
 * @since 2025/10/1
 */
@Slf4j
@Builder
@RequiredArgsConstructor
public class EnhancedImageClient {

    private final EnhancedImageModel enhancedImageModel;
    
    /**
     * 创建参数构建器实例
     * 
     * @return 新的参数构建器实例
     */
    public ParamBuilder param() {
        return new ParamBuilder();
    }
    
    /**
     * 参数构建器类，用于链式设置图像生成的各种参数
     */
    public class ParamBuilder {
        /** 生成图像的数量 */
        private Integer n;
        /** 使用的模型名称 */
        private String model;
        /** 图像宽度 */
        private Integer width;
        /** 图像高度 */
        private Integer height;
        /** 图像质量 */
        private String quality;
        /** 响应格式 */
        private String responseFormat;
        /** 图像尺寸 */
        private String size;
        /** 图像风格 */
        private String style;
        /** 用户标识 */
        private String user;
        /** 输入图像（用于图像变换等操作） */
        private String image;
        /** 图像生成提示词 */
        private String prompt;
        /** 负面提示词，指定不希望出现在图像中的内容 */
        private String negativePrompt;
        /** 随机种子，用于控制生成的一致性 */
        private Long seed;
        /** 指导强度，控制提示词对生成过程的影响程度 */
        private Integer guidanceScale;
        /** 分类器自由引导比例 */
        private Double cfg;
        /** 推理步数，控制生成过程的迭代次数 */
        private Integer inferenceSteps;
        
        /**
         * 设置生成图像数量
         * 
         * @param n 图像数量
         * @return 参数构建器实例
         */
        public ParamBuilder n(Integer n) {
            this.n = n;
            return this;
        }
        
        /**
         * 设置模型名称
         * 
         * @param model 模型名称
         * @return 参数构建器实例
         */
        public ParamBuilder model(String model) {
            this.model = model;
            return this;
        }
        
        /**
         * 设置图像宽度
         * 
         * @param width 图像宽度（像素）
         * @return 参数构建器实例
         */
        public ParamBuilder width(Integer width) {
            this.width = width;
            return this;
        }
        
        /**
         * 设置图像高度
         * 
         * @param height 图像高度（像素）
         * @return 参数构建器实例
         */
        public ParamBuilder height(Integer height) {
            this.height = height;
            return this;
        }
        
        /**
         * 设置图像质量
         * 
         * @param quality 图像质量（如 standard, hd）
         * @return 参数构建器实例
         */
        public ParamBuilder quality(String quality) {
            this.quality = quality;
            return this;
        }
        
        /**
         * 设置响应格式
         * 
         * @param responseFormat 响应格式（如 url, b64_json）
         * @return 参数构建器实例
         */
        public ParamBuilder responseFormat(String responseFormat) {
            this.responseFormat = responseFormat;
            return this;
        }
        
        /**
         * 设置图像尺寸
         * 
         * @param size 图像尺寸（如 1024x1024）
         * @return 参数构建器实例
         */
        public ParamBuilder size(String size) {
            this.size = size;
            return this;
        }
        
        /**
         * 设置图像风格
         * 
         * @param style 图像风格（如 vivid, natural）
         * @return 参数构建器实例
         */
        public ParamBuilder style(String style) {
            this.style = style;
            return this;
        }
        
        /**
         * 设置用户标识
         * 
         * @param user 用户标识，用于区分不同用户的请求
         * @return 参数构建器实例
         */
        public ParamBuilder user(String user) {
            this.user = user;
            return this;
        }
        
        /**
         * 设置输入图像
         * 
         * @param image 输入图像数据（base64编码或URL）
         * @return 参数构建器实例
         */
        public ParamBuilder image(String image) {
            this.image = image;
            return this;
        }
        
        /**
         * 设置图像生成提示词
         * 
         * @param prompt 描述期望图像内容的文本
         * @return 参数构建器实例
         */
        public ParamBuilder prompt(String prompt) {
            this.prompt = prompt;
            return this;
        }
        
        /**
         * 设置负面提示词
         * 
         * @param negativePrompt 描述不希望出现在图像中的内容
         * @return 参数构建器实例
         */
        public ParamBuilder negativePrompt(String negativePrompt) {
            this.negativePrompt = negativePrompt;
            return this;
        }
        
        /**
         * 设置随机种子
         * 
         * @param seed 随机种子值，相同种子会产生相似结果
         * @return 参数构建器实例
         */
        public ParamBuilder seed(Long seed) {
            this.seed = seed;
            return this;
        }
        
        /**
         * 设置指导强度
         * 
         * @param guidanceScale 指导强度值，控制提示词的影响程度
         * @return 参数构建器实例
         */
        public ParamBuilder guidanceScale(Integer guidanceScale) {
            this.guidanceScale = guidanceScale;
            return this;
        }
        
        /**
         * 设置分类器自由引导比例
         * 
         * @param cfg 分类器自由引导比例
         * @return 参数构建器实例
         */
        public ParamBuilder cfg(Double cfg) {
            this.cfg = cfg;
            return this;
        }
        
        /**
         * 设置推理步数
         * 
         * @param inferenceSteps 推理步数，影响生成质量和时间
         * @return 参数构建器实例
         */
        public ParamBuilder inferenceSteps(Integer inferenceSteps) {
            this.inferenceSteps = inferenceSteps;
            return this;
        }
        
        /**
         * 执行图像生成请求并返回完整响应
         * 
         * @return 包含生成图像信息的完整响应对象
         */
        public ImageResponse call() {
            EnhancedImageOptions imageOptions = buildOptions();
            return this.call(imageOptions);
        }
        
        /**
         * 执行图像生成请求并返回图像URL
         * 
         * @return 生成图像的访问URL
         */
        public String output() {
            EnhancedImageOptions imageOptions = buildOptions();
            return this.call(imageOptions).getResult().getOutput().getUrl();
        }



        /**
         * 执行连续图像生成操作（接龙模式）
         * 
         * 该方法会根据设置的参数连续生成指定步数的图像，每一步都会基于前一步的结果进行生成，
         * 形成图像接龙的效果。每一步生成的图像URL会被记录并返回。
         * 
         * 实现逻辑：
         * 1. 验证输入参数step的有效性（非空且在1-7之间）
         * 2. 当step超过3时给出性能警告
         * 3. 循环执行step次图像生成：
         *    - 第一次直接使用当前设置的提示词生成图像
         *    - 后续每次将上一次生成的图像作为输入图像（image参数）
         * 4. 记录并返回每一步生成的图像URL
         * 
         * @param step 连续生成的步数，必须大于0且小于等于7，建议不超过3步以保证性能
         * @return 包含每步生成图像URL的列表，列表顺序即为生成顺序
         * @throws IllegalArgumentException 当step为null或不在有效范围内时抛出
         * @throws RuntimeException 当图像处理或网络请求出现异常时抛出
         */
        public List<String> solitaire(Integer step) throws IOException {
            Assert.notNull(this.model, "model 不得为 null");
            Assert.notNull(this.image, "image 不得为 null");
            Assert.notNull(step, "step 不得为 null");
            Assert.isTrue(step > 0 && step <= 7, "step 必须大于 0 且小于等于 7");
            LoggerUtils.logWarnIfTrue(step > 3, "step 大于 3 时可能会导致生成图像时间大幅增加");

            List<String> solitaire = new ArrayList<>();
            for (int i = 0; i < step; i++) {
                
                // 从第二步开始，将上一步生成的图像作为输入图像
                if (i != 0) {
                    String url = solitaire.get(i - 1);
                    File file = ImageUtils.createImageAsUrl(url);
                    this.image = ImageUtils.convert(file);
                }
                
                String output = this.output();
                log.info("step: {}, output: {}", i + 1, output);
                solitaire.add(output);
            }
            log.info("solitaire: {}", solitaire);
            return solitaire;
        }

        /**
         * 构建图像选项对象
         * 
         * @return 包含所有设置参数的EnhancedImageOptions对象
         */
        private EnhancedImageOptions buildOptions() {
            return EnhancedImageOptions.builder()
                    .n(this.n)
                    .model(this.model)
                    .width(this.width)
                    .height(this.height)
                    .quality(this.quality)
                    .responseFormat(this.responseFormat)
                    .size(this.size)
                    .style(this.style)
                    .user(this.user)
                    .image(this.image)
                    .prompt(this.prompt)
                    .negativePrompt(this.negativePrompt)
                    .seed(this.seed)
                    .guidanceScale(this.guidanceScale)
                    .cfg(this.cfg)
                    .inferenceSteps(this.inferenceSteps)
                    .build();
        }

        /**
         * 调用底层模型执行图像生成
         * 
         * @param options 图像生成选项
         * @return 图像响应对象
         */
        private ImageResponse call(EnhancedImageOptions options) {
            return EnhancedImageClient.this.enhancedImageModel.call(new ImagePrompt(options.getPrompt(), options));
        }
    }
}
