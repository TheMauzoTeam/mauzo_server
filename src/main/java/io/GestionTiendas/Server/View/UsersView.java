package io.GestionTiendas.Server.View;

import java.util.Date;
import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.MediaType;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import io.GestionTiendas.Server.ServerUtils;
import io.GestionTiendas.Server.ServerApp;
import io.GestionTiendas.Server.Models.Users;
import io.GestionTiendas.Server.Controllers.UsersCtrl;

public class UsersView {
    /**
     * Vista para permitir el inicio de sesion de lo usuarios, cuya entrada será en
     * el http://HOST-SERVIDOR/api/login.
     * 
     * El contenido que recibirá esta vista http es mediante una peticion POST con
     * la estructura de atributos de username y password.
     * 
     * @param req      El header de la petición HTTP.
     * @param jsonData El body de la petición HTTP.
     * @return La respuesta generada por parte de la vista.
     */
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response loginMethod(@Context final HttpServletRequest req, String jsonData) {
        return ServerUtils.genericMethod(req, null, jsonData, () -> {
            ResponseBuilder response = null;

            // Convertimos la información JSON recibida en un objeto.
            final JsonObject jsonRequest = Json.createReader(new StringReader(jsonData)).readObject();

            final String username = jsonRequest.getString("username");
            final String password = jsonRequest.getString("password");

            Users userAux = UsersCtrl.getController().getUser(username);

            // Comprobamos la contraseña si es valida.
            if (userAux.getPassword() == password) {
                // Inicializamos las variables de retorno al usuario, el token durará un dia.
                String token = null;
                final long dateExp = System.currentTimeMillis() + 86400000;

                // Generamos el token de seguridad.
                // Comando para generar la key: openssl rand -base64 172 | tr -d '\n'
                token = Jwts.builder().setIssuedAt(new Date()).setIssuer(System.getenv("HOSTNAME"))
                        .setId(Integer.toString(userAux.getId())).setSubject(userAux.getUsername())
                        .claim("adm", userAux.isAdmin()).setExpiration(new Date(dateExp))
                        .signWith(SignatureAlgorithm.HS512, ServerUtils.getBase64Key()).compact();

                // Retornamos al cliente la respuesta con el token.
                response = Response.status(Status.OK);
                response.header(HttpHeaders.AUTHORIZATION, "Bearer" + " " + token);
            } else {
                // En caso de no estar disponible, paramos el login.
                ServerApp.getLoggerSystem().severe("Inicio de sesión fallido para la IP: " + req.getRemoteAddr());
                response = Response.status(Status.FORBIDDEN);
            }

            return response;
        });
    }

    /**
     * Vista que permite a un administrador registrar usuarios dentro del servidor,
     * permitiendo asi agregar de manera dinamica usuarios validos o otros
     * administradores validos dentro del sistema.
     * 
     * El contenido que recibirá esta vista http es mediante una peticion POST con
     * la estructura de atributos de username, email, password, firstname, lastname
     * y isAdmin.
     * 
     * @param req      El header de la petición HTTP.
     * @param jsonData El body de la petición HTTP.
     * @return La respuesta generada por parte de la vista.
     */
    @POST
    @Path("/users")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerMethod(@Context final HttpServletRequest req, String jsonData) {
        return ServerUtils.genericAdminMethod(req, null, jsonData, () -> {
            // Incializamos el objeto.
            Users userAux = new Users();

            // Convertimos la información JSON recibida en un objeto.
            final JsonObject jsonRequest = Json.createReader(new StringReader(jsonData)).readObject();

            // Agregamos la información al usuario.
            userAux.setUsername(jsonRequest.getString("username"));
            userAux.setFirstName(jsonRequest.getString("firstname"));
            userAux.setLastName(jsonRequest.getString("lastname"));
            userAux.setEmail(jsonRequest.getString("email"));
            userAux.setPassword(jsonRequest.getString("password"));
            userAux.setAdmin(jsonRequest.getBoolean("isadmin"));

            // Agregamos el usuario a la lista.
            UsersCtrl.getController().addUser(userAux);

            // Si todo ha ido bien hasta ahora, lanzamos la respuesta 200 OK.
            return Response.status(Status.OK);
        });
    }
}