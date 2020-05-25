package io.Mauzo.Server.Templates;

import java.util.Date;

/**
 * Modelo de venta con atributos iguales a la base de datos.
 * 
 * @author Neirth Sergio Martínez
 */
public class Sale {
    private Integer id;
    private Date stampRef;
    private Integer userId;
    private Integer prodId;
    private Integer discId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getStampRef() {
        return stampRef;
    }

    public void setStampRef(Date stampRef) {
        this.stampRef = stampRef;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getProdId() {
        return prodId;
    }

    public void setProdId(Integer prodId) {
        this.prodId = prodId;
    }

    public Integer getDiscId() {
        return discId;
    }

    public void setDiscId(Integer discId) {
        this.discId = discId;
    }
}