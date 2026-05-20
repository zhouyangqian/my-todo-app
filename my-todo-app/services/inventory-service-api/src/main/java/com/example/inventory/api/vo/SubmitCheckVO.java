package com.example.inventory.api.vo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 提交盘点结果请求VO
 */
@Data
public class SubmitCheckVO {

    @NotEmpty(message = "盘点明细不能为空")
    @Valid
    private List<CheckItemSubmit> items;

    @Data
    public static class CheckItemSubmit {
        @NotNull(message = "明细ID不能为空")
        private Long itemId;

        @NotNull(message = "实盘数量不能为空")
        private BigDecimal actualQuantity;

        private String remark;
    }
}
