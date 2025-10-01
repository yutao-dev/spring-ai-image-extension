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
3. 在此过程中，我们需要提供一份 `ImageUtils` 工具类用于图像编码。
4. 我们此次针对文生图、图生图进行了统一测试，没有出现明显的 bug。

**至此，ImageOptions 层改造工作已完成。**

### 2.3 ImageModel 层改造
#### 2.3.1 前言
1. 在之前我们已经进行了Api与Options层的改造，本章节我们将围绕ImageModel进行改造，围绕ImageModel.call()方法进行
2. 有关这一部分的源码阅读以及改造流程的思路梳理，可以移步并参考[源文档](https://dcn7850oahi9.feishu.cn/docx/DDehdPBMSoGTycxmFTLcER4In0F?from=from_copylink)的6.4章节
3. 源文档给出了详细的源码阅读方法论梳理，并给出详细的改造步骤以及踩坑记录，我们将根据源文档，将流程梳理成一步到位的方法，并给出相应的代码实现

#### 2.3.2 源码复制
1. 我们从[github仓库的源码地址](https://github.com/spring-projects/spring-ai/blob/1.0.0/models/spring-ai-openai/src/main/java/org/springframework/ai/openai/OpenAiImageModel.java)中，将源代码复制到本项目中
2. 翻译注释
3. 修改类名为`EnhancedImageModel`

#### 2.3.3 改造思路
1. 我们在[源文档](https://dcn7850oahi9.feishu.cn/docx/DDehdPBMSoGTycxmFTLcER4In0F?from=from_copylink)中已经进行了改造分析
2. 需要去除冗余的深拷贝，即`ImagePrompt requestImagePrompt = buildRequestImagePrompt(imagePrompt);`，因为这里本质是直接的参数合并，默认是自定义参数覆盖默认参数
3. 以及`OpenAiImageApi.OpenAiImageRequest imageRequest = createRequest(requestImagePrompt);`, 这里是创建请求的逻辑，因为我们已经将ImageOptions作为直接的参数请求，因此我们也需要修改这里
4. 将上述的两处代码进行合并，即ImagePrompt优先与自定义参数合并，自定义参数再与默认参数合并，创建请求的逻辑也只需要ImageOptions作为参数即可
5. 我们将这里的参数合并，封装为工具类方法，即`BeanUtils.nullChooseOther(Object defaultValue, Object value, Class<?> clazz)`
6. 其他的地方也需要进行修改，例如修改OpenAiImageApi、OpenAiImageOptions类，修改成自定义的类，并修改相应的构造函数，只需要根据检查报错修改即可
   