package io.Mauzo.Server;

import io.Mauzo.Server.Managers.*;
import io.Mauzo.Server.Templates.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ManagersTest {
    /**
     * Preparación de la URL de JDBC con la base de datos
     * de pruebas donde se ejecutarán todos los test de
     * validación respecto a los métodos a probar.
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

        try (Connection connection = DriverManager.getConnection(url)) {
            connection.createStatement().execute("DROP TABLE IF EXISTS Sales; DROP TABLE IF EXISTS Refunds; DROP TABLE IF EXISTS Users; DROP TABLE IF EXISTS Products; DROP TABLE IF EXISTS Discounts;"); 
        }
    }

    /**
     * Hacemos un test que compruebe los métodos de la clase RefundsMgt
     *
     * @throws Exception Se indica que se espera que lance una excepción SQL
     */
    //TEST REFUNDS
    @Test(expected = SQLException.class)
    public void testRefunds() throws Exception {
        Refund refund = new Refund();

        RefundsMgt refundsMgt = ServerPools.getController().acquireRefunds();

        //Probamos los métodos
        refundsMgt.add(refund);
        refundsMgt.get(0);
        refundsMgt.getList();
        refundsMgt.modify(refund);
        refundsMgt.remove(refund);

        ServerPools.getController().releaseRefunds(refundsMgt);
    }

    //TEST PRODUCTOS
    /**
     * Hacemos un test que compruebe los métodos de la clase ProductsMgt
     *
     * @throws Exception Se indica que se espera que lance una excepción SQL
     */
    @Test(expected = SQLException.class)
    public void testProducts() throws Exception {
        Product product = new Product();

        ProductsMgt productsMgt = ServerPools.getController().acquireProducts();

        productsMgt.add(product);
        productsMgt.get(0);
        productsMgt.get("cable");
        productsMgt.getList();
        productsMgt.modify(product);
        productsMgt.remove(product);

        ServerPools.getController().releaseProducts(productsMgt);
    }

    /**
     * Hacemos un test que compruebe los métodos de la clase SalesMgt
     *
     * @throws Exception Se indica que se espera que lance una excepción SQL
     */
    //TEST SALES
    @Test(expected = SQLException.class)
    public void testSales() throws Exception {
        Sale sale = new Sale();

        SalesMgt salesMgt = ServerPools.getController().acquireSales();

        salesMgt.add(sale);
        salesMgt.get(0);
        salesMgt.getList();
        salesMgt.modify(sale);
        salesMgt.remove(sale);

        ServerPools.getController().releaseSales(salesMgt);
    }

    /**
     * Hacemos un test que compruebe los métodos de la clase UsersMgt
     *
     * @throws Exception Se indica que se espera que lance una excepción SQL
     */
    //TEST USERS
    @Test
    public void testUsers() throws Exception {
        User user = new User();

        UsersMgt usersMgt = ServerPools.getController().acquireUsers();

        usersMgt.add(user);
        usersMgt.get(0);
        usersMgt.get("Estefania");
        usersMgt.getList();
        usersMgt.modify(user);
        usersMgt.remove(user);

        ServerPools.getController().releaseUsers(usersMgt);
    }

    /**
     * Hacemos un test que compruebe los métodos de la clase Discounts
     *
     * @throws Exception Se indica que se espera que lance una excepción SQL
     */
    //TEST DISCOUNTS
    @Test
    public void testDiscounts() throws Exception {
        Discount discount = new Discount();

        DiscountsMgt discountsMgt = ServerPools.getController().acquireDiscounts();

        discountsMgt.add(discount);
        discountsMgt.get(0);
        discountsMgt.getList();
        discountsMgt.modify(discount);
        discountsMgt.remove(discount);

        ServerPools.getController().releaseDiscounts(discountsMgt);
    }
}
