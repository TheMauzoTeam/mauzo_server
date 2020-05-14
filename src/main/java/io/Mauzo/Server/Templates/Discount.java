package io.Mauzo.Server.Templates;

public class Discount {
    private Integer id;
    private String code;
    private String desc;
    private Float priceDisc;

    public int getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() { return code; }

    public void setCode(String code) { this.code = code; }

    public String getDesc() { return desc; }

    public void setDesc(String desc) { this.desc = desc; }

    public float getPrizeDisc() {
        return priceDisc;
    }

    public void setPriceDisc(Float priceDisc) { this.priceDisc = priceDisc; }
}