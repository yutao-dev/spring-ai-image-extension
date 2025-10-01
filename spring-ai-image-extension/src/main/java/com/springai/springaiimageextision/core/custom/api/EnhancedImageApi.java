package com.springai.springaiimageextision.core.custom.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.springai.springaiimageextision.core.custom.option.EnhancedImageOptions;
import org.springframework.ai.model.ApiKey;
import org.springframework.ai.model.NoopApiKey;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.openai.api.common.OpenAiApiConstants;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;


/**
 * OpenAI 图像 API。
 *
 * @author 王玉涛
 * @author Filip Hrisafov
 * @see <a href= "https://platform.openai.com/docs/api-reference/images">图像</a>
 */
public class EnhancedImageApi {

	public static final String DEFAULT_IMAGE_MODEL = ImageModel.DALL_E_3.getValue();

	private final RestClient restClient;

	private final String imagesPath;

	/**
	 * 使用提供的基础 URL 创建一个新的 OpenAI 图像 API。
	 * @param baseUrl OpenAI API 的基础 URL。
	 * @param apiKey OpenAI apiKey。
	 * @param headers 要使用的 HTTP 头部。
	 * @param imagesPath 要使用的图像路径。
	 * @param restClientBuilder 要使用的 REST 客户端构建器。
	 * @param responseErrorHandler 要使用的响应错误处理器。
	 */
	public EnhancedImageApi(String baseUrl, ApiKey apiKey, MultiValueMap<String, String> headers, String imagesPath,
							RestClient.Builder restClientBuilder, ResponseErrorHandler responseErrorHandler) {

		// @formatter:off
		this.restClient = restClientBuilder.clone()
			.baseUrl(baseUrl)
			.defaultHeaders(h -> {
				h.setContentType(MediaType.APPLICATION_JSON);
				h.addAll(headers);
			})
			.defaultStatusHandler(responseErrorHandler)
			.defaultRequest(requestHeadersSpec -> {
				if (!(apiKey instanceof NoopApiKey)) {
					requestHeadersSpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey.getValue());
				}
			})
			.build();
		// @formatter:on

		this.imagesPath = imagesPath;
	}

	public ResponseEntity<OpenAiImageResponse> createImage(EnhancedImageOptions imageOptions) {
		Assert.notNull(imageOptions, "图像请求不能为空。");
		Assert.hasLength(imageOptions.getPrompt(), "提示词不能为空。");

		return this.restClient.post()
			.uri(this.imagesPath)
			.body(imageOptions)
			.retrieve()
			.toEntity(OpenAiImageResponse.class);
	}

	public static Builder builder() {
		return new Builder();
	}

	/**
	 * OpenAI 图像 API 模型。
	 * <a href="https://platform.openai.com/docs/models/dall-e">DALL·E</a>
	 */
	public enum ImageModel {

		/**
		 * 2023年11月发布的最新 DALL·E 模型。
		 */
		DALL_E_3("dall-e-3"),

		/**
		 * 2022年11月发布的上一代 DALL·E 模型。这是 DALL·E 的第二次迭代，
		 * 相比原模型具有更真实、准确且分辨率高4倍的图像。
		 */
		DALL_E_2("dall-e-2");

		private final String value;

		ImageModel(String model) {
			this.value = model;
		}

		public String getValue() {
			return this.value;
		}

	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record OpenAiImageResponse(
		@JsonProperty("created") Long created,
		@JsonProperty("data") List<Data> data) {
	}
	// @formatter:on

	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Data(@JsonProperty("url") String url, @JsonProperty("b64_json") String b64Json,
			@JsonProperty("revised_prompt") String revisedPrompt) {

	}

	/**
	 * 构建器用于构造 {@link EnhancedImageApi} 实例。
	 */
	public static class Builder {

		private String baseUrl = OpenAiApiConstants.DEFAULT_BASE_URL;

		private ApiKey apiKey;

		private MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();

		private RestClient.Builder restClientBuilder = RestClient.builder();

		private ResponseErrorHandler responseErrorHandler = RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER;

		private String imagesPath = "v1/images/generations";

		public Builder baseUrl(String baseUrl) {
			Assert.hasText(baseUrl, "baseUrl 不能为空");
			this.baseUrl = baseUrl;
			return this;
		}

		public Builder imagesPath(String imagesPath) {
			Assert.hasText(imagesPath, "imagesPath 不能为空");
			this.imagesPath = imagesPath;
			return this;
		}

		public Builder apiKey(ApiKey apiKey) {
			Assert.notNull(apiKey, "apiKey 不能为空");
			this.apiKey = apiKey;
			return this;
		}

		public Builder apiKey(String simpleApiKey) {
			Assert.notNull(simpleApiKey, "simpleApiKey 不能为空");
			this.apiKey = new SimpleApiKey(simpleApiKey);
			return this;
		}

		public Builder headers(MultiValueMap<String, String> headers) {
			Assert.notNull(headers, "headers 不能为空");
			this.headers = headers;
			return this;
		}

		public Builder restClientBuilder(RestClient.Builder restClientBuilder) {
			Assert.notNull(restClientBuilder, "restClientBuilder 不能为空");
			this.restClientBuilder = restClientBuilder;
			return this;
		}

		public Builder responseErrorHandler(ResponseErrorHandler responseErrorHandler) {
			Assert.notNull(responseErrorHandler, "responseErrorHandler 不能为空");
			this.responseErrorHandler = responseErrorHandler;
			return this;
		}

		public EnhancedImageApi build() {
			Assert.notNull(this.apiKey, "必须设置 apiKey");
			return new EnhancedImageApi(this.baseUrl, this.apiKey, this.headers, this.imagesPath, this.restClientBuilder,
					this.responseErrorHandler);
		}

	}

}