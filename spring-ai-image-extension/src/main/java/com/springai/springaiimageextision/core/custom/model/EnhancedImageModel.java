package com.springai.springaiimageextision.core.custom.model;

import java.util.List;

import com.springai.springaiimageextision.core.custom.api.EnhancedImageApi;
import com.springai.springaiimageextision.core.custom.option.EnhancedImageOptions;
import com.springai.springaiimageextision.core.util.BeanUtils;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImageOptions;
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
 * OpenAiImageModel 是一个实现 ImageModel 接口的类。它提供了一个客户端用于调用 OpenAI 图像生成 API。
 *
 * @author 王玉涛
 * @since 0.8.0
 */
public class EnhancedImageModel implements ImageModel {

	private static final Logger logger = LoggerFactory.getLogger(EnhancedImageModel.class);

	private static final ImageModelObservationConvention DEFAULT_OBSERVATION_CONVENTION = new DefaultImageModelObservationConvention();

	/**
	 * 用于图像完成请求的默认选项。
	 */
	private final EnhancedImageOptions defaultOptions;

	/**
	 * 用于重试 OpenAI 图像 API 调用的重试模板。
	 */
	private final RetryTemplate retryTemplate;

	/**
	 * 对 OpenAI 图像 API 的低级访问。
	 */
	private final EnhancedImageOptions openAiImageApi;

	/**
	 * 用于仪器仪表的观察注册表。
	 */
	private final ObservationRegistry observationRegistry;

	/**
	 * 用于生成观察数据的约定。
	 */
	private ImageModelObservationConvention observationConvention = DEFAULT_OBSERVATION_CONVENTION;

	/**
	 * 创建 OpenAiImageModel 的实例。
	 * @param openAiImageApi 用于与 OpenAI 图像 API 交互的 OpenAiImageApi 实例。
	 * @throws IllegalArgumentException 如果 openAiImageApi 为 null
	 */
	public EnhancedImageModel(OpenAiImageApi openAiImageApi) {
		this(openAiImageApi, OpenAiImageOptions.builder().build(), RetryUtils.DEFAULT_RETRY_TEMPLATE);
	}

	/**
	 * 初始化 OpenAiImageModel 的新实例。
	 * @param openAiImageApi 用于与 OpenAI 图像 API 交互的 OpenAiImageApi 实例。
	 * @param options 用于配置图像模型的 OpenAiImageOptions。
	 * @param retryTemplate 重试模板。
	 */
	public EnhancedImageModel(OpenAiImageApi openAiImageApi, OpenAiImageOptions options, RetryTemplate retryTemplate) {
		this(openAiImageApi, options, retryTemplate, ObservationRegistry.NOOP);
	}

	/**
	 * 初始化 OpenAiImageModel 的新实例。
	 * @param imageApi 用于与 OpenAI 图像 API 交互的 OpenAiImageApi 实例。
	 * @param options 用于配置图像模型的 OpenAiImageOptions。
	 * @param retryTemplate 重试模板。
	 * @param observationRegistry 用于仪器仪表的 ObservationRegistry。
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

	@Override
	public ImageResponse call(ImagePrompt imagePrompt) {
		// 在继续之前，构建最终的请求 ImagePrompt，
		// 合并运行时和默认选项。
		EnhancedImageOptions imageOptions = mergeOptions(imagePrompt);

		var observationContext = ImageModelObservationContext.builder()
			.imagePrompt(imagePrompt)
			.provider(OpenAiApiConstants.PROVIDER_NAME)
			.build();

		return ImageModelObservationDocumentation.IMAGE_MODEL_OPERATION
			.observation(this.observationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext,
					this.observationRegistry)
			.observe(() -> {
				ResponseEntity<OpenAiImageApi.OpenAiImageResponse> imageResponseEntity = this.retryTemplate
					.execute(ctx -> this.openAiImageApi.createImage(imageRequest));

				ImageResponse imageResponse = convertResponse(imageResponseEntity, imageRequest);

				observationContext.setResponse(imageResponse);

				return imageResponse;
			});
	}

	/**
	 * 合并运行时和默认选项。
	 * @param imagePrompt 提示
	 * @return 合并后的选项
	 */
	private EnhancedImageOptions mergeOptions(ImagePrompt imagePrompt) {
		String prompt = imagePrompt.getInstructions().get(0).getText();
		EnhancedImageOptions enhancedImageOptions = (EnhancedImageOptions) imagePrompt.getOptions();

		String defaultPrompt = BeanUtils.nullThenChooseOther(prompt, options.getPrompt(), String.class);

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

	private OpenAiImageApi.OpenAiImageRequest createRequest(ImagePrompt imagePrompt) {
		String instructions = imagePrompt.getInstructions().get(0).getText();
		OpenAiImageOptions imageOptions = (OpenAiImageOptions) imagePrompt.getOptions();

		OpenAiImageApi.OpenAiImageRequest imageRequest = new OpenAiImageApi.OpenAiImageRequest(instructions,
				OpenAiImageApi.DEFAULT_IMAGE_MODEL);

		return ModelOptionsUtils.merge(imageOptions, imageRequest, OpenAiImageApi.OpenAiImageRequest.class);
	}

	private ImageResponse convertResponse(ResponseEntity<OpenAiImageApi.OpenAiImageResponse> imageResponseEntity,
			OpenAiImageApi.OpenAiImageRequest openAiImageRequest) {
		OpenAiImageApi.OpenAiImageResponse imageApiResponse = imageResponseEntity.getBody();
		if (imageApiResponse == null) {
			logger.warn("请求没有返回图像响应: {}", openAiImageRequest);
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

	private ImagePrompt buildRequestImagePrompt(ImagePrompt imagePrompt) {
		// 处理运行时选项
		OpenAiImageOptions runtimeOptions = null;
		if (imagePrompt.getOptions() != null) {
			runtimeOptions = ModelOptionsUtils.copyToTarget(imagePrompt.getOptions(), ImageOptions.class,
					OpenAiImageOptions.class);
		}

		OpenAiImageOptions requestOptions = runtimeOptions == null ? this.defaultOptions : OpenAiImageOptions.builder()
			// 处理可移植的图像选项
			.model(ModelOptionsUtils.mergeOption(runtimeOptions.getModel(), this.defaultOptions.getModel()))
			.N(ModelOptionsUtils.mergeOption(runtimeOptions.getN(), this.defaultOptions.getN()))
			.responseFormat(ModelOptionsUtils.mergeOption(runtimeOptions.getResponseFormat(),
					this.defaultOptions.getResponseFormat()))
			.width(ModelOptionsUtils.mergeOption(runtimeOptions.getWidth(), this.defaultOptions.getWidth()))
			.height(ModelOptionsUtils.mergeOption(runtimeOptions.getHeight(), this.defaultOptions.getHeight()))
			.style(ModelOptionsUtils.mergeOption(runtimeOptions.getStyle(), this.defaultOptions.getStyle()))
			// 处理 OpenAI 特定的图像选项
			.quality(ModelOptionsUtils.mergeOption(runtimeOptions.getQuality(), this.defaultOptions.getQuality()))
			.user(ModelOptionsUtils.mergeOption(runtimeOptions.getUser(), this.defaultOptions.getUser()))
			.build();

		return new ImagePrompt(imagePrompt.getInstructions(), requestOptions);
	}

	/**
	 * 使用提供的约定来报告观察数据
	 * @param observationConvention 提供的约定
	 */
	public void setObservationConvention(ImageModelObservationConvention observationConvention) {
		Assert.notNull(observationConvention, "observationConvention 不能为空");
		this.observationConvention = observationConvention;
	}

}