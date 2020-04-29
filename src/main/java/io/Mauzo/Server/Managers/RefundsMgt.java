package io.Mauzo.Server.Managers;

import io.Mauzo.Server.ServerApp;
import io.Mauzo.Server.Templates.Refund;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RefundsMgt implements  ManagersIntf<Refund>{
    private static RefundsMgt controller = null;

    /**
     *Método para añadir devoluciones a la base de datos
     *
     * @param obj   El objeto en cuestión.
     * @throws SQLException Excepcion en la consulta SQL
     */
    @Override
    public void add(Refund obj) throws SQLException {
        final Connection connection = ServerApp.getConnection();

        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO Refunds (id, dateRefund, userId, saleId) VALUES (?, ?, ?, ?);")){
            statement.setInt(1, obj.getId());
            statement.setDate(2, (Date) obj.getDateRefund());
            statement.setInt(3, obj.getUserId());
            statement.setInt(4, obj.getSaleId());

            statement.execute();
        }
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

        final Connection connection = ServerApp.getConnection();

        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM User WHERE id = ?;")){
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()){
                if (!(resultSet.isLast()))
                    while (resultSet.next()) {
                        refund = new Refund();

                        refund.setId(resultSet.getInt("id"));
                        refund.setDateRefund(resultSet.getDate("dateRefund"));
                        refund.setUserId(resultSet.getInt("userId"));
                        refund.setSaleId(resultSet.getInt("saleId"));
                    }
                else
                    throw new ManagerErrorException("No se ha encontrado la devolución");
            }
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

        final Connection connection = ServerApp.getConnection();

        try(PreparedStatement statement = connection.prepareStatement("SELECT * FROM Refunds")) {
            try (ResultSet resultSet = statement.executeQuery()) {
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
        final Connection connection = ServerApp.getConnection();

        try (PreparedStatement statement = connection.prepareStatement("UPDATE Refunds SET id = ?, dateRefund = ?, userId = ?, SaleId = ? WHILE id = ?;")){
            statement.setInt(1, obj.getId());
            statement.setDate(2, (Date) obj.getDateRefund());
            statement.setInt(3, obj.getUserId());
            statement.setInt(4, obj.getSaleId());

            if (statement.execute() == false)
                throw new ManagerErrorException("No se ha encontrado la devolución");
        }
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
        final Connection connection = ServerApp.getConnection();

        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM Refunds WHERE id = ?;")) {
            statement.setInt(1, obj.getId());

            if (statement.execute() == false)
                throw new ManagerErrorException("No se ha encontrado la devolución");

        }
    }

    /**
     * Método para recuperar el controlador de la clase RefundsMgt.
     *
     * @return El controlador de la clase RefundMgt.
     */
    public static RefundsMgt getController() {
        if (controller == null)
            controller = new RefundsMgt();

        return controller;
    }
}