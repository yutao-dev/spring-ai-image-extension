package com.springai.springaiimageextision.core.custom.bean;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author 王玉涛
 * @version 1.0
 * @since 2025/10/1
 */
@Data
@Configuration
@ConfigurationProperties("ai.enhanced.image.options")
public class ImageOptionsProperties {
    /**
     * 生成图像的数量
     * 对应 OpenAI API 的 'n' 参数
     */
    private Integer n;

    /**
     * 使用的模型名称
     */
    private String model;

    /**
     * 图像宽度（像素）
     * 对应 OpenAI API 的 'size_width' 参数
     */
    private Integer width;

    /**
     * 图像高度（像素）
     * 对应 OpenAI API 的 'size_height' 参数
     */
    private Integer height;

    /**
     * 图像质量设置
     * 可选值：standard、hd
     */
    private String quality;

    /**
     * 响应格式
     * 可选值：url、b64_json
     * 对应 OpenAI API 的 'response_format' 参数
     */
    private String responseFormat;

    /**
     * 图像尺寸规格
     * 格式："{width}x{height}"，例如 "1024x1024"
     * 对应 OpenAI API 的 'size' 参数
     */
    private String size;

    /**
     * 图像风格
     * 可选值：vivid、natural
     * 对应 OpenAI API 的 'style' 参数
     */
    private String style;

    /**
     * 用户标识符
     * 用于违规监控和滥用检测
     * 对应 OpenAI API 的 'user' 参数
     */
    private String user;

    /**
     * 自定义字段，注意：该字段需要与厂商的需求对齐
     * 通常用于指定参考图像或蒙版图像
     */
    private String image;

    /**
     * 自定义字段，用于文本提示词发送
     * 图像生成的主要描述文本
     */
    private String prompt;


    /**
     * 反向提示词，表示不希望出现的元素
     */
    private String negativePrompt;

    /**
     * 自定义字段，用于指定种子值，用于结果复现，相同的seed会有相似的输出
     */
    private Long seed;

    /**
     * 自定义字段，用于指定 guidance scale，用于控制生成图像的随机性，值越高则生成图像越严格
     */
    private Integer guidanceScale;

    /**
     * 自定义字段，用于指定 cfg，影响图文一致性，值越高则生成图像越有个性化，建议≥4.0
     */
    private Double cfg;

    /**
     * 自定义字段，用于指定推理步骤数，用于控制生成图像的随机性，值越高则生成图像越随机
     */
    private Integer inferenceSteps;
}
