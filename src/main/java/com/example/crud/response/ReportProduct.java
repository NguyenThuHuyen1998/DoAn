package com.example.crud.response;

import java.util.Map;

public class ReportProduct {
    private double total;
    private long totalCount;
    private Map<String, Object> products;

    public ReportProduct(double total, int totalCount, Map<String, Object> products) {
        this.total = total;
        this.totalCount = totalCount;
        this.products = products;
    }


    public ReportProduct() {
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public Map<String, Object> getProducts() {
        return products;
    }

    public void setProducts(Map<String, Object> products) {
        this.products = products;
    }
}
