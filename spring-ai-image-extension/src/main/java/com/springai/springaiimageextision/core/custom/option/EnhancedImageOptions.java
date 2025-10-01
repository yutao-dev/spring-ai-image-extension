package com.springai.springaiimageextision.core.custom.option;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.image.ImageOptions;

/**
 * OpenAI 图像API选项。OpenAiImageOptions.java
 *
 * @author 王玉涛
 * @author Mark Pollack
 * @author Christian Tzolov
 * @since 0.8.0
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EnhancedImageOptions implements ImageOptions {

	/**
	 * 生成图像的数量
	 * 对应 OpenAI API 的 'n' 参数
	 * 因为这里对接了硅基流动的厂商，因此这里使用 'batch_size' 代替 'n'
	 */
	@JsonProperty("batch_size")
	private Integer n;

	/**
	 * 使用的模型名称
	 * 对应 OpenAI API 的 'model' 参数
	 */
	@JsonProperty("model")
	private String model;

	/**
	 * 图像宽度（像素）
	 * 对应 OpenAI API 的 'size_width' 参数
	 */
	@JsonProperty("size_width")
	private Integer width;

	/**
	 * 图像高度（像素）
	 * 对应 OpenAI API 的 'size_height' 参数
	 */
	@JsonProperty("size_height")
	private Integer height;

	/**
	 * 图像质量设置
	 * 可选值：standard、hd
	 * 对应 OpenAI API 的 'quality' 参数
	 */
	@JsonProperty("quality")
	private String quality;

	/**
	 * 响应格式
	 * 可选值：url、b64_json
	 * 对应 OpenAI API 的 'response_format' 参数
	 */
	@JsonProperty("response_format")
	private String responseFormat;

	/**
	 * 图像尺寸规格
	 * 格式："{width}x{height}"，例如 "1024x1024"
	 * 对应 OpenAI API 的 'size' 参数
	 */
	@JsonProperty("size")
	private String size;

	/**
	 * 图像风格
	 * 可选值：vivid、natural
	 * 对应 OpenAI API 的 'style' 参数
	 */
	@JsonProperty("style")
	private String style;

	/**
	 * 用户标识符
	 * 用于违规监控和滥用检测
	 * 对应 OpenAI API 的 'user' 参数
	 */
	@JsonProperty("user")
	private String user;

	/**
	 * 自定义字段，注意：该字段需要与厂商的需求对齐
	 * 通常用于指定参考图像或蒙版图像
	 */
	@JsonProperty("image")
	private String image;

	/**
	 * 自定义字段，用于文本提示词发送
	 * 图像生成的主要描述文本
	 */
	@JsonProperty("prompt")
	private String prompt;


	/**
	 * 反向提示词，表示不希望出现的元素
	 */
	@JsonProperty("negative_prompt")
	private String negativePrompt;

	/**
	 * 自定义字段，用于指定种子值，用于结果复现，相同的seed会有相似的输出
	 */
	@JsonProperty("seed")
	private Long seed;

	/**
	 * 自定义字段，用于指定 guidance scale，用于控制生成图像的随机性，值越高则生成图像越严格
	 */
	@JsonProperty("guidance_scale")
	private Integer guidanceScale;

	/**
	 * 自定义字段，用于指定 cfg，影响图文一致性，值越高则生成图像越有个性化，建议≥4.0
	 */
	@JsonProperty("cfg")
	private String cfg;

	/**
	 * 自定义字段，用于指定推理步骤数，用于控制生成图像的随机性，值越高则生成图像越随机
	 */
	@JsonProperty("num_inference_steps")
	private Integer inferenceSteps;

	/**
	 * 根据现有的选项创建一个新的 OpenAiImageOptions 实例。
	 * @param fromOptions 要复制的选项
	 * @return 新的 OpenAiImageOptions 实例
	 */
	public static EnhancedImageOptions fromOptions(EnhancedImageOptions fromOptions) {
		EnhancedImageOptions options = new EnhancedImageOptions();
		options.n = fromOptions.n;
		options.model = fromOptions.model;
		options.width = fromOptions.width;
		options.height = fromOptions.height;
		options.quality = fromOptions.quality;
		options.responseFormat = fromOptions.responseFormat;
		options.size = fromOptions.size;
		options.style = fromOptions.style;
		options.user = fromOptions.user;
		return options;
	}


	@Override
	public Integer getWidth() {
		if (this.width != null) {
			return this.width;
		}
		else if (this.size != null) {
			try {
				String[] dimensions = this.size.split("x");
				if (dimensions.length != 2) {
					return null;
				}
				return Integer.parseInt(dimensions[0]);
			}
			catch (Exception ex) {
				return null;
			}
		}
		return null;
	}

	public void setWidth(Integer width) {
		this.width = width;
		if (this.width != null && this.height != null) {
			this.size = this.width + "x" + this.height;
		}
	}

	@Override
	public Integer getHeight() {
		if (this.height != null) {
			return this.height;
		}
		else if (this.size != null) {
			try {
				String[] dimensions = this.size.split("x");
				if (dimensions.length != 2) {
					return null;
				}
				return Integer.parseInt(dimensions[1]);
			}
			catch (Exception ex) {
				return null;
			}
		}
		return null;
	}

	public void setHeight(Integer height) {
		this.height = height;
		if (this.width != null && this.height != null) {
			this.size = this.width + "x" + this.height;
		}
	}

	public String getSize() {
		if (this.size != null) {
			return this.size;
		}
		return (this.width != null && this.height != null) ? this.width + "x" + this.height : null;
	}

	public void setSize(String size) {
		this.size = size;

		// 解析尺寸字符串以更新宽度和高度
		if (size != null) {
			try {
				String[] dimensions = size.split("x");
				if (dimensions.length == 2) {
					this.width = Integer.parseInt(dimensions[0]);
					this.height = Integer.parseInt(dimensions[1]);
				}
			}
			catch (Exception ex) {
				// 如果解析失败，则保持宽度和高度不变
			}
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof EnhancedImageOptions that)) {
			return false;
		}
		return Objects.equals(this.n, that.n) && Objects.equals(this.model, that.model)
				&& Objects.equals(this.width, that.width) && Objects.equals(this.height, that.height)
				&& Objects.equals(this.quality, that.quality)
				&& Objects.equals(this.responseFormat, that.responseFormat) && Objects.equals(this.size, that.size)
				&& Objects.equals(this.style, that.style) && Objects.equals(this.user, that.user);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.n, this.model, this.width, this.height, this.quality, this.responseFormat, this.size,
				this.style, this.user);
	}

	@Override
	public String toString() {
		return "OpenAiImageOptions{" + "n=" + this.n + ", model='" + this.model + '\'' + ", width=" + this.width
				+ ", height=" + this.height + ", quality='" + this.quality + '\'' + ", responseFormat='"
				+ this.responseFormat + '\'' + ", size='" + this.size + '\'' + ", style='" + this.style + '\''
				+ ", user='" + this.user + '\'' + '}';
	}

	/**
	 * 创建此选项实例的副本。
	 * @return 具有相同选项的新实例
	 */
	public EnhancedImageOptions copy() {
		return fromOptions(this);
	}

}