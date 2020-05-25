package io.Mauzo.Server.Templates;


import java.sql.Date;
import java.util.HashMap;

/**
 * @author Ant04X Antonio Izquierdo
 */
public class Inform {

    private Integer id;
    private Integer nSales;
    private Integer nRefunds;
    private Integer nDiscounts;
    private HashMap<User, Integer> userSales = new HashMap<>();

    private Date dStart;
    private Date dEnd;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getnSales() {
        return nSales;
    }

    public void setnSales(Integer nSales) {
        this.nSales = nSales;
    }

    public Integer getnRefunds() {
        return nRefunds;
    }

    public void setnRefunds(Integer nRefunds) {
        this.nRefunds = nRefunds;
    }

    public Integer getnDiscounts() {
        return nDiscounts;
    }

    public void setnDiscounts(Integer nDiscounts) {
        this.nDiscounts = nDiscounts;
    }

    public HashMap<User, Integer> getUserSales() {
        return userSales;
    }

    public void addUserSales(User user, Integer salesCount) {
        userSales.put(user, salesCount);
    }

    public void deleteUserSales(User user) {
        userSales.remove(user);
    }

    public Date getdStart() {
        return dStart;
    }

    public void setdStart(Date dStart) {
        this.dStart = dStart;
    }

    public Date getdEnd() {
        return dEnd;
    }

    public void setdEnd(Date dEnd) {
        this.dEnd = dEnd;
    }
}