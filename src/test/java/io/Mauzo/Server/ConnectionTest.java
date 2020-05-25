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
     * Test que comprueba que las operaciones de la conexión individual ifuncionen
     * de manera correcta sobre una base de datos que opere, de manera muy semejante
     * a la base de datos que se utiliza en producción.
     * 
     * @throws Exception Puede arrojar cualquier tipo de Excepción.
     */
    @Test
    public void checkIndividualConnection() throws Exception {
        // Obtenemos una conexion indivual.
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
        // Variable comprobante del test.
        boolean testPassed = true;

        // Obtenemos una conexion del pool de conexiones de tipo users.
        UsersMgt connUsers = Connections.getController().acquireUsers();;
 
        if (connUsers == null)
            testPassed = false;

        Connections.getController().releaseUsers(connUsers);

        // Obtenemos una conexion del pool de conexiones de tipo sales.
        SalesMgt connSales = Connections.getController().acquireSales();

        if (connSales == null)
            testPassed = false;

        Connections.getController().releaseSales(connSales);

        // Obtenemos una conexion del pool de conexiones de tipo products.
        ProductsMgt connProducts = Connections.getController().acquireProducts();

        if (connProducts == null)
            testPassed = false;

        Connections.getController().releaseProducts(connProducts);

        // Obtenemos una conexion del pool de conexiones de tipo refunds.
        RefundsMgt connRefunds = Connections.getController().acquireRefunds();

        if (connRefunds == null)
            testPassed = false;

        Connections.getController().releaseRefunds(connRefunds);

        // Obtenemos una conexion del pool de conexiones de tipo discounts.
        DiscountsMgt connDiscounts = Connections.getController().acquireDiscounts();

        if(connDiscounts == null)
            testPassed = false;

        Connections.getController().releaseDiscounts(connDiscounts);

        // Obtenemos una conexion del pool de conexiones de tipo informs.
        InformsMgt connInforms = Connections.getController().acquireInforms();

        if(connInforms == null)
            testPassed = false;

        Connections.getController().releaseInforms(connInforms);

        // Si no es falso, significa que ha pasado la prueba
        Assert.assertTrue(testPassed);
    }
}