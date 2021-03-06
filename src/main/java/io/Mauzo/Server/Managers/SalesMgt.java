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

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import io.Mauzo.Server.Templates.Sale;

/**
 * Clase para gestionar las ventas respecto a la base de datos.
 * 
 * La utilidad de esta clase es poder implementar métodos que permitan
 * hacer operaciones CRUD con la base de datos mediante el lenguaje SQL,
 * para lograr una eficiencia en las consultas, utilizamos Consultas Preparadas,
 * esto nos ayuda a que solo tengamos que introducir las variables que deseamos 
 * a la base de datos.
 * 
 * @author Neirth Sergio Martínez
 */
public class SalesMgt implements ManagersIntf<Sale> {
    // Dejamos preparadas las consultas
    private final PreparedStatement addQuery;
    private final PreparedStatement getIdQuery;
    private final PreparedStatement getListQuery;
    private final PreparedStatement modifyQuery;
    private final PreparedStatement deleteQuery;

    SalesMgt(Connection conn) throws SQLException {
        this.addQuery = conn.prepareStatement("INSERT INTO Sales (stampRef, userId, prodId, discId) VALUES (?, ?, ?, ?);");
        this.getIdQuery = conn.prepareStatement("SELECT * FROM Sales WHERE id = ?;");
        this.getListQuery = conn.prepareStatement("SELECT * FROM Sales;");
        this.modifyQuery = conn.prepareStatement("UPDATE Sales SET stampRef = ?, userId = ?, prodId = ?, discId = ? WHERE id = ?;");
        this.deleteQuery = conn.prepareStatement("DELETE FROM Sales WHERE id = ?;");
    }

    /**
     * Método para añadir una venta a la base de datos.
     * 
     * @param sale  El objeto de la venta.
     * @throws SQLException Excepcion en la consulta SQL.
     */
    @Override
    public void add(Sale sale) throws SQLException {
        // Asociamos los valores respecto a la sentencia sql.
        addQuery.setDate(1, new Date(sale.getStampRef().getTime()));
        addQuery.setInt(2, sale.getUserId());
        addQuery.setInt(3, sale.getProdId());

        // Este es un posible valor nulo.
        if(sale.getDiscId() != null) {
            addQuery.setInt(4, sale.getDiscId());
        } else {
            addQuery.setNull(4, Types.INTEGER);
        }

        // Ejecutamos la sentencia sql.
        addQuery.execute();
    }

    /**
     * Método para obtener la venta a partir de un id.
     * 
     * @param id    El id del objeto de la venta.
     * @return El objeto de la venta.
     * @throws SQLException Excepcion en la consulta SQL.
     * @throws ManagerErrorException Excepcion dada al no encontrar la venta.
     */
    @Override
    public Sale get(int id) throws SQLException, ManagerErrorException {
        // Asociamos los valores respecto a la sentencia sql.
        getIdQuery.setInt(1, id);
        
        Sale sale = new Sale();

        // Ejecutamos la sentencia sql y recuperamos lo que nos ha retornado.
        try (ResultSet rs = getIdQuery.executeQuery()) {
            if (rs.next()) {
                // Le damos valor al objeto
                sale.setId(rs.getInt("id"));
                sale.setStampRef(new Date(rs.getDate("stampRef").getTime()));
                sale.setUserId(rs.getInt("userId"));
                sale.setProdId(rs.getInt("prodId"));
                sale.setDiscId(rs.getInt("discId"));
            } else {
                throw new ManagerErrorException("No se ha encontrado la venta");
            }
        }

        return sale;
    }

    /**
     * Método para obtener en forma de lista de ventas, las ventas presentes
     * en la base de datos.
     * 
     * @return El listado de ventas.
     * @throws SQLException Excepcion en la consulta SQL.
     */
    @Override
    public List<Sale> getList() throws SQLException {
        List<Sale> salesList = new ArrayList<>();

        // Ejecutamos la sentencia sql y recuperamos lo que nos ha retornado.
        try (ResultSet rs = getListQuery.executeQuery()) {
            while (rs.next()) {
                // Preparamos un nuevo objeto de ventas.
                Sale sale = new Sale();

                // Le damos valor al objeto
                sale.setId(rs.getInt("id"));
                sale.setStampRef(rs.getDate("stampRef"));
                sale.setUserId(rs.getInt("userId"));
                sale.setProdId(rs.getInt("prodId"));
                sale.setDiscId(rs.getInt("discId"));

                // Lo añadimos a la lista a retornar.
                salesList.add(sale);
            }
        }

        return salesList;
    }

    /**
     * Método para actualizar la venta en la base de datos.
     * 
     * @param sale La venta encapsulado en un objeto.
     * @throws SQLException Excepcion en la consulta SQL.
     * @throws ManagerErrorException Excepcion dada al no encontrar la venta solicitada.
     */
    @Override
    public void modify(Sale sale) throws SQLException, ManagerErrorException {
        modifyQuery.setDate(1, (Date) sale.getStampRef());
        modifyQuery.setInt(2, sale.getUserId());
        modifyQuery.setInt(3, sale.getProdId());

        // Este es un posible valor nulo.
        if(sale.getDiscId() != null) {
            modifyQuery.setInt(4, sale.getDiscId());
        } else {
            modifyQuery.setNull(4, Types.INTEGER);
        }

        modifyQuery.setInt(5, sale.getId());

        if (modifyQuery.executeUpdate() == 0)
            throw new ManagerErrorException("No se ha encontrado la venta.");
    }

    /**
     * Método para eliminar la venta en la base de datos.
     * 
     * @param sale La venta encapsulado en un objeto.
     * @throws SQLException Excepcion en la consulta SQL.
     * @throws ManagerErrorException Excepcion dada al no encontrar la venta solicitado.
     */
    @Override
    public void remove(Sale sale) throws SQLException, ManagerErrorException {
        deleteQuery.setInt(1, sale.getId());

        // Ejecutamos la sentencia sql.
        if (deleteQuery.executeUpdate() == 0) 
            throw new ManagerErrorException("No se ha encontrado el usuario durante la eliminación del mismo.");
    }
}