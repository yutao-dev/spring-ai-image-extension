package com.springai.springaiimageextision.core.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
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
        // 提取文件扩展名
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex != -1 && lastDotIndex < filename.length() - 1) {
            filename = filename.substring(lastDotIndex + 1).toLowerCase();
        }
        // 返回标准MIME类型格式
        return "image/" + filename;
    }
}
