package com.example.common.core.util;

import com.example.common.core.constant.ErrorCodes;
import com.example.common.core.exception.BusinessException;
import com.github.stuxuhai.jpinyin.ChineseHelper;
import com.github.stuxuhai.jpinyin.PinyinHelper;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

/**
 * 编码自动生成工具类
 * <p>
 * 提供通用的业务编码自动生成能力，支持：
 * <ul>
 *   <li>前缀 + 中文名称拼音首字母 + 时间戳的默认格式</li>
 *   <li>数据库唯一性冲突自动重试（默认最多5次）</li>
 *   <li>重试时追加随机后缀避免碰撞</li>
 * </ul>
 * </p>
 *
 * <p>使用示例（租户注册场景）：</p>
 * <pre>{@code
 * String code = CodeGenerateUtil.generate("ZU", tenantName, code ->
 *     tenantMapper.selectCount(
 *         new LambdaQueryWrapper<Tenant>().eq(Tenant::getTenantCode, code)
 *     ) > 0
 * );
 * }</pre>
 */
public final class CodeGenerateUtil {

    private static final SecureRandom RANDOM = new SecureRandom();
    /** 随机字符集（排除易混淆字符 0/O、1/I/L） */
    private static final String RANDOM_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final DateTimeFormatter TIMESTAMP_FMT =
            DateTimeFormatter.ofPattern("yyMMddHHmmss");
    private static final int DEFAULT_MAX_RETRIES = 5;
    /** 拼音首字母最大长度，确保整个编码不超过 VARCHAR(64) */
    private static final int MAX_INITIALS_LEN = 20;

    private CodeGenerateUtil() {}

    /**
     * 生成唯一业务编码
     * <p>格式：{@code {prefix}-{拼音首字母}-{yyMMddHHmmss}}</p>
     * <p>冲突时追加2位随机字符重试，最多5次。</p>
     *
     * @param prefix        编码前缀（如 "ZU"）
     * @param name          源名称（中文或中英文混合）
     * @param existsChecker 唯一性校验函数，返回 true 表示编码已存在
     * @return 生成的唯一编码
     * @throws BusinessException 重试耗尽后仍冲突时抛出
     */
    public static String generate(String prefix, String name,
                                   Function<String, Boolean> existsChecker) {
        String initials = toPinyinInitials(name);
        if (initials.length() > MAX_INITIALS_LEN) {
            initials = initials.substring(0, MAX_INITIALS_LEN);
        }
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FMT);
        String base = prefix + "-" + initials + "-" + timestamp;

        if (!existsChecker.apply(base)) {
            return base;
        }

        for (int i = 1; i <= DEFAULT_MAX_RETRIES; i++) {
            String retry = base + randomSuffix(2);
            if (!existsChecker.apply(retry)) {
                return retry;
            }
        }

        throw new BusinessException(ErrorCodes.TENANT_CODE_GENERATE_FAILED,
                "编码生成失败：尝试" + (DEFAULT_MAX_RETRIES + 1) + "次后仍存在冲突");
    }

    /**
     * 提取中文名称的拼音首字母（大写）
     * <p>非中文字母数字保持原样并大写，其他字符忽略。</p>
     *
     * @param name 源名称
     * @return 拼音首字母字符串（大写），空名称为 "XX"
     */
    public static String toPinyinInitials(String name) {
        if (name == null || name.isBlank()) {
            return "XX";
        }
        StringBuilder sb = new StringBuilder();
        for (char c : name.toCharArray()) {
            if (ChineseHelper.isChinese(c)) {
                try {
                    String pinyin = PinyinHelper.getShortPinyin(String.valueOf(c));
                    if (pinyin != null && !pinyin.isEmpty()) {
                        sb.append(Character.toUpperCase(pinyin.charAt(0)));
                    }
                } catch (Exception ignored) {
                    // 拼音转换失败则跳过该字符
                }
            } else if (Character.isLetterOrDigit(c)) {
                sb.append(Character.toUpperCase(c));
            }
        }
        if (sb.length() == 0) {
            return "XX";
        }
        return sb.toString();
    }

    /** 生成指定长度的随机大写字母+数字后缀 */
    private static String randomSuffix(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(RANDOM_CHARS.charAt(RANDOM.nextInt(RANDOM_CHARS.length())));
        }
        return sb.toString();
    }
}
