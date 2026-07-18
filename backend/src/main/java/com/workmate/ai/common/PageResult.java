package com.workmate.ai.common;

import java.util.List;

public class PageResult<T> {

    private List<T> records;
    private Long pageNum;
    private Long pageSize;
    private Long total;
    private Long pages;

    public PageResult(List<T> records, Long pageNum, Long pageSize, Long total, Long pages) {
        this.records = records;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.pages = pages;
    }

    public List<T> getRecords() { return records; }
    public Long getPageNum() { return pageNum; }
    public Long getPageSize() { return pageSize; }
    public Long getTotal() { return total; }
    public Long getPages() { return pages; }
}