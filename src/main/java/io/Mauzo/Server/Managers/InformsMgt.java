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

import io.Mauzo.Server.Templates.Inform;

import java.sql.*;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * @author Ant04X Antonio Izquierdo
 */
public class InformsMgt implements ManagersIntf<Inform> {

    private final Connection conn;
    private final PreparedStatement getNumberSales;
    private final PreparedStatement getNumberRefunds;
    private final PreparedStatement getNumberDiscounts;

    InformsMgt(Connection conn) throws SQLException {
        // Reemplazamos la conexión por la del objeto.
        this.conn = conn;

        getNumberSales = conn.prepareStatement("SELECT count(id) AS nSales FROM Sales WHERE stampref BETWEEN ? AND ?");
        getNumberRefunds = conn.prepareStatement("SELECT count(id) AS nRefunds FROM Refunds WHERE dateRefund BETWEEN ? AND ?");
        getNumberDiscounts = conn.prepareStatement("SELECT count(id) AS nDiscounts FROM Sales WHERE discId IS NOT NULL AND stampref BETWEEN ? AND ?");
    }

    @Override
    public void add(Inform inform) throws SQLException {
        throw new UnsupportedOperationException("Esta operación no esta soportada en este método.");
    }

    /**
     * Método para obtener en forma de objeto el informe, a partir de el mes de
     * informe, el informe encapsulado.
     *
     * @param month El mes del informe.
     * @return El informe encapsulado en forma de objeto.
     * @throws SQLException Excepción en la consulta SQL.
     * @throws ManagerErrorException Excepción dada al no encontrar el informe solicitado.
     */
    @Override
    public Inform get(int month) throws SQLException, ManagerErrorException {

        // Se comprueba la validez del mes.
        if (1 > month || month > 12)
            throw new ManagerErrorException("Mes proporcionado imposible durante la obtención del mismo.");

        // Se generan los límites del informe.
        Date dStart = new Date(new GregorianCalendar(YearMonth.now().getYear(), month, 1).getTimeInMillis());
        Date dEnd = new Date(new GregorianCalendar(YearMonth.now().getYear(), month, Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)).getTimeInMillis());

        // Se establecen en la base de datos los límites.
        getNumberSales.setDate(1, dStart);
        getNumberSales.setDate(2, dEnd);

        getNumberRefunds.setDate(1, dStart);
        getNumberRefunds.setDate(2, dEnd);

        getNumberDiscounts.setDate(1, dStart);
        getNumberDiscounts.setDate(2, dEnd);

        Inform inform = new Inform(); // Se crea el objeto a devolver.

        conn.setAutoCommit(false); // Desactivamos la conexión como AutoCommit.

        // Ejecutamos las sentencias sql y recuperamos lo que nos han retornado.
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
            conn.setAutoCommit(true); // Se reestablece la conexión.

        }

        return inform;
    }

    /**
     * Método para obtener en forma de lista de informes, los informes presentes
     * en la base de datos.
     *
     * @return El listado de informes.
     * @throws SQLException Exception en la consulta SQL.
     */
    @Override
    // Va del 1 al 12 generar informs por mes e introducirlos a una lista.
    public List<Inform> getList() throws SQLException {
        List<Inform> informs = new ArrayList<Inform>();

        try {
            // Se añade un informe por mes.
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

    /**
     * Método no soportado por este tipo.
     *
     * @param obj  El nombre del objeto en la base de datos.
     * @throws SQLException
     * @throws ManagerErrorException
     */
    @Override
    public void remove(Inform obj) throws SQLException, ManagerErrorException {
        throw new UnsupportedOperationException("Esta operación no esta soportada en este método.");
    }
}