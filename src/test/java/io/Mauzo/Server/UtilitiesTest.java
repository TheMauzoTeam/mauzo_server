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

import io.Mauzo.Server.Managers.*;
import io.Mauzo.Server.Templates.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

public class UtilitiesTest {

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
     * Comprueba que los token se generan y validan
     * correctamente con un usuario de ejemplo.
     *
     * @throws Exception
     */
    @Test
    public void testToken() throws Exception{
        // Obtenemos con UsersMgt el usuario de prueba.
        UsersMgt usersMgt = Connections.getController().acquireUsers();
        User userTest = usersMgt.get("pacoman");

        // Se obtiene una fecha limite del usuario de ejemplo.
        final long dateExp = System.currentTimeMillis() + 86400000;

        // Generación de la clave encriptada del token.
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(ServerUtils.loadProperties().getProperty("mauzo.test.encryptkey"));
        Key privateKey = new SecretKeySpec(apiKeySecretBytes, SignatureAlgorithm.HS512.getJcaName());

        // Establece la clave privada a modo de prueba.
        ServerUtils.setKey(privateKey);

        // Generación del token
        String token = Jwts.builder().setIssuedAt(new Date()).setIssuer(System.getenv("HOSTNAME"))
                .setId(Integer.toString(userTest.getId())).setSubject(userTest.getUsername())
                .claim("adm", userTest.isAdmin()).setExpiration(new Date(dateExp))
                .signWith(ServerUtils.getKey(), SignatureAlgorithm.HS512).compact();

        // Compromabos que el test ha hecho todo correctamente.
        Assert.assertTrue(usersMgt != null && userTest != null && ServerUtils.isTokenLogged(token) && ServerUtils.isTokenAdmin(token));

    }


    
}