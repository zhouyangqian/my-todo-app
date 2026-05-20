package com.example.inventory.api.vo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量入库请求VO
 */
@Data
public class BatchInboundVO {

    @NotEmpty(message = "入库明细不能为空")
    @Valid
    private List<InboundVO> items;
}
