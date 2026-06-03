package com.example.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.finance.entity.BankAccount;
import com.example.finance.mapper.BankAccountMapper;
import com.example.common.core.util.CodeGenerateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 银行账户业务服务类
 * <p>
 * 继承 MyBatis-Plus 的 ServiceImpl，提供银行账户的完整业务逻辑，
 * 包括分页查询、获取启用账户列表、获取默认账户、创建、更新、余额调整和软删除。
 * 支持默认账户管理（同一租户仅允许一个默认账户），余额调整支持增加和减少。
 * 所有数据库写操作均使用 @Transactional 注解保证事务一致性。
 * </p>
 *
 * @author finance-team
 * @since 1.0
 */
@Slf4j
@Service
public class BankAccountServiceImpl extends ServiceImpl<BankAccountMapper, BankAccount> implements BankAccountService {

    /**
     * 分页查询银行账户列表
     *
     * @param tenantId    租户ID，用于多租户数据隔离
     * @param page        当前页码（从1开始）
     * @param size        每页记录数
     * @param accountName 账户名称（可选），传入时进行模糊匹配
     * @param accountType 账户类型（可选），1-现金账户, 2-银行账户, 3-支付宝, 4-微信
     * @return 分页结果对象，包含符合条件的银行账户列表及分页信息
     */
    @Override
    public Page<BankAccount> getPage(Long tenantId, int page, int size,
                                      String accountName, Integer accountType) {
        // 构建查询条件：限定租户 + 未删除
        LambdaQueryWrapper<BankAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BankAccount::getTenantId, tenantId)
               .eq(BankAccount::getDeleted, 0);
        // 可选过滤：按账户名称模糊查询
        if (accountName != null && !accountName.isEmpty()) {
            wrapper.like(BankAccount::getAccountName, accountName);
        }
        // 可选过滤：按账户类型精确筛选
        if (accountType != null) {
            wrapper.eq(BankAccount::getAccountType, accountType);
        }
        // 按账户类型升序排列
        wrapper.orderByAsc(BankAccount::getAccountType);
        return page(new Page<>(page, size), wrapper);
    }

    /**
     * 获取指定租户下所有启用的银行账户列表
     * <p>
     * 通常用于前端下拉选择框的数据源，仅返回状态为"启用"的账户。
     * </p>
     *
     * @param tenantId 租户ID
     * @return 所有启用状态的银行账户列表，按账户类型升序排列
     */
    @Override
    public List<BankAccount> getAllAccounts(Long tenantId) {
        return list(
            new LambdaQueryWrapper<BankAccount>()
                .eq(BankAccount::getTenantId, tenantId)
                // 仅查询启用状态的账户
                .eq(BankAccount::getStatus, 1)
                .eq(BankAccount::getDeleted, 0)
                .orderByAsc(BankAccount::getAccountType)
        );
    }

    /**
     * 获取指定租户的默认银行账户
     * <p>
     * 默认账户用于快速选择，每个租户只能有一个默认账户。
     * </p>
     *
     * @param tenantId 租户ID
     * @return 默认账户对象，不存在时返回null
     */
    @Override
    public BankAccount getDefaultAccount(Long tenantId) {
        return getOne(
            new LambdaQueryWrapper<BankAccount>()
                .eq(BankAccount::getTenantId, tenantId)
                // 查询标记为默认的账户
                .eq(BankAccount::getIsDefault, 1)
                // 仅查询启用状态的账户
                .eq(BankAccount::getStatus, 1)
                .eq(BankAccount::getDeleted, 0)
        );
    }

    /**
     * 创建银行账户
     * <p>
     * 创建前会校验账户编码在同租户下的唯一性。
     * 新建账户的余额自动初始化为0。如果设为默认账户，会先清除其他账户的默认标记。
     * </p>
     *
     * @param account 银行账户实体对象（需包含 tenantId, accountCode, accountName, accountType 等字段）
     * @return 创建成功后的银行账户对象（含自动生成的ID）
     * @throws IllegalArgumentException 当账户编码已存在时抛出
     */
    @Override
    @Transactional
    public BankAccount create(BankAccount account) {
        // 自动生成账户编码（格式：BANK-拼音首字母-时间戳）
        String generatedCode = CodeGenerateUtil.generate("BANK",
                account.getAccountName(),
                code -> getOne(new LambdaQueryWrapper<BankAccount>()
                        .eq(BankAccount::getAccountCode, code)
                        .eq(BankAccount::getTenantId, account.getTenantId())
                        .eq(BankAccount::getDeleted, 0)) != null
        );
        account.setAccountCode(generatedCode);

        // 新建账户余额初始化为0
        account.setBalance(BigDecimal.ZERO);
        // 如果设为默认账户，需要先清除其他账户的默认标记
        if (account.getIsDefault() != null && account.getIsDefault() == 1) {
            clearDefaultAccount(account.getTenantId());
        }
        save(account);
        log.info("创建银行账户: {}", account.getAccountCode());
        return account;
    }

    /**
     * 更新银行账户信息
     * <p>
     * 如果将账户设为默认账户，会先清除同租户下其他账户的默认标记。
     * </p>
     *
     * @param account 银行账户实体对象（需包含 id 和要更新的字段）
     * @return 更新后的银行账户对象
     */
    @Override
    @Transactional
    public BankAccount update(BankAccount account) {
        // 如果设为默认账户，需要先清除其他账户的默认标记
        if (account.getIsDefault() != null && account.getIsDefault() == 1) {
            clearDefaultAccount(account.getTenantId());
        }
        updateById(account);
        log.info("更新银行账户: {}", account.getAccountCode());
        return account;
    }

    /**
     * 调整银行账户余额
     * <p>
     * 收支记录审核通过或取消时调用此方法。
     * 增加余额用于收入确认，减少余额用于支出确认。
     * 减少余额时会校验账户余额是否充足（不允许透支）。
     * </p>
     *
     * @param accountId 银行账户ID
     * @param amount    调整金额（正数）
     * @param isAdd     true-增加余额（收入），false-减少余额（支出）
     * @throws IllegalArgumentException 当银行账户不存在时抛出
     * @throws IllegalStateException    当减少余额且账户余额不足时抛出
     */
    @Override
    @Transactional
    public void adjustBalance(Long accountId, BigDecimal amount, boolean isAdd) {
        BankAccount account = getById(accountId);
        if (account == null) {
            throw new IllegalArgumentException("银行账户不存在");
        }

        BigDecimal newBalance;
        if (isAdd) {
            // 收入：余额增加
            newBalance = account.getBalance().add(amount);
        } else {
            // 支出：余额减少，需校验余额是否充足
            newBalance = account.getBalance().subtract(amount);
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalStateException("账户余额不足");
            }
        }

        account.setBalance(newBalance);
        updateById(account);
        log.info("调整账户余额: 账户ID={}, 调整金额={}, 调整后余额={}",
                accountId, isAdd ? amount : amount.negate(), newBalance);
    }

    /**
     * 软删除银行账户
     * <p>
     * 仅允许删除余额为0的账户，避免删除仍有资金余额的账户导致数据不一致。
     * </p>
     *
     * @param id 银行账户ID
     * @throws IllegalStateException 当账户余额不为零时抛出
     */
    @Override
    @Transactional
    public void delete(Long id) {
        BankAccount account = getById(id);
        if (account != null) {
            // 校验余额：只有余额为0的账户才能删除
            if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
                throw new IllegalStateException("账户余额不为零,无法删除");
            }
            // 执行软删除：设置deleted字段为1
            account.setDeleted(1);
            updateById(account);
            log.info("删除银行账户: {}", account.getAccountCode());
        }
    }

    /**
     * 清除指定租户下所有账户的默认账户标记
     * <p>
     * 在设置新的默认账户前调用，确保同一租户始终只有一个默认账户。
     * </p>
     *
     * @param tenantId 租户ID
     */
    private void clearDefaultAccount(Long tenantId) {
        update(
            new LambdaUpdateWrapper<BankAccount>()
                .eq(BankAccount::getTenantId, tenantId)
                .eq(BankAccount::getIsDefault, 1)
                .set(BankAccount::getIsDefault, 0)
        );
    }
}
