package io.Mauzo.Server;

import io.Mauzo.Server.Managers.*;
import io.Mauzo.Server.Templates.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Date;

public class ManagersTest {
    //Before para insertar datos en las tablas que tengan relación entre si y el administrador
    /**
     * Preparación de la URL de JDBC con la base de datos
     * de pruebas donde se ejecutarán todos los test de
     * validación respecto a los métodos a probar.
     */
    @Before
    public void prepareDatabase() throws Exception {
        String url = ServerUtils.loadProperties().getProperty("mauzo.debugDatabase.url");
        
        ServerApp.setUrl(url);

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se ha encontrado el driver de PostgreSQL: " + e.toString());
        }

        UsersMgt usersMgt = Connections.getController().acquireUsers();
        ProductsMgt productsMgt = Connections.getController().acquireProducts();
        SalesMgt salesMgt = Connections.getController().acquireSales();
        DiscountsMgt discountsMgt = Connections.getController().acquireDiscounts();
        RefundsMgt refundsMgt = Connections.getController().acquireRefunds();

        try (Statement st = DriverManager.getConnection(url).createStatement()) {
            st.execute("TRUNCATE TABLE Refunds CASCADE; TRUNCATE TABLE Sales CASCADE; TRUNCATE TABLE Users CASCADE; TRUNCATE TABLE Products CASCADE; TRUNCATE TABLE Discounts CASCADE;");
            st.execute("ALTER SEQUENCE discounts_id_seq RESTART; ALTER SEQUENCE products_id_seq RESTART; ALTER SEQUENCE users_id_seq RESTART; ALTER SEQUENCE sales_id_seq RESTART; ALTER SEQUENCE refunds_id_seq RESTART;");

            User user = new User();
            user.setFirstName("Paco");
            user.setLastName("Sanchez");
            user.setEmail("paco@gmail.com");
            user.setUsername("pacoman");
            user.setPassword("pacothebest");
            user.setAdmin(true);
            usersMgt.add(user);

            Product product = new Product();
            product.setName("raqueta");
            product.setCode("1452");
            product.setDescription("Con cuerdas");
            product.setPrice(1.52f);
            productsMgt.add(product);

            Discount discount = new Discount();
            discount.setCode("56230");
            discount.setDesc("50%");
            discount.setPriceDisc(15f);
            discountsMgt.add(discount);

            Sale sale = new Sale();
            sale.setStampRef(new Date());
            sale.setUserId(usersMgt.get("pacoman").getId());
            sale.setDiscId(discountsMgt.get("56230").getId());
            sale.setProdId(productsMgt.get("1452").getId());
            salesMgt.add(sale);

            Refund refund = new Refund();
            refund.setSaleId(1);
            refund.setUserId(usersMgt.get("pacoman").getId());
            refund.setDateRefund(new Date());
            refundsMgt.add(refund);

        } finally {
            Connections.getController().releaseUsers(usersMgt);
            Connections.getController().releaseProducts(productsMgt);
            Connections.getController().releaseRefunds(refundsMgt);
            Connections.getController().releaseDiscounts(discountsMgt);
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
        RefundsMgt refundsMgt = Connections.getController().acquireRefunds();
        Refund refund = refundsMgt.get(1);
        Connections.getController().releaseRefunds(refundsMgt);

        Assert.assertTrue( refundsMgt != null && refund.getDateRefund() != null);

    }

    //TEST PRODUCTOS
    /**
     * Hacemos un test que compruebe los métodos de la clase ProductsMgt
     *
     * @throws Exception Se indica que se espera que lance una excepción SQL
     */
    @Test
    public void testProducts() throws Exception {
        ProductsMgt productsMgt = Connections.getController().acquireProducts();
        Product product = productsMgt.get(1);
        product.setName("pelota");
        productsMgt.modify(product);
        product = productsMgt.get(1);
        Connections.getController().releaseProducts(productsMgt);

        Assert.assertTrue(productsMgt != null && product.getCode() != null && product.getName().equals("pelota"));
    }

    //TEST SALES
    /**
     * Hacemos un test que compruebe los métodos de la clase SalesMgt
     *
     * @throws Exception Se indica que se espera que lance una excepción SQL
     */
    @Test
    public void testSales() throws Exception {
        SalesMgt salesMgt = Connections.getController().acquireSales();
        Sale sale = salesMgt.get(1);

        Connections.getController().releaseSales(salesMgt);

        Assert.assertTrue(salesMgt != null);
    }

    //TEST USERS
    /**
     * Hacemos un test que compruebe los métodos de la clase UsersMgt
     *
     * @throws Exception Se indica que se espera que lance una excepción SQL
     */
    @Test
    public void testUsers() throws Exception {
        UsersMgt usersMgt = Connections.getController().acquireUsers();
        User user = usersMgt.get(1);

        user.setUsername("adolfo");
        usersMgt.modify(user);
        user = usersMgt.get(1);
        Connections.getController().releaseUsers(usersMgt);

        Assert.assertTrue(usersMgt != null && user.getFirstName() != null && user.getUsername().equals("adolfo"));
    }

    //TEST DISCOUNTS
    /**
     * Hacemos un test que compruebe los métodos de la clase Discounts
     *
     * @throws Exception Se indica que se espera que lance una excepción SQL
     */
    @Test
    public void testDiscounts() throws Exception {
        DiscountsMgt discountsMgt = Connections.getController().acquireDiscounts();
        Discount discount = discountsMgt.get(1);

        discount.setPriceDisc(78f);
        discountsMgt.modify(discount);
        discount = discountsMgt.get(1);
        Connections.getController().releaseDiscounts(discountsMgt);

        Assert.assertTrue(discountsMgt != null && discount.getCode() != null && discount.getPrizeDisc().equals(78f));
    }
}
