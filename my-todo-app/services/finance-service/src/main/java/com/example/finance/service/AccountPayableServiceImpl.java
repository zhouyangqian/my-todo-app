package com.example.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.finance.entity.AccountPayable;
import com.example.finance.mapper.AccountPayableMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 应付账款业务服务类
 * <p>
 * 继承 MyBatis-Plus 的 ServiceImpl，提供应付账款的完整业务逻辑，
 * 包括分页查询、创建、付款确认、供应商应付总额统计和逾期应付查询。
 * 所有数据库写操作均使用 @Transactional 注解保证事务一致性。
 * </p>
 *
 * @author finance-team
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountPayableServiceImpl extends ServiceImpl<AccountPayableMapper, AccountPayable> implements AccountPayableService {

    /**
     * 分页查询应付账款列表
     *
     * @param tenantId    租户ID，用于多租户数据隔离
     * @param page        当前页码（从1开始）
     * @param size        每页记录数
     * @param supplierId  供应商ID（可选），传入时仅查询该供应商的应付记录
     * @param status      结算状态（可选），0-未结算, 1-部分结算, 2-已结算
     * @return 分页结果对象，包含符合条件的应付账款列表及分页信息
     */
    @Override
    public Page<AccountPayable> getPage(Long tenantId, int page, int size,
                                         Long supplierId, Integer status) {
        // 构建查询条件：限定租户 + 未删除
        LambdaQueryWrapper<AccountPayable> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccountPayable::getTenantId, tenantId)
               .eq(AccountPayable::getDeleted, 0);
        // 可选过滤：按供应商ID筛选
        if (supplierId != null) {
            wrapper.eq(AccountPayable::getSupplierId, supplierId);
        }
        // 可选过滤：按结算状态筛选
        if (status != null) {
            wrapper.eq(AccountPayable::getStatus, status);
        }
        // 按创建时间倒序排列，最新的记录排在前面
        wrapper.orderByDesc(AccountPayable::getCreatedAt);
        return page(new Page<>(page, size), wrapper);
    }

    /**
     * 创建应付账款记录
     * <p>
     * 新建时自动初始化：已付金额设为0，未付金额设为应付金额，状态设为"未结算"。
     * </p>
     *
     * @param payable 应付账款实体对象（需包含 tenantId, supplierId, amount, bizNo 等字段）
     * @return 创建成功后的应付账款对象（含自动生成的ID）
     */
    @Override
    @Transactional
    public AccountPayable create(AccountPayable payable) {
        // 初始化已付金额为0
        payable.setPaidAmount(BigDecimal.ZERO);
        // 未付金额等于应付总金额
        payable.setUnpaidAmount(payable.getAmount());
        // 初始状态为未结算
        payable.setStatus(0);
        save(payable);
        log.info("创建应付账款: 供应商ID={}, 金额={}", payable.getSupplierId(), payable.getAmount());
        return payable;
    }

    /**
     * 应付账款付款操作
     * <p>
     * 付款后自动更新已付金额和未付金额，并根据付款情况更新结算状态：
     * 未付金额 <= 0 时变为"已结算"，已付金额 > 0 且未付金额 > 0 时变为"部分结算"。
     * </p>
     *
     * @param id     应付账款记录ID
     * @param amount 本次付款金额
     * @throws IllegalArgumentException 当应付账款记录不存在时抛出
     */
    @Override
    @Transactional
    public void makePayment(Long id, BigDecimal amount) {
        AccountPayable payable = getById(id);
        if (payable == null) {
            throw new IllegalArgumentException("应付账款不存在");
        }

        // 计算新的已付金额 = 原已付金额 + 本次付款金额
        BigDecimal newPaidAmount = payable.getPaidAmount().add(amount);
        // 计算新的未付金额 = 应付总金额 - 新已付金额
        BigDecimal newUnpaidAmount = payable.getAmount().subtract(newPaidAmount);

        payable.setPaidAmount(newPaidAmount);
        // 未付金额不允许为负数，取0和计算值的较大者
        payable.setUnpaidAmount(newUnpaidAmount.max(BigDecimal.ZERO));

        // 根据未付金额判断并更新结算状态
        if (newUnpaidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            payable.setStatus(2); // 已结算：全部付清
        } else if (newPaidAmount.compareTo(BigDecimal.ZERO) > 0) {
            payable.setStatus(1); // 部分结算：已付部分款项
        }

        updateById(payable);
        log.info("应付账款付款: ID={}, 金额={}", id, amount);
    }

    /**
     * 获取指定供应商的应付总额（仅统计未结算和部分结算的记录）
     *
     * @param supplierId 供应商ID
     * @param tenantId   租户ID
     * @return 该供应商所有未结清应付账款的未付金额之和
     */
    @Override
    public BigDecimal getTotalPayable(Long supplierId, Long tenantId) {
        // 查询该供应商下所有未结算(0)和部分结算(1)的应付记录
        List<AccountPayable> list = list(
            new LambdaQueryWrapper<AccountPayable>()
                .eq(AccountPayable::getTenantId, tenantId)
                .eq(AccountPayable::getSupplierId, supplierId)
                .in(AccountPayable::getStatus, 0, 1)
                .eq(AccountPayable::getDeleted, 0)
        );
        // 使用流式计算汇总所有未付金额
        return list.stream()
                .map(AccountPayable::getUnpaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 获取逾期应付账款列表
     * <p>
     * 查询条件：状态为未结算或部分结算，且到期日期早于当前时间。
     * 用于逾期付款提醒和账龄分析。
     * </p>
     *
     * @param tenantId 租户ID
     * @return 逾期未支付的应付账款列表
     */
    @Override
    public List<AccountPayable> getOverdueList(Long tenantId) {
        return list(
            new LambdaQueryWrapper<AccountPayable>()
                .eq(AccountPayable::getTenantId, tenantId)
                // 仅查询未结算和部分结算的记录
                .in(AccountPayable::getStatus, 0, 1)
                // 到期日期早于当前时间即为逾期
                .lt(AccountPayable::getDueDate, LocalDateTime.now())
                .eq(AccountPayable::getDeleted, 0)
        );
    }

    /**
     * 根据采购订单自动创建应付账款
     * <p>
     * 由采购完成事件触发，自动生成对应的应付账款记录。
     * 业务单号设置为采购订单号，用于后续追溯关联。
     * </p>
     *
     * @param tenantId        租户ID
     * @param purchaseOrderId 采购订单ID
     * @param purchaseOrderNo 采购订单号（作为业务单号）
     * @param supplierId      供应商ID
     * @param amount          应付金额
     * @param operatorId      操作人ID
     */
    @Override
    @Transactional
    public void createPayableFromPurchase(Long tenantId, Long purchaseOrderId,
            String purchaseOrderNo, Long supplierId, BigDecimal amount, Long operatorId) {
        AccountPayable payable = new AccountPayable();
        payable.setTenantId(tenantId);
        payable.setBizNo(purchaseOrderNo);
        payable.setSupplierId(supplierId);
        payable.setAmount(amount);
        payable.setPaidAmount(BigDecimal.ZERO);
        payable.setUnpaidAmount(amount);
        payable.setStatus(0);
        payable.setCreatedBy(operatorId);
        save(payable);
        log.info("自动创建应付账款: orderNo={}, amount={}", purchaseOrderNo, amount);
    }
}
