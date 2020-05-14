package io.Mauzo.Server.Templates;

import java.util.Date;

public class Refund {

    private int id;
    private Date dateRefund;
    private int userId;
    private int salesId;
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDateRefund() {
        return dateRefund;
    }

    public void setDateRefund(Date dateRefund) {
        this.dateRefund = dateRefund;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

}