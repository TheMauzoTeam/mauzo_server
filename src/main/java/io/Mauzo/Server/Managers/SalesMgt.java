package io.Mauzo.Server.Managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.Mauzo.Server.Connections;
import io.Mauzo.Server.Templates.Sale;

public class SalesMgt implements ManagersIntf<Sale> {
    private static SalesMgt controller = null;

    /**
     * Método para añadir una venta a la base de datos.
     * 
     * @param sale  El objeto de la venta.
     * @throws SQLException Excepcion en la consulta SQL.
     */
    @Override
    public void add(Sale sale) throws SQLException {
        // Guardamos el puntero de conexion con la base de datos.
        Connection conn = Connections.getController().acquireConnection();

        // Preparamos la consulta SQL.
        try(PreparedStatement st = conn.prepareStatement("INSERT INTO Sales (stampRef, userId, prodId, discId, refundId) VALUES (?, ?, ?, ?, ?)")) {
            // Asociamos los valores respecto a la sentencia sql.
            st.setLong(1, sale.getStampRef().getTime());
            st.setInt(2, sale.getUserId());
            st.setInt(3, sale.getProdId());
            st.setInt(4, sale.getDiscId());
            st.setInt(4, sale.getProdId());

            // Ejecutamos la sentencia sql.
            st.execute();
        }

        Connections.getController().releaseConnection(conn);
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
        // Preparamos una instancia del objeto a devolver
        Sale sale = null;

        // Guardamos el puntero de conexion con la base de datos.
        Connection conn = Connections.getController().acquireConnection();

        // Preparamos la consulta sql.
        try (PreparedStatement st = conn.prepareStatement("SELECT * FROM Sales WHERE id = ?;")) {
            // Asociamos los valores respecto a la sentencia sql.
            st.setInt(1, id);

            // Ejecutamos la sentencia sql y recuperamos lo que nos ha retornado.
            try (ResultSet rs = st.executeQuery()) {
                if (!(rs.isLast()))
                    while (rs.next()) {
                        sale = new Sale();

                        sale.setId(rs.getInt("id"));
                        sale.setStampRef(new Date(rs.getLong("stampRef")));
                        sale.setUserId(rs.getInt("userId"));
                        sale.setProdId(rs.getInt("prodId"));
                        sale.setDiscId(rs.getInt("discId"));
                        sale.setRefundId(rs.getInt("refundId"));
                    }
                else
                    throw new ManagerErrorException("No se ha encontrado la venta");
            }
        }

        Connections.getController().releaseConnection(conn);

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
        // Preparamos una instancia del objeto a devolver
        List<Sale> salesList = null;

        // Guardamos el puntero de conexion con la base de datos.
        Connection conn = Connections.getController().acquireConnection();

        try(PreparedStatement st = conn.prepareStatement("SELECT * FROM Sales")) {
            try(ResultSet rs = st.executeQuery()) {
                salesList = new ArrayList<>();

                while (rs.next()) {
                    Sale sale = new Sale();

                    sale.setId(rs.getInt("id"));
                    sale.setStampRef(new Date(rs.getLong("stampRef")));
                    sale.setUserId(rs.getInt("userId"));
                    sale.setProdId(rs.getInt("prodId"));
                    sale.setDiscId(rs.getInt("discId"));
                    sale.setRefundId(rs.getInt("refundId"));

                    salesList.add(sale);
                }
            }
        }

        Connections.getController().releaseConnection(conn);

        return salesList;
    }

    /**
     * Método para actualizar la venta en la base de datos.
     * 
     * @param user La venta encapsulado en un objeto.
     * @throws SQLException Excepcion en la consulta SQL.
     * @throws ManagerErrorException Excepcion dada al no encontrar la venta solicitada.
     */
    @Override
    public void modify(Sale sale) throws SQLException, ManagerErrorException {
        // Guardamos el puntero de conexion con la base de datos.
        Connection conn = Connections.getController().acquireConnection();

        try (PreparedStatement st = conn.prepareStatement("UPDATE Sales SET stampRef = ?, userId = ?, prodId = ?, discId = ?, refundId = ? WHERE id = ?")) {
            st.setLong(1, sale.getStampRef().getTime());
            st.setInt(2, sale.getUserId());
            st.setInt(3, sale.getProdId());
            st.setInt(4, sale.getDiscId());
            st.setInt(5, sale.getRefundId());
            st.setInt(6, sale.getId());

            if(st.execute() == false)
                throw new ManagerErrorException("No se ha encontrado la venta.");
        }

        Connections.getController().releaseConnection(conn);
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
        // Guardamos el puntero de conexion con la base de datos.
        Connection conn = Connections.getController().acquireConnection();

        // Preparamos la sentencia sql.
        try (PreparedStatement st = conn.prepareStatement("DELETE FROM Sales WHERE id = ?;")) {
            st.setInt(1, sale.getId());

            // Ejecutamos la sentencia sql.
            if(st.execute() == false) 
                throw new ManagerErrorException("No se ha encontrado el usuario durante la eliminación del mismo.");
        }
        
        Connections.getController().releaseConnection(conn);
    }

    /**
     * Método para recuperar el controlador de la clase SalesMgt.
     * 
     * @return El controlador de la clase SalesMgt.
     */
    public static SalesMgt getController() {
        if (controller == null)
            controller = new SalesMgt();

        return controller;
    }
}