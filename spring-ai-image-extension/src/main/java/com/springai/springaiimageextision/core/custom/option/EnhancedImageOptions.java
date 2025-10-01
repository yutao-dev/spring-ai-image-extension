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
	 * 要生成的图像数量。必须介于1到10之间。对于dall-e-3，仅支持n=1。
	 */
	@JsonProperty("n")
	private Integer n;

	/**
	 * 用于图像生成的模型。
	 */
	@JsonProperty("model")
	private String model;

	/**
	 * 生成图像的宽度。对于dall-e-2，必须是256、512或1024之一。
	 * 此属性与'size'属性相互关联 - 设置宽度和高度将自动计算并设置"widthxheight"格式的大小。
	 * 相反，设置有效的尺寸字符串将解析并设置单独的宽度和高度值。
	 */
	@JsonProperty("size_width")
	private Integer width;

	/**
	 * 生成图像的高度。对于dall-e-2，必须是256、512或1024之一。
	 * 此属性与'size'属性相互关联 - 设置宽度和高度将自动计算并设置"widthxheight"格式的大小。
	 * 相反，设置有效的尺寸字符串将解析并设置单独的宽度和高度值。
	 */
	@JsonProperty("size_height")
	private Integer height;

	/**
	 * 将生成的图像质量。hd 创建具有更精细细节和跨图像更大一致性的图像。
	 * 此参数仅支持 dall-e-3。
	 */
	@JsonProperty("quality")
	private String quality;

	/**
	 * 生成图像的返回格式。必须是 url 或 b64_json 之一。
	 */
	@JsonProperty("response_format")
	private String responseFormat;

	/**
	 * 生成图像的尺寸。对于 dall-e-2，必须是 256x256、512x512 或 1024x1024 之一。
	 * 对于 dall-e-3 模型，必须是 1024x1024、1792x1024 或 1024x1792 之一。
	 * 当同时设置宽度和高度时，此属性会自动计算，格式为"widthxheight"。
	 * 当直接设置此属性时，它必须遵循"WxH"格式，其中W和H是有效整数。
	 * 无效格式将导致宽度和高度值为空。
	 */
	@JsonProperty("size")
	private String size;

	/**
	 * 生成图像的样式。必须是 vivid 或 natural 之一。
	 * Vivid 使模型倾向于生成超现实和戏剧性图像。
	 * Natural 使模型产生更自然、不那么超现实的图像。
	 * 此参数仅支持 dall-e-3。
	 */
	@JsonProperty("style")
	private String style;

	/**
	 * 代表最终用户的唯一标识符，可以帮助 OpenAI 监控和检测滥用行为。
	 */
	@JsonProperty("user")
	private String user;

	/**
	 * 描述要生成的图像的提示。
	 */
	@JsonProperty("prompt")
	private String prompt;

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