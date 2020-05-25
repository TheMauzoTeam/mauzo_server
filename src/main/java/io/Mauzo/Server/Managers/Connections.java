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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import io.Mauzo.Server.ServerApp;
import io.Mauzo.Server.ServerUtils;

/**
 * Clase la cual contiene los grupos de conexiones.
 * 
 * Esta clase tiene conexiones preinicializadas con preparedStatements para
 * realizar las consultas de manera agil y sencilla. Esto está diseñado para
 * albergar un número maximo de conexiones permitidas en el servidor, esto
 * ocurre porque hay infraestructuras cuyos motores de bases de datos tienen un
 * limite de conexiones.
 * 
 * @author Neirth Sergio Martínez
 */
public class Connections {
    private static Connections controller = null;

    private final int maxConnections = Integer.valueOf(ServerUtils.loadProperties().getProperty("mauzo.maxParallel.typeConnections"));

    private final Semaphore uSemaphore = new Semaphore(maxConnections);
    private final Semaphore sSemaphore = new Semaphore(maxConnections);
    private final Semaphore rSemaphore = new Semaphore(maxConnections);
    private final Semaphore pSemaphore = new Semaphore(maxConnections);
    private final Semaphore dSemaphore = new Semaphore(maxConnections);
    private final Semaphore iSemaphore = new Semaphore(maxConnections);

    private final List<UsersMgt> uConnectionList = new ArrayList<>();
    private final List<SalesMgt> sConnectionList = new ArrayList<>();
    private final List<RefundsMgt> rConnectionList = new ArrayList<>();
    private final List<ProductsMgt> pConnectionList = new ArrayList<>();
    private final List<DiscountsMgt> dConnectionList = new ArrayList<>();
    private final List<InformsMgt> iConnectionList = new ArrayList<>();

    /**
     * Constructor privado del cual inicializa los objetos que manejarán las
     * conexiones de los gestores a la base de datos proporcionado como URL JBDC.
     * 
     * @throws SQLException Puede lanzar alguna excepción si ocurre algún problema
     *                      con la base de datos.
     */
    private Connections() throws SQLException {
        for (int i = 0; i < maxConnections; i++) {
            uConnectionList.add(new UsersMgt(ServerApp.setConnection()));
            sConnectionList.add(new SalesMgt(ServerApp.setConnection()));
            rConnectionList.add(new RefundsMgt(ServerApp.setConnection()));
            pConnectionList.add(new ProductsMgt(ServerApp.setConnection()));
            dConnectionList.add(new DiscountsMgt(ServerApp.setConnection()));
            iConnectionList.add(new InformsMgt(ServerApp.setConnection()));
        }
    }

    /**
     * Método para adquirir una conexión de usuarios.
     * 
     * En vez de utilizar un new UsersMgt(), utilizamos este método, dado que hay
     * infraestructuras donde la base de datos es bastante limitada (Por ejemplo en
     * Heroku), asi conseguimos tener conexiones, que se vayan a aprovechar de forma
     * eficaz.
     * 
     * @return Una conexion con la base de de datos de tipo usuarios.
     * @throws InterruptedException Ha sido interrumpido el método.
     */
    public UsersMgt acquireUsers() throws InterruptedException {
        uSemaphore.acquire();

        UsersMgt aux = uConnectionList.get(0);
        uConnectionList.remove(0);

        return aux;
    }

    /**
     * Método para adquirir una conexión de ventas.
     * 
     * En vez de utilizar un new SalesMgt(), utilizamos este método, dado que hay
     * infraestructuras donde la base de datos es bastante limitada (Por ejemplo en
     * Heroku), asi conseguimos tener conexiones, que se vayan a aprovechar de forma
     * eficaz.
     * 
     * @return Una conexion con la base de de datos de tipo ventas.
     * @throws InterruptedException Ha sido interrumpido el método.
     */
    public SalesMgt acquireSales() throws InterruptedException {
        sSemaphore.acquire();

        SalesMgt aux = sConnectionList.get(0);
        sConnectionList.remove(0);

        return aux;
    }

    /**
     * Método para adquirir una conexión de devoluciones.
     * 
     * En vez de utilizar un new RefundsMgt(), utilizamos este método, dado que hay
     * infraestructuras donde la base de datos es bastante limitada (Por ejemplo en
     * Heroku), asi conseguimos tener conexiones, que se vayan a aprovechar de forma
     * eficaz.
     * 
     * @return Una conexion con la base de de datos de tipo devoluciones.
     * @throws InterruptedException Ha sido interrumpido el método.
     */
    public RefundsMgt acquireRefunds() throws InterruptedException {
        rSemaphore.acquire();

        RefundsMgt aux = rConnectionList.get(0);
        rConnectionList.remove(0);

        return aux;
    }

    /**
     * Método para adquirir una conexión de productos.
     * 
     * En vez de utilizar un new ProductsMgt(), utilizamos este método, dado que hay
     * infraestructuras donde la base de datos es bastante limitada (Por ejemplo en
     * Heroku), asi conseguimos tener conexiones, que se vayan a aprovechar de forma
     * eficaz.
     * 
     * @return Una conexion con la base de de datos de tipo productos.
     * @throws InterruptedException Ha sido interrumpido el método.
     */
    public ProductsMgt acquireProducts() throws InterruptedException {
        pSemaphore.acquire();

        ProductsMgt aux = pConnectionList.get(0);
        pConnectionList.remove(0);

        return aux;
    }

    public InformsMgt acquireInforms() throws InterruptedException {
        iSemaphore.acquire();

        InformsMgt aux = iConnectionList.get(0);
        iConnectionList.remove(0);

        return aux;
    }

    /**
     * Método para adquirir una conexión de descuentos.
     * 
     * En vez de utilizar un new DiscountsMgt(), utilizamos este método, dado que
     * hay infraestructuras donde la base de datos es bastante limitada (Por ejemplo
     * en Heroku), asi conseguimos tener conexiones, que se vayan a aprovechar de
     * forma eficaz.
     * 
     * @return Una conexion con la base de de datos de tipo descuentos.
     * @throws InterruptedException Ha sido interrumpido el método.
     */
    public DiscountsMgt acquireDiscounts() throws InterruptedException {
        dSemaphore.acquire();

        DiscountsMgt aux = dConnectionList.get(0);
        dConnectionList.remove(0);

        return aux;
    }

    /**
     * Método para devolver una conexión de tipo usuarios.
     * 
     * @param users La conexión de tipo usuarios.
     */
    public void releaseUsers(UsersMgt users) {
        uConnectionList.add(users);
        uSemaphore.release();
    }

    /**
     * Método para devolver una conexión de tipo ventas.
     * 
     * @param sales La conexión de tipo ventas.
     */
    public void releaseSales(SalesMgt sales) {
        sConnectionList.add(sales);
        sSemaphore.release();
    }

    /**
     * Método para devolver una conexión de tipo devoluciones.
     * 
     * @param refunds La conexión de tipo devoluciones.
     */
    public void releaseRefunds(RefundsMgt refunds) {
        rConnectionList.add(refunds);
        rSemaphore.release();
    }

    /**
     * Método para devolver una conexión de tipo productos.
     * 
     * @param products La conexión de tipo productos.
     */
    public void releaseProducts(ProductsMgt products) {
        pConnectionList.add(products);
        pSemaphore.release();
    }

    /**
     * Método para devolver una conexión de tipo descuentos.
     * 
     * @param discounts La conexión de tipo descuentos.
     */
    public void releaseDiscounts(DiscountsMgt discounts) {
        dConnectionList.add(discounts);
        dSemaphore.release();
    }

    public void releaseInforms(InformsMgt informs) {
        iConnectionList.add(informs);
        iSemaphore.release();
    }

    public static Connections getController() throws SQLException {
        if (controller == null)
            controller = new Connections();

        return controller;
    }
}