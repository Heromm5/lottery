package com.hobart.lottery.common.result;

import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),

    // 参数错误 400-499
    PARAM_ERROR(400, "参数错误"),
    PARAM_IS_NULL(401, "参数为空"),

    // 认证错误 1000-1099
    UNAUTHORIZED(1000, "未登录或登录已过期"),
    TOKEN_INVALID(1001, "Token无效"),
    TOKEN_EXPIRED(1002, "Token已过期"),

    // 权限错误 1100-1199
    FORBIDDEN(1100, "没有权限"),

    // 业务错误 2000-2999
    DATA_NOT_FOUND(2000, "数据不存在"),
    DATA_ALREADY_EXISTS(2001, "数据已存在"),
    DATA_ERROR(2002, "数据错误"),

    // 系统错误 9000-9999
    SYSTEM_ERROR(9000, "系统错误"),
    SERVICE_UNAVAILABLE(9001, "服务不可用");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
