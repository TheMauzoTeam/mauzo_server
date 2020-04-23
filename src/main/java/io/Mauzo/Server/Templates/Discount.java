package io.Mauzo.Server.Templates;

public class Discount {
    private int id;
    private String code;
    private String desc;
    private float priceDisc;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() { return code; }

    public void setCode(String code) { this.code = code; }

    public String getDesc() { return desc; }

    public void setDesc(String desc) { this.desc = desc; }

    public float getPrizeDisc() {
        return priceDisc;
    }

    public void setPriceDisc(float priceDisc) { this.priceDisc = priceDisc; }
}