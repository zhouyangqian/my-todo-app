package com.example.common.core.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一 API 响应封装类
 * <p>
 * 所有接口返回值统一使用该类封装，包含状态码、提示信息、数据和时间戳。
 * 前端根据 code 判断请求是否成功。
 * </p>
 *
 * @param <T> 响应数据的泛型类型
 */
@Data
public class ApiResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 响应状态码，200 表示成功 */
    private Integer code;

    /** 响应提示信息 */
    private String message;

    /** 响应数据 */
    private T data;

    /** 响应时间戳（毫秒） */
    private Long timestamp;

    /** 默认构造函数，自动设置当前时间戳 */
    public ApiResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 全参构造函数
     *
     * @param code    状态码
     * @param message 提示信息
     * @param data    响应数据
     */
    public ApiResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /** 返回成功响应（无数据） */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "success", null);
    }

    /**
     * 返回成功响应（带数据）
     *
     * @param data 响应数据
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    /**
     * 返回成功响应（带自定义信息和数据）
     *
     * @param message 提示信息
     * @param data    响应数据
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    /**
     * 返回错误响应（带错误码和错误信息）
     *
     * @param code    错误码
     * @param message 错误信息
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    /**
     * 返回错误响应（默认500错误码）
     *
     * @param message 错误信息
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(500, message, null);
    }

    /** 判断当前响应是否为成功 */
    public boolean isSuccess() {
        return this.code != null && this.code == 200;
    }
}
