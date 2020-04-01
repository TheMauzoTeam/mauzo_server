package io.GestionTiendas.Server.Helpers;

import java.sql.SQLException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.servlet.http.HttpServletRequest;

import io.GestionTiendas.Server.ServerApp;
import io.GestionTiendas.Server.Models.Users;

public class Utils {
    private interface Content {
        ResponseBuilder executeContent() throws SQLException;
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
     * @return La respuesta con la cual responderemos al cliente.
     */
    public static Response genericMethod(HttpServletRequest req, String paramId, String jsonData, Content content) {
        // Obtenemos el token
        String token = Users.getToken(req);

        // Convertimos la información JSON recibida en un objeto.
        ResponseBuilder response = null;

        if (Users.isLogged(token) != false) {
            try {
                // Validamos la conexión con la base de datos.
                if (ServerApp.getConnection() == null)
                    ServerApp.setConnection();

                // Lanzamos el resto de la secuencia a ejecutar.
                response = content.executeContent();
            } catch (SQLException e) {
                // Detectamos errores en la SQL
                ServerApp.getLoggerSystem().warning("Error en procesar la consulta SQL.");
                response = Response.serverError();
            } catch (Exception e) {
                // En caso de existir otros errores, devolvemos un error 500 y listo.
                ServerApp.getLoggerSystem().warning("Error imprevisto, devolviendo error 500...");
                response = Response.serverError();
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
     * @return La respuesta con la cual responderemos al cliente.
     */
    public static Response genericAdminMethod(HttpServletRequest req, String paramId, String jsonData, Content content) {
        // Obtenemos el token
        String token = Users.getToken(req);

        // Convertimos la información JSON recibida en un objeto.
        ResponseBuilder response = null;

        if (Users.isLogged(token) != false && Users.isAdmin(token) == true) {
            try {
                // Validamos la conexión con la base de datos.
                if (ServerApp.getConnection() == null)
                    ServerApp.setConnection();

                // Lanzamos el resto de la secuencia a ejecutar.
                response = content.executeContent();
            } catch (SQLException e) {
                // Detectamos errores en la SQL
                ServerApp.getLoggerSystem().warning("Error en procesar la consulta SQL.");
                response = Response.serverError();
            } catch (Exception e) {
                // En caso de existir otros errores, devolvemos un error 500 y listo.
                ServerApp.getLoggerSystem().warning("Error imprevisto, devolviendo error 500...");
                response = Response.serverError();
            }
        } else {
            response = Response.status(Status.FORBIDDEN);
        }

        // Lanzamos la respuesta.
        return response.build();
    }
}