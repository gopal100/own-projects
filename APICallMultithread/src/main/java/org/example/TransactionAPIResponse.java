package org.example;

import com.google.gson.annotations.SerializedName;

public class TransactionAPIResponse {
    private Integer page;
    @SerializedName("per_page")
    private Integer perPage;
    private Integer total;
    @SerializedName("total_pages")
    private Integer totalPages;
    private TransactionData[] data;

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPerPage() {
        return perPage;
    }

    public void setPerPage(Integer perPage) {
        this.perPage = perPage;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public TransactionData[] getData() {
        return data;
    }

    public void setData(TransactionData[] data) {
        this.data = data;
    }
}
