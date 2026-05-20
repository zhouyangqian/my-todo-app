package com.example.inventory.api.vo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量出库请求VO
 */
@Data
public class BatchOutboundVO {

    @NotEmpty(message = "出库明细不能为空")
    @Valid
    private List<OutboundVO> items;
}
