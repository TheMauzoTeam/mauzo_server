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

// Paquetes relativos al framework estandar de Java.
import java.io.StringReader;

// Paquetes relativos a los Json de entrada y salida.
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

// Paquetes relativos a la interfaz web.
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.MediaType;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

// Paquetes propios de la aplicación.
import io.Mauzo.Server.ServerUtils;
import io.Mauzo.Server.ServerApp;
import io.Mauzo.Server.Templates.User;
import io.Mauzo.Server.Managers.Connections;
import io.Mauzo.Server.Managers.UsersMgt;
import io.Mauzo.Server.Managers.ManagersIntf.ManagerErrorException;

/**
 * Clase controladora de usuarios, la cual gestiona las operaciones CRUD
 * con la base de datos a traves de una interfaz Rest API.
 * 
 * Existen dos formas de acceder a los datos, de manera general, la cual
 * te mostrará todos los datos existentes en la base de datos en relación
 * a lo solicitado, y de manera concreta, la cual vas a poder acceder a la
 * unidad de información solicitada.
 * 
 * @author neirth Sergio Martinez
 */
@Component
@Path("/users")
public class UsersCtrl {
    /**
     * Controlador que permite a un administrador obtener un listado de usuarios dentro 
     * del servidor, permitiendo asi obtener de manera dinamica los usuarios validos u 
     * otros administradores validos dentro del sistema.
     * 
     * El contenido que recibirá esta vista http es mediante una peticion GET con
     * la estructura de atributos de id, username, email, firstname, lastname
     * y isAdmin.
     * 
     * @param req      El header de la petición HTTP.
     * @return La respuesta generada por parte de la vista.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLists(@Context final HttpServletRequest req) {
        return ServerUtils.genericAdminMethod(req, null, null, () -> {
            JsonArrayBuilder jsonResponse = Json.createArrayBuilder();
            
            // Adquirimos una conexión de usuarios
            UsersMgt usersMgt = Connections.getController().acquireUsers();

            try {
                // Recorremos la lista que nos ha entregado el servidor.
                for (User user : usersMgt.getList()) {
                    // Inicializamos los objetos a usar.
                    JsonObjectBuilder jsonObj = Json.createObjectBuilder();

                    // Construimos el objeto Json con los atributo del usuario.
                    jsonObj.add("id", user.getId());
                    jsonObj.add("username", user.getUsername());
                    jsonObj.add("firstname", user.getFirstName());
                    jsonObj.add("lastname", user.getLastName());
                    jsonObj.add("email", user.getEmail());
                    jsonObj.add("isAdmin", user.isAdmin());

                    // Capturamos posible null procedente de la BBDD.
                    try {
                        jsonObj.add("userPic", ServerUtils.byteArrayToBase64(ServerUtils.imageToByteArray(user.getUserPic(), "png")));
                    } catch (Exception e) {
                        jsonObj.addNull("userPic");
                    }
                    
                    // Lo añadimos al Json Array.
                    jsonResponse.add(jsonObj);
                }
            } finally {
                // Devolvemos la conexión de usuarios
                Connections.getController().releaseUsers(usersMgt);
            }

            return Response.ok(jsonResponse.build().toString(), MediaType.APPLICATION_JSON);
        });
    }

    /**
     * Controlador que permite a un administrador registrar usuarios dentro del
     * servidor, permitiendo asi agregar de manera dinamica usuarios validos u otros
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
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerUser(@Context final HttpServletRequest req, String jsonData) {
        return ServerUtils.genericAdminMethod(req, null, jsonData, () -> {
            ResponseBuilder response = Response.status(Status.BAD_REQUEST);

            // Si la informacion que recibe es nula, no se procesa nada.
            if (jsonData.length() != 0) {
                // Convertimos la información JSON recibida en un objeto.
                final JsonObject jsonRequest = Json.createReader(new StringReader(jsonData)).readObject();

                // Adquirimos una conexión de usuarios
                UsersMgt usersMgt = Connections.getController().acquireUsers();

                try {
                    // Incializamos el objeto.
                    User userAux = new User();

                    // Agregamos la información al usuario.
                    userAux.setUsername(jsonRequest.getString("username"));
                    userAux.setFirstName(jsonRequest.getString("firstname"));
                    userAux.setLastName(jsonRequest.getString("lastname"));
                    userAux.setEmail(jsonRequest.getString("email"));
                    userAux.setPassword(jsonRequest.getString("password"));
                    userAux.setAdmin(jsonRequest.getBoolean("isAdmin"));

                    // Capturamos posible null procedente del Json.
                    try {
                        userAux.setUserPic(ServerUtils.imageFromByteArray(ServerUtils.byteArrayFromBase64(jsonRequest.getString("userPic"))));
                    } catch (Exception e) {
                        userAux.setUserPic(null);
                    }

                    // Agregamos el usuario a la lista.
                    usersMgt.add(userAux);
                } finally {
                    // Devolvemos la conexión de usuarios
                    Connections.getController().releaseUsers(usersMgt);
                }

                // Si todo ha ido bien hasta ahora, lanzamos la respuesta 200 OK.
                response = Response.status(Status.OK);
            }

            return response;
        });
    }

    /**
     * Controlador que permite a un administrador obtener un usuario especifico dentro 
     * del servidor, permitiendo asi obtener de manera dinamica el usuario requerido
     * como parametro en la interfaz web.
     * 
     * El contenido que recibirá esta vista http es mediante una peticion GET con
     * la estructura de atributos de id, username, email, firstname, lastname
     * y isAdmin.
     * 
     * @param req      El header de la petición HTTP.
     * @param paramId  El ID de usuario en la peticion HTTP.
     * @return La respuesta generada por parte de la vista.
     */
    @GET
    @Path("{param_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@Context final HttpServletRequest req, @PathParam("param_id") int paramId) {
        return ServerUtils.genericAdminMethod(req, paramId, null, () -> {
            ResponseBuilder response = null;

            // Adquirimos una conexión de usuarios
            UsersMgt usersMgt = Connections.getController().acquireUsers();

            try {
                // Inicializamos los objetos a usar.
                JsonObjectBuilder jsonResponse = Json.createObjectBuilder();
                User user = usersMgt.get(paramId);
                
                // Generamos un JSON con los atributos del usuario.
                jsonResponse.add("id", user.getId());
                jsonResponse.add("username", user.getUsername());
                jsonResponse.add("firstname", user.getFirstName());
                jsonResponse.add("lastname", user.getLastName());
                jsonResponse.add("email", user.getEmail());
                jsonResponse.add("isAdmin", user.isAdmin());
               
                // Capturamos posible null procedente de la BBDD.
                try {
                    jsonResponse.add("userPic", ServerUtils.byteArrayToBase64(ServerUtils.imageToByteArray(user.getUserPic(), "png")));
                } catch (Exception e) {
                    jsonResponse.addNull("userPic");
                }

                // Lanzamos la respuesta 200 OK si todo ha ido bien.
                response = Response.ok(jsonResponse.build().toString(), MediaType.APPLICATION_JSON);
            } catch (ManagerErrorException e) {
                // Si no se ha encontrado, lanzamos la respuesta 404 NOT FOUND.
                ServerApp.getLoggerSystem().debug(e.toString());
                response = Response.status(Status.NOT_FOUND);
            } finally {
                // Devolvemos la conexión de usuarios
                Connections.getController().releaseUsers(usersMgt);
            }

            return response;
        });
    }

    /**
     * Controlador que gestiona las actualizaciones de información de los usuarios
     * cuya informacion se recibe mediante una peticion PUT a la interfaz web
     * http://HOST_URL/api/users/(id)
     * 
     * @param req      El header de la petición HTTP.
     * @param paramId  El ID de usuario en la peticion HTTP.
     * @param jsonData El body de la petición HTTP.
     * @return La respuesta generada por parte de la vista.
     */
    @PUT
    @Path("{param_id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modifyUser(@Context final HttpServletRequest req, @PathParam("param_id") int paramId, String jsonData) {
        return ServerUtils.genericAdminMethod(req, paramId, jsonData, () -> {
            ResponseBuilder response = Response.status(Status.BAD_REQUEST);

            // Si la informacion que recibe es nula, no se procesa nada.
            if (jsonData.length() != 0) {
                // Convertimos la información JSON recibida en un objeto.
                final JsonObject jsonRequest = Json.createReader(new StringReader(jsonData)).readObject();

                // Adquirimos una conexión de usuarios
                UsersMgt usersMgt = Connections.getController().acquireUsers();

                try {
                    // Incializamos el objeto.
                    User userAux = usersMgt.get(paramId);

                    // Agregamos la información al usuario.
                    userAux.setFirstName(jsonRequest.isNull("firstname") ? userAux.getFirstName() : jsonRequest.getString("firstname"));
                    userAux.setLastName(jsonRequest.isNull("lastname") ? userAux.getLastName() : jsonRequest.getString("lastname"));
                    userAux.setEmail(jsonRequest.isNull("email") ? userAux.getEmail() : jsonRequest.getString("email"));
                    userAux.setPassword(jsonRequest.isNull("password") ? userAux.getPassword() : jsonRequest.getString("password"));
                    userAux.setAdmin(jsonRequest.isNull("isAdmin") ?  userAux.isAdmin() : jsonRequest.getBoolean("isAdmin"));
                    userAux.setUserPic(jsonRequest.isNull("userPic") ? userAux.getUserPic() : ServerUtils.imageFromByteArray(ServerUtils.byteArrayFromBase64(jsonRequest.getString("userPic"))));
                    
                    // Agregamos el usuario a la lista.
                    usersMgt.modify(userAux);

                    // Si todo ha ido bien hasta ahora, lanzamos la respuesta 200 OK.
                    response = Response.status(Status.OK);
                } catch (ManagerErrorException e) {
                    // Si no se ha encontrado, lanzamos la respuesta 404 NOT FOUND.
                    ServerApp.getLoggerSystem().debug(e.toString());
                    response = Response.status(Status.NOT_FOUND);
                } finally {
                   // Devolvemos la conexión de usuarios
                   Connections.getController().releaseUsers(usersMgt);
                }
            }

            return response;
        });
    }

    /**
     * Controlador para eliminar usuarios pasados por parametro en la interfaz web
     * http://HOST_URL/api/users/(id) con el tipo de petición DELETE.
     * 
     * @param req      El header de la petición HTTP.
     * @param paramId  El ID de usuario en la peticion HTTP.
     * @return La respuesta generada por parte de la vista.
     */
    @DELETE
    @Path("{param_id}")
    public Response deleteUser(@Context final HttpServletRequest req, @PathParam("param_id") int paramId) {
        return ServerUtils.genericAdminMethod(req, paramId, null, () -> {
            ResponseBuilder response;

            // Adquirimos una conexión de usuarios
            UsersMgt usersMgt = Connections.getController().acquireUsers();

            try {
                // Obtenemos el usuario de la base de datos.
                User userAux = usersMgt.get(paramId);

                // Agregamos el usuario a la lista.
                usersMgt.remove(userAux);

                // Si todo ha ido bien hasta ahora, lanzamos la respuesta 200 OK.
                response = Response.status(Status.OK);
            } catch (ManagerErrorException e) {
                // Si no se ha encontrado, lanzamos la respuesta 404 NOT FOUND.
                ServerApp.getLoggerSystem().debug(e.toString());
                response = Response.status(Status.NOT_FOUND);
            } finally {
                // Devolvemos la conexión de usuarios
                Connections.getController().releaseUsers(usersMgt);
            }

            return response;
        });
    }
}