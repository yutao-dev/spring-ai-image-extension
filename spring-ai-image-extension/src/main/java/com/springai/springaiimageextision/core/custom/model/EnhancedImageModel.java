package com.springai.springaiimageextision.core.custom.model;

import java.util.List;
import java.util.Objects;

import com.springai.springaiimageextision.core.custom.api.EnhancedImageApi;
import com.springai.springaiimageextision.core.custom.option.EnhancedImageOptions;
import com.springai.springaiimageextision.core.util.BeanUtils;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.image.ImageResponseMetadata;
import org.springframework.ai.image.observation.DefaultImageModelObservationConvention;
import org.springframework.ai.image.observation.ImageModelObservationContext;
import org.springframework.ai.image.observation.ImageModelObservationConvention;
import org.springframework.ai.image.observation.ImageModelObservationDocumentation;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.openai.api.OpenAiImageApi;
import org.springframework.ai.openai.api.common.OpenAiApiConstants;
import org.springframework.ai.openai.metadata.OpenAiImageGenerationMetadata;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;



/**
 * EnhancedImageModel 是一个实现 ImageModel 接口的增强型图像模型类。
 * 它提供了一个客户端用于调用增强的图像生成 API，支持更多自定义选项和功能。
 *
 * @author 王玉涛
 * @since 0.8.0
 */
public class EnhancedImageModel implements ImageModel {

	private static final Logger logger = LoggerFactory.getLogger(EnhancedImageModel.class);

	private static final ImageModelObservationConvention DEFAULT_OBSERVATION_CONVENTION = new DefaultImageModelObservationConvention();

	/**
	 * 图像生成请求的默认配置选项。
	 * 这些选项将在未提供运行时选项时使用。
	 */
	private final EnhancedImageOptions defaultOptions;

	/**
	 * 用于处理 API 调用重试机制的模板。
	 * 当图像 API 调用失败时，将根据配置进行重试。
	 */
	private final RetryTemplate retryTemplate;

	/**
	 * 对底层图像生成 API 的低级访问接口。
	 * 用于实际执行图像生成请求。
	 */
	private final EnhancedImageApi openAiImageApi;

	/**
	 * 用于监控和指标收集的注册表。
	 * 支持对图像生成操作进行观测和性能监控。
	 */
	private final ObservationRegistry observationRegistry;

	/**
	 * 图像模型观测数据的生成约定。
	 * 定义如何收集和报告观测数据。
	 */
	private ImageModelObservationConvention observationConvention = DEFAULT_OBSERVATION_CONVENTION;

	/**
	 * 创建 EnhancedImageModel 的实例，使用默认配置。
	 * @param enhancedImageApi 用于与图像生成 API 交互的 EnhancedImageApi 实例
	 * @throws IllegalArgumentException 如果 enhancedImageApi 为 null
	 */
	public EnhancedImageModel(EnhancedImageApi enhancedImageApi) {
		this(enhancedImageApi, EnhancedImageOptions.builder().build(), RetryUtils.DEFAULT_RETRY_TEMPLATE);
	}

	/**
	 * 创建 EnhancedImageModel 的实例，指定配置选项和重试模板。
	 * @param enhancedImageApi 用于与图像生成 API 交互的 EnhancedImageApi 实例
	 * @param options 图像生成的配置选项
	 * @param retryTemplate 重试机制模板
	 */
	public EnhancedImageModel(EnhancedImageApi enhancedImageApi, EnhancedImageOptions options, RetryTemplate retryTemplate) {
		this(enhancedImageApi, options, retryTemplate, ObservationRegistry.NOOP);
	}

	/**
	 * 创建 EnhancedImageModel 的完整配置实例。
	 * @param imageApi 用于与图像生成 API 交互的 EnhancedImageApi 实例
	 * @param options 图像生成的配置选项
	 * @param retryTemplate 重试机制模板
	 * @param observationRegistry 观测注册表，用于指标收集
	 */
	public EnhancedImageModel(EnhancedImageApi imageApi, EnhancedImageOptions options, RetryTemplate retryTemplate,
							  ObservationRegistry observationRegistry) {
		Assert.notNull(imageApi, "OpenAiImageApi 不能为空");
		Assert.notNull(options, "options 不能为空");
		Assert.notNull(retryTemplate, "retryTemplate 不能为空");
		Assert.notNull(observationRegistry, "observationRegistry 不能为空");
		this.openAiImageApi = imageApi;
		this.defaultOptions = options;
		this.retryTemplate = retryTemplate;
		this.observationRegistry = observationRegistry;
	}
	
	/**
	 * 根据图像提示生成图像。
	 * 此方法会合并默认选项和运行时选项，执行 API 调用，并处理重试和观测。
	 * @param imagePrompt 包含生成图像所需信息的提示
	 * @return 图像生成响应，包含生成的图像信息
	 */
	@Override
	public ImageResponse call(ImagePrompt imagePrompt) {
		// 在继续之前，构建最终的请求 ImagePrompt，
		// 合并运行时和默认选项。
		EnhancedImageOptions imageOptions = mergeOptions(imagePrompt);

		var observationContext = ImageModelObservationContext.builder()
			.imagePrompt(imagePrompt)
			.provider(OpenAiApiConstants.PROVIDER_NAME)
			.build();

		ImageResponse response = ImageModelObservationDocumentation.IMAGE_MODEL_OPERATION
				.observation(this.observationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext,
						this.observationRegistry)
				.observe(() -> {
					ResponseEntity<EnhancedImageApi.OpenAiImageResponse> imageResponseEntity = this.retryTemplate
							.execute(ctx -> this.openAiImageApi.createImage(imageOptions));

					ImageResponse imageResponse = convertResponse(imageResponseEntity, imageOptions);

					observationContext.setResponse(imageResponse);

					return imageResponse;
				});
		
		return Objects.isNull(response) ? new ImageResponse(List.of()) : response;
	}

	/**
	 * 合并运行时选项和默认选项，生成最终的图像生成配置。
	 * 优先级顺序：运行时选项 > 默认选项
	 * @param imagePrompt 包含运行时选项的图像提示
	 * @return 合并后的选项配置
	 */
	private EnhancedImageOptions mergeOptions(ImagePrompt imagePrompt) {
		String prompt = imagePrompt.getInstructions().get(0).getText();
		EnhancedImageOptions enhancedImageOptions = (EnhancedImageOptions) imagePrompt.getOptions();

		String defaultPrompt = BeanUtils.nullThenChooseOther(prompt, enhancedImageOptions.getPrompt(), String.class);

		return EnhancedImageOptions.builder()
				.prompt(BeanUtils.nullThenChooseOther(defaultPrompt, this.defaultOptions.getPrompt(), String.class))
				.image(enhancedImageOptions.getImage())
				.n(BeanUtils.nullThenChooseOther(enhancedImageOptions.getN(), this.defaultOptions.getN(), Integer.class))
				.size(BeanUtils.nullThenChooseOther(enhancedImageOptions.getSize(), this.defaultOptions.getSize(), String.class))
				.model(BeanUtils.nullThenChooseOther(enhancedImageOptions.getModel(), this.defaultOptions.getModel(), String.class))
				.height(BeanUtils.nullThenChooseOther(enhancedImageOptions.getHeight(), this.defaultOptions.getHeight(), Integer.class))
				.style(BeanUtils.nullThenChooseOther(enhancedImageOptions.getStyle(), this.defaultOptions.getStyle(), String.class))
				.width(BeanUtils.nullThenChooseOther(enhancedImageOptions.getWidth(), this.defaultOptions.getWidth(), Integer.class))
				.responseFormat(BeanUtils.nullThenChooseOther(enhancedImageOptions.getResponseFormat(), this.defaultOptions.getResponseFormat(), String.class))
				.quality(BeanUtils.nullThenChooseOther(enhancedImageOptions.getQuality(), this.defaultOptions.getQuality(), String.class))
				.user(BeanUtils.nullThenChooseOther(enhancedImageOptions.getUser(), this.defaultOptions.getUser(), String.class))
				.negativePrompt(BeanUtils.nullThenChooseOther(enhancedImageOptions.getNegativePrompt(), this.defaultOptions.getNegativePrompt(), String.class))
				.seed(BeanUtils.nullThenChooseOther(enhancedImageOptions.getSeed(), this.defaultOptions.getSeed(), Long.class))
				.guidanceScale(BeanUtils.nullThenChooseOther(enhancedImageOptions.getGuidanceScale(), this.defaultOptions.getGuidanceScale(), Integer.class))
				.inferenceSteps(BeanUtils.nullThenChooseOther(enhancedImageOptions.getInferenceSteps(), this.defaultOptions.getInferenceSteps(), Integer.class))
				.cfg(BeanUtils.nullThenChooseOther(enhancedImageOptions.getCfg(), this.defaultOptions.getCfg(), String.class))
				.build();
	}

	/**
	 * 创建 OpenAI 图像请求对象。
	 * 该方法主要用于兼容性目的，将 ImagePrompt 转换为 OpenAI 格式的请求。
	 * @param imagePrompt 图像提示
	 * @return OpenAI 图像请求对象
	 */
	private OpenAiImageApi.OpenAiImageRequest createRequest(ImagePrompt imagePrompt) {
		String instructions = imagePrompt.getInstructions().get(0).getText();
		OpenAiImageOptions imageOptions = (OpenAiImageOptions) imagePrompt.getOptions();

		OpenAiImageApi.OpenAiImageRequest imageRequest = new OpenAiImageApi.OpenAiImageRequest(instructions,
				OpenAiImageApi.DEFAULT_IMAGE_MODEL);

		return ModelOptionsUtils.merge(imageOptions, imageRequest, OpenAiImageApi.OpenAiImageRequest.class);
	}

	/**
	 * 将 API 响应转换为标准的 ImageResponse 格式。
	 * @param imageResponseEntity 来自 API 的响应实体
	 * @param enhancedImageOptions 用于生成图像的选项
	 * @return 标准格式的图像响应
	 */
	private ImageResponse convertResponse(ResponseEntity<EnhancedImageApi.OpenAiImageResponse> imageResponseEntity,
			EnhancedImageOptions enhancedImageOptions) {
		EnhancedImageApi.OpenAiImageResponse imageApiResponse = imageResponseEntity.getBody();
		if (imageApiResponse == null) {
			logger.warn("请求没有返回图像响应: {}", enhancedImageOptions);
			return new ImageResponse(List.of());
		}

		List<ImageGeneration> imageGenerationList = imageApiResponse.data()
			.stream()
			.map(entry -> new ImageGeneration(new Image(entry.url(), entry.b64Json()),
					new OpenAiImageGenerationMetadata(entry.revisedPrompt())))
			.toList();

		ImageResponseMetadata openAiImageResponseMetadata = new ImageResponseMetadata(imageApiResponse.created());
		return new ImageResponse(imageGenerationList, openAiImageResponseMetadata);
	}

	/**
	 * 设置自定义的观测约定。
	 * 允许用户自定义如何收集和报告观测数据。
	 * @param observationConvention 自定义的观测约定
	 */
	public void setObservationConvention(ImageModelObservationConvention observationConvention) {
		Assert.notNull(observationConvention, "observationConvention 不能为空");
		this.observationConvention = observationConvention;
	}

}