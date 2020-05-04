package io.Mauzo.Server.Managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.Mauzo.Server.Templates.Sale;

public class SalesMgt implements ManagersIntf<Sale> {
    // Dejamos preparadas las consultas
    private final PreparedStatement addQuery;
    private final PreparedStatement getIdQuery;
    private final PreparedStatement getListQuery;
    private final PreparedStatement modifyQuery;
    private final PreparedStatement deleteQuery;

    public SalesMgt(Connection conn) throws SQLException {
        this.addQuery = conn.prepareStatement("INSERT INTO Sales (stampRef, userId, prodId, discId, refundId) VALUES (?, ?, ?, ?, ?)");
        this.getIdQuery = conn.prepareStatement("SELECT * FROM Sales WHERE id = ?;");
        this.getListQuery = conn.prepareStatement("SELECT * FROM Sales");
        this.modifyQuery = conn.prepareStatement("UPDATE Sales SET stampRef = ?, userId = ?, prodId = ?, discId = ?, refundId = ? WHERE id = ?");
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
        synchronized(addQuery) {
            // Asociamos los valores respecto a la sentencia sql.
            addQuery.setLong(1, sale.getStampRef().getTime());
            addQuery.setInt(2, sale.getUserId());
            addQuery.setInt(3, sale.getProdId());
            addQuery.setInt(4, sale.getDiscId());
            addQuery.setInt(4, sale.getProdId());

            // Ejecutamos la sentencia sql.
            addQuery.execute();
        }
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
        synchronized(getIdQuery) {
            // Asociamos los valores respecto a la sentencia sql.
            getIdQuery.setInt(1, id);
            
            Sale sale = new Sale();

            // Ejecutamos la sentencia sql y recuperamos lo que nos ha retornado.
            try (ResultSet rs = getIdQuery.executeQuery()) {
                if (!(rs.isLast()))
                    while (rs.next()) {
                        

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

            return sale;
        }
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
        synchronized(getListQuery) {
            List<Sale> salesList = new ArrayList<>();

            try(ResultSet rs = getListQuery.executeQuery()) {
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

            return salesList;
        }
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
        synchronized(modifyQuery) {
            modifyQuery.setLong(1, sale.getStampRef().getTime());
            modifyQuery.setInt(2, sale.getUserId());
            modifyQuery.setInt(3, sale.getProdId());
            modifyQuery.setInt(4, sale.getDiscId());
            modifyQuery.setInt(5, sale.getRefundId());
            modifyQuery.setInt(6, sale.getId());

            if(modifyQuery.execute() == false)
                throw new ManagerErrorException("No se ha encontrado la venta.");
        }
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
        synchronized(deleteQuery) {
            deleteQuery.setInt(1, sale.getId());

            // Ejecutamos la sentencia sql.
            if(deleteQuery.execute() == false) 
                throw new ManagerErrorException("No se ha encontrado el usuario durante la eliminación del mismo.");
        }
    }
}