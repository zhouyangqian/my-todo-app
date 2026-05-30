package com.example.auth.service;

import com.example.auth.api.dto.CaptchaDTO;
import com.example.auth.entity.Captcha;
import com.example.auth.mapper.CaptchaMapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现
 * <p>
 * 生成图形验证码，使用java.awt绘制，验证码值存储在Redis中。
 * Redis不可用时降级到本地缓存，数据库审计记录写入失败不影响验证码功能。
 * 验证码有效期5分钟。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    private final StringRedisTemplate stringRedisTemplate;
    private final CaptchaMapper captchaMapper;

    /** 验证码字符集（去除易混淆字符） */
    private static final String CAPTCHA_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    /** 验证码长度 */
    private static final int CAPTCHA_LENGTH = 4;
    /** 验证码有效期（分钟） */
    private static final int CAPTCHA_EXPIRE_MINUTES = 5;
    /** Redis Key前缀 */
    private static final String REDIS_KEY_PREFIX = "captcha:";
    /** 图片宽度 */
    private static final int IMG_WIDTH = 120;
    /** 图片高度 */
    private static final int IMG_HEIGHT = 40;

    private final SecureRandom random = new SecureRandom();

    /** 本地缓存（Redis不可用时的降级方案） */
    private final ConcurrentHashMap<String, LocalCacheEntry> localCaptchaStore = new ConcurrentHashMap<>();

    /** 本地缓存条目 */
    private static class LocalCacheEntry {
        final String code;
        final long expireTimeMillis;

        LocalCacheEntry(String code, long expireTimeMillis) {
            this.code = code;
            this.expireTimeMillis = expireTimeMillis;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireTimeMillis;
        }
    }

    /**
     * 定期清理过期的本地缓存
     */
    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredLocalCaptchas() {
        localCaptchaStore.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    @Override
    public CaptchaDTO generateCaptcha() {
        // 生成验证码Key
        String captchaKey = UUID.randomUUID().toString().replace("-", "");

        // 生成随机验证码
        StringBuilder codeBuilder = new StringBuilder(CAPTCHA_LENGTH);
        for (int i = 0; i < CAPTCHA_LENGTH; i++) {
            codeBuilder.append(CAPTCHA_CHARS.charAt(random.nextInt(CAPTCHA_CHARS.length())));
        }
        String code = codeBuilder.toString();

        // 绘制验证码图片
        String base64Image = generateCaptchaImage(code);

        // 存储验证码值（Redis优先，本地缓存降级）
        String redisKey = REDIS_KEY_PREFIX + captchaKey;
        String codeUpper = code.toUpperCase();
        try {
            stringRedisTemplate.opsForValue().set(redisKey, codeUpper, CAPTCHA_EXPIRE_MINUTES, TimeUnit.MINUTES);
            log.debug("验证码已存入Redis: captchaKey={}", captchaKey);
        } catch (Exception e) {
            log.warn("Redis存储验证码失败，降级到本地缓存: {}", e.getMessage());
            localCaptchaStore.put(redisKey, new LocalCacheEntry(codeUpper,
                    System.currentTimeMillis() + (long) CAPTCHA_EXPIRE_MINUTES * 60 * 1000));
        }

        // 数据库审计记录（best-effort，不影响主流程）
        try {
            Captcha captcha = new Captcha();
            captcha.setCaptchaKey(captchaKey);
            captcha.setCodeHash(sha256(codeUpper));
            captcha.setExpiryTime(LocalDateTime.now().plusMinutes(CAPTCHA_EXPIRE_MINUTES));
            captcha.setUsed(0);
            captchaMapper.insert(captcha);
        } catch (Exception e) {
            log.warn("数据库记录验证码审计日志失败: {}", e.getMessage());
        }

        return CaptchaDTO.builder()
                .captchaKey(captchaKey)
                .captchaImage(base64Image)
                .build();
    }

    @Override
    public boolean validateCaptcha(String captchaKey, String captchaValue) {
        if (captchaKey == null || captchaValue == null) {
            return false;
        }

        String redisKey = REDIS_KEY_PREFIX + captchaKey;
        String storedCode = null;

        // 优先从Redis获取
        try {
            storedCode = stringRedisTemplate.opsForValue().get(redisKey);
            if (storedCode != null) {
                stringRedisTemplate.delete(redisKey);
            }
        } catch (Exception e) {
            log.warn("Redis读取验证码失败，尝试本地缓存: {}", e.getMessage());
        }

        // Redis中没有，尝试本地缓存
        if (storedCode == null) {
            LocalCacheEntry entry = localCaptchaStore.remove(redisKey);
            if (entry != null && !entry.isExpired()) {
                storedCode = entry.code;
            }
        }

        // 数据库更新为已使用（best-effort）
        if (storedCode != null) {
            try {
                captchaMapper.update(null,
                        new LambdaUpdateWrapper<Captcha>()
                                .eq(Captcha::getCaptchaKey, captchaKey)
                                .set(Captcha::getUsed, 1)
                );
            } catch (Exception e) {
                log.warn("数据库更新验证码状态失败: {}", e.getMessage());
            }
        }

        if (storedCode == null) {
            log.warn("验证码不存在或已过期: captchaKey={}", captchaKey);
            return false;
        }

        boolean valid = storedCode.equalsIgnoreCase(captchaValue);
        if (!valid) {
            log.warn("验证码错误: captchaKey={}, input={}", captchaKey, captchaValue);
        }
        return valid;
    }

    /**
     * 使用java.awt绘制验证码图片并转为Base64
     *
     * @param code 验证码文本
     * @return Base64编码的PNG图片
     */
    private String generateCaptchaImage(String code) {
        BufferedImage image = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 背景
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, IMG_WIDTH, IMG_HEIGHT);

        // 边框
        g.setColor(Color.LIGHT_GRAY);
        g.drawRect(0, 0, IMG_WIDTH - 1, IMG_HEIGHT - 1);

        // 干扰线
        for (int i = 0; i < 6; i++) {
            g.setColor(new Color(random.nextInt(200), random.nextInt(200), random.nextInt(200)));
            int x1 = random.nextInt(IMG_WIDTH);
            int y1 = random.nextInt(IMG_HEIGHT);
            int x2 = random.nextInt(IMG_WIDTH);
            int y2 = random.nextInt(IMG_HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }

        // 干扰点
        for (int i = 0; i < 30; i++) {
            g.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            int x = random.nextInt(IMG_WIDTH);
            int y = random.nextInt(IMG_HEIGHT);
            g.fillOval(x, y, 2, 2);
        }

        // 绘制验证码字符
        g.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 28));
        for (int i = 0; i < code.length(); i++) {
            // 每个字符随机颜色
            g.setColor(new Color(random.nextInt(150), random.nextInt(150), random.nextInt(150)));
            // 每个字符轻微随机旋转和偏移
            int x = 15 + i * 25;
            int y = 30 + random.nextInt(6) - 3;
            g.drawString(String.valueOf(code.charAt(i)), x, y);
        }

        g.dispose();

        // 转Base64
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("验证码图片生成失败", e);
        }
    }

    /**
     * SHA-256哈希
     */
    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256哈希计算失败", e);
        }
    }
}
