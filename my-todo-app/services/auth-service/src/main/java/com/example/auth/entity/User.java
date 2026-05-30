package com.example.auth.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.common.core.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户实体类（认证服务）
 * <p>
 * 对应 sys_user 表，与 user-service 共享同一张用户表。
 * 包含用户基本信息、认证信息、账号状态、锁定信息、登录统计等。
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户名 */
    @TableField("user_name")
    private String userName;

    /** 密码（BCrypt 加密存储） */
    @TableField("pass_word")
    private String passWord;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 真实姓名 */
    private String realName;

    /** 头像URL */
    private String avatar;

    /** 账号状态：0-禁用，1-正常 */
    private Integer status;

    /** 锁定状态：0-未锁定，1-已锁定 */
    private Integer locked;

    /** 锁定截止时间（超过此时间自动解锁） */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime lockedUntil;

    /** 连续登录失败次数（达到上限后锁定账号） */
    private Integer loginFailCount;

    /** 最后登录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime lastLoginTime;

    /** 最后登录IP */
    private String lastLoginIp;

    /** 密码最后修改时间 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime passwordChangedAt;

    /** 账号是否未过期（非数据库字段，Spring Security 使用） */
    @TableField(exist = false)
    private Boolean accountNonExpired = true;

    /** 账号是否未锁定（非数据库字段，Spring Security 使用） */
    @TableField(exist = false)
    private Boolean accountNonLocked = true;

    /** 凭证是否未过期（非数据库字段，Spring Security 使用） */
    @TableField(exist = false)
    private Boolean credentialsNonExpired = true;

    /** 账号是否启用（非数据库字段，Spring Security 使用） */
    @TableField(exist = false)
    private Boolean enabled = true;

    /** 判断账号是否启用 */
    public boolean isEnabled() {
        return this.status != null && this.status == 1;
    }

    /** 判断账号是否未锁定（考虑锁定截止时间） */
    public boolean isAccountNonLocked() {
        if (this.locked == null || this.locked == 0) {
            return true;
        }
        // 锁定时间未过期则仍为锁定状态
        if (this.lockedUntil != null && this.lockedUntil.isAfter(LocalDateTime.now())) {
            return false;
        }
        return true;
    }

    /** 账号永不过期 */
    public boolean isAccountNonExpired() {
        return true;
    }

    /** 凭证永不过期 */
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
