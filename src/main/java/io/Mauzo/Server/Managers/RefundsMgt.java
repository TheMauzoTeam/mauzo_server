package io.Mauzo.Server.Managers;

import io.Mauzo.Server.Templates.Refund;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RefundsMgt implements  ManagersIntf<Refund>{
    private final PreparedStatement addQuery;
    private final PreparedStatement getIdQuery;
    private final PreparedStatement getListQuery;
    private final PreparedStatement modifyQuery;
    private final PreparedStatement removeQuery;

    public RefundsMgt(Connection connection) throws SQLException{
        addQuery = connection.prepareStatement("INSERT INTO Refunds ( dateRefund, userId, saleId ) VALUES (?, ?, ?);");
        getIdQuery = connection.prepareStatement("SELECT * FROM Refunds WHERE id = ?;");
        getListQuery = connection.prepareStatement("SELECT * FROM Refunds");
        modifyQuery = connection.prepareStatement("UPDATE Refunds SET dateRefund = ?, userId = ?, saleId = ? WHERE id = ?;");
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
        addQuery.setDate(1, new Date(obj.getDateRefund().getTime()));
        addQuery.setInt(2, obj.getUserId());
        addQuery.setInt(3, obj.getSaleId());

        addQuery.execute();
    }

    /**
     * Método para obtener en forma de objeto la devolución a partir de un id.
     *
     * @param id    ID del objeto en la base de datos.
     * @return La devolución
     * @throws SQLException Excepción en la consulta SQL
     * @throws ManagerErrorException Excepción dada al no encontrar la devolución
     */
    @Override
    public Refund get(int id) throws SQLException, ManagerErrorException {
            Refund refund = null;

            getIdQuery.setInt(1, id);

            try (ResultSet resultSet = getIdQuery.executeQuery()) {
                if (resultSet.next()) {

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

            try (ResultSet resultSet = getListQuery.executeQuery()) {
                refundList = new ArrayList<>();

                while (resultSet.next()) {
                    Refund refund = new Refund();

                    refund.setId(resultSet.getInt("id"));
                    refund.setDateRefund(resultSet.getDate("dateRefund"));
                    refund.setUserId(resultSet.getInt("userId"));
                    refund.setSaleId(resultSet.getInt("saleId"));

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
        modifyQuery.setDate(1, (Date) obj.getDateRefund());
        modifyQuery.setInt(2, obj.getUserId());
        modifyQuery.setInt(3, obj.getSaleId());
        modifyQuery.setInt(4, obj.getId());

        if (modifyQuery.execute() == false)
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
        removeQuery.setInt(1, obj.getId());
 
        if (removeQuery.execute() == false)
            throw new ManagerErrorException("No se ha encontrado la devolución");
    }
}