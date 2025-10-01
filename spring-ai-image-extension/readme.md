# Spring AI 图像扩展项目 - 释放AI图像生成的无限潜能 🎨

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-green)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.2-blue)](https://spring.io/projects/spring-ai)
[![Java](https://img.shields.io/badge/Java-17-orange)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)

## 🌟 项目简介

欢迎来到Spring AI图像扩展项目！这是一个基于Spring AI框架的强大图像生成增强工具包，旨在突破原生Spring AI在图像生成方面的限制，为开发者提供更丰富的图像生成功能和更灵活的参数配置选项。

无论你是想要快速生成图像的初学者，还是需要深度定制图像生成流程的高级开发者，这个项目都能满足你的需求！

### 🚀 核心亮点

- ✅ **增强图像模型集成** - 支持多种AI图像模型，同时支持SpringAI原生不支持的图生图模型，轻松切换不同厂商
- 🔄 **ImageClient客户端工具** - 链式调用设计，提供极致开发体验
- 🔌 **标准化接口服务** - 统一接口形式，便于快速集成和使用
- 🧪 **完整单元测试** - 全面覆盖，确保功能稳定可靠
- 🎯 **图片接龙功能** - 独创功能，支持连续图像生成创作

## 📦 项目模块架构

```
+-----------------------------+
|     EnhancedImageClient     |  ← 高级客户端（链式调用）
+-------------+---------------+
              |
+-------------v---------------+
|     EnhancedImageModel      |  ← 核心模型层
+-------------+---------------+
              |
+-------------v---------------+
|     EnhancedImageOptions    |  ← 参数配置层
+-------------+---------------+
              |
+-------------v---------------+
|     EnhancedImageApi        |  ← 底层API封装
+-----------------------------+
```

### 1. **quick-start快速开始模块** ⚡
想要快速体验AI图像生成？只需三步：

1. 在配置文件中设置api-key与base-url
2. 启动项目
3. 访问 `http://localhost:8080/api/quick-start/images?prompt=图片生成提示词` 即可快速开始

### 2. **core核心模块** 🔧
提供完整的图像生成增强改造方案，包含：

- **增强图像模型集成** - 对现有图像模型进行功能扩展和优化
- **ImageClient 客户端工具** - 基于增强功能构建，提供更丰富的调用方式和更好的用户体验
- **标准化接口服务** - 以统一的接口形式对外提供服务，便于开发者快速集成和使用
- **完整单元测试** - 所有类均配备完整的单元测试，确保功能稳定性和代码可靠性

## 🎯 核心功能特性

### 🔮 多模型支持
支持不同厂商随意更换，通用大部分遵循OpenAI API规范的厂商：
- Qwen/Qwen-Image
- Kwai-Kolors/Kolors
- 更多模型持续集成中...

### 🖼️ 双模式生成
- **文生图(Image-to-Image)** - 根据文本描述生成图像
- **图生图(Image Editing)** - 基于现有图像进行编辑和修改

### 🛠️ 丰富的参数配置

#### 必需参数
| 参数名        | 类型    | 是否必填 | 可选值/范围                             | 说明                 |
|------------|-------|------|------------------------------------|--------------------|
| model      | 枚举字符串 | ✅    | Qwen/Qwen-Image/Kwai-Kolors/Kolors | 模型选择，服务方可能动态调整可用模型 |
| prompt     | 字符串   | ✅    | -                                  | 描述生成图像内容的文本提示      |
| image_size | 字符串   | ✅    | 见下方尺寸表                             | 分辨率格式必须为宽度 x 高度    |

#### 图像尺寸推荐
| 模型          | 推荐分辨率（格式：宽 x 高）                                                                                                                   |
|-------------|-----------------------------------------------------------------------------------------------------------------------------------|
| Kwai-Kolors | 1024x1024 (1:1)<br>960x1280 (3:4)<br>768x1024 (3:4)<br>720x1440 (1:2)<br>720x1280 (9:16)                                          |
| Qwen-Image  | 1328x1328 (1:1)<br>1664x928 (16:9)<br>928x1664 (9:16)<br>1472x1140 (4:3)<br>1140x1472 (3:4)<br>1584x1056 (3:2)<br>1056x1584 (2:3) |

#### 生成参数控制
| 参数名                 | 类型 | 默认值 | 范围             | 说明                                        |
|---------------------|----|-----|----------------|-------------------------------------------|
| batch_size          | 整数 | 1   | 1 ~ 4          | 生成图像数量                                    |
| seed                | 整数 | -   | 0 ~ 9999999999 | 随机种子，用于结果复现                               |
| num_inference_steps | 整数 | 20  | 1 ~ 100        | 推理步数（步数越多细节越精细，耗时越长）                      |
| guidance_scale      | 数值 | 7.5 | 0 ~ 20         | 控制生成图像与提示词的匹配度（值越高越严格，越低越创意）              |
| cfg                 | 数值 | -   | 0.1 ~ 20       | 仅 Qwen 模型：平衡生成精度与创造性（文本生成时需 > 1，官方推荐 4.0） |

#### 可选参数拓展
| 参数名             | 类型  | 说明                                             |
|-----------------|-----|------------------------------------------------|
| negative_prompt | 字符串 | 负面提示词（描述不希望出现在图像中的内容）                          |
| image           | 字符串 | Base64 编码的参考图像（格式示例：data:image/png;base64,XXX） |

## 🌟 独特功能：图片接龙 🔄

这是本项目最具创新性的功能！图片接龙允许你基于一张图片连续生成多张相关图片，形成图像创作序列。

### 文生图到图生图的完整创作流程

通过我们的图片接龙功能，你可以实现从文本到图像，再到图像编辑的完整创作流程：

1. **第一步：文生图** - 使用文本描述生成初始图像
2. **后续步骤：图生图** - 基于前一步生成的图像进行连续编辑

例如：生成一只小猫 → 调整配色风格 → 更换服装样式 → 改变背景环境

```java
// 文生图到图生图的完整接龙示例
String output = enhancedImageClient.param()
                .model("Qwen/Qwen-Image")
                .prompt("请随机生成原神胡桃的图片")
                .negativePrompt("星球")
                .inferenceSteps(20)
                .output();

File imageAsUrl = ImageUtils.createImageAsUrl(output);
String convertAsUrl = ImageUtils.convert(imageAsUrl);

List<String> solitaireAsUrl = enhancedImageClient.param()
        .model("Qwen/Qwen-Image-Edit")
        .prompt("请让图片的配色更加唯美")
        .image(convertAsUrl)
        .negativePrompt("星球")
        .cfg(7.5)  // 提示词相关性因子
        .solitaire(3);
```

### 图生图连续编辑玩法

如果你已经有了一张图像，可以直接进行连续编辑：

```java
// 图生图连续编辑示例
List<String> imageUrls = enhancedImageClient.param()
    .model("Qwen/Qwen-Image-Edit")     // 使用图生图模型
    .image(base64Image)                // 输入基础图像
    .solitaire(3, List.of(
        "调整图像的色彩饱和度",          // 第一次编辑
        "修改人物的发型",               // 第二次编辑
        "更换背景为城市夜景"            // 第三次编辑
    ));
```

### Web API 调用示例

你也可以通过Web API实现相同的功能：

```bash
# 文生图到图生图接龙
curl -X POST "http://localhost:8080/api/core/images/solitaire" \
     -F "prompt=一只可爱的橘猫坐在草地上" \
     -F "prompts=让猫咪戴上红色蝴蝶结,把草地换成沙滩背景,给猫咪穿上小马甲" \
     -F "step=4"

# 图生图连续编辑
curl -X POST "http://localhost:8080/api/core/images/solitaire" \
     -F "file=@input-image.jpg" \
     -F "prompt=基础编辑指令" \
     -F "prompts=调整图像的色彩饱和度,修改人物的发型,更换背景为城市夜景" \
     -F "step=3"
```

## 🚀 快速开始指南

### 1. 配置API密钥和基础URL
在 [application.yaml](src/main/resources/application.yaml) 中配置以下参数：
```yaml
spring:
  ai:
    openai:
      # OpenAI API密钥，从环境变量SPRING_AI_OPENAI_API_KEY获取
      api-key: ${SPRING_AI_OPENAI_API_KEY}
      # OpenAI API基础URL，从环境变量SPRING_AI_OPENAI_BASE_URL获取
      base-url: ${SPRING_AI_OPENAI_BASE_URL}
```

### 2. 快速体验
1. 配置文件中配置api-key与base-url
2. 启动项目
3. 访问 `http://localhost:8080/api/quick-start/images?prompt=图片生成提示词` 即可快速开始

### 3. 高级配置
在 [application.yaml](src/main/resources/application.yaml) 中可以配置默认参数：
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

## 📚 项目文档

1. 在项目的每个包下都附带readme.md
2. [core模块下的readme.md](src/main/java/com/springai/springaiimageextision/core/readme.md)提供完整的教程和深入的技术细节
3. [quickstart模块下的readme.md](src/main/java/com/springai/springaiimageextision/quickstart/readme.md)提供快速开始指南

## 📖 参考资料

项目详细设计参考[飞书源文档: SpringAI模型调用、自定义集群深入探索](https://dcn7850oahi9.feishu.cn/docx/DDehdPBMSoGTycxmFTLcER4In0F?from=from_copylink)

## 🤝 贡献与支持

如果你觉得这个项目有趣或有用，欢迎：
1. 提交Issue报告问题
2. 发起Pull Request贡献代码
3. 分享给更多需要的人

让我们一起探索AI图像生成的无限可能！✨