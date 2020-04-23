package io.Mauzo.Server.Templates;

import java.util.Date;

public class Sale {
    private int id;
    private Date stampRef;
    private int userId;
    private int prodId;
    private int discId;
    private int refundId;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the stampRef
     */
    public Date getStampRef() {
        return stampRef;
    }

    /**
     * @param stampRef the stampRef to set
     */
    public void setStampRef(Date stampRef) {
        this.stampRef = stampRef;
    }

    /**
     * @return the userId
     */
    public int getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * @return the prodId
     */
    public int getProdId() {
        return prodId;
    }

    /**
     * @param prodId the prodId to set
     */
    public void setProdId(int prodId) {
        this.prodId = prodId;
    }

    /**
     * @return the discId
     */
    public int getDiscId() {
        return discId;
    }

    /**
     * @param discId the discId to set
     */
    public void setDiscId(int discId) {
        this.discId = discId;
    }

    /**
     * @return the refundId
     */
    public int getRefundId() {
        return refundId;
    }

    /**
     * @param refundId the refundId to set
     */
    public void setRefundId(int refundId) {
        this.refundId = refundId;
    }
}