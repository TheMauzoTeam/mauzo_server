package io.Mauzo.Server.Templates;

import java.util.Date;

public class Sale {
    private int id;
    private Date stampRef;
    private int userId;
    private int prodId;
    private int discId;
    private int refundId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getStampRef() {
        return stampRef;
    }

    public void setStampRef(Date stampRef) {
        this.stampRef = stampRef;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getProdId() {
        return prodId;
    }

    public void setProdId(int prodId) {
        this.prodId = prodId;
    }

    public int getDiscId() {
        return discId;
    }

    public void setDiscId(int discId) {
        this.discId = discId;
    }

    public int getRefundId() {
        return refundId;
    }

    public void setRefundId(int refundId) {
        this.refundId = refundId;
    }
}