package com.example.finance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.finance.entity.BankAccount;

import java.math.BigDecimal;
import java.util.List;

/**
 * 银行账户业务服务接口
 *
 * @author finance-team
 * @since 1.0
 */
public interface BankAccountService extends IService<BankAccount> {

    Page<BankAccount> getPage(Long tenantId, int page, int size,
                               String accountName, Integer accountType);

    List<BankAccount> getAllAccounts(Long tenantId);

    BankAccount getDefaultAccount(Long tenantId);

    BankAccount create(BankAccount account);

    BankAccount update(BankAccount account);

    void adjustBalance(Long accountId, BigDecimal amount, boolean isAdd);

    void delete(Long id);
}
