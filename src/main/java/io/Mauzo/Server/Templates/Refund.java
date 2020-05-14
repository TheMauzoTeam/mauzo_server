package io.Mauzo.Server.Templates;

import java.util.Date;

public class Refund {

    private Integer id;
    private Date dateRefund;
    private Integer userId;
    private Integer saleId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getDateRefund() {
        return dateRefund;
    }

    public void setDateRefund(Date dateRefund) {
        this.dateRefund = dateRefund;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getSaleId() {
        return saleId;
    }

    public void setSaleId(Integer saleId) {
        this.saleId = saleId;
    }
}