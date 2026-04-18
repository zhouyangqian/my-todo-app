package com.example.common.core.result;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页查询结果封装类
 * <p>
 * 封装分页查询的返回数据，包含数据列表、总记录数、当前页码、每页大小和总页数。
 * </p>
 *
 * @param <T> 分页数据的泛型类型
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前页数据列表 */
    private List<T> records;

    /** 总记录数 */
    private Long total;

    /** 当前页码（从1开始） */
    private Long current;

    /** 每页大小 */
    private Long size;

    /** 总页数 */
    private Long pages;

    /** 默认构造函数，初始化空分页结果 */
    public PageResult() {
        this.records = Collections.emptyList();
        this.total = 0L;
        this.current = 1L;
        this.size = 10L;
        this.pages = 0L;
    }

    /**
     * 全参构造函数，自动计算总页数
     *
     * @param records 当前页数据列表
     * @param total   总记录数
     * @param current 当前页码
     * @param size    每页大小
     */
    public PageResult(List<T> records, Long total, Long current, Long size) {
        this.records = records;
        this.total = total;
        this.current = current;
        this.size = size;
        this.pages = (total + size - 1) / size; // 向上取整计算总页数
    }

    /**
     * 静态工厂方法，快速创建分页结果
     *
     * @param records 当前页数据列表
     * @param total   总记录数
     * @param current 当前页码
     * @param size    每页大小
     * @return PageResult 实例
     */
    public static <T> PageResult<T> of(List<T> records, Long total, Long current, Long size) {
        return new PageResult<>(records, total, current, size);
    }
}
