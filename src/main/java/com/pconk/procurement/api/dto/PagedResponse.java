package com.pconk.procurement.api.dto;

import java.util.List;

public class PagedResponse<T> {
    public List<T> data;
    public long totalCount;
    public int page;
    public int size;
    public long totalPages;
    public boolean isLastPage;

    public PagedResponse(List<T> data, long totalCount, int page, int size) {
        this.data = data;
        this.totalCount = totalCount;
        this.page = page;
        this.size = size;
        this.totalPages = size == 0 ? 0 : (long) Math.ceil((double) totalCount / size);
        this.isLastPage = size == 0 || (long) (page + 1) * size >= totalCount;
    }
}