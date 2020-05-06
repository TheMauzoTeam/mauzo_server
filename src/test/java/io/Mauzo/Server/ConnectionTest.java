package io.Mauzo.Server;

import org.junit.Test;

import java.sql.Connection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

import io.Mauzo.Server.ServerApp;
import io.Mauzo.Server.ServerPools;

public class ConnectionTest {
    /**
     * Preparación de la URL de JDBC con la base de datos
     * de pruebas donde se ejecutarán todos los test de 
     * validación respecto a los métodos a probar.
     */
    @BeforeClass
    public static void prepareDatabase() {
        ServerApp.setUrl("jdbc:postgresql://ec2-46-137-84-173.eu-west-1.compute.amazonaws.com:5432/dctftmid36ou55?user=rmhzzizdqrajgj&password=b97cce549242c156238562ef5850dbd35f5cb77a4779129cf2d59ed3f2054528");
    }

    /**
     * Test que comprueba que las operaciones de la conexión individual
     * ifuncionen de manera correcta sobre una base de datos que opere,
     * de manera muy semejante a la base de datos que se utiliza en 
     * producción.
     * 
     * @throws Exception    Puede arrojar cualquier tipo de Excepción.
     */
    @Test
    public void checkIndividualConnection() throws Exception {
        // Obtenemos una conexion indivual.
        Connection conn = ServerApp.getConnection();

        // Si no es nulo, significa que ha pasado la prueba.
        Assert.assertNotNull(conn);
    }

    /**
     * Test que comprueba que las operaciones del grupo de conexiones
     * funcionen de manera correcta sobre una base de datos que opere,
     * de manera muy semejante a la base de datos que se utiliza en 
     * producción.
     * 
     * @throws Exception    Puede arrojar cualquier tipo de Excepción.
     */
    @Test
    public void checkPoolsConnection() throws Exception {
        // Variable comprobante del test.
        boolean testPassed = true;

        // Obtenemos una conexion del pool de conexiones.
        if(ServerPools.getController().acquireUsers() == null)
            testPassed = false;
        
        if(ServerPools.getController().acquireSales() == null)
            testPassed = false;

        if(ServerPools.getController().acquireProducts() == null)
            testPassed = false;
            
        if(ServerPools.getController().acquireRefunds() == null)
            testPassed = false;
            
        // if(ServerPools.getController().acquireInforms() == null)
        //     testPassed = false;
            
        // if(ServerPools.getController().acquireDiscounts() == null)
        //     testPassed = false;
            
        // Si no es falso, significa que ha pasado la prueba
        Assert.assertTrue(testPassed);
    }
}