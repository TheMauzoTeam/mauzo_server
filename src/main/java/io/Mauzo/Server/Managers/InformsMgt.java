package io.Mauzo.Server.Managers;

import io.Mauzo.Server.Templates.Inform;

import java.sql.*;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class InformsMgt implements ManagersIntf<Inform> {

    private final Connection conn;
    private final PreparedStatement getNumberSales;
    private final PreparedStatement getNumberRefunds;
    private final PreparedStatement getNumberDiscounts;

    InformsMgt(Connection conn) throws SQLException {
        this.conn = conn;

        getNumberSales = conn.prepareStatement("SELECT count(id) AS nSales FROM Sales WHERE stampref BETWEEN ? AND ?");
        getNumberRefunds = conn.prepareStatement("SELECT count(id) AS nRefunds FROM Refunds WHERE dateRefund BETWEEN ? AND ?");
        getNumberDiscounts = conn.prepareStatement("SELECT count(id) AS nDiscounts FROM Sales WHERE discId IS NOT NULL AND stampref BETWEEN ? AND ?");
    }

    @Override
    public void add(Inform inform) throws SQLException {
        throw new UnsupportedOperationException("Esta operación no esta soportada en este método.");
    }

    @Override
    public Inform get(int month) throws SQLException, ManagerErrorException {

        if (1 > month || month > 12)
            throw new ManagerErrorException("Mes proporcionado imposible durante la obtención del mismo.");

        Date dStart = new Date(new GregorianCalendar(YearMonth.now().getYear(), month, 1).getTimeInMillis());
        Date dEnd = new Date(new GregorianCalendar(YearMonth.now().getYear(), month, Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)).getTimeInMillis());

        getNumberSales.setDate(1, dStart);
        getNumberSales.setDate(2, dEnd);

        getNumberRefunds.setDate(1, dStart);
        getNumberRefunds.setDate(2, dEnd);

        getNumberDiscounts.setDate(1, dStart);
        getNumberDiscounts.setDate(2, dEnd);

        Inform inform = new Inform();

        conn.setAutoCommit(false);

        try (ResultSet rsSales = getNumberSales.executeQuery();
            ResultSet rsRefunds = getNumberRefunds.executeQuery();
            ResultSet rsDiscounts = getNumberDiscounts.executeQuery()){

            conn.commit();

            if (rsSales.next() && rsRefunds.next() && rsDiscounts.next()) {
                inform.setId(month);
                inform.setnSales(rsSales.getInt("nSales"));
                inform.setnRefunds(rsRefunds.getInt("nRefunds"));
                inform.setnDiscounts(rsDiscounts.getInt("nDiscounts"));

                inform.setdStart(dStart);
                inform.setdEnd(dEnd);

            }
        } finally {
            conn.setAutoCommit(true);

        }

        return inform;
    }

    @Override
    // Va del 1 al 12 generar informs por mes e introducirlos a una lista.
    public List<Inform> getList() throws SQLException {
        List<Inform> informs = new ArrayList<Inform>();

        try {
            for (int i = 1; i <= 12; i++)
                informs.add(get(i));

        } catch (ManagerErrorException e) {
            throw new SQLException(e.getMessage());
        }

        return informs;
    }

    @Override
    public void modify(Inform obj) throws SQLException, ManagerErrorException {
        throw new UnsupportedOperationException("Esta operación no esta soportada en este método.");
    }

    @Override
    public void remove(Inform obj) throws SQLException, ManagerErrorException {
        throw new UnsupportedOperationException("Esta operación no esta soportada en este método.");
    }
}