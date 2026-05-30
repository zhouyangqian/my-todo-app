package com.example.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.finance.entity.AccountReceivable;
import com.example.finance.mapper.AccountReceivableMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 应收账款业务服务类
 * <p>
 * 继承 MyBatis-Plus 的 ServiceImpl，提供应收账款的完整业务逻辑，
 * 包括分页查询、创建、收款确认、客户应收总额统计和逾期应收查询。
 * 所有数据库写操作均使用 @Transactional 注解保证事务一致性。
 * </p>
 *
 * @author finance-team
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountReceivableServiceImpl extends ServiceImpl<AccountReceivableMapper, AccountReceivable> implements AccountReceivableService {

    /**
     * 分页查询应收账款列表
     *
     * @param tenantId   租户ID，用于多租户数据隔离
     * @param page       当前页码（从1开始）
     * @param size       每页记录数
     * @param customerId 客户ID（可选），传入时仅查询该客户的应收记录
     * @param status     结算状态（可选），0-未结算, 1-部分结算, 2-已结算
     * @return 分页结果对象，包含符合条件的应收账款列表及分页信息
     */
    @Override
    public Page<AccountReceivable> getPage(Long tenantId, int page, int size,
                                            Long customerId, Integer status) {
        // 构建查询条件：限定租户 + 未删除
        LambdaQueryWrapper<AccountReceivable> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccountReceivable::getTenantId, tenantId)
               .eq(AccountReceivable::getDeleted, 0);
        // 可选过滤：按客户ID筛选
        if (customerId != null) {
            wrapper.eq(AccountReceivable::getCustomerId, customerId);
        }
        // 可选过滤：按结算状态筛选
        if (status != null) {
            wrapper.eq(AccountReceivable::getStatus, status);
        }
        // 按创建时间倒序排列，最新的记录排在前面
        wrapper.orderByDesc(AccountReceivable::getCreatedAt);
        return page(new Page<>(page, size), wrapper);
    }

    /**
     * 创建应收账款记录
     * <p>
     * 新建时自动初始化：已收金额设为0，未收金额设为应收金额，状态设为"未结算"。
     * </p>
     *
     * @param receivable 应收账款实体对象（需包含 tenantId, customerId, amount, bizNo 等字段）
     * @return 创建成功后的应收账款对象（含自动生成的ID）
     */
    @Override
    @Transactional
    public AccountReceivable create(AccountReceivable receivable) {
        // 初始化已收金额为0
        receivable.setReceivedAmount(BigDecimal.ZERO);
        // 未收金额等于应收总金额
        receivable.setUnreceivedAmount(receivable.getAmount());
        // 初始状态为未结算
        receivable.setStatus(0);
        save(receivable);
        log.info("创建应收账款: 客户ID={}, 金额={}", receivable.getCustomerId(), receivable.getAmount());
        return receivable;
    }

    /**
     * 应收账款收款操作
     * <p>
     * 收款后自动更新已收金额和未收金额，并根据收款情况更新结算状态：
     * 未收金额 <= 0 时变为"已结算"，已收金额 > 0 且未收金额 > 0 时变为"部分结算"。
     * </p>
     *
     * @param id     应收账款记录ID
     * @param amount 本次收款金额
     * @throws IllegalArgumentException 当应收账款记录不存在时抛出
     */
    @Override
    @Transactional
    public void receivePayment(Long id, BigDecimal amount) {
        AccountReceivable receivable = getById(id);
        if (receivable == null) {
            throw new IllegalArgumentException("应收账款不存在");
        }

        // 计算新的已收金额 = 原已收金额 + 本次收款金额
        BigDecimal newReceivedAmount = receivable.getReceivedAmount().add(amount);
        // 计算新的未收金额 = 应收总金额 - 新已收金额
        BigDecimal newUnreceivedAmount = receivable.getAmount().subtract(newReceivedAmount);

        receivable.setReceivedAmount(newReceivedAmount);
        // 未收金额不允许为负数，取0和计算值的较大者
        receivable.setUnreceivedAmount(newUnreceivedAmount.max(BigDecimal.ZERO));

        // 根据未收金额判断并更新结算状态
        if (newUnreceivedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            receivable.setStatus(2); // 已结算：全部收回
        } else if (newReceivedAmount.compareTo(BigDecimal.ZERO) > 0) {
            receivable.setStatus(1); // 部分结算：已收部分款项
        }

        updateById(receivable);
        log.info("应收账款收款: ID={}, 金额={}", id, amount);
    }

    /**
     * 获取指定客户的应收总额（仅统计未结算和部分结算的记录）
     *
     * @param customerId 客户ID
     * @param tenantId   租户ID
     * @return 该客户所有未结清应收账款的未收金额之和
     */
    @Override
    public BigDecimal getTotalReceivable(Long customerId, Long tenantId) {
        // 查询该客户下所有未结算(0)和部分结算(1)的应收记录
        List<AccountReceivable> list = list(
            new LambdaQueryWrapper<AccountReceivable>()
                .eq(AccountReceivable::getTenantId, tenantId)
                .eq(AccountReceivable::getCustomerId, customerId)
                .in(AccountReceivable::getStatus, 0, 1)
                .eq(AccountReceivable::getDeleted, 0)
        );
        // 使用流式计算汇总所有未收金额
        return list.stream()
                .map(AccountReceivable::getUnreceivedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 获取逾期应收账款列表
     * <p>
     * 查询条件：状态为未结算或部分结算，且到期日期早于当前时间。
     * 用于逾期催收提醒和账龄分析。
     * </p>
     *
     * @param tenantId 租户ID
     * @return 逾期未收回的应收账款列表
     */
    @Override
    public List<AccountReceivable> getOverdueList(Long tenantId) {
        return list(
            new LambdaQueryWrapper<AccountReceivable>()
                .eq(AccountReceivable::getTenantId, tenantId)
                // 仅查询未结算和部分结算的记录
                .in(AccountReceivable::getStatus, 0, 1)
                // 到期日期早于当前时间即为逾期
                .lt(AccountReceivable::getDueDate, LocalDateTime.now())
                .eq(AccountReceivable::getDeleted, 0)
        );
    }

    /**
     * 根据销售订单自动创建应收账款
     * <p>
     * 由销售完成事件触发，自动生成对应的应收账款记录。
     * 业务单号设置为销售订单号，用于后续追溯关联。
     * </p>
     *
     * @param tenantId      租户ID
     * @param salesOrderId  销售订单ID
     * @param salesOrderNo  销售订单号（作为业务单号）
     * @param customerId    客户ID
     * @param amount        应收金额
     * @param operatorId    操作人ID
     */
    @Override
    @Transactional
    public void createReceivableFromSales(Long tenantId, Long salesOrderId,
            String salesOrderNo, Long customerId, BigDecimal amount, Long operatorId) {
        AccountReceivable receivable = new AccountReceivable();
        receivable.setTenantId(tenantId);
        receivable.setBizNo(salesOrderNo);
        receivable.setCustomerId(customerId);
        receivable.setAmount(amount);
        receivable.setReceivedAmount(BigDecimal.ZERO);
        receivable.setUnreceivedAmount(amount);
        receivable.setStatus(0);
        receivable.setCreatedBy(operatorId);
        save(receivable);
        log.info("自动创建应收账款: orderNo={}, amount={}", salesOrderNo, amount);
    }
}
