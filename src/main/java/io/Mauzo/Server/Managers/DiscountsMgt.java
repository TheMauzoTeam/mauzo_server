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

package io.Mauzo.Server.Managers;

import io.Mauzo.Server.Templates.Discount;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ant04X Antonio Izquierdo
 */
public class DiscountsMgt implements ManagersIntf<Discount> {
    private final PreparedStatement addQuery;
    private final PreparedStatement getIdQuery;
    private final PreparedStatement getNameQuery;
    private final PreparedStatement getListQuery;
    private final PreparedStatement modifyQuery;
    private final PreparedStatement deleteQuery;

    DiscountsMgt(Connection conn) throws SQLException {
        addQuery = conn.prepareStatement("INSERT INTO Discounts (codeDisc, descDisc, pricePerc) VALUES(?, ?, ?)");
        getIdQuery = conn.prepareStatement("SELECT * FROM Discounts WHERE id = ?;");
        getNameQuery = conn.prepareStatement("SELECT * FROM Discounts WHERE codeDisc = ?;");
        getListQuery = conn.prepareStatement("SELECT * FROM Discounts");
        modifyQuery = conn.prepareStatement("UPDATE Discounts SET codeDisc = ?, descDisc = ?, pricePerc = ? WHERE id = ?;");
        deleteQuery = conn.prepareStatement("DELETE FROM Discounts WHERE id = ?;");
    }

    /**
     * Método para añadir descuentos a la base de datos.
     *
     * @param discount El descuento encapsulado en un objeto.
     * @throws SQLException Excepción en la consulta SQL.
     */
    @Override
    public void add(Discount discount) throws SQLException {
        // Asociamos los valores respecto a la sentencia sql.
        addQuery.setString(1, discount.getCode());
        addQuery.setString(2, discount.getDesc());
        addQuery.setFloat(3, discount.getPrizeDisc());

        // Ejecutamos la sentencia sql.
        addQuery.execute();
    }

    /**
     * Método para obtener en forma de objeto el descuento, a partir de un id de
     * descuento, el descuento encapsulado.
     *
     * @param id El id del descuento.
     * @return El descuento encapsulado en forma de objeto.
     * @throws SQLException Excepción en la consulta SQL.
     * @throws ManagerErrorException Excepción dada al no encontrar el descuento solicitado.
     */
    @Override
    public Discount get(int id) throws SQLException, ManagerErrorException {
        Discount discount = null;

        // Asociamos los valores respecto a la sentencia sal.
        getIdQuery.setInt(1, id);

        // Ejecutamos la sentencia sql y recuperamos lo que nos ha retornado.
        try (ResultSet rs = getIdQuery.executeQuery()) {
            if (rs.next()) {
                discount = new Discount();

                discount.setId(rs.getInt("id"));
                discount.setCode(rs.getString("codeDisc"));
                discount.setDesc(rs.getString("descDisc"));
                discount.setPriceDisc(rs.getFloat("pricePerc"));
            } else
                throw new ManagerErrorException("No se ha encontrado el descuento.");
        }

        return discount;
    }

    public Discount get(String name) throws SQLException, ManagerErrorException {
        Discount discount = null;

        getNameQuery.setString(1, name);

        try (ResultSet rs = getNameQuery.executeQuery()) {
            if (rs.next()) {
                discount = new Discount();

                discount.setId(rs.getInt("id"));
                discount.setCode(rs.getString("codeDisc"));
                discount.setDesc(rs.getString("descDisc"));
                discount.setPriceDisc(rs.getFloat("pricePerc"));
            } else
                throw new ManagerErrorException("No se ha encontrado el descuento");
        }

        return discount;
    }

    /**
     * Método para obtener en forma de lista de descuentos, los descuentos presentes
     * en la base de datos.
     *
     * @return El listado de descuentos.
     * @throws SQLException Esception en la consulta SQL.
     */
    @Override
    public List<Discount> getList() throws SQLException {
        List<Discount> discountList = null;

        try (ResultSet rs = getListQuery.executeQuery()) {
            discountList = new ArrayList<>();

            while (rs.next()) {
                Discount discount = new Discount();

                discount.setId(rs.getInt("id"));
                discount.setCode(rs.getString("codeDisc"));
                discount.setDesc(rs.getString("descDisc"));
                discount.setPriceDisc(rs.getFloat("pricePerc"));

                discountList.add(discount);
            }
        }

        return discountList;
    }

    /**
     * Método para actualizar el usuario en la base de datos.
     *
     * @param discount El descuento encapsulado en un objeto.
     * @throws SQLException Excepción en la consulta SQL.
     * @throws ManagerErrorException Excepción dada al no encontrar el descuento solicitado.
     */
    @Override
    public void modify(Discount discount) throws SQLException, ManagerErrorException {
        // Asociamos los valores respecto a la sentencia sql.
        modifyQuery.setString(1, discount.getCode());
        modifyQuery.setString(2, discount.getDesc());
        modifyQuery.setFloat(3, discount.getPrizeDisc());
        modifyQuery.setInt(4, discount.getId());

        // Ejecutamos la sentencia sql.
        if (modifyQuery.executeUpdate() == 0)
            throw new ManagerErrorException("No se ha encontrado el descuento durante la actualización del mismo.");
    }

    /**
     * Método para eliminar el usuario en la base de datos.
     *
     * @param discount El descuento encapsulado en un objeto.
     * @throws SQLException Excepción en la consulta SQL.
     * @throws ManagerErrorException Excepción dada al no encontrar el descuento solicitado.
     */
    @Override
    public void remove(Discount discount) throws SQLException, ManagerErrorException {
        deleteQuery.setInt(1, discount.getId());

        // Ejecutamos la setencia sql.
        if (deleteQuery.executeUpdate() == 0)
            throw new ManagerErrorException("No se ha encontrado el descuento durante la eliminación del mismo.");
    }
}
