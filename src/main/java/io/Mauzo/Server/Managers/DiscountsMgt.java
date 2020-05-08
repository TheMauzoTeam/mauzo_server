package io.Mauzo.Server.Managers;

import io.Mauzo.Server.Templates.Discount;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DiscountsMgt implements ManagersIntf<Discount> {
    private final PreparedStatement addQuery;
    private final PreparedStatement getIdQuery;
    private final PreparedStatement getListQuery;
    private final PreparedStatement modifyQuery;
    private final PreparedStatement deleteQuery;

    public DiscountsMgt(Connection conn) throws SQLException {
        addQuery = conn.prepareStatement("INSERT INTO Discounts (id, codeDisc, descDisc, pricePrec)");
        getIdQuery = conn.prepareStatement("SELECT * FROM Discounts WHERE id = ?;");
        getListQuery = conn.prepareStatement("SELECT * FROM Discounts");
        modifyQuery = conn.prepareStatement("UPDATE Discounts SET codeDisc = ?, descDisc = ?, pricePerc = ? WHILE id = ?;");
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
        addQuery.setInt(1, discount.getId());
        addQuery.setString(2, discount.getCode());
        addQuery.setString(3, discount.getDesc());
        addQuery.setFloat(4, discount.getPrizeDisc());

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
            discount = new Discount();
            if (!(rs.isLast()))
                while (rs.next()) {
                    discount = new Discount();

                    discount.setId(rs.getInt("id"));
                    discount.setCode(rs.getString("codeDisc"));
                    discount.setDesc(rs.getString("descDisc"));
                    discount.setPriceDisc(rs.getFloat("pricePerc"));
                }
            else
                throw new ManagerErrorException("No se ha encontrado el descuento.");
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

        // Ejecutamos la sentencia sql.
        if (modifyQuery.execute() == false)
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
        if (deleteQuery.execute() == false)
            throw new ManagerErrorException("No se ha encontrado el descuento durante la eliminación del mismo.");
    }
}
