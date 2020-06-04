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
import java.awt.image.BufferedImage;
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
            discount.setPricePerc(15f);
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

    /**
     * Comprueba que la integridad de la imágen se mantiene en el cambio de formatos.
     *
     * @throws Exception
     */
    @Test
    public void testImage() throws Exception{
        boolean result = true; // Resultado de la prueba

        // Pasamos la imágen de Base 64 a binario.
        String image64 = "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";
        BufferedImage image = ServerUtils.imageFromByteArray(ServerUtils.byteArrayFromBase64(image64));

        // Se pasa la imágen de binario a Base 64
        String img64Aux = ServerUtils.byteArrayToBase64(ServerUtils.imageToByteArray(image, "png"));
        BufferedImage image1 = ServerUtils.imageFromByteArray(ServerUtils.byteArrayFromBase64(img64Aux));

        // Comprobamos la correspondencia de los colores pixel a pixel.
        if (image.getWidth() == image1.getWidth() && image.getHeight() == image1.getHeight()) {
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    if (image.getRGB(x, y) != image1.getRGB(x, y))
                        result = false;
                }
            }
        } else {
            result = false;
        }

        Assert.assertTrue(result); // Si no ha habido una incongluencia el test se pasará.
    }
    
}