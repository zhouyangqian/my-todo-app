package com.example.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.erp.entity.SupplierContact;
import com.example.erp.mapper.SupplierContactMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 供应商联系人服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierContactServiceImpl extends ServiceImpl<SupplierContactMapper, SupplierContact> implements SupplierContactService {

    @Override
    public List<SupplierContact> getContactsBySupplierId(Long supplierId) {
        LambdaQueryWrapper<SupplierContact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SupplierContact::getSupplierId, supplierId)
               .orderByDesc(SupplierContact::getIsDefault)
               .orderByDesc(SupplierContact::getCreatedAt);
        return list(wrapper);
    }

    @Transactional
    @Override
    public SupplierContact createContact(SupplierContact contact) {
        if (contact.getIsDefault() == null) {
            contact.setIsDefault(0);
        }
        if (contact.getIsDefault() == 1) {
            clearDefaultContact(contact.getSupplierId());
        }
        save(contact);
        log.info("创建供应商联系人: supplierId={}, name={}", contact.getSupplierId(), contact.getName());
        return contact;
    }

    @Transactional
    @Override
    public SupplierContact updateContact(SupplierContact contact) {
        if (contact.getIsDefault() != null && contact.getIsDefault() == 1) {
            clearDefaultContact(contact.getSupplierId());
        }
        updateById(contact);
        log.info("更新供应商联系人: id={}", contact.getId());
        return contact;
    }

    @Transactional
    @Override
    public void deleteContact(Long id) {
        SupplierContact contact = getById(id);
        if (contact != null) {
            contact.setDeleted(1);
            updateById(contact);
            log.info("删除供应商联系人: id={}", id);
        }
    }

    @Transactional
    @Override
    public void setDefaultContact(Long id) {
        SupplierContact contact = getById(id);
        if (contact == null) {
            throw new IllegalArgumentException("联系人不存在: " + id);
        }
        clearDefaultContact(contact.getSupplierId());
        contact.setIsDefault(1);
        updateById(contact);
        log.info("设置供应商默认联系人: supplierId={}, contactId={}", contact.getSupplierId(), id);
    }

    private void clearDefaultContact(Long supplierId) {
        LambdaUpdateWrapper<SupplierContact> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SupplierContact::getSupplierId, supplierId)
                     .eq(SupplierContact::getIsDefault, 1)
                     .set(SupplierContact::getIsDefault, 0);
        update(updateWrapper);
    }
}
