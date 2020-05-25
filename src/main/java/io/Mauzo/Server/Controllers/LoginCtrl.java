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
package io.Mauzo.Server.Controllers;

// Paquetes relacionados con el framework estandar.
import java.io.StringReader;
import java.util.Date;

// Paquetes relacionados con Json.
import javax.json.Json;
import javax.json.JsonObject;

// Paquetes relacionados con el servidor.
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

// Paquetes relacionados con Spring Boot.
import org.springframework.stereotype.Component;

// Paquetes relacionados con Json Web Token.
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

// Paquetes relacionados con el Proyecto.
import io.Mauzo.Server.ServerApp;
import io.Mauzo.Server.ServerUtils;
import io.Mauzo.Server.Managers.Connections;
import io.Mauzo.Server.Managers.UsersMgt;
import io.Mauzo.Server.Managers.ManagersIntf.ManagerErrorException;
import io.Mauzo.Server.Templates.User;

/**
 * Clase controladora de inicios de sesion de usuarios, la cual 
 * valida si el usuario y la contraseña proporcionados pueden 
 * iniciar sesión en el sistema.
 * 
 * Para ello, utilizamos una contraseña encriptada en MD5, la 
 * cual nos enviará el cliente ya encripada. En caso de ser un 
 * login correcto. Le enviaremos un Json Web Token valido para
 * operar con el resto de endpoints.
 * 
 * @author neirth Sergio Martinez
 */
@Component
@Path("/login")
public class LoginCtrl {
    /**
     * Controlador para permitir el inicio de sesion de lo usuarios, cuya entrada
     * será en el http://HOST-SERVIDOR/api/login.
     * 
     * El contenido que recibirá esta vista http es mediante una peticion POST con
     * la estructura de atributos de username y password.
     * 
     * @param req      El header de la petición HTTP.
     * @param jsonData El body de la petición HTTP.
     * @return La respuesta generada por parte de la vista.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response loginMethod(@Context final HttpServletRequest req, String jsonData) {
        return ServerUtils.genericMethod(req, null, jsonData, () -> {
            ResponseBuilder response = Response.status(Status.BAD_REQUEST);

            // Si la informacion que recibe es nula, no se procesa nada.
            if(jsonData.length() != 0) {
                // Convertimos la información JSON recibida en un objeto.
                final JsonObject jsonRequest = Json.createReader(new StringReader(jsonData)).readObject();

                final String username = jsonRequest.getString("username");
                final String password = jsonRequest.getString("password");

                UsersMgt usersMgt = Connections.getController().acquireUsers();
                
                try {
                    User userAux = usersMgt.get(username);

                    if(userAux != null) {
                        // Comprobamos la contraseña si es valida.
                        if (userAux.getPassword().equalsIgnoreCase(password)) {
                            // Inicializamos las variables de retorno al usuario, el token durará un dia.
                            String token = null;
                            final long dateExp = System.currentTimeMillis() + 86400000;

                            // Generamos el token de seguridad.
                            // Comando para generar la key: openssl rand -base64 172 | tr -d '\n'
                            token = Jwts.builder().setIssuedAt(new Date()).setIssuer(System.getenv("HOSTNAME"))
                                    .setId(Integer.toString(userAux.getId())).setSubject(userAux.getUsername())
                                    .claim("adm", userAux.isAdmin()).setExpiration(new Date(dateExp))
                                    .signWith(ServerUtils.getKey(), SignatureAlgorithm.HS512).compact();

                            // Retornamos al cliente la respuesta con el token.
                            response = Response.status(Status.OK);
                            response.header(HttpHeaders.AUTHORIZATION, "Bearer" + " " + token);
                        } else {
                            throw new ManagerErrorException("Login invalido para el usuario " + username + " con IP " + req.getRemoteAddr());
                        }
                    } else {
                        throw new ManagerErrorException("Login invalido para el usuario " + username + " con IP " + req.getRemoteAddr());
                    }
                } catch (ManagerErrorException e) {
                    ServerApp.getLoggerSystem().error(e.toString());
                    response = Response.status(Status.FORBIDDEN);
                } finally {
                    Connections.getController().releaseUsers(usersMgt);
                }
            }
            
            return response;
        });
    }
}