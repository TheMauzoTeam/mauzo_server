package io.Mauzo.Server.Managers;

import io.Mauzo.Server.ServerApp;
import io.Mauzo.Server.Templates.Discount;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DiscountsMgt implements ManagersIntf<Discount> {
    private static DiscountsMgt controller = null;


    /**
     * Método para añadir descuentos a la base de datos.
     *
     * @param discount El descuento encapsulado en un objeto.
     * @throws SQLException Excepción en la consulta SQL.
     */
    @Override
    public void add(Discount discount) throws SQLException {
        // Guardamos el puntero de conexión con la base de datos.
        final Connection conn = ServerApp.getConnection();

        // Preparamos la consulta sql.
        // Las prepared statement no son de usar y tirar
        try (PreparedStatement st = conn.prepareStatement("INSERT INTO Discounts (id, codeDisc, descDisc, pricePrec)")) {

            // Asociamos los valores respecto a la sentencia sql.
            st.setInt(1, discount.getId());
            st.setString(2, discount.getCode());
            st.setString(3, discount.getDesc());
            st.setFloat(4, discount.getPrizeDisc());

            // Ejecutamos la sentencia sql.
            st.execute();
        }
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

        // Guardamos el puntero de conexión con la base de datos.
        final Connection conn = ServerApp.getConnection();

        // Preparamos le consulta sql.
        try (PreparedStatement st = conn.prepareStatement("SELECT * FROM Discounts WHERE id = ?;")) {
            // Asociamos los valores respecto a la sentencia sal.
            st.setInt(1, id);

            // Ejecutamos la sentencia sql y recuperamos lo que nos ha retornado.
            try (ResultSet rs = st.executeQuery()) {
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

        // Guardamos el pontero de conexión con la base de datos.
        final Connection conn = ServerApp.getConnection();

        // Lanzamos la consulta SQL y generamos la lista de descuentos.
        try (PreparedStatement st = conn.prepareStatement("SELECT * FROM Discounts")) {
            try (ResultSet rs = st.executeQuery()) {
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
        final Connection conn = ServerApp.getConnection();

        // Preparamos la sentencia sql.
        try (PreparedStatement st = conn.prepareStatement("UPDATE Discounts SET codeDisc = ?, descDisc = ?, pricePerc = ? WHILE id = ?;")) {
            // Asociamos los valores respecto a la sentencia sql.
            st.setString(1, discount.getCode());
            st.setString(2, discount.getDesc());
            st.setFloat(3, discount.getPrizeDisc());

            // Ejecutamos la sentencia sql.
            if (st.execute() == false)
                throw new ManagerErrorException("No se ha encontrado el descuento durante la actualización del mismo.");
        }
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
        final Connection conn = ServerApp.getConnection();

        // Preparamos la sentencia SQL.
        try (PreparedStatement st = conn.prepareStatement("DELETE FROM Discounts WHERE id = ?;")) {
            st.setInt(1, discount.getId());
            // Ejecutamos la setencia sql.
            if (st.execute() == false)
                throw new ManagerErrorException("No se ha encontrado el descuento durante la eliminación del mismo.");
        }
    }

    /**
     * Método para recuperar el controlador de la clase DiscountsMgt.
     *
     * @return El controlador de la clase DiscountsMgt.
     */
    public static DiscountsMgt getController() {
        if (controller == null)
            controller = new DiscountsMgt();
        return controller;
    }
}
