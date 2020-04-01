package io.GestionTiendas.Server.Helpers;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import io.GestionTiendas.Server.ServerApp;

public class Utils {
    private interface Content {
        void executeContent() throws SQLException;
    }
    
    /**
     * Método para procesar y responder a una petición generica con las comprobaciones,
     * requeridas por el servidor, y por ende por el proyecto.
     * 
     * Se checkeará si el usuario ha iniciado sesión validando el token que nos 
     * habrá entregado en la cabecera AUTHENTICATOR de la petición HTTP.
     * 
     * Si la cabecera no estuviera presente, si hubiera algún inconveniente con la
     * base de datos o otro problema, el método devolverá al cliente un error 500.
     * 
     * Si no, devolverá un 200 indicando que ha sido procesado correctamente.
     * 
     * @param req La petición HTTP.
     * @param paramId A que Id se refiere, en caso de referirse a uno.
     * @param jsonData Los datos en Json que ha enviado el cliente
     * @param content Funcion lambda con todos los pasos a seguir.
     * @return La respuesta con la cual responderemos al cliente.
     */
    public static Response genericMethod(HttpServletRequest req, String paramId, String jsonData, Content content) {
        // Obtenemos el token
        String token = Utils.getToken(req);

        // Convertimos la información JSON recibida en un objeto.
        final JsonObject jsonRequest = Json.createReader(new StringReader(jsonData)).readObject();
        ResponseBuilder response = null;

        if (Utils.isLogged(token) != false) {
            try {
                // Validamos la conexión con la base de datos.
                if (ServerApp.getConnection() == null)
                    ServerApp.setConnection();

                final Connection mainSql = ServerApp.getConnection();
                
                // Lanzamos el resto de la secuencia a ejecutar.
                content.executeContent();

                // Entregamos el mensaje construido y construimos los encabezados de la respuesta.
                response = Response.status(Status.OK);
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

    public static Response genericAdminMethod(HttpServletRequest req, String paramId, String jsonData, Content content) {
        // Obtenemos el token
        String token = Utils.getToken(req);

        // Convertimos la información JSON recibida en un objeto.
        final JsonObject jsonRequest = Json.createReader(new StringReader(jsonData)).readObject();
        ResponseBuilder response = null;

        if (Utils.isLogged(token) != false && Utils.isAdmin(token) == true) {
            try {
                // Validamos la conexión con la base de datos.
                if (ServerApp.getConnection() == null)
                    ServerApp.setConnection();

                final Connection mainSql = ServerApp.getConnection();
                
                // Lanzamos el resto de la secuencia a ejecutar.
                content.executeContent();

                // Entregamos el mensaje construido y construimos los encabezados de la respuesta.
                response = Response.status(Status.OK);
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