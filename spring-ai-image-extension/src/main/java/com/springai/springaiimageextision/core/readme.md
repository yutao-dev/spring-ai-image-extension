# Core 核心包

## 1. 前言

SpringAI 项目原生功能并未包含图像生成功能，如需扩展其他字段则需要手动编码实现。本包旨在提供增强的图像处理能力，避免重复开发，主要包含以下核心组件：

- ✅ **增强图像模型集成** - 对现有图像模型进行功能扩展和优化
- 🔄 **ImageClient 客户端工具** - 基于增强功能构建，提供更丰富的调用方式和更好的用户体验
- 🔌 **标准化接口服务** - 以统一的接口形式对外提供服务，便于开发者快速集成和使用
- 🧪 **完整单元测试** - 所有类均配备完整的单元测试，确保功能稳定性和代码可靠性

## 2. 改造过程

### 2.1 API 改造

#### 2.1.1 获取原始代码

为实现良好的扩展性与向后兼容性，我们执行了以下初始化操作：

1. 复制原始 `OpenAiImageApi.class` 源码作为基础模板
2. 将类名从 `OpenAiImageApi.class` 更改为 `EnhancedImageApi.class`
3. 源码获取路径（GitHub）：[OpenAI Image API](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai/src/main/java/org/springframework/ai/openai/api/OpenAiImageApi.java)
4. 根据需要可能会继续复制其他相关源码，当前阶段优先确保 API 层代码能够正常运行和调试

> ⚠️ **重要提示**：后续将在此基础之上进行进一步的适配与功能增强，以支持更多种类的图像模型和服务提供商。

#### 2.2.2 进行适配化改造

根据来源文档提供的改造思路，我们进行了以下关键步骤的改造（详细内容请参考飞书文档: [SpringAI模型调用、自定义集群深入探索](https://dcn7850oahi9.feishu.cn/docx/DDehdPBMSoGTycxmFTLcER4In0F?from=from_copylink)）：

1. 移除原始类中的内嵌请求参数类 **OpenAiImageRequest**，其他部分暂不改造，留待后续优化
2. 解决 `createImage()` 方法出现的编译错误
3. 复制原始类 [OpenAiImageOptions](https://github.com/spring-projects/spring-ai/blob/1.0.0/models/spring-ai-openai/src/main/java/org/springframework/ai/openai/OpenAiImageOptions.java)
4. 将类名修改为 `EnhancedImageOptions`
5. 添加 `@Data` 注解并移除冗余的原始 setter 与 getter 方法，为后续动态字段扩展提供便利
6. 添加 `@Builder`、`@NoArgsConstructor`、`@AllArgsConstructor` 注解。由于 `@Builder` 会生成全参构造函数并覆盖默认无参构造函数，而显式声明无参构造函数又会覆盖 `@Builder` 生成的构造函数，因此需要同时引入这三个注解
7. 修改 `EnhancedImageApi.createImage()` 方法，将其入参类型更改为 `EnhancedImageOptions`
8. 此时会遇到 `Assert.hasLength(imageOptions.prompt(), "提示词不能为空。")` 编译错误。根据来源文档介绍，我们将 Options 作为请求参数直接传递，因此需要在 `EnhancedImageOptions` 中优先添加 prompt 请求参数以完成适配化改造，随后即可修改相关代码完成适配

**至此，API 层的初始化改造工作已顺利完成**

### 2.2 ImageOptions 层改造

#### 2.2.1 前言

1. 在 2.1 中，我们已经完成了 API 层的初步改造。接下来，我们将对 `EnhancedImageOptions` 进行改造，以支持更多参数。
2. 本节的目标是添加更多的自定义参数，同时避免进行过度设计，防止项目复杂度无谓提升。

#### 2.2.2 改造思路

1. 我们已在来源文档中进行了参数分析，该分析基于硅基流动厂商提供的 API 文档。因此，我们需要将这些参数添加到 `EnhancedImageOptions` 中，以支持更多功能。
2. 下面直接列出需要添加的参数及其详细说明：

##### 参数详细说明

###### 必需参数

| 参数名        | 类型    | 是否必填 | 可选值/范围                             | 说明                 |
|------------|-------|------|------------------------------------|--------------------|
| model      | 枚举字符串 | ✅    | Qwen/Qwen-Image/Kwai-Kolors/Kolors | 模型选择，服务方可能动态调整可用模型 |
| prompt     | 字符串   | ✅    | -                                  | 描述生成图像内容的文本提示      |
| image_size | 字符串   | ✅    | 见下方尺寸表                             | 分辨率格式必须为宽度 x 高度    |

###### 图像尺寸预览

| 模型          | 推荐分辨率（格式：宽 x 高）                                                                                                                   |
|-------------|-----------------------------------------------------------------------------------------------------------------------------------|
| Kwai-Kolors | 1024x1024 (1:1)<br>960x1280 (3:4)<br>768x1024 (3:4)<br>720x1440 (1:2)<br>720x1280 (9:16)                                          |
| Qwen-Image  | 1328x1328 (1:1)<br>1664x928 (16:9)<br>928x1664 (9:16)<br>1472x1140 (4:3)<br>1140x1472 (3:4)<br>1584x1056 (3:2)<br>1056x1584 (2:3) |

###### 生成参数控制

| 参数名                 | 类型 | 默认值 | 范围             | 说明                                        |
|---------------------|----|-----|----------------|-------------------------------------------|
| batch_size          | 整数 | 1   | 1 ~ 4          | 生成图像数量                                    |
| seed                | 整数 | -   | 0 ~ 9999999999 | 随机种子，用于结果复现                               |
| num_inference_steps | 整数 | 20  | 1 ~ 100        | 推理步数（步数越多细节越精细，耗时越长）                      |
| guidance_scale      | 数值 | 7.5 | 0 ~ 20         | 控制生成图像与提示词的匹配度（值越高越严格，越低越创意）              |
| cfg                 | 数值 | -   | 0.1 ~ 20       | 仅 Qwen 模型：平衡生成精度与创造性（文本生成时需 > 1，官方推荐 4.0） |

###### 可选参数拓展

| 参数名             | 类型  | 说明                                             |
|-----------------|-----|------------------------------------------------|
| negative_prompt | 字符串 | 负面提示词（描述不希望出现在图像中的内容）                          |
| image           | 字符串 | Base64 编码的参考图像（格式示例：data:image/png;base64,XXX） |

3. 我们将根据以上列出的参数，在 `EnhancedImageOptions` 中添加对应字段，以支持更多参数配置。

#### 2.2.3 字段添加

1. 根据改造思路，我们将以下参数添加到 `EnhancedImageOptions` 类中：
   - `image`：Base64 编码后的参数
   - `negative_prompt`：负面提示词
   - `seed`：种子值，范围 0 ~ 9999999999，相似的输入会生成相似的图像
   - `guidance_scale`：引导比例
   - `cfg`：Qwen 模型专属参数，表示个性化的生成精度
   - `num_inference_steps`：生成步骤

2. 使用 `@JsonProperty` 注解将上述参数与厂商 API 文档进行对齐。

#### 2.2.4 单元测试

1. 至此我们需要进行单元测试验证是否可以调试通过，这里我们以 Qwen 模型为例进行测试。
2. 这次主要是针对 Options 层与 Api 层进行调试，因为这两层包含了请求发送的最小闭环，我们可以通过构造 Options 直接进行请求。
   ```java
   /**
     * 从配置文件中读取OpenAI API密钥
     */
    @Value("${spring.ai.openai.api-key}")
    private String apiKey;
    
    /**
     * 从配置文件中读取OpenAI API基础URL
     */
    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    /**
     * 测试EnhancedImageApi和EnhancedImageOptions功能
     * 包括文生图和图生图两种模式的测试
     */
    @Test
    void testApiAndOptions() throws IOException {
        // 构建EnhancedImageApi实例，设置API密钥和基础URL
        EnhancedImageApi imageApi = EnhancedImageApi
                .builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .build();

        // 配置文生图选项：指定模型、推理步数和提示词
        EnhancedImageOptions imageOptions = EnhancedImageOptions.builder()
                .model("Qwen/Qwen-Image")
                .inferenceSteps(30)
                .prompt("请你生成小狗图片")
                .build();

        // 加载测试用的原始图片资源

        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("static/风景图片01.png");
        Assert.notNull(resource, "没有找到图片");
        String filePath = java.net.URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8);
        File file = new File(filePath);
        
        // 配置图生图选项：指定模型、输入图片和提示词
        EnhancedImageOptions imageOptionsEdit = EnhancedImageOptions.builder()
                .model("Qwen/Qwen-Image-Edit")
                .image(ImageUtils.convert(file))
                .prompt("请你将图片的天空改为星空")
                .build();

        // 调用API生成图片，分别获取文生图和图生图的结果
        EnhancedImageApi.OpenAiImageResponse aiImageResponse = imageApi.createImage(imageOptions).getBody();
        EnhancedImageApi.OpenAiImageResponse aiImageResponseEdit = imageApi.createImage(imageOptionsEdit).getBody();

        // 验证API响应不为空
        Assert.notNull(aiImageResponse, "没有获取到图片");
        Assert.notNull(aiImageResponseEdit, "没有获取到图片");

        // 获取响应数据列表
        List<EnhancedImageApi.Data> dataList = aiImageResponse.data();
        List<EnhancedImageApi.Data> dataListEdit = aiImageResponseEdit.data();

        // 验证数据列表不为空
        Assert.notEmpty(dataList, "没有获取到图片");
        Assert.notEmpty(dataListEdit, "没有获取到图片");

        // 获取第一张生成的图片数据
        EnhancedImageApi.Data data = dataList.get(0);
        EnhancedImageApi.Data dataEdit = dataListEdit.get(0);

        // 提取图片URL
        String url = data.url();
        String urlEdit = dataEdit.url();
        Assert.hasText(url, "没有获取到图片地址");
        Assert.hasText(urlEdit, "没有获取到图片地址");

        // 输出生成的图片URL
        System.out.println("文生图地址: " + url);
        System.out.println("图生图地址: " + urlEdit);
    }
   ```
3. 在此过程中，我们需要提供一份 `ImageUtils` 工具类用于图像编码。
   ```java
   /**
    * 图片工具类，提供图片处理相关功能
    *
    * @author 王玉涛
    * @version 1.0
    * @since 2025/9/21
    */
   @Slf4j
   public class ImageUtils {
   
       /**
        * 私有构造函数，防止实例化
        */
       private ImageUtils() {}
   
       /**
        * 将图片文件转换为Base64编码的数据URL格式
        *
        * @param imageFile 需要转换的图片文件对象
        * @return 图片的Base64数据URL字符串，格式为 "data:image/[type];base64,[encodedString]"
        */
       public static String convert(File imageFile) throws IOException {
           // 检查文件是否存在
           if (!imageFile.exists()) {
               log.error("图片文件不存在: {}", imageFile.getAbsolutePath());
               throw new FileNotFoundException("图片文件不存在: " + imageFile.getAbsolutePath());
           }
   
           log.info("开始转换图片文件: {}", imageFile.getAbsolutePath());
   
           try (InputStream is = new FileInputStream(imageFile);
                ByteArrayOutputStream os = new ByteArrayOutputStream()) {
   
               // 使用8KB缓冲区读取文件内容，提高IO效率
               byte[] buffer = new byte[8192];
               int bytesRead;
               while ((bytesRead = is.read(buffer)) != -1) {
                   os.write(buffer, 0, bytesRead);
               }
   
               // 获取文件的MIME类型并构造Base64数据URL
               String mimeType = getMimeType(imageFile.getName());
               String base64Data = Base64.getEncoder().encodeToString(os.toByteArray());
               String dataUrl = "data:" + mimeType + ";base64," + base64Data;
   
               log.info("图片转换完成，文件大小: {} bytes, MIME类型: {}",
                       os.size(), mimeType);
   
               return dataUrl;
           } catch (IOException e) {
               log.error("读取图片文件时发生错误: {}", imageFile.getAbsolutePath(), e);
               throw e;
           }
       }
   
       /**
        * 根据文件名获取MIME类型
        *
        * @param filename 文件名
        * @return MIME类型字符串，格式为 "image/[extension]"
        */
       private static String getMimeType(String filename) {
           // 提取文件扩展名
           int lastDotIndex = filename.lastIndexOf('.');
           if (lastDotIndex != -1 && lastDotIndex < filename.length() - 1) {
               filename = filename.substring(lastDotIndex + 1).toLowerCase();
           }
           // 返回标准MIME类型格式
           return "image/" + filename;
       }
   }
   ```
4. 我们此次针对文生图、图生图进行了统一测试，没有出现明显的 bug。

**至此，ImageOptions 层改造工作已完成。**

### 2.3 ImageModel 层改造

#### 2.3.1 前言

1. 在前面的章节中，我们已经完成了 API 层和 Options 层的改造工作。本章节将重点对 ImageModel 层进行改造，主要围绕 `ImageModel.call()` 方法展开。
2. 关于这部分的源码分析和改造思路，可以参考[源文档](https://dcn7850oahi9.feishu.cn/docx/DDehdPBMSoGTycxmFTLcER4In0F?from=from_copylink)第 6.4 节的内容。
3. 源文档提供了详细的源码阅读方法论和改造步骤，并记录了相关注意事项。我们将基于源文档的指导，将改造流程梳理为一套高效、清晰的方法，并提供相应的代码实现。

#### 2.3.2 源码复制

1. 从 [GitHub 仓库地址](https://github.com/spring-projects/spring-ai/blob/1.0.0/models/spring-ai-openai/src/main/java/org/springframework/ai/openai/OpenAiImageModel.java) 获取原始源码并复制到项目中。
2. 翻译注释内容，确保代码可读性。
3. 将类名修改为 `EnhancedImageModel`，以符合项目命名规范。

#### 2.3.3 改造思路

1. 我们已在[源文档](https://dcn7850oahi9.feishu.cn/docx/DDehdPBMSoGTycxmFTLcER4In0F?from=from_copylink)中完成了详细的改造分析。
2. 需要移除冗余的深拷贝操作：`ImagePrompt requestImagePrompt = buildRequestImagePrompt(imagePrompt);`，因为本质上这是参数合并的过程，默认规则是自定义参数覆盖默认参数。
3. 同时需要调整 `OpenAiImageApi.OpenAiImageRequest imageRequest = createRequest(requestImagePrompt);` 的逻辑，由于我们已将 `ImageOptions` 作为直接请求参数，因此也需要相应修改。
4. 将上述两个步骤合并：优先使用 ImagePrompt 中的参数，其次使用自定义参数，最后使用默认参数；同时简化请求创建逻辑，使其仅依赖 `ImageOptions`。
5. 为了更好地管理参数合并逻辑，我们将封装一个工具方法 `BeanUtils.nullChooseOther(Object defaultValue, Object value, Class<?> clazz)` 来处理参数优先级。
6. 其他部分也需要同步修改，例如将 `OpenAiImageApi` 和 `OpenAiImageOptions` 类替换为我们自定义的类，并更新相关的构造函数。只需根据编译错误进行相应调整即可。

至此，我们成功完成了 ImageModel 层的改造工作。

#### 2.3.4 单元测试

1. 本次单元测试延续上一章节的测试流程，继续验证文生图和图生图功能。
2. 不同的是，我们通过设计不同的 `ImagePrompt` 来验证 Prompt 参数的优先级是否最高。
3. 对于运行时参数与默认参数的优先级关系，我们通过手动构造并注入 Bean 实例来进行验证。
4. 我们创建 `ImageOptionsProperties` 类来构造并注入 Bean 实例，从而测试参数优先级顺序是否正确。

##### 参数示例

```java
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
   private String cfg;

   /**
    * 自定义字段，用于指定推理步骤数，用于控制生成图像的随机性，值越高则生成图像越随机
    */
   private Integer inferenceSteps;
}

```

##### YAML 配置文件示例
```yaml
ai:
  enhanced:
    image:
      options:
        # 指定用于增强图像生成的模型为Qwen/Qwen-Image
        model: Qwen/Qwen-Image
        # 设置生成图像的尺寸为1328x1328像素
        size: 1328x1328
        # 图像生成的提示词：生成一张小狗图片
        prompt: 生成一张小狗图片
        # 负面提示词，避免生成包含天空的图像内容
        negative-prompt: 天空
        # 响应格式设置为URL，返回图像的访问链接
        response-format: url
        # 推理步数设置为20步，控制生成图像的质量和细节
        inference-steps: 20
```

##### 单元测试前置准备

- 在进行单元测试之前，需要进行一些配置，以便提供携带 defaultOptions 的 ImageModel 类。
- 创建 `EnhancedImageModelConfig` 类，用于加载配置默认的 ImageModel 类。
```java

/**
 * @author 王玉涛
 * @version 1.0
 * @since 2025/10/1
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class EnhancedImageModelConfig {

    /**
     * 从配置文件中读取OpenAI API密钥
     */
    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    /**
     * 从配置文件中读取OpenAI API基础URL
     */
    @Value("${spring.ai.openai.base-url}")
    private String baseUrl; 
    
    /**
     * 配置文生图选项：指定模型、推理步数和提示词
     */
    private final ImageOptionsProperties properties;
    
    /**
     * 创建EnhancedImageApi实例
     * 用于与图像生成API进行通信
     * 
     * @return EnhancedImageApi 实例
     */
    @Bean
    public EnhancedImageApi enhancedImageApi() {
        log.info("Initializing EnhancedImageApi with baseUrl: {}", baseUrl);
        return EnhancedImageApi.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .build();
    }
    
    /**
     * 创建EnhancedImageOptions实例
     * 包含图像生成的各种配置参数
     * 
     * @return EnhancedImageOptions 实例
     */
    @Bean
    public EnhancedImageOptions enhancedImageOptions() {
        log.info("Building EnhancedImageOptions with model: {} and prompt: {}", 
                properties.getModel(), properties.getPrompt());
        
        return EnhancedImageOptions.builder()
                .seed(properties.getSeed())
                .model(properties.getModel())
                .inferenceSteps(properties.getInferenceSteps())
                .prompt(properties.getPrompt())
                .negativePrompt(properties.getNegativePrompt())
                .guidanceScale(properties.getGuidanceScale())
                .cfg(properties.getCfg())
                .width(properties.getWidth())
                .height(properties.getHeight())
                .style(properties.getStyle())
                .size(properties.getSize())
                .quality(properties.getQuality())
                .responseFormat(properties.getResponseFormat())
                .user(properties.getUser())
                .image(properties.getImage())
                .n(properties.getN())
                .build();
    }
    
    /**
     * 创建EnhancedImageModel实例
     * 整合API客户端和配置选项，提供完整的图像生成能力
     * 
     * @return EnhancedImageModel 实例
     */
    @Bean
    public EnhancedImageModel enhancedImageModel() {
        log.info("Creating EnhancedImageModel with configured API and options");
        return new EnhancedImageModel(enhancedImageApi(), enhancedImageOptions(), RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }
}
```

##### 单元测试方法

```java

/**
  * 测试EnhancedImageModel功能
  * 包括文生图和图生图两种模式的测试
  */
@Test
void testModel() throws IOException {
    // 加载测试用的原始图片资源
    ClassLoader classLoader = getClass().getClassLoader();
    URL resource = classLoader.getResource("static/风景图片01.png");
    Assert.notNull(resource, "没有找到图片");
    String filePath = java.net.URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8);
    File file = new File(filePath);

    // 配置图生图选项：指定模型、输入图片、提示词和推理步数
    EnhancedImageOptions imageOptionsEdit = EnhancedImageOptions.builder()
            .model("Qwen/Qwen-Image-Edit")
            .prompt("请你将天空改为黑色")
            .image(ImageUtils.convert(file))
            .inferenceSteps(20)
            .build();

    // 配置文生图选项：指定模型和提示词
    EnhancedImageOptions imageOptions = EnhancedImageOptions.builder()
            .model("Qwen/Qwen-Image")
            .prompt("生成一张小猫图片")
            .build();

    // 调用模型生成图片，分别获取文生图和图生图的结果
    ImageResponse imageResponseEdit = enhancedImageModel.call(new ImagePrompt("将图片的水变为岩浆", imageOptionsEdit));
    ImageResponse imageResponse = enhancedImageModel.call(new ImagePrompt("生成小狗图片", imageOptions));
    
    // 验证生成的图片不为空
    Assert.notNull(imageResponseEdit, "编辑图片响应为空");
    Assert.notNull(imageResponse, "生成图片响应为空");
    
    // 获取生成的图片结果
    Image editImage = imageResponseEdit.getResult().getOutput();
    Image generatedImage = imageResponse.getResult().getOutput();
    
    // 验证图片结果不为空
    Assert.notNull(editImage, "编辑图片结果为空");
    Assert.notNull(generatedImage, "生成图片结果为空");
    
    // 验证图片URL不为空
    Assert.hasText(editImage.getUrl(), "编辑图片URL为空");
    Assert.hasText(generatedImage.getUrl(), "生成图片URL为空");

    // 输出生成的图片信息
    System.out.println("编辑图片: " + editImage.getUrl());
    System.out.println("生成图片: " + generatedImage.getUrl());
}
```

**经过测试验证，ImagePrompt 可以正确覆盖默认提示词，并生成不同的图片结果。**

**至此，Spring AI Image API 集成和文生图、图生图功能已初步完成！**

## 3. EnhancedImageClient 图片模型客户端封装

### 3.1 前言

在前面的章节中，我们已经完成了从底层 API 到顶层 ImageModel 的链路改造。接下来，我们将进一步提升抽象层级，创建一个 EnhancedImageClient，将 ImageModel 封装成更易用的客户端形式。

EnhancedImageClient 的设计将参考 ChatClient 的封装逻辑，包括但不限于链式调用封装、提示词优化等特性，以提供更加流畅和直观的开发体验。

EnhancedImageClient 在源文档中并没有设计，这是额外的设计。

### 3.2 初步构建

EnhancedImageClient 的初步构建将从基础的链式调用功能开始实现：

1. 首先在 core 包下创建 client 子包，作为客户端功能的统一入口点
2. 创建 EnhancedImageClient 类，作为图像生成功能的主要交互接口
3. 设计链式调用方法结构，使开发者能够通过简洁的方式调用图像生成服务：

   // 使用示例
   enhancedImageClient.param()
       .prompt("生成一张小猫图片")
       .output();

### 3.3 单元测试类测试

我们依旧是针对文生图以及图生图两种模式进行测试，通过单元测试来验证我们的功能是否正常。

#### 配置 EnhancedImageClient 实例

通过 config 配置，配置出一个 EnhancedImageClient 实例：
```java

/**
 * 创建EnhancedImageClient实例
 * 提供图像生成API的访问入口
 *
 * @return EnhancedImageClient 实例
 */
@Bean
public EnhancedImageClient enhancedImageClient() {
   return new EnhancedImageClient(enhancedImageModel());
}
```

#### 测试方法

测试方法如下：
```java

/**
  * 测试图像生成功能和图像编辑功能
  * 
  * 该测试方法主要验证两个功能：
  * 1. 使用 Qwen/Qwen-Image 模型生成星空图片
  * 2. 使用 Qwen/Qwen-Image-Edit 模型对现有图片进行美化处理
  * 
  * 测试流程：
  * 1. 调用图像生成接口，生成一张星空图片，排除星球元素
  * 2. 读取本地图片文件并转换为Base64编码
  * 3. 调用图像编辑接口，对读取的图片进行美化处理
  * 4. 验证两个接口的输出均不为空
  */
@Test
void testClient() throws IOException {
    // 生成星空图片，使用负面提示词排除星球元素
    String output = enhancedImageClient.param()
            .model("Qwen/Qwen-Image")
            .prompt("请生成一张优美的星空图片")
            .negativePrompt("星球")
            .cfg(7.5)  // 设置提示词相关性因子
            .output();

    // 读取本地图片文件并转换为Base64编码，用于后续编辑
    File imageFile = ImageUtils.findImageFile("static/风景图片01.png");
    String convert = ImageUtils.convert(imageFile);
    
    // 对读取的图片进行美化处理
    String outputEdit = enhancedImageClient.param()
            .model("Qwen/Qwen-Image-Edit")
            .prompt("请你美化当前的图片")
            .cfg(8.0)  // 设置提示词相关性因子
            .image(convert)  // 提供待编辑的图片数据
            .output();
    
    // 验证生成和编辑结果均不为空
    Assertions.assertNotNull(output);
    Assertions.assertNotNull(outputEdit);
    
    // 输出结果到控制台以便查看
    System.out.println(output);
    System.out.println(outputEdit);
}
```

经过测试，方法均可跑通。

**至此，EnhancedImageClient 的初步构建和链式调用功能已实现，接下来，我们将继续完善功能，添加更多特性。**

**这里我们放出一张架构图，向大家展示我们目前的抽象层级结构**
```markdown
+-----------------------------+
|     EnhancedImageClient     |
+-------------+---------------+
              |
+-------------v---------------+
|     EnhancedImageModel      |
+-------------+---------------+
              |
+-------------v---------------+
|     EnhancedImageOptions    |
+-------------+---------------+
              |
+-------------v---------------+
|     EnhancedImageApi        |
+-----------------------------+
```

## 4. Web接口提供
### 4.1 前言
*    在之前的章节，我们已经实现了从Api底层到Client顶层的全链路改造+封装
*    接下来，我们将通过Web接口，提供可以根据是否上传图片，自动选择图文生图或者图生图的功能

### 4.2 接口文档设计
- 接口uri: /api/core/images
- 请求方式：POST
- 参数：
  - file：图片文件
  - prompt：提示词
- 返回参数：url string类型

### 4.3 创建Web服务
1. 创建[EnhancedImageController.java](application/controller/EnhancedImageController.java)
2. 创建[EnhancedImageService.java](application/service/EnhancedImageService.java)
3. 在[ImageUtils.java](util/ImageUtils.java)中添加增强方法方法

### 4.4 测试接口
1. 我们分别测试上传于不上传图片的两种情况，测试接口是否正常
2. 我们经过测试发现图片很容易超出大小限制，所以我们通过配置文件增大图片大小限制
3. 并且请求本身也会超出限制，所以我们也通过配置文件增大请求大小限制
    ```yaml
    spring:
      servlet:
        multipart:
          max-file-size: 30MB
          max-request-size: 30MB
    ````
4. 经过测试，接口可以测通，需要注意文件大小、类型检测，这里不应该由ImageClient客户端检测，而是在业务层进行检测

## 5. 高阶玩法：图片接龙
### 5.1 前言
1. 在之前我们已经实现了ImageClient客户端封装，实现了链式调用的模式，接下来，我们将实现图片接龙功能
2. 图片接龙功能，即输入一张图片，然后根据图片生成一张新的图片，然后根据新的图片生成一张新的图片，直到到达指定的步骤为止
3. 该功能需要图片编辑模型，而如果我们想要实现文生图后再图生图，则可以通过在业务层中两次调用客户端实现该操作

### 5.2 改造思路
1. 为了依旧保证Client调用的流畅性，且保证参数传递、接龙功能参数传递互不影响，我们可以进行如下改造设计
2. 原始设计是：通过.param()方法开启参数设置，然后通过.output()方法获取结果
3. 现在设计为：通过.param()方法开启参数设置，其他的功能不变，添加.solitaire(Integer step)方法，该方法用于设置接龙步骤，随后会获取到List<String>结果
4. 代码示例
    ```java
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
    ```
   
### 5.3 单元测试提供
1. 我们通过测试纯图生图，以及先文生图后图生图的结果，并验证接龙功能是否正常
2. 单元测试类提供
    ```java
    /**
     * 测试图像接龙生成功能
     *
     * 该测试方法验证基于现有图片的连续图像生成功能（图像接龙）：
     * 1. 读取指定的二次元风格图片作为基础图像
     * 2. 使用 Qwen/Qwen-Image-Edit 模型基于该图像生成配色更加唯美的新图像序列
     * 3. 应用负面提示词"星球"来排除特定元素
     * 4. 设置提示词相关性因子为7.5以平衡创造性和遵循提示的程度
     * 5. 生成包含3个图像的接龙序列
     *
     * 此外，还测试了文生图+图生图的组合接龙方式：
     * 1. 先使用 Qwen/Qwen-Image 模型根据提示生成初始二次元图片
     * 2. 再使用 Qwen/Qwen-Image-Edit 模型对该图片进行配色优化
     * 3. 同样生成包含3个图像的接龙序列
     */
    @Test
    void testSolitaire() throws IOException {
        log.info("接龙测试: 纯图生图");
        File imageFile = ImageUtils.findImageFile("static/二次元图片.png");
        String convert = ImageUtils.convert(imageFile);
    
        List<String> solitaire = enhancedImageClient.param()
                .model("Qwen/Qwen-Image-Edit")
                .prompt("请让图片的配色更加唯美")
                .image(convert)
                .negativePrompt("星球")
                .cfg(7.5)  // 提示词相关性因子
                .solitaire(3);
    
        log.info("solitaire: {}", solitaire);
    
        log.info("接龙测试: 文生图+图生图 图像接龙");
        String output = enhancedImageClient.param()
                .model("Qwen/Qwen-Image")
                .prompt("请随机生成原神胡桃的图片")
                .negativePrompt("星球")
                .inferenceSteps(20)
                .output();
        System.out.println(output);
        File imageAsUrl = ImageUtils.createImageAsUrl(output);
        String convertAsUrl = ImageUtils.convert(imageAsUrl);
        List<String> solitaireAsUrl = enhancedImageClient.param()
                .model("Qwen/Qwen-Image-Edit")
                .prompt("请让图片的配色更加唯美")
                .image(convertAsUrl)
                .negativePrompt("星球")
                .cfg(7.5)  // 提示词相关性因子
                .solitaire(3);
    
        log.info("solitaireAsUrl: {}", solitaireAsUrl);
    }
    ```
**经过测试，纯图生图和文生图+图生图的图像接龙功能已经正常实现了。**

### 5.4 进一步丰富功能
1. 当前的功能主要是基于单提示词重复调用，因此我们可以更全面化一些
2. 我们根据原先的图片接龙功能，参数接受再添加集合形式，通过分步获取集合的提示词，并传入给模型进行生成
3. 而为了提升鲁棒性，我们将这样定义调用的顺序：顺序后移，如果到下标超过最后一个则直接使用最后一个