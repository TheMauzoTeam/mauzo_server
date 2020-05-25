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
package io.Mauzo.Server;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Test;
import org.junit.Assert;
import org.junit.BeforeClass;

import io.Mauzo.Server.Managers.Connections;
import io.Mauzo.Server.Managers.DiscountsMgt;
import io.Mauzo.Server.Managers.InformsMgt;
import io.Mauzo.Server.Managers.ProductsMgt;
import io.Mauzo.Server.Managers.RefundsMgt;
import io.Mauzo.Server.Managers.SalesMgt;
import io.Mauzo.Server.Managers.UsersMgt;

/**
 * Test para probar la correcta comunicación del servidor a la base de datos.
 * 
 * Este test prueba tanto las posibles conexiones individuales como tambien las
 * conexiones del grupo de conexiones.
 * 
 * @author Neirth Sergio Martinez
 */
public class ConnectionTest {
    /**
     * Preparación de la URL de JDBC con la base de datos de pruebas donde se
     * ejecutarán todos los test de validación respecto a los métodos a probar.
     */
    @BeforeClass
    public static void prepareDatabase() throws Exception {
        String url = ServerUtils.loadProperties().getProperty("mauzo.debugDatabase.url");

        ServerApp.setUrl(url);

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se ha encontrado el driver de PostgreSQL: " + e.toString());
        }
    }

    /**
     * Test que comprueba que las operaciones de la conexión individual funcionen
     * de manera correcta sobre una base de datos que opere, de manera muy semejante
     * a la base de datos que se utiliza en producción.
     * 
     * @throws Exception Puede arrojar cualquier tipo de Excepción.
     */
    @Test
    public void checkIndividualConnection() throws Exception {
        // Informamos al usuario del test que se ejecuta
        System.out.println("Ejecutando test de conexiones");

        // Obtenemos una conexión indivual.
        try (Connection conn = ServerApp.getConnection()) {
            // Si no es nulo, significa que ha pasado la prueba.
            Assert.assertNotNull(conn);
        }
    }

    /**
     * Test que comprueba que las operaciones del grupo de conexiones funcionen de
     * manera correcta sobre una base de datos que opere, de manera muy semejante a
     * la base de datos que se utiliza en producción.
     * 
     * @throws Exception Puede arrojar cualquier tipo de Excepción.
     */
    @Test
    public void checkPoolsConnection() throws Exception {
        // Informamos al usuario del test que se ejecuta
        System.out.println("Ejecutando test de grupo de conexiones");

        // Variable comprobante del test.
        boolean testPassed = true;

        // Obtenemos una conexión del pool de conexiones de tipo users.
        UsersMgt connUsers = Connections.getController().acquireUsers();;
 
        if (connUsers == null)
            testPassed = false;

        Connections.getController().releaseUsers(connUsers);

        // Obtenemos una conexión del pool de conexiones de tipo sales.
        SalesMgt connSales = Connections.getController().acquireSales();

        if (connSales == null)
            testPassed = false;

        Connections.getController().releaseSales(connSales);

        // Obtenemos una conexión del pool de conexiones de tipo products.
        ProductsMgt connProducts = Connections.getController().acquireProducts();

        if (connProducts == null)
            testPassed = false;

        Connections.getController().releaseProducts(connProducts);

        // Obtenemos una conexión del pool de conexiones de tipo refunds.
        RefundsMgt connRefunds = Connections.getController().acquireRefunds();

        if (connRefunds == null)
            testPassed = false;

        Connections.getController().releaseRefunds(connRefunds);

        // Obtenemos una conexión del pool de conexiones de tipo discounts.
        DiscountsMgt connDiscounts = Connections.getController().acquireDiscounts();

        if(connDiscounts == null)
            testPassed = false;

        Connections.getController().releaseDiscounts(connDiscounts);

        // Obtenemos una conexión del pool de conexiones de tipo informs.
        InformsMgt connInforms = Connections.getController().acquireInforms();

        if(connInforms == null)
            testPassed = false;

        Connections.getController().releaseInforms(connInforms);

        // Si no es falso, significa que ha pasado la prueba
        Assert.assertTrue(testPassed);
    }
}