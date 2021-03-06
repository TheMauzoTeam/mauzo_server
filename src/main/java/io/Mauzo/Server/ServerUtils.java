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

// Paquetes del framework estandar de java.
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;

// Paquetes del framework extendido de java.
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.DatatypeConverter;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;

// Paquetes de la libreria de registros log4j.
import org.apache.logging.log4j.Level;

// Paquetes para la validación del token de inicio de sesión.
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.SignatureException;

/**
 * Clase de utiliades del proyecto de Mauzo Server.
 * 
 * Esta clase contiene una serie de utilidades usadas en las clases
 * controladoras, metodos de conversión de imagenes a byte array y base64 o
 * comprobar si el token es valido.
 * 
 * @author lluminar Lidia Martínez
 * @author Neirth Sergio Martinez
 * @author Ant04X Antonio Izquierdo
 */
public class ServerUtils {
    public interface Content {
        /**
         * Funcion lambda para permitir la personalizacion de instrucciones que se van a
         * ejecutar en los metodos generic de este archivo de utilidades.
         * 
         * @return La respuesta del servidor preconstruida.
         * @throws Exception Puede devolver cualquier tipo de excepcion que será
         *                   capturado por los métodos generic.
         */
        ResponseBuilder executeContent() throws Exception;
    }

    private static Key privateKey;

    /**
     * Método para procesar y responder a una petición generica con las
     * comprobaciones, requeridas por el servidor, y por ende por el proyecto.
     * 
     * Se checkeará si el usuario ha iniciado sesión validando el token que nos
     * habrá entregado en la cabecera AUTHENTICATOR de la petición HTTP.
     * 
     * Si la cabecera no estuviera presente, si hubiera algún inconveniente con la
     * base de datos o otro problema, el método devolverá al cliente un error 500.
     * 
     * Si no, devolverá un 200 indicando que ha sido procesado correctamente.
     * 
     * @param req      La petición HTTP.
     * @param paramId  A que Id se refiere, en caso de referirse a uno.
     * @param jsonData Los datos en Json que ha enviado el cliente
     * @param content  Funcion lambda con todos los pasos a seguir.
     * 
     * @author Neirth Sergio Martinez
     * 
     * @return La respuesta con la cual responderemos al cliente.
     */
    public static Response genericMethod(HttpServletRequest req, Integer paramId, String jsonData, Content content) {
        // Convertimos la información JSON recibida en un objeto.
        ResponseBuilder response = null;

        try {
            // Lanzamos el resto de la secuencia a ejecutar.
            response = content.executeContent();
        } catch (Exception e) {
            // Invocamos la funcion para escribir en el registro la excepción.
            writeServerException(e);

            // Establecemos la respuesta como error.
            response = Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage());
        }

        // Lanzamos la respuesta.
        return response.build();
    }

    /**
     * Método para procesar y responder a una petición generica con las
     * comprobaciones, requeridas por el servidor, y por ende por el proyecto.
     * 
     * Se checkeará si el usuario ha iniciado sesión validando el token que nos
     * habrá entregado en la cabecera AUTHENTICATOR de la petición HTTP.
     * 
     * Si la cabecera no estuviera presente, si hubiera algún inconveniente con la
     * base de datos o otro problema, el método devolverá al cliente un error 500.
     * 
     * Si no, devolverá un 200 indicando que ha sido procesado correctamente.
     * 
     * @param req      La petición HTTP.
     * @param paramId  A que Id se refiere, en caso de referirse a uno.
     * @param jsonData Los datos en Json que ha enviado el cliente
     * @param content  Funcion lambda con todos los pasos a seguir.
     *
     * @author Neirth Sergio Martinez
     * 
     * @return La respuesta con la cual responderemos al cliente.
     */
    public static Response genericUserMethod(HttpServletRequest req, Integer paramId, String jsonData,
            Content content) {
        // Obtenemos el token
        String token = getToken(req);

        // Convertimos la información JSON recibida en un objeto.
        ResponseBuilder response = null;

        if (isTokenLogged(token) != false) {
            try {
                // Lanzamos el resto de la secuencia a ejecutar.
                response = content.executeContent();
            } catch (Exception e) {
                // Invocamos la funcion para escribir en el registro la excepción.
                writeServerException(e);

                // Establecemos la respuesta como error.
                response = Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage());
            }
        } else {
            response = Response.status(Status.FORBIDDEN);
        }

        // Lanzamos la respuesta.
        return response.build();
    }

    /**
     * Método para procesar y responder a una petición generica con las
     * comprobaciones requeridas a un usuario administrador, requeridas por el
     * servidor, y por ende por el proyecto.
     * 
     * Se checkeará si el usuario ha iniciado sesión validando el token que nos
     * habrá entregado en la cabecera AUTHENTICATOR de la petición HTTP.
     * 
     * Si la cabecera no estuviera presente, si hubiera algún inconveniente con la
     * base de datos o otro problema, el método devolverá al cliente un error 500.
     * 
     * Si no, devolverá un 200 indicando que ha sido procesado correctamente.
     * 
     * @param req      La petición HTTP.
     * @param paramId  A que Id se refiere, en caso de referirse a uno.
     * @param jsonData Los datos en Json que ha enviado el cliente
     * @param content  Funcion lambda con todos los pasos a seguir.
     * 
     * @author Neirth Sergio Martinez
     * 
     * @return La respuesta con la cual responderemos al cliente.
     */
    public static Response genericAdminMethod(HttpServletRequest req, Integer paramId, String jsonData,
            Content content) {
        // Obtenemos el token
        String token = getToken(req);

        // Convertimos la información JSON recibida en un objeto.
        ResponseBuilder response = null;

        if (isTokenLogged(token) != false && isTokenAdmin(token) == true) {
            try {
                // Lanzamos el resto de la secuencia a ejecutar.
                response = content.executeContent();
            } catch (Exception e) {
                // Invocamos la funcion para escribir en el registro la excepción.
                writeServerException(e);

                // Establecemos la respuesta como error.
                response = Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage());
            }
        } else {
            response = Response.status(Status.FORBIDDEN);
        }

        // Lanzamos la respuesta.
        return response.build();
    }

    /**
     * Este metodo privado de la clase de utilidades escribe en el registro, en
     * función de que si el servidor está trabajando en modo debug o en modo release
     * la información en el registro.
     * 
     * En caso de estar en modo debug, el servidor mostrará todo el Stack Trace del
     * cual se ha propiciado el fallo capturado, en caso contrario, solo se mostrará
     * un mensaje de la excepción causante del problema.
     * 
     * @author Neirth Sergio Martinez
     * 
     * @param e La excepción capturada.
     */
    public static void writeServerException(Exception e) {
        // En caso de existir otros errores, devolvemos un error 500 y listo.
        if (ServerApp.getLoggerSystem().getLevel() == Level.DEBUG) {
            // Informacion necesaria en procesos de debug.
            ServerApp.getLoggerSystem().debug("An exception has occurred, getting the stacktrace of the exception: ");
            e.printStackTrace();
        } else {
            // Informacion a mostrar en procesos de producción.
            ServerApp.getLoggerSystem().error("An exception has occurred, " + e.toString());
        }
    }

    /**
     * Método para recuperar el token recibido desde la cabecera AUTHORIZATION el
     * token de seguridad.
     * 
     * @author Neirth Sergio Martinez
     * 
     * @param req La cabecera de la petición.
     * @return El token recibido.
     */
    public static String getToken(final HttpServletRequest req) {
        // Declaramos las variables
        String token = null;
        List<String> authHeader = null;

        // Obtenemos el token
        authHeader = Collections.list(req.getHeaders(HttpHeaders.AUTHORIZATION));

        // Comprobamos que exista un token.
        // FIXME: 08/06/2020 Es al contrario, ..., el if va en un nivel superior al try
        try {
            if (authHeader.size() != 0)
                token = authHeader.get(0).substring(7).trim();    
        } catch (Exception e) {
            token = null;
        }


        // Devolvemos el token.
        return token;
    }

    /**
     * Método para validar si el token pasado por parametro es de un login valido o
     * no.
     * 
     * @author Neirth Sergio Martinez
     * 
     * @param token El token JWT.
     * @return Verdadero si es valido o falso si no lo es.
     */
    public static boolean isTokenLogged(final String token) {
        // Iniciamos el parser con la clave.
        boolean returnVar = false;

        try {
            // Inicializamos el parser del token.
            final JwtParser jwtsParser = Jwts.parser();

            // Cargamos la llave y validamos el token
            jwtsParser.setSigningKey(ServerUtils.getKey());

            // Obtenemos los datos del token.
            final Claims claims = jwtsParser.parseClaimsJws(token).getBody();

            // Ejecutamos la consulta de verificación.
            Statement st = ServerApp.getConnection().createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM Users WHERE id = " + claims.getId());

            // Obtenemos el resultado.
            rs.next();

            // Si no ha lanzado una excepción anda okey el token.
            returnVar = claims.getId().equals(rs.getString("id"));
        } catch (final SignatureException e) {
            // Si la firma es invalida, anulamos el token.
            returnVar = false;
        } catch (final ExpiredJwtException e) {
            // Si el token ha expirado, anulamos el token.
            returnVar = false;
        } catch (final Exception e) {
            // Si es cualquier otra excepción no prevista, anulamos el token.
            returnVar = false;
        }

        // Devolvemos el estado del token esté como este.
        return returnVar;
    }

    /**
     * Método para validar si el token pasado por paraametro es de un usuario
     * administrador.
     * 
     * @author Neirth Sergio Martinez
     * 
     * @param token El token JWT.
     * @return Verdadero si es valido o falso si no lo es.
     */
    public static boolean isTokenAdmin(final String token) {
        // Iniciamos el parser con la clave.
        boolean returnVar = false;

        try {
            // Inicializamos el parser del token.
            final JwtParser jwtsParser = Jwts.parser();

            // Cargamos la llave.
            jwtsParser.setSigningKey(ServerUtils.getKey());

            // Validamos y obtenemos los datos del token.
            final Claims claims = jwtsParser.parseClaimsJws(token).getBody();

            // Ejecutamos la consulta de verificación.
            Statement st = ServerApp.getConnection().createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM Users WHERE id = " + claims.getId());

            // Obtenemos el resultado.
            rs.next();

            // Si no ha lanzado una excepción, comprobamos si es admin.
            returnVar = claims.get("adm").toString().equals("true") && rs.getBoolean("isAdmin");
        } catch (final SignatureException e) {
            // Si la firma es invalida, anulamos el token.
            returnVar = false;
        } catch (final ExpiredJwtException e) {
            // Si el token ha expirado, anulamos el token.
            returnVar = false;
        } catch (final Exception e) {
            // Si es cualquier otra excepción no prevista, anulamos el token.
            returnVar = false;
        }

        // Devolvemos el estado del token esté como este.
        return returnVar;
    }

    /**
     * Método para recuperar la llave que firma el Json Web Token que recibe el
     * cliente, tambien sirve esta llave para validar si el token de seguridad no ha
     * sido manipulado.
     * 
     * @author Neirth Sergio Martinez
     * 
     * @return Llave en forma de objeto del Json Web Token
     */
    public static Key getKey() {
        if (privateKey == null) {
            byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(System.getenv("LOGIN_KEY"));
            privateKey = new SecretKeySpec(apiKeySecretBytes, SignatureAlgorithm.HS512.getJcaName());
        }

        return privateKey;
    }
    
    /**
     * Introduce una clave privada arbitraria para propósitos de Test.
     *
     * @author Ant04x Antonio Izquierdo
     *
     * @param privateKey
     */
    static void setKey(Key privateKey) {
        ServerUtils.privateKey = privateKey;
    }

    /**
     * Método estatico para convertir una imagen a un array de bytes.
     * 
     * Hay componentes en este servidor que trabajan con imagenes, dado que los
     * productos y los usuarios tienen esta posibilidad de mostrar una imagen.
     * 
     * @author lluminar Lidia Martínez
     *
     * @param imageBuf La imagen en buffer.
     * @param type     El formato de salida de la image.
     * @return La imagen convertida a un array de bytes.
     */
    public static byte[] imageToByteArray(BufferedImage imageBuf, String type) {
        // Declaramos la variable de salida.
        byte[] imageArr = null;

        if (imageBuf != null) {
            // Abrimos un stream de salida
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                // Convertimos la imagen a un byte array
                ImageIO.write(imageBuf, type, out);
                imageArr = out.toByteArray();
            } catch (Exception e) {
                // En caso de problemas, devolvemos un null.
                imageArr = null;
            }
        }

        return imageArr;
    }

    /**
     * Método estatico para convertir un array de bytes a una imagen.
     * 
     * Hay componentes en este servidor que trabajan con imagenes, dado que los
     * productos y los usuarios tienen esta posibilidad de mostrar una imagen.
     * 
     * @author lluminar Lidia Martínez
     *
     * @param imageArr La imagen en array de bytes.
     * @return La imagen convertida a un BufferedImage.
     */
    public static BufferedImage imageFromByteArray(byte[] imageArr) {
        // Declaramos la variable de salida.
        BufferedImage imageBuf = null;

        if (imageArr != null) {
            // Abrimos un stream de entrada
            try (InputStream in = new ByteArrayInputStream(imageArr)) {
                // Convertimos el bytearray a una imagen.
                imageBuf = ImageIO.read(in);
            } catch (IOException e) {
                // En caso de problemas, devolvemos un null.
                imageBuf = null;
            }
        }

        return imageBuf;
    }

    /**
     * Tranforma una cadena en Base64 a un array de bytes
     * 
     * @author lluminar Lidia Martínez
     *
     * @param base64 Cadena de carácteres en Base64
     * @return Array de Bytes
     */
    public static byte[] byteArrayFromBase64(String base64) {
        return (base64 == null) ? null : Base64.getDecoder().decode(base64);
    }

    /**
     * Transforma un array de Bytes a una cadena de Base64
     * 
     * @author lluminar Lidia Martínez
     *
     * @param array array de bytes
     * @return Cadena de carácteres en Base64
     */
    public static String byteArrayToBase64(byte[] array) {
        return (array == null) ? null : Base64.getEncoder().encodeToString(array);
    }

    /**
     * Obtiene el fichero application.properties, lo mapea a una nueva instancia
     * Properties y la devuelve para su uso.
     * 
     * @author Neirth Sergio Martinez
     * 
     * @return El objeto de properties.
     */
    public static Properties loadProperties() {
        // Obtenemos el contexto actual
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        // Creamos un objeto properties
        Properties properties = new Properties();

        // Obtenemos el fichero application.properties y lo cargamos en el objeto
        try (InputStream resourceStream = loader.getResourceAsStream("application.properties")) {
            properties.load(resourceStream);
        } catch (IOException e) {
            ServerApp.getLoggerSystem().error("Error obtaining application.properties");

            e.printStackTrace();
        }

        // Devolvemos el objeto
        return properties;
    }
}