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
import io.Mauzo.Server.Managers.UsersMgt;
import io.Mauzo.Server.Managers.ManagersIntf.ManagerErrorException;

@Component
@Path("/users")
public class UsersCtrl {
    /**
     * Controlador que permite a un administrador obtener un listado de usuarios dentro 
     * del servidor, permitiendo asi obtener de manera dinamica los usuarios validos o 
     * otros administradores validos dentro del sistema.
     * 
     * El contenido que recibirá esta vista http es mediante una peticion GET con
     * la estructura de atributos de username, email, password, firstname, lastname
     * y isAdmin.
     * 
     * @param req      El header de la petición HTTP.
     * @return La respuesta generada por parte de la vista.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLists(@Context final HttpServletRequest req){
        return ServerUtils.genericAdminMethod(req, null, null, () -> {
            JsonArrayBuilder jsonResponse = Json.createArrayBuilder();
            
            // Recorremos la lista que nos ha entregado el servidor.
            for (User user : UsersMgt.getController().getList()) {
                // Inicializamos los objetos a usar.
                JsonObjectBuilder jsonObj = Json.createObjectBuilder();

                // Construimos el objeto Json con los atributo del usuario.
                jsonObj.add("id", user.getId());
                jsonObj.add("username", user.getUsername());
                jsonObj.add("firstname", user.getFirstName());
                jsonObj.add("lastname", user.getLastName());
                jsonObj.add("email", user.getEmail());
                jsonObj.add("isAdmin", user.isAdmin());
                jsonObj.add("userPic", ServerUtils.byteArrayToBase64(ServerUtils.imageToByteArray(user.getUserPic(), "png")));

                // Lo añadimos al Json Array.
                jsonResponse.add(jsonObj);
            }
            
            return Response.ok(jsonResponse.build().toString(), MediaType.APPLICATION_JSON);
        });
    }

    /**
     * Controlador que permite a un administrador registrar usuarios dentro del
     * servidor, permitiendo asi agregar de manera dinamica usuarios validos o otros
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
            if(jsonData.length() != 0) {
                // Convertimos la información JSON recibida en un objeto.
                final JsonObject jsonRequest = Json.createReader(new StringReader(jsonData)).readObject();

                // Incializamos el objeto.
                User userAux = new User();

                // Agregamos la información al usuario.
                userAux.setUsername(jsonRequest.getString("username"));
                userAux.setFirstName(jsonRequest.getString("firstname"));
                userAux.setLastName(jsonRequest.getString("lastname"));
                userAux.setEmail(jsonRequest.getString("email"));
                userAux.setPassword(jsonRequest.getString("password"));
                userAux.setAdmin(jsonRequest.getBoolean("isAdmin"));
                userAux.setUserPic(ServerUtils.imageFromByteArray(ServerUtils.byteArrayFromBase64(jsonRequest.getString("userPic"))));

                // Agregamos el usuario a la lista.
                UsersMgt.getController().add(userAux);

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
     * la estructura de atributos de username, email, password, firstname, lastname
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

            try {
                // Inicializamos los objetos a usar.
                JsonObjectBuilder jsonResponse = Json.createObjectBuilder();
                User user = UsersMgt.getController().get(paramId);
                
                // Generamos un JSON con los atributos del usuario.
                jsonResponse.add("id", user.getId());
                jsonResponse.add("username", user.getUsername());
                jsonResponse.add("firstname", user.getFirstName());
                jsonResponse.add("lastname", user.getLastName());
                jsonResponse.add("email", user.getEmail());
                jsonResponse.add("isAdmin", user.isAdmin());
                jsonResponse.add("userPic", ServerUtils.byteArrayToBase64(ServerUtils.imageToByteArray(user.getUserPic(), "png")));

                // Lanzamos la respuesta 200 OK si todo ha ido bien.
                response = Response.ok(jsonResponse.build().toString(), MediaType.APPLICATION_JSON);
            } catch(ManagerErrorException e) {
                // Si no se ha encontrado, lanzamos la respuesta 404 NOT FOUND.
                ServerApp.getLoggerSystem().info(e.toString());
                response = Response.status(Status.NOT_FOUND);
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
            if(jsonData.length() != 0) {
                // Convertimos la información JSON recibida en un objeto.
                final JsonObject jsonRequest = Json.createReader(new StringReader(jsonData)).readObject();

                try {
                    // Incializamos el objeto.
                    User userAux = UsersMgt.getController().get(paramId);

                    // Agregamos la información al usuario.
                    userAux.setFirstName(jsonRequest.isNull("firstname") ? userAux.getFirstName() : jsonRequest.getString("firstname"));
                    userAux.setLastName(jsonRequest.isNull("lastname") ? userAux.getLastName() : jsonRequest.getString("lastname"));
                    userAux.setEmail(jsonRequest.isNull("email") ? userAux.getEmail() : jsonRequest.getString("email"));
                    userAux.setPassword(jsonRequest.isNull("password") ? userAux.getPassword() : jsonRequest.getString("password"));
                    userAux.setAdmin(jsonRequest.isNull("isAdmin") ?  userAux.isAdmin() : jsonRequest.getBoolean("isAdmin"));
                    userAux.setUserPic(jsonRequest.isNull("userpic") ? userAux.getUserPic() : ServerUtils.imageFromByteArray(ServerUtils.byteArrayFromBase64(jsonRequest.getString("userPic"))));
                    
                    // Agregamos el usuario a la lista.
                    UsersMgt.getController().modify(userAux);

                    // Si todo ha ido bien hasta ahora, lanzamos la respuesta 200 OK.
                    response = Response.status(Status.OK);
                } catch (ManagerErrorException e) {
                    // Si no se ha encontrado, lanzamos la respuesta 404 NOT FOUND.
                    ServerApp.getLoggerSystem().info(e.toString());
                    response = Response.status(Status.NOT_FOUND);
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

            try {
                // Obtenemos el usuario de la base de datos.
                User userAux = UsersMgt.getController().get(paramId);

                // Agregamos el usuario a la lista.
                UsersMgt.getController().remove(userAux);

                // Si todo ha ido bien hasta ahora, lanzamos la respuesta 200 OK.
                response = Response.status(Status.OK);
            } catch (ManagerErrorException e) {
                // Si no se ha encontrado, lanzamos la respuesta 404 NOT FOUND.
                ServerApp.getLoggerSystem().info(e.toString());
                response = Response.status(Status.NOT_FOUND);
            }

            return response;
        });
    }
}