package io.GestionTiendas.Server.View;

import java.util.Date;
import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.MediaType;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import io.GestionTiendas.Server.ServerApp;
import io.GestionTiendas.Server.Controllers.UsersCtrl;
import io.GestionTiendas.Server.Helpers.Utils;
import io.GestionTiendas.Server.Models.Users;

@Path("/login")
public class UsersView {
    private static String base64Key = System.getenv("LOGIN_KEY");

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postMethod(@Context final HttpServletRequest req, @PathParam("param_id") String paramId, String jsonData) {
        return Utils.genericMethod(req, paramId, jsonData, () -> {
            ResponseBuilder response = null;

            // Convertimos la información JSON recibida en un objeto.
            final JsonObject jsonRequest = Json.createReader(new StringReader(jsonData)).readObject();

            final String username = jsonRequest.getString("username");
            final String password = jsonRequest.getString("password");

            Users userAux = UsersCtrl.getUser(username);

            // Comprobamos la contraseña si es valida
            if (userAux.isPassword(password)) {
                // Inicializamos las variables de retorno al usuario, el token durará un dia.
                String token = null;
                final long dateExp = System.currentTimeMillis() + 86400000;

                // Generamos el token de seguridad.
                // Comando para generar la key: openssl rand -base64 172 | tr -d '\n'
                token = Jwts.builder().setIssuedAt(new Date()).setIssuer(System.getenv("HOSTNAME"))
                        .setId(Integer.toString(userAux.getId())).setSubject(userAux.getUsername())
                        .claim("adm", userAux.isAdmin()).setExpiration(new Date(dateExp))
                        .signWith(SignatureAlgorithm.HS512, base64Key).compact();

                // Retornamos al cliente la respuesta con el token.
                response = Response.status(Status.ACCEPTED);
                response.header(HttpHeaders.AUTHORIZATION, "Bearer" + " " + token);
            } else {
                // En caso de no estar disponible, paramos el login.
                ServerApp.getLoggerSystem().severe("Inicio de sesión fallido para la IP: " + req.getRemoteAddr());
                response = Response.status(Status.FORBIDDEN);
            }

            return response;
        });
    }
}