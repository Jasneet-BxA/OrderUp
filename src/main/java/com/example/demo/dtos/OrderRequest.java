package com.example.demo.dtos;

public class OrderRequest {
    private int productId;
    private String customerName;

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {


        this.customerName = customerName;
    }

    @Override
    public String toString() {
        return "OrderRequest{" +
                "productId=" + productId +
                ", customerName='" + customerName + '\'' +
                '}';
    }
}
