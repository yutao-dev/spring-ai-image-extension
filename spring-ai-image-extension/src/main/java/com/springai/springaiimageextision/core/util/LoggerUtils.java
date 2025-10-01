package com.springai.springaiimageextision.core.util;

import lombok.extern.slf4j.Slf4j;

import java.util.function.BooleanSupplier;

/**
 * 日志工具类，提供基于条件的日志记录功能
 *
 * @author 王玉涛
 * @version 1.0
 * @since 2025/10/1
 */
@Slf4j
public class LoggerUtils {

    /**
     * 私有构造函数，防止实例化
     */
    private LoggerUtils() {}
    
    /**
     * 当条件为真时记录警告日志
     *
     * @param condition 布尔供应器，用于判断是否需要记录日志
     * @param logMessage 日志消息模板，支持占位符
     * @param args 日志参数，用于替换消息模板中的占位符
     */
    public static void logWarnIfTrue(boolean condition, String logMessage, String... args) {
        if (condition) {
            log.warn(logMessage, (Object[]) args);
        }
    }
    
    /**
     * 当条件为真时记录错误日志
     *
     * @param condition 布尔供应器，用于判断是否需要记录日志
     * @param logMessage 日志消息模板，支持占位符
     * @param args 日志参数，用于替换消息模板中的占位符
     */
    public static void logErrorIfTrue(boolean condition, String logMessage, String... args) {
        if (condition) {
            log.error(logMessage, (Object[]) args);
        }
    }
    
    /**
     * 当条件为真时记录信息日志
     *
     * @param condition 布尔供应器，用于判断是否需要记录日志
     * @param logMessage 日志消息模板，支持占位符
     * @param args 日志参数，用于替换消息模板中的占位符
     */
    public static void logInfoIfTrue(boolean condition, String logMessage, String... args) {
        if (condition) {
            log.info(logMessage, (Object[]) args);
        }
    }
    
    /**
     * 当条件为真时记录调试日志
     *
     * @param condition 布尔供应器，用于判断是否需要记录日志
     * @param logMessage 日志消息模板，支持占位符
     * @param args 日志参数，用于替换消息模板中的占位符
     */
    public static void logDebugIfTrue(boolean condition, String logMessage, String... args) {
        if (condition) {
            log.debug(logMessage, (Object[]) args);
        }
    }
}
