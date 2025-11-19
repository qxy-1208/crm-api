package com.crm.utils;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.crm.common.exception.ServerException;

/**
 * 校验工具类
 * 提供字符串、对象、数组的空值校验及反向校验功能
 *
 * @author 阿沐 babamu@126.com
 * <a href="https://maku.net">MAKU</a>
 */
public class AssertUtils {

    /**
     * 校验字符串为空或空白
     * 若为空则抛出异常
     *
     * @param str      待校验字符串
     * @param variable 变量名称（用于异常信息）
     */
    public static void isBlank(String str, String variable) {
        if (StrUtil.isBlank(str)) {
            throw new ServerException(variable + "不能为空");
        }
    }

    /**
     * 校验字符串不为空且非空白
     * 若为空则抛出异常（与isBlank反向）
     *
     * @param str     待校验字符串
     * @param message 异常提示信息
     */
    public static void notBlank(String str, String message) {
        if (StrUtil.isBlank(str)) {
            throw new ServerException(message);
        }
    }

    /**
     * 校验对象为null
     * 若为null则抛出异常
     *
     * @param object   待校验对象
     * @param variable 变量名称（用于异常信息）
     */
    public static void isNull(Object object, String variable) {
        if (object == null) {
            throw new ServerException(variable + "不能为空");
        }
    }

    /**
     * 校验对象不为null
     * 若为null则抛出异常（与isNull反向）
     *
     * @param object  待校验对象
     * @param message 异常提示信息
     */
    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new ServerException(message);
        }
    }

    /**
     * 校验数组为空
     * 若为空则抛出异常
     *
     * @param array    待校验数组
     * @param variable 变量名称（用于异常信息）
     */
    public static void isArrayEmpty(Object[] array, String variable) {
        if (ArrayUtil.isEmpty(array)) {
            throw new ServerException(variable + "不能为空");
        }
    }

    /**
     * 校验数组不为空
     * 若为空则抛出异常（与isArrayEmpty反向）
     *
     * @param array   待校验数组
     * @param message 异常提示信息
     */
    public static void notArrayEmpty(Object[] array, String message) {
        if (ArrayUtil.isEmpty(array)) {
            throw new ServerException(message);
        }
    }

}