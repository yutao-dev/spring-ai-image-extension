package com.springai.springaiimageextision.core.util;

import java.util.Objects;

/**
 * Bean工具类，提供常用的Bean操作方法
 *
 * @author 王玉涛
 * @version 1.0
 * @since 2025/9/22
 */
public class BeanUtils {

    /**
     * 私有构造方法，防止实例化
     */
    private BeanUtils() {}

    /**
     * 如果给定值为null，则返回默认值；否则返回原值
     *
     * @param <T> 泛型类型
     * @param value 待检查的值
     * @param defaultValue 默认值
     * @param clazz 返回值的类型Class对象
     * @return 当value为null时返回defaultValue，否则返回value
     */
    public static <T> T nullThenChooseOther(Object value, Object defaultValue, Class<T> clazz) {
        return clazz.cast(Objects.isNull(value) ? defaultValue : value);
    }
}