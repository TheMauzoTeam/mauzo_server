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

import io.Mauzo.Server.Templates.Refund;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase para gestionar las devoluciones en la base de datos.
 *
 * La utilidad de esta de clase es poder implementar métodos que permitan hacer operaciones CRUD.
 * Para lograr eficiencia en las consultas utilizamos PreparedStatement, esto nos permite introducir las variables que necesitamos en la base de datos.
 *
 * @author lluminar Lidia Martínez
 */
public class RefundsMgt implements  ManagersIntf<Refund>{
    private final PreparedStatement addQuery;
    private final PreparedStatement getIdQuery;
    private final PreparedStatement getListQuery;
    private final PreparedStatement modifyQuery;
    private final PreparedStatement removeQuery;

    //Constructor con las sentencias a la base de datos
    // FIXME: 08/06/2020 Un espacio, por favor
    RefundsMgt(Connection connection) throws SQLException{
        addQuery = connection.prepareStatement("INSERT INTO Refunds ( dateRefund, userId, saleId ) VALUES (?, ?, ?);");
        getIdQuery = connection.prepareStatement("SELECT * FROM Refunds WHERE id = ?;");
        getListQuery = connection.prepareStatement("SELECT * FROM Refunds");
        modifyQuery = connection.prepareStatement("UPDATE Refunds SET dateRefund = ?, userId = ?, saleId = ?  WHERE id = ?;");
        removeQuery = connection.prepareStatement("DELETE FROM Refunds WHERE id = ?;");
    }

    /**
     *Método para añadir devoluciones a la base de datos
     *
     * @param obj   El objeto en cuestión.
     * @throws SQLException Excepcion en la consulta SQL
     */
    @Override
    public void add(Refund obj) throws SQLException {

        // Asociamos los valores
        addQuery.setDate(1, new Date(obj.getDateRefund().getTime()));
        addQuery.setInt(2, obj.getUserId());
        addQuery.setInt(3, obj.getSaleId());

        // FIXME: 08/06/2020 Creo que preferiría executeUpdate
        addQuery.execute();
    }

    /**
     * Método para obtener en forma de objeto la devolución a partir de un id.
     *
     * @param id    ID del objeto en la base de datos.
     * @return La devolución EIN!?!?!?!?!?!?!?!?!?!?!?!?
     * @throws SQLException Excepción en la consulta SQL
     * @throws ManagerErrorException Excepción dada al no encontrar la devolución
     */
    @Override
    public Refund get(int id) throws SQLException, ManagerErrorException {
            Refund refund = null;

            //Asociamos los valores
            getIdQuery.setInt(1, id);

            //Ejecutamos la sentencia y conseguimos el resto de datos relacionados
            try (ResultSet resultSet = getIdQuery.executeQuery()) {
                if (resultSet.next()) {

                    // Envía los atributos de la base de datos
                    refund = new Refund();
                    refund.setId(resultSet.getInt("id"));
                    refund.setDateRefund(resultSet.getDate("dateRefund"));
                    refund.setUserId(resultSet.getInt("userId"));
                    refund.setSaleId(resultSet.getInt("saleId"));

                } else
                    throw new ManagerErrorException("No se ha encontrado la devolución");
            }

            return refund;
    }

    /**
     * Método para obtener la lista de devoluciones presentes en la base de datos
     *
     * @return La lista de devoluciones
     * @throws SQLException Excepcion en la consulta SQL.
     */
    @Override
    public List<Refund> getList() throws SQLException {
            List<Refund> refundList = null;

            // Ejecutamos la sentencia
            try (ResultSet resultSet = getListQuery.executeQuery()) {
                refundList = new ArrayList<>();

                while (resultSet.next()) {
                    Refund refund = new Refund();

                    // Envía los atributos de la base de datos
                    refund.setId(resultSet.getInt("id"));
                    refund.setDateRefund(resultSet.getDate("dateRefund"));
                    refund.setUserId(resultSet.getInt("userId"));
                    refund.setSaleId(resultSet.getInt("saleId"));

                    // Añade la devolución a la lista
                    refundList.add(refund);
                }
            }

            return refundList;
    }

    /**
     * Permite modificar la devolución
     *
     * @param obj La devolución
     * @throws SQLException Excepcion en la consulta SQL.
     * @throws ManagerErrorException Excepción lanzada al no encontrar la devolución
     */
    @Override
    public void modify(Refund obj) throws SQLException, ManagerErrorException {
        // Envía los valores a la base de datos para modificarla
        modifyQuery.setDate(1, (Date) obj.getDateRefund());
        modifyQuery.setInt(2, obj.getUserId());
        modifyQuery.setInt(3, obj.getSaleId());
        modifyQuery.setInt(4, obj.getId());


        // Si no encuentra la devolución lanza una Excepción
        if (modifyQuery.executeUpdate() == 0)
            throw new ManagerErrorException("No se ha encontrado la devolución");
    }

    /**
     * Método para eliminar la devolución de la base de datos
     *
     * @param obj La devolución
     * @throws SQLException Excepcion en la consulta SQL.
     * @throws ManagerErrorException Excepción al no encontrar la devolución
     */
    @Override
    public void remove(Refund obj) throws SQLException, ManagerErrorException {
        // Elimina la devolución
        removeQuery.setInt(1, obj.getId());

        // En el caso de no encontrarla lanza una Excepción
        if (removeQuery.executeUpdate() == 0)
            throw new ManagerErrorException("No se ha encontrado la devolución");
    }
}