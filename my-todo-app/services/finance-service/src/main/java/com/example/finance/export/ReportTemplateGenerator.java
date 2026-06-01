package com.example.finance.export;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.util.*;

/**
 * 财务报表模板生成器
 * 使用 EasyExcel 生成标准化的报表模板文件
 */
@Slf4j
@Component
public class ReportTemplateGenerator {

    /**
     * 生成资产负债表模板
     */
    public void generateBalanceSheetTemplate(OutputStream out) {
        List<List<String>> headers = Arrays.asList(
            Collections.singletonList("项目"),
            Collections.singletonList("行次"),
            Collections.singletonList("期末余额"),
            Collections.singletonList("期初余额")
        );

        List<List<Object>> data = new ArrayList<>();
        data.add(Arrays.asList("流动资产：", "", "", ""));
        data.add(Arrays.asList("  货币资金", "1", "", ""));
        data.add(Arrays.asList("  应收账款", "2", "", ""));
        data.add(Arrays.asList("  预付款项", "3", "", ""));
        data.add(Arrays.asList("  存货", "4", "", ""));
        data.add(Arrays.asList("流动资产合计", "5", "", ""));
        data.add(Arrays.asList("非流动资产：", "", "", ""));
        data.add(Arrays.asList("  固定资产", "6", "", ""));
        data.add(Arrays.asList("  无形资产", "7", "", ""));
        data.add(Arrays.asList("非流动资产合计", "8", "", ""));
        data.add(Arrays.asList("资产总计", "9", "", ""));
        data.add(Arrays.asList("", "", "", ""));
        data.add(Arrays.asList("流动负债：", "", "", ""));
        data.add(Arrays.asList("  应付账款", "10", "", ""));
        data.add(Arrays.asList("  预收款项", "11", "", ""));
        data.add(Arrays.asList("流动负债合计", "12", "", ""));
        data.add(Arrays.asList("非流动负债：", "", "", ""));
        data.add(Arrays.asList("非流动负债合计", "13", "", ""));
        data.add(Arrays.asList("负债合计", "14", "", ""));
        data.add(Arrays.asList("", "", "", ""));
        data.add(Arrays.asList("所有者权益：", "", "", ""));
        data.add(Arrays.asList("  实收资本", "15", "", ""));
        data.add(Arrays.asList("  未分配利润", "16", "", ""));
        data.add(Arrays.asList("所有者权益合计", "17", "", ""));
        data.add(Arrays.asList("负债和所有者权益总计", "18", "", ""));

        EasyExcel.write(out)
            .head(headers)
            .sheet("资产负债表")
            .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
            .doWrite(data);
        log.info("资产负债表模板生成完成");
    }

    /**
     * 生成利润表模板
     */
    public void generateIncomeStatementTemplate(OutputStream out) {
        List<List<String>> headers = Arrays.asList(
            Collections.singletonList("项目"),
            Collections.singletonList("行次"),
            Collections.singletonList("本月金额"),
            Collections.singletonList("本年累计金额")
        );

        List<List<Object>> data = new ArrayList<>();
        data.add(Arrays.asList("一、营业收入", "1", "", ""));
        data.add(Arrays.asList("  减：营业成本", "2", "", ""));
        data.add(Arrays.asList("      税金及附加", "3", "", ""));
        data.add(Arrays.asList("      销售费用", "4", "", ""));
        data.add(Arrays.asList("      管理费用", "5", "", ""));
        data.add(Arrays.asList("      财务费用", "6", "", ""));
        data.add(Arrays.asList("  加：其他收益", "7", "", ""));
        data.add(Arrays.asList("二、营业利润", "8", "", ""));
        data.add(Arrays.asList("  加：营业外收入", "9", "", ""));
        data.add(Arrays.asList("  减：营业外支出", "10", "", ""));
        data.add(Arrays.asList("三、利润总额", "11", "", ""));
        data.add(Arrays.asList("  减：所得税费用", "12", "", ""));
        data.add(Arrays.asList("四、净利润", "13", "", ""));

        EasyExcel.write(out)
            .head(headers)
            .sheet("利润表")
            .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
            .doWrite(data);
        log.info("利润表模板生成完成");
    }

    /**
     * 生成现金流量表模板
     */
    public void generateCashFlowTemplate(OutputStream out) {
        List<List<String>> headers = Arrays.asList(
            Collections.singletonList("项目"),
            Collections.singletonList("行次"),
            Collections.singletonList("本期金额"),
            Collections.singletonList("上期金额")
        );

        List<List<Object>> data = new ArrayList<>();
        data.add(Arrays.asList("一、经营活动产生的现金流量：", "", "", ""));
        data.add(Arrays.asList("  销售商品、提供劳务收到的现金", "1", "", ""));
        data.add(Arrays.asList("  收到的税费返还", "2", "", ""));
        data.add(Arrays.asList("  收到其他与经营活动有关的现金", "3", "", ""));
        data.add(Arrays.asList("  经营活动现金流入小计", "4", "", ""));
        data.add(Arrays.asList("  购买商品、接受劳务支付的现金", "5", "", ""));
        data.add(Arrays.asList("  支付给职工以及为职工支付的现金", "6", "", ""));
        data.add(Arrays.asList("  支付的各项税费", "7", "", ""));
        data.add(Arrays.asList("  支付其他与经营活动有关的现金", "8", "", ""));
        data.add(Arrays.asList("  经营活动现金流出小计", "9", "", ""));
        data.add(Arrays.asList("经营活动产生的现金流量净额", "10", "", ""));
        data.add(Arrays.asList("", "", "", ""));
        data.add(Arrays.asList("二、投资活动产生的现金流量：", "", "", ""));
        data.add(Arrays.asList("  投资活动现金流入小计", "11", "", ""));
        data.add(Arrays.asList("  投资活动现金流出小计", "12", "", ""));
        data.add(Arrays.asList("投资活动产生的现金流量净额", "13", "", ""));
        data.add(Arrays.asList("", "", "", ""));
        data.add(Arrays.asList("三、筹资活动产生的现金流量：", "", "", ""));
        data.add(Arrays.asList("  筹资活动现金流入小计", "14", "", ""));
        data.add(Arrays.asList("  筹资活动现金流出小计", "15", "", ""));
        data.add(Arrays.asList("筹资活动产生的现金流量净额", "16", "", ""));
        data.add(Arrays.asList("", "", "", ""));
        data.add(Arrays.asList("四、汇率变动对现金的影响", "17", "", ""));
        data.add(Arrays.asList("五、现金及现金等价物净增加额", "18", "", ""));

        EasyExcel.write(out)
            .head(headers)
            .sheet("现金流量表")
            .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
            .doWrite(data);
        log.info("现金流量表模板生成完成");
    }
}
