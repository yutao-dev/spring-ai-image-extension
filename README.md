# Spring AI 图像扩展项目

## 项目总体介绍

本项目是基于 Spring AI 框架的图像生成功能扩展包，旨在提供更丰富的图像生成能力和更灵活的参数配置选项。Spring AI 原生功能并未包含完整的图像生成功能，本项目通过扩展增强，支持更多厂商的图像模型和自定义参数。

### 1. 项目目前集成的模块

1. **quick-start快速开始模块** - 提供快速开始服务
   - 配置文件中配置api-key与base-url
   - 启动项目
   - 访问http://localhost:8080/api/quick-start/images?prompt=图片生成提示词即可快速开始

2. **core核心模块** - 提供增强Image的完整改造方案
   - ✅ **增强图像模型集成** - 对现有图像模型进行功能扩展和优化
   - 🔄 **ImageClient 客户端工具** - 基于增强功能构建，提供更丰富的调用方式和更好的用户体验
   - 🔌 **标准化接口服务** - 以统一的接口形式对外提供服务，便于开发者快速集成和使用
   - 🧪 **完整单元测试** - 所有类均配备完整的单元测试，确保功能稳定性和代码可靠性

### 2. 核心功能特性

#### 2.1 增强的图像模型支持
- 支持不同厂商随意更换，**通用大部分遵循OpenAI API规范的厂商**
- 支持多种图像生成模型（Qwen/Qwen-Image/Kwai-Kolors/Kolors等）
- 支持文生图和图生图两种生成模式
- 提供丰富的自定义参数配置

#### 2.2 扩展的参数配置
支持以下参数配置：

##### 必需参数
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

##### 生成参数控制
| 参数名                 | 类型 | 默认值 | 范围             | 说明                                        |
|---------------------|----|-----|----------------|-------------------------------------------|
| batch_size          | 整数 | 1   | 1 ~ 4          | 生成图像数量                                    |
| seed                | 整数 | -   | 0 ~ 9999999999 | 随机种子，用于结果复现                               |
| num_inference_steps | 整数 | 20  | 1 ~ 100        | 推理步数（步数越多细节越精细，耗时越长）                      |
| guidance_scale      | 数值 | 7.5 | 0 ~ 20         | 控制生成图像与提示词的匹配度（值越高越严格，越低越创意）              |
| cfg                 | 数值 | -   | 0.1 ~ 20       | 仅 Qwen 模型：平衡生成精度与创造性（文本生成时需 > 1，官方推荐 4.0） |

##### 可选参数拓展
| 参数名             | 类型  | 说明                                             |
|-----------------|-----|------------------------------------------------|
| negative_prompt | 字符串 | 负面提示词（描述不希望出现在图像中的内容）                          |
| image           | 字符串 | Base64 编码的参考图像（格式示例：data:image/png;base64,XXX） |

### 3. 使用说明

#### 3.1 配置API密钥和基础URL
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

#### 3.2 快速开始
1. 配置文件中配置api-key与base-url
2. 启动项目
3. 访问 http://localhost:8080/api/quick-start/images?prompt=图片生成提示词  即可快速开始

#### 3.3 高级配置
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

### 4. 项目文档
1. 在项目的每个包下都附带readme.md
2. [core模块下的readme.md](src/main/java/com/springai/springaiimageextision/core/readme.md)将会提供完整的教程
3. 项目参考[飞书源文档: SpringAI模型调用、自定义集群深入探索](https://dcn7850oahi9.feishu.cn/docx/DDehdPBMSoGTycxmFTLcER4In0F?from=from_copylink)
