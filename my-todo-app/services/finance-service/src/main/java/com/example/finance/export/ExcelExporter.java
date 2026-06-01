package com.example.finance.export;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import lombok.extern.slf4j.Slf4j;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 通用 Excel 导出工具类
 * <p>
 * 基于 EasyExcel 实现通用的 Excel 导出功能，支持：
 * <ul>
 *   <li>自定义表头和数据字段映射</li>
 *   <li>自动列宽适配</li>
 *   <li>大数据量自动分 sheet（默认每 sheet 10万行）</li>
 * </ul>
 * </p>
 */
@Slf4j
public class ExcelExporter {

    /** 每个 sheet 最大行数，EasyExcel 默认限制 100 万行，这里设 10 万行以提升性能 */
    private static final int MAX_ROWS_PER_SHEET = 100000;

    /**
     * 导出数据到 Excel
     *
     * @param data     数据列表
     * @param headers  表头名称数组
     * @param fields   对应的字段名称数组（与 headers 一一对应）
     * @param out      输出流
     * @param <T>      数据类型
     */
    public static <T> void export(List<T> data, String[] headers, String[] fields, OutputStream out) {
        if (data == null || data.isEmpty()) {
            writeEmptySheet(headers, out);
            return;
        }

        // 构建表头
        List<List<String>> headList = new ArrayList<>();
        for (String header : headers) {
            List<String> col = new ArrayList<>();
            col.add(header);
            headList.add(col);
        }

        // 构建数据行：通过反射读取字段值
        List<List<Object>> dataList = new ArrayList<>();
        for (T item : data) {
            List<Object> row = new ArrayList<>();
            for (String field : fields) {
                row.add(getFieldValue(item, field));
            }
            dataList.add(row);
        }

        // 分 sheet 写入
        int totalRows = dataList.size();
        int sheetCount = (totalRows + MAX_ROWS_PER_SHEET - 1) / MAX_ROWS_PER_SHEET;

        ExcelWriter excelWriter = EasyExcel.write(out)
                .head(headList)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .build();

        for (int i = 0; i < sheetCount; i++) {
            int fromIndex = i * MAX_ROWS_PER_SHEET;
            int toIndex = Math.min(fromIndex + MAX_ROWS_PER_SHEET, totalRows);
            List<List<Object>> sheetData = dataList.subList(fromIndex, toIndex);
            String sheetName = sheetCount == 1 ? "数据" : "数据_" + (i + 1);
            WriteSheet writeSheet = EasyExcel.writerSheet(i, sheetName).build();
            excelWriter.write(sheetData, writeSheet);
        }

        excelWriter.finish();
        log.info("Excel导出完成: 总行数={}, sheet数={}", totalRows, sheetCount);
    }

    /**
     * 空数据时写出仅含表头的空表
     */
    private static void writeEmptySheet(String[] headers, OutputStream out) {
        List<List<String>> headList = new ArrayList<>();
        for (String header : headers) {
            List<String> col = new ArrayList<>();
            col.add(header);
            headList.add(col);
        }
        EasyExcel.write(out)
                .head(headList)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("数据")
                .doWrite(new ArrayList<>());
    }

    /**
     * 通过反射获取对象字段值
     */
    private static Object getFieldValue(Object obj, String fieldName) {
        try {
            // 支持 nested.field 格式
            if (fieldName.contains(".")) {
                String[] parts = fieldName.split("\\.");
                Object current = obj;
                for (String part : parts) {
                    if (current == null) return "";
                    var field = current.getClass().getDeclaredField(part);
                    field.setAccessible(true);
                    current = field.get(current);
                }
                return current != null ? current : "";
            }

            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(obj);
            return value != null ? value : "";
        } catch (NoSuchFieldException e) {
            // 尝试从父类查找
            return getFieldValueFromSuperclass(obj, fieldName);
        } catch (IllegalAccessException e) {
            log.warn("无法访问字段: {}", fieldName, e);
            return "";
        }
    }

    /**
     * 从父类中查找字段
     */
    private static Object getFieldValueFromSuperclass(Object obj, String fieldName) {
        Class<?> clazz = obj.getClass().getSuperclass();
        while (clazz != null) {
            try {
                var field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(obj);
                return value != null ? value : "";
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (IllegalAccessException e) {
                log.warn("无法访问父类字段: {}", fieldName, e);
                return "";
            }
        }
        return "";
    }
}
