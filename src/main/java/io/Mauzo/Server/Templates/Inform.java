/*
 * MIT License
 *
 * Copyright (c) 2020 The Mauzo Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.Mauzo.Server.Templates;

import java.sql.Date;

/**
 * Modelo de informe con atributos iguales a la base de datos.
 *
 * @author Ant04X Antonio Izquierdo
 */
public class Inform {

    // FIXME: 08/06/2020 Normalmente el id es un Long
    private Integer id;
    private Integer nSales;
    private Integer nRefunds;
    private Integer nDiscounts;

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