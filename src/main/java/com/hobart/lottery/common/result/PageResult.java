package com.hobart.lottery.common.result;

import lombok.Data;

import java.util.List;

/**
 * 分页结果封装
 */
@Data
public class PageResult<T> {
    private List<T> records;
    private long total;
    private int size;
    private int current;
    private int pages;

    public PageResult() {}

    public PageResult(List<T> records, long total, int size, int current) {
        this.records = records;
        this.total = total;
        this.size = size;
        this.current = current;
        this.pages = (int) Math.ceil((double) total / size);
    }
}
