package io.Mauzo.Server;

import io.Mauzo.Server.Managers.*;
import io.Mauzo.Server.Templates.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;

import java.util.Date;

public class ManagersTest {
    //Before para insertar datos en las tablas que tengan relación entre si y el administrador
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

        try (Statement st = DriverManager.getConnection(url).createStatement()) {
            st.execute("DROP TABLE IF EXISTS Refunds; DROP TABLE IF EXISTS Sales; DROP TABLE IF EXISTS Users; DROP TABLE IF EXISTS Products; DROP TABLE IF EXISTS Discounts;");
        }
    }

    /**
     * Hacemos un test que compruebe los métodos de la clase RefundsMgt
     *
     * @throws Exception Se indica que se espera que lance una excepción SQL
     */
    //TEST REFUNDS
    @Test
    public void testRefunds() throws Exception {
        Refund refund = new Refund();

        refund.setDateRefund(new Date());
        refund.setUserId(1);

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
    @Test
    public void testProducts() throws Exception {
        Product product = new Product();

        product.setCode("1878");
        product.setDescription("redondo");
        product.setName("balón");
        product.setPrice(10.0f);

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
    @Test
    public void testSales() throws Exception {
        Sale sale = new Sale();

        sale.setDiscId(1);
        sale.setProdId(1);
        sale.setUserId(1);
        sale.setStampRef(new Date());

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

        user.setUsername("brrrr");
        user.setFirstName("brrrrr");
        user.setLastName("brrrrrrrrr");
        user.setPassword("brrrrrrrrrrrr");
        user.setEmail("brrrrrrr@BRRRRRRRRRRR.br");

        UsersMgt usersMgt = ServerPools.getController().acquireUsers();

        usersMgt.add(user);
        User user1 = usersMgt.get(1);
        usersMgt.getList();
        usersMgt.modify(user1);
        usersMgt.remove(user1);

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


        discount.setCode("151");
        discount.setDesc("753");
        discount.setPriceDisc(15.9f);

        DiscountsMgt discountsMgt = ServerPools.getController().acquireDiscounts();

        discountsMgt.add(discount);
        Discount discount1 = discountsMgt.get(1);
        discountsMgt.getList();
        discountsMgt.modify(discount1);
        discountsMgt.remove(discount1);

        ServerPools.getController().releaseDiscounts(discountsMgt);
    }
}
