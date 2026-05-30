package com.example.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.core.exception.BusinessException;
import com.example.erp.api.vo.CreateSalesQuotationVO;
import com.example.erp.api.dto.SalesQuotationDTO;
import com.example.erp.api.dto.SalesQuotationItemDTO;
import com.example.erp.entity.Customer;
import com.example.erp.entity.Product;
import com.example.erp.entity.SalesQuotation;
import com.example.erp.entity.SalesQuotationItem;
import com.example.erp.entity.SalesOrder;
import com.example.erp.entity.SalesOrderItem;
import com.example.erp.mapper.SalesQuotationItemMapper;
import com.example.erp.mapper.SalesQuotationMapper;
import com.example.erp.mapper.SalesOrderItemMapper;
import com.example.erp.mapper.SalesOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 销售报价单服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalesQuotationServiceImpl extends ServiceImpl<SalesQuotationMapper, SalesQuotation> implements SalesQuotationService {

    private final SalesQuotationItemMapper salesQuotationItemMapper;
    private final SalesOrderMapper salesOrderMapper;
    private final SalesOrderItemMapper salesOrderItemMapper;
    private final CustomerService customerService;
    private final ProductService productService;

    @Override
    public Page<SalesQuotationDTO> getQuotationPage(Long tenantId, int page, int size,
                                                     Long customerId, Integer status,
                                                     String startDate, String endDate) {
        LambdaQueryWrapper<SalesQuotation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SalesQuotation::getTenantId, tenantId)
               .eq(SalesQuotation::getDeleted, 0);
        if (customerId != null) {
            wrapper.eq(SalesQuotation::getCustomerId, customerId);
        }
        if (status != null) {
            wrapper.eq(SalesQuotation::getStatus, status);
        }
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(SalesQuotation::getQuotationDate, LocalDate.parse(startDate));
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(SalesQuotation::getQuotationDate, LocalDate.parse(endDate));
        }
        wrapper.orderByDesc(SalesQuotation::getCreatedAt);

        Page<SalesQuotation> result = page(new Page<>(page, size), wrapper);

        Page<SalesQuotationDTO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<SalesQuotationDTO> voList = result.getRecords().stream().map(this::convertToDTO).collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public SalesQuotationDTO getQuotationById(Long quotationId) {
        SalesQuotation quotation = getById(quotationId);
        if (quotation == null) {
            throw new BusinessException("报价单不存在");
        }
        SalesQuotationDTO dto = convertToDTO(quotation);

        List<SalesQuotationItem> items = salesQuotationItemMapper.selectList(
            new LambdaQueryWrapper<SalesQuotationItem>()
                .eq(SalesQuotationItem::getQuotationId, quotationId)
                .eq(SalesQuotationItem::getDeleted, 0)
        );
        dto.setItems(items.stream().map(this::convertItemToDTO).collect(Collectors.toList()));
        return dto;
    }

    @Transactional
    @Override
    public SalesQuotation createQuotation(CreateSalesQuotationVO request, Long tenantId) {
        Customer customer = customerService.getById(request.getCustomerId());
        if (customer == null || customer.getDeleted() == 1) {
            throw new BusinessException("客户不存在或已停用");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CreateSalesQuotationVO.QuotationItemRequest itemReq : request.getItems()) {
            Product product = productService.getById(itemReq.getProductId());
            if (product == null || product.getStatus() == 0) {
                throw new BusinessException("商品不存在或已停用: " + itemReq.getProductId());
            }
            BigDecimal discountRate = itemReq.getDiscountRate() != null ? itemReq.getDiscountRate() : BigDecimal.valueOf(100);
            BigDecimal lineAmount = itemReq.getQuantity()
                .multiply(itemReq.getUnitPrice())
                .multiply(discountRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            totalAmount = totalAmount.add(lineAmount);
        }

        SalesQuotation quotation = new SalesQuotation();
        quotation.setTenantId(tenantId);
        quotation.setQuotationNo(generateQuotationNo(tenantId));
        quotation.setCustomerId(request.getCustomerId());
        quotation.setCustomerName(customer.getCustomerName());
        quotation.setQuotationDate(request.getQuotationDate() != null ? request.getQuotationDate() : LocalDate.now());
        quotation.setValidUntil(request.getValidUntil());
        quotation.setTotalAmount(totalAmount);
        quotation.setStatus(0);
        quotation.setRemark(request.getRemark());
        save(quotation);

        for (CreateSalesQuotationVO.QuotationItemRequest itemReq : request.getItems()) {
            Product product = productService.getById(itemReq.getProductId());
            BigDecimal discountRate = itemReq.getDiscountRate() != null ? itemReq.getDiscountRate() : BigDecimal.valueOf(100);
            BigDecimal lineAmount = itemReq.getQuantity()
                .multiply(itemReq.getUnitPrice())
                .multiply(discountRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            SalesQuotationItem item = new SalesQuotationItem();
            item.setTenantId(tenantId);
            item.setQuotationId(quotation.getId());
            item.setProductId(product.getId());
            item.setProductCode(product.getProductCode());
            item.setProductName(product.getProductName());
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(itemReq.getUnitPrice());
            item.setDiscountRate(discountRate);
            item.setAmount(lineAmount);
            item.setRemark(itemReq.getRemark());
            salesQuotationItemMapper.insert(item);
        }

        log.info("创建销售报价单: {}", quotation.getQuotationNo());
        return quotation;
    }

    @Transactional
    @Override
    public SalesQuotation updateQuotation(Long quotationId, CreateSalesQuotationVO request) {
        SalesQuotation quotation = getById(quotationId);
        if (quotation == null) {
            throw new BusinessException("报价单不存在");
        }
        if (quotation.getStatus() != 0) {
            throw new BusinessException("只有草稿状态的报价单可以编辑");
        }

        Customer customer = customerService.getById(request.getCustomerId());
        if (customer == null || customer.getDeleted() == 1) {
            throw new BusinessException("客户不存在或已停用");
        }

        salesQuotationItemMapper.delete(
            new LambdaQueryWrapper<SalesQuotationItem>()
                .eq(SalesQuotationItem::getQuotationId, quotationId)
        );

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CreateSalesQuotationVO.QuotationItemRequest itemReq : request.getItems()) {
            Product product = productService.getById(itemReq.getProductId());
            if (product == null || product.getStatus() == 0) {
                throw new BusinessException("商品不存在或已停用: " + itemReq.getProductId());
            }
            BigDecimal discountRate = itemReq.getDiscountRate() != null ? itemReq.getDiscountRate() : BigDecimal.valueOf(100);
            BigDecimal lineAmount = itemReq.getQuantity()
                .multiply(itemReq.getUnitPrice())
                .multiply(discountRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            totalAmount = totalAmount.add(lineAmount);

            SalesQuotationItem item = new SalesQuotationItem();
            item.setTenantId(quotation.getTenantId());
            item.setQuotationId(quotationId);
            item.setProductId(product.getId());
            item.setProductCode(product.getProductCode());
            item.setProductName(product.getProductName());
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(itemReq.getUnitPrice());
            item.setDiscountRate(discountRate);
            item.setAmount(lineAmount);
            item.setRemark(itemReq.getRemark());
            salesQuotationItemMapper.insert(item);
        }

        quotation.setCustomerId(request.getCustomerId());
        quotation.setCustomerName(customer.getCustomerName());
        quotation.setQuotationDate(request.getQuotationDate());
        quotation.setValidUntil(request.getValidUntil());
        quotation.setTotalAmount(totalAmount);
        quotation.setRemark(request.getRemark());
        updateById(quotation);

        log.info("更新销售报价单: {}", quotation.getQuotationNo());
        return quotation;
    }

    @Transactional
    @Override
    public void sendQuotation(Long quotationId) {
        SalesQuotation quotation = getById(quotationId);
        if (quotation == null) {
            throw new BusinessException("报价单不存在");
        }
        if (quotation.getStatus() != 0) {
            throw new BusinessException("只有草稿状态的报价单可以发送");
        }
        quotation.setStatus(1);
        updateById(quotation);
        log.info("发送销售报价单: {}", quotation.getQuotationNo());
    }

    @Transactional
    @Override
    public void acceptQuotation(Long quotationId) {
        SalesQuotation quotation = getById(quotationId);
        if (quotation == null) {
            throw new BusinessException("报价单不存在");
        }
        if (quotation.getStatus() != 1) {
            throw new BusinessException("只有已发送状态的报价单可以接受");
        }
        quotation.setStatus(2);
        updateById(quotation);
        log.info("接受销售报价单: {}", quotation.getQuotationNo());
    }

    @Transactional
    @Override
    public void rejectQuotation(Long quotationId) {
        SalesQuotation quotation = getById(quotationId);
        if (quotation == null) {
            throw new BusinessException("报价单不存在");
        }
        if (quotation.getStatus() != 1) {
            throw new BusinessException("只有已发送状态的报价单可以拒绝");
        }
        quotation.setStatus(3);
        updateById(quotation);
        log.info("拒绝销售报价单: {}", quotation.getQuotationNo());
    }

    @Transactional
    @Override
    public SalesOrder convertToOrder(Long quotationId, Long warehouseId) {
        SalesQuotation quotation = getById(quotationId);
        if (quotation == null) {
            throw new BusinessException("报价单不存在");
        }
        if (quotation.getStatus() != 2) {
            throw new BusinessException("只有已接受状态的报价单可以转为订单");
        }
        if (quotation.getConvertedOrderId() != null) {
            throw new BusinessException("该报价单已转订单");
        }

        List<SalesQuotationItem> quotationItems = salesQuotationItemMapper.selectList(
            new LambdaQueryWrapper<SalesQuotationItem>()
                .eq(SalesQuotationItem::getQuotationId, quotationId)
                .eq(SalesQuotationItem::getDeleted, 0)
        );

        SalesOrder order = new SalesOrder();
        order.setTenantId(quotation.getTenantId());
        order.setOrderNo(generateOrderNo(quotation.getTenantId()));
        order.setCustomerId(quotation.getCustomerId());
        order.setWarehouseId(warehouseId);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(quotation.getTotalAmount());
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setReceivedAmount(quotation.getTotalAmount());
        order.setDeliveredAmount(BigDecimal.ZERO);
        order.setDeliveredQuantity(BigDecimal.ZERO);
        order.setOrderStatus(0);
        order.setRemark("由报价单 " + quotation.getQuotationNo() + " 转入");
        salesOrderMapper.insert(order);

        for (SalesQuotationItem qi : quotationItems) {
            SalesOrderItem item = new SalesOrderItem();
            item.setTenantId(quotation.getTenantId());
            item.setOrderId(order.getId());
            item.setProductId(qi.getProductId());
            item.setProductCode(qi.getProductCode());
            item.setProductName(qi.getProductName());
            item.setQuantity(qi.getQuantity());
            item.setPrice(qi.getUnitPrice());
            item.setDiscountAmount(BigDecimal.ZERO);
            item.setAmount(qi.getAmount());
            item.setDeliveredQuantity(BigDecimal.ZERO);
            item.setDeliveredAmount(BigDecimal.ZERO);
            salesOrderItemMapper.insert(item);
        }

        quotation.setStatus(5);
        quotation.setConvertedOrderId(order.getId());
        updateById(quotation);

        log.info("报价单 {} 转为销售订单 {}", quotation.getQuotationNo(), order.getOrderNo());
        return order;
    }

    private String generateQuotationNo(Long tenantId) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "SQ" + dateStr;

        Long count = lambdaQuery()
            .eq(SalesQuotation::getTenantId, tenantId)
            .likeRight(SalesQuotation::getQuotationNo, prefix)
            .count();

        String seqNo = String.format("%04d", count + 1);
        return prefix + seqNo;
    }

    private String generateOrderNo(Long tenantId) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "SO" + dateStr;

        Long count = salesOrderMapper.selectCount(
            new LambdaQueryWrapper<SalesOrder>()
                .eq(SalesOrder::getTenantId, tenantId)
                .likeRight(SalesOrder::getOrderNo, prefix)
        );

        String seqNo = String.format("%04d", count + 1);
        return prefix + seqNo;
    }

    private SalesQuotationDTO convertToDTO(SalesQuotation quotation) {
        SalesQuotationDTO dto = new SalesQuotationDTO();
        BeanUtils.copyProperties(quotation, dto);
        dto.setStatusText(getStatusText(quotation.getStatus()));
        return dto;
    }

    private SalesQuotationItemDTO convertItemToDTO(SalesQuotationItem item) {
        SalesQuotationItemDTO dto = new SalesQuotationItemDTO();
        BeanUtils.copyProperties(item, dto);
        return dto;
    }

    private String getStatusText(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "草稿";
            case 1 -> "已发送";
            case 2 -> "已接受";
            case 3 -> "已拒绝";
            case 4 -> "已过期";
            case 5 -> "已转订单";
            default -> "未知";
        };
    }
}
