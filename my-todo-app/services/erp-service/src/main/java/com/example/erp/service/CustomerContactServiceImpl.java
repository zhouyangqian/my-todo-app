package com.example.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.erp.entity.CustomerContact;
import com.example.erp.mapper.CustomerContactMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 客户联系人服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerContactServiceImpl extends ServiceImpl<CustomerContactMapper, CustomerContact> implements CustomerContactService {

    @Override
    public List<CustomerContact> getContactsByCustomerId(Long customerId) {
        LambdaQueryWrapper<CustomerContact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerContact::getCustomerId, customerId)
               .orderByDesc(CustomerContact::getIsDefault)
               .orderByDesc(CustomerContact::getCreatedAt);
        return list(wrapper);
    }

    @Transactional
    @Override
    public CustomerContact createContact(CustomerContact contact) {
        if (contact.getIsDefault() == null) {
            contact.setIsDefault(0);
        }
        if (contact.getIsDefault() == 1) {
            clearDefaultContact(contact.getCustomerId());
        }
        save(contact);
        log.info("创建客户联系人: customerId={}, name={}", contact.getCustomerId(), contact.getName());
        return contact;
    }

    @Transactional
    @Override
    public CustomerContact updateContact(CustomerContact contact) {
        if (contact.getIsDefault() != null && contact.getIsDefault() == 1) {
            clearDefaultContact(contact.getCustomerId());
        }
        updateById(contact);
        log.info("更新客户联系人: id={}", contact.getId());
        return contact;
    }

    @Transactional
    @Override
    public void deleteContact(Long id) {
        CustomerContact contact = getById(id);
        if (contact != null) {
            contact.setDeleted(1);
            updateById(contact);
            log.info("删除客户联系人: id={}", id);
        }
    }

    @Transactional
    @Override
    public void setDefaultContact(Long id) {
        CustomerContact contact = getById(id);
        if (contact == null) {
            throw new IllegalArgumentException("联系人不存在: " + id);
        }
        clearDefaultContact(contact.getCustomerId());
        contact.setIsDefault(1);
        updateById(contact);
        log.info("设置客户默认联系人: customerId={}, contactId={}", contact.getCustomerId(), id);
    }

    private void clearDefaultContact(Long customerId) {
        LambdaUpdateWrapper<CustomerContact> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CustomerContact::getCustomerId, customerId)
                     .eq(CustomerContact::getIsDefault, 1)
                     .set(CustomerContact::getIsDefault, 0);
        update(updateWrapper);
    }
}
