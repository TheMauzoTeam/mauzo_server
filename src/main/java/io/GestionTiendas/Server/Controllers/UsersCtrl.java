package io.GestionTiendas.Server.Controllers;

// Paquetes relativos al framework estandar de Java.
import java.util.Date;
import java.io.StringReader;

// Paquetes relativos a los Json de entrada y salida.
import javax.json.Json;
import javax.json.JsonObject;

// Paquetes relativos a la interfaz web.
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.MediaType;
import javax.servlet.http.HttpServletRequest;

// Paquetes relativos al token de autenticacion.
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

// Paquetes propios de la aplicación.
import io.GestionTiendas.Server.ServerUtils;
import io.GestionTiendas.Server.ServerApp;
import io.GestionTiendas.Server.Templates.Users;
import io.GestionTiendas.Server.Managers.UsersMgt;
import io.GestionTiendas.Server.Managers.UsersMgt.UsersException;

public class UsersCtrl {
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
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response loginMethod(@Context final HttpServletRequest req, String jsonData) {
        return ServerUtils.genericMethod(req, null, jsonData, () -> {
            ResponseBuilder response = null;

            // Convertimos la información JSON recibida en un objeto.
            final JsonObject jsonRequest = Json.createReader(new StringReader(jsonData)).readObject();

            final String username = jsonRequest.getString("username");
            final String password = jsonRequest.getString("password");

            try {
                Users userAux = UsersMgt.getController().getUser(username);

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
                    throw new UsersException("Contraseña incorrecta para el usuario " + username + " con IP " + req.getRemoteAddr());
                }
            } catch (UsersException e) {
                ServerApp.getLoggerSystem().severe(e.toString());
                response = Response.status(Status.FORBIDDEN);
            }

            return response;
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
    @Path("/users")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerMethod(@Context final HttpServletRequest req, String jsonData) {
        return ServerUtils.genericAdminMethod(req, null, jsonData, () -> {
            // Convertimos la información JSON recibida en un objeto.
            final JsonObject jsonRequest = Json.createReader(new StringReader(jsonData)).readObject();

            // Incializamos el objeto.
            Users userAux = new Users();

            // Agregamos la información al usuario.
            userAux.setUsername(jsonRequest.getString("username"));
            userAux.setFirstName(jsonRequest.getString("firstname"));
            userAux.setLastName(jsonRequest.getString("lastname"));
            userAux.setEmail(jsonRequest.getString("email"));
            userAux.setPassword(jsonRequest.getString("password"));
            userAux.setAdmin(jsonRequest.getBoolean("isadmin"));

            // Agregamos el usuario a la lista.
            UsersMgt.getController().addUser(userAux);

            // Si todo ha ido bien hasta ahora, lanzamos la respuesta 200 OK.
            return Response.status(Status.OK);
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
    @Path("/users/{param_id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modifyUserMethod(@Context final HttpServletRequest req, @PathParam("param_id") String paramId,
            String jsonData) {
        return ServerUtils.genericAdminMethod(req, paramId, jsonData, () -> {
            // Convertimos la información JSON recibida en un objeto.
            final JsonObject jsonRequest = Json.createReader(new StringReader(jsonData)).readObject();
            ResponseBuilder response;

            try {
                // Incializamos el objeto.
                Users userAux = UsersMgt.getController().getUser(paramId);

                // Agregamos la información al usuario.
                userAux.setFirstName(jsonRequest.isNull("firstname") ? jsonRequest.getString("firstname") : userAux.getFirstName());
                userAux.setLastName(jsonRequest.isNull("lastname") ? jsonRequest.getString("lastname") : userAux.getLastName());
                userAux.setEmail(jsonRequest.isNull("email") ? jsonRequest.getString("email") : userAux.getEmail());
                userAux.setPassword(jsonRequest.isNull("password") ? jsonRequest.getString("password") : userAux.getPassword());
                userAux.setAdmin(jsonRequest.isNull("isadmin") ? jsonRequest.getBoolean("isadmin") : userAux.isAdmin());

                // Agregamos el usuario a la lista.
                UsersMgt.getController().modifyUser(userAux);

                // Si todo ha ido bien hasta ahora, lanzamos la respuesta 200 OK.
                response = Response.status(Status.OK);
            } catch (UsersException e) {
                // Si no se ha encontrado, lanzamos la respuesta 404 NOT FOUND.
                ServerApp.getLoggerSystem().info(e.toString());
                response = Response.status(Status.NOT_FOUND);
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
    @Path("/users/{param_id}")
    public Response deleteUserMethod(@Context final HttpServletRequest req, @PathParam("param_id") String paramId) {
        return ServerUtils.genericAdminMethod(req, paramId, null, () -> {
            ResponseBuilder response;

            try {
                // Obtenemos el usuario de la base de datos.
                Users userAux = UsersMgt.getController().getUser(paramId);

                // Agregamos el usuario a la lista.
                UsersMgt.getController().removeUser(userAux);

                // Si todo ha ido bien hasta ahora, lanzamos la respuesta 200 OK.
                response = Response.status(Status.OK);
            } catch (UsersException e) {
                // Si no se ha encontrado, lanzamos la respuesta 404 NOT FOUND.
                ServerApp.getLoggerSystem().info(e.toString());
                response = Response.status(Status.NOT_FOUND);
            }

            return response;
        });
    }
}