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

    @Override
    public void add(Discount discount) throws SQLException {
        final Connection conn = ServerApp.getConnection();

        try (PreparedStatement st = conn.prepareStatement("INSERT INTO Discounts (id, codeDisc, descDisc, pricePrec)")) {

            st.setInt(1, discount.getId());
            st.setString(2, discount.getCode());
            st.setString(3, discount.getDesc());
            st.setFloat(4, discount.getPrizeDisc());

            st.execute();
        }
    }

    @Override
    public Discount get(int id) throws SQLException, ManagerErrorException {
        Discount discount = null;

        final Connection conn = ServerApp.getConnection();

        try (PreparedStatement st = conn.prepareStatement("SELECT * FROM Discounts WHERE id = ?;")) {
            st.setInt(1, id);

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
    
    public Discount get(String name) throws SQLException, ManagerErrorException {
        throw new UnsupportedOperationException("Esta operación no está soportada.");
    }

    @Override
    public List<Discount> getList() throws SQLException {
        List<Discount> discountList = null;

        final Connection conn = ServerApp.getConnection();

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

    @Override
    public void modify(Discount discount) throws SQLException, ManagerErrorException {
        final Connection conn = ServerApp.getConnection();

        try (PreparedStatement st = conn.prepareStatement("UPDATE Discounts SET codeDisc = ?, descDisc = ?, pricePerc = ? WHILE id = ?;")) {
            st.setString(1, discount.getCode());
            st.setString(2, discount.getDesc());
            st.setFloat(3, discount.getPrizeDisc());
            if (st.execute() == false)
                throw new ManagerErrorException("No se ha encontrado el descuento durante la actualización del mismo.");
        }
    }

    @Override
    public void remove(Discount discount) throws SQLException, ManagerErrorException {
        final Connection conn = ServerApp.getConnection();

        try (PreparedStatement st = conn.prepareStatement("DELETE FROM Discounts WHERE id = ?;")) {
            st.setInt(1, discount.getId());
            if (st.execute() == false)
                throw new ManagerErrorException("No se ha encontrado el descuento durante la eliminación del mismo.");
        }
    }

    public static DiscountsMgt getController() {
        if (controller == null)
            controller = new DiscountsMgt();
        return controller;
    }
}
