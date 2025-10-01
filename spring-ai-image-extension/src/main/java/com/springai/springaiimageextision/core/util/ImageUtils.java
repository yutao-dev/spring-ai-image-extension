package com.springai.springaiimageextision.core.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

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
     * 查找静态资源目录下的图片文件
     *
     * @param imagePath 图片文件路径，相对于静态资源目录
     * @return 图片文件对象
     */
    public static File findImageFile(String imagePath) {
        ClassLoader classLoader = ImageUtils.class.getClassLoader();
        URL resource = classLoader.getResource(imagePath);
        Assert.notNull(resource, "没有找到图片");
        String filePath = java.net.URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8);
        return new File(filePath);
    }

    /**
     * 将图片文件转换为Base64编码的数据URL格式
     *
     * @param imageFile 需要转换的图片文件对象
     * @return 图片的Base64数据URL字符串，格式为 "data:image/[type];base64,[encodedString]"
     * @throws IOException 当文件读取失败或文件不存在时抛出
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
        Assert.notNull(filename, "文件名不能为空");
        Assert.hasText(filename, "文件名不能为空");
        // 提取文件扩展名
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex != -1 && lastDotIndex < filename.length() - 1) {
            filename = filename.substring(lastDotIndex + 1).toLowerCase();
        }
        // 返回标准MIME类型格式
        return "image/" + filename;
    }

    /**
     * 从文件名中提取文件扩展名并标准化图片类型
     *
     * @param originalFilename 原始文件名（包含扩展名）
     * @return 标准化的文件类型字符串，如 "png"、"jpg"、"webp" 等
     */
    public static String getFileType(String originalFilename) {
        Assert.notNull(originalFilename, "文件名不能为空");
        Assert.hasText(originalFilename, "文件名不能为空");
        originalFilename = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

        originalFilename = findStartImageType(originalFilename);

        return originalFilename;
    }

    /**
     * 尝试从文件名中获取图片类型
     *
     * @param originalFilename 文件名
     * @return 图片类型，如"png"、"jpg"、"jpeg"、"webp"等
     */
    private static String findStartImageType(String originalFilename) {
        Assert.notNull(originalFilename, "文件名不能为空");
        Assert.hasText(originalFilename, "文件名不能为空");
        if (originalFilename.startsWith("png")) {
            return "png";
        } else if (originalFilename.startsWith("jpg") || originalFilename.startsWith("jpeg")) {
            return "jpg";
        } else if (originalFilename.startsWith("webp")) {
            return "webp";
        } else {
            return "jpg";
        }
    }


    /**
     * 判断文件类型是否为图片
     *
     * @param type 文件名
     * @return true表示为图片，false表示非图片
     */
    private static boolean isImageType(String type) {
        return "png".equals(type) || "jpg".equals(type) || "jpeg".equals(type) || "webp".equals(type);
    }

    /**
     * 判断文件类型是否为图片
     *
     * @param type 文件类型
     */
    public static void isImage(String type) {
        Assert.isTrue(isImageType(type), "类型错误");
    }

    /**
     * 获取文件类型并判断是否为图片
     *
     * @param originalFilename 文件名
     * @param isOriginalName 是否使用原始文件名
     */
    public static void isImage(String originalFilename, boolean isOriginalName) {
        if (isOriginalName) {
            originalFilename = getFileType(originalFilename);
        }
        isImage(originalFilename);
    }

public static File convertToFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        try {
            // 创建临时文件
            String originalFilename = file.getOriginalFilename();
            String fileType = getFileType(originalFilename);
            File tempFile = File.createTempFile("temp_", "." + fileType);
            
            // 将MultipartFile写入临时文件
            file.transferTo(tempFile);
            
            return tempFile;
        } catch (IOException e) {
            log.error("将MultipartFile转换为File时发生错误", e);
            throw new IOException("文件转换失败", e);
        }
    }

    /**
     * 从URL创建图片文件
     *
     * @param url 图片URL
     * @return 图片文件对象
     */
    public static File createImageAsUrl(String url) throws IOException {
        try {
            URL imageUrl = new URL(url);
            
            // 创建临时文件
            String fileName = url.substring(url.lastIndexOf("/") + 1);
            // 清理文件名，只保留字母、数字、点号和下划线，去除非法字符
            fileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
            String fileType = getFileType(fileName);
            File tempFile = File.createTempFile("image_", "." + fileType);
            
            // 从URL下载图片内容并写入临时文件
            try (InputStream inputStream = imageUrl.openStream();
                 FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            
            return tempFile;
        } catch (IOException e) {
            log.error("从URL创建图片文件时发生错误: {}", url, e);
            throw e;
        }
    }
}
