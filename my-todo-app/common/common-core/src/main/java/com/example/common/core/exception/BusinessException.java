package com.example.common.core.exception;

import lombok.Getter;

/**
 * 业务异常类
 * <p>
 * 用于在业务逻辑中抛出可预期的异常，携带错误码和错误信息。
 * 由全局异常处理器统一捕获并返回给前端。
 * </p>
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 错误码 */
    private final Integer code;

    /** 错误信息 */
    private final String message;

    /**
     * 仅传入错误信息，默认错误码 500
     *
     * @param message 错误信息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
        this.message = message;
    }

    /**
     * 传入错误码和错误信息
     *
     * @param code    错误码
     * @param message 错误信息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 传入错误码、错误信息和原始异常（用于异常链追踪）
     *
     * @param code    错误码
     * @param message 错误信息
     * @param cause   原始异常
     */
    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    /**
     * 静态工厂方法：仅传入错误信息
     *
     * @param message 错误信息
     * @return BusinessException 实例
     */
    public static BusinessException of(String message) {
        return new BusinessException(message);
    }

    /**
     * 静态工厂方法：传入错误码和错误信息
     *
     * @param code    错误码
     * @param message 错误信息
     * @return BusinessException 实例
     */
    public static BusinessException of(Integer code, String message) {
        return new BusinessException(code, message);
    }
}
