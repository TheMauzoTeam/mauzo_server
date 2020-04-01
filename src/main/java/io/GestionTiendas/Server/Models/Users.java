package io.GestionTiendas.Server.Models;

import java.util.List;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import javax.ws.rs.core.HttpHeaders;
import javax.servlet.http.HttpServletRequest;

// Paquetes para la validación del token de inicio de sesión.
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;

import io.GestionTiendas.Server.ServerApp;

public class Users {
    private static String base64Key = System.getenv("LOGIN_KEY");

    /**
     * Método para recuperar el token recibido desde la cabecera AUTHENTICATOR el
     * token de seguridad.
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
        if (authHeader.size() != 0)
            token = authHeader.get(0).substring(7).trim();

        // Devolvemos el token.
        return token;
    }

    /**
     * Método para validar si el token pasado por parametro es de un login valido o
     * no.
     * 
     * @param token El token JWT.
     * @return Verdadero si es valido o falso si no lo es.
     */
    public static boolean isLogged(final String token) {
        // Iniciamos el parser con la clave.
        boolean returnVar = false;

        try {
            // Inicializamos el parser del token.
            final JwtParser jwtsParser = Jwts.parser();

            // Cargamos la llave y validamos el token
            jwtsParser.setSigningKey(base64Key);
            jwtsParser.parseClaimsJws(token);

            // Si no ha lanzado una excepción anda okey el token.
            returnVar = true;
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
     * @param token El token JWT.
     * @return Verdadero si es valido o falso si no lo es.
     */
    public static boolean isAdmin(final String token) {
        // Iniciamos el parser con la clave.
        boolean returnVar = false;

        try {
            // Inicializamos el parser del token.
            final JwtParser jwtsParser = Jwts.parser();

            // Cargamos la llave y validamos el token.
            jwtsParser.setSigningKey(base64Key);
            jwtsParser.parseClaimsJws(token);

            // Obtenemos los datos del token.
            final Claims claims = jwtsParser.parseClaimsJws(token).getBody();

            // Validamos la conexión con la base de datos.
            if (ServerApp.getConnection() == null)
                ServerApp.setConnection();

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
}