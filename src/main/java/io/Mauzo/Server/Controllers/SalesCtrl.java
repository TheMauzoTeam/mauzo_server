package io.Mauzo.Server.Controllers;

// Paquetes relativos al framework estandar de Java.
import java.io.StringReader;
import java.util.Date;

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
import io.Mauzo.Server.ServerPools;
import io.Mauzo.Server.Templates.Sale;
import io.Mauzo.Server.Managers.SalesMgt;
import io.Mauzo.Server.Managers.ManagersIntf.ManagerErrorException;

@Component
@Path("/sales")
public class SalesCtrl {
    /**
     * Controlador que permite a un usuario obtener un listado de ventas dentro 
     * del servidor, permitiendo asi obtener de manera dinamica los ventas validos u 
     * otros usuarios validos dentro del sistema.
     * 
     * El contenido que recibirá esta vista http es mediante una peticion GET con
     * la estructura de atributos de id, stampRef, userId, prodId y discId.
     * 
     * @param req      El header de la petición HTTP.
     * @return La respuesta generada por parte de la vista.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSalesMethod(@Context final HttpServletRequest req) {
        return ServerUtils.genericUserMethod(req, null, null, () -> {
            JsonArrayBuilder jsonResponse = Json.createArrayBuilder();

            // Adquirimos una conexión de ventas
            SalesMgt salesMgt = ServerPools.getController().acquireSales();

            try {
                for (Sale saleAux : salesMgt.getList()) {
                    // Inicializamos los objetos a usar.
                    JsonObjectBuilder jsonObj = Json.createObjectBuilder();
    
                    // Construimos el objeto Json con los atributo de la venta.
                    jsonObj.add("id", saleAux.getId());
                    jsonObj.add("stampRef", saleAux.getStampRef().getTime());
                    jsonObj.add("userId", saleAux.getUserId());
                    jsonObj.add("prodId", saleAux.getProdId());

                    // Capturamos posible null procedente de la BBDD.
                    try {
                        jsonObj.add("discId", saleAux.getDiscId());
                    } catch (NullPointerException e) {
                        jsonObj.addNull("discId");
                    }
    
                    // Lo añadimos al Json Array.
                    jsonResponse.add(jsonObj);
                }
            } finally {
                // Devolvemos la conexión de ventas
                ServerPools.getController().releaseSales(salesMgt);
            }

            return Response.ok(jsonResponse.build().toString());
        });
    }

    /**
     * Controlador que permite a un usuario registrar ventas dentro del
     * servidor, permitiendo asi agregar de manera dinamica ventas validos u otros
     * usuarios validos dentro del sistema.
     * 
     * El contenido que recibirá esta vista http es mediante una peticion GET con
     * la estructura de atributos de id, stampRef, userId, prodId y discId.
     * 
     * @param req      El header de la petición HTTP.
     * @param jsonData El body de la petición HTTP.
     * @return La respuesta generada por parte de la vista.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addSalesMethod(@Context final HttpServletRequest req, String jsonData) {
        return ServerUtils.genericUserMethod(req, null, jsonData, () -> {
            ResponseBuilder response = Response.status(Status.BAD_REQUEST);

            // Adquirimos una conexión de ventas
            SalesMgt salesMgt = ServerPools.getController().acquireSales();

            try {
                // Si la informacion que recibe es nula, no se procesa nada
                if (jsonData.length() != 0) {
                    // Convertimos la información JSON recibida en un objeto.
                    final JsonObject jsonRequest = Json.createReader(new StringReader(jsonData)).readObject();
                
                    // Incializamos el objeto.
                    Sale saleAux = new Sale();

                    // Agregamos la información de la venta.
                    saleAux.setStampRef(new Date(Long.valueOf(jsonRequest.getString("stampRef"))));
                    saleAux.setUserId(jsonRequest.getInt("userId"));
                    saleAux.setDiscId(jsonRequest.getInt("discId"));
                    saleAux.setProdId(jsonRequest.getInt("prodId"));

                
                    // Agregamos la venta a la lista.
                    salesMgt.add(saleAux);

                    // Si todo ha ido bien hasta ahora, lanzamos la respuesta 200 OK.
                    response = Response.status(Status.OK);
                }
            } finally {
                // Devolvemos la conexión de ventas
                ServerPools.getController().releaseSales(salesMgt);
            }           

            return response;
        });
    }

    /**
     * Controlador que permite a un usuario obtener una venta especifica dentro 
     * del servidor, permitiendo asi obtener de manera dinamica la venta requeria
     * como parametro en la interfaz web.
     * 
     * El contenido que recibirá esta vista http es mediante una peticion GET con
     * la estructura de atributos de id, stampRef, userId, prodId y discId.
     * 
     * @param req      El header de la petición HTTP.
     * @param paramId  El ID de usuario en la peticion HTTP.
     * @return La respuesta generada por parte de la vista.
     */
    @GET
    @Path("{param_id}")
    public Response getSaleMethod(@Context final HttpServletRequest req, @PathParam("param_id") int paramId) {
        return ServerUtils.genericUserMethod(req, paramId, null, () -> {
            ResponseBuilder response = null;

            // Adquirimos una conexión de ventas
            SalesMgt salesMgt = ServerPools.getController().acquireSales();

            try {
                // Inicializamos los objetos a usar.
                JsonObjectBuilder jsonResponse = Json.createObjectBuilder();
                Sale saleAux = salesMgt.get(paramId);

                // Generamos un JSON con los atributos de la venta.
                jsonResponse.add("id", saleAux.getId());
                jsonResponse.add("stampRef", saleAux.getStampRef().getTime());
                jsonResponse.add("userId", saleAux.getUserId());
                jsonResponse.add("prodId", saleAux.getProdId());
                
                // Capturamos posible null procedente de la BBDD.
                try {
                    jsonResponse.add("discId", saleAux.getDiscId());
                } catch (NullPointerException e) {
                    jsonResponse.addNull("discId");
                }

                response = Response.ok(jsonResponse.build().toString(), MediaType.APPLICATION_JSON);
            } catch (ManagerErrorException e) {
                // Si no se ha encontrado, lanzamos la respuesta 404 NOT FOUND.
                ServerApp.getLoggerSystem().debug(e.toString());
                response = Response.status(Status.NOT_FOUND);
            } finally {
                // Devolvemos la conexión de ventas
                ServerPools.getController().releaseSales(salesMgt);
            }

            return response;
        });
    }

    /**
     * Controlador que gestiona las actualizaciones de información de las ventas
     * cuya informacion se recibe mediante una peticion PUT a la interfaz web.
     * http://HOST_URL/api/sales/(id)
     * 
     * @param req      El header de la petición HTTP.
     * @param paramId  El ID de usuario en la peticion HTTP.
     * @param jsonData El body de la petición HTTP.
     * @return La respuesta generada por parte de la vista.
     */
    @PUT
    @Path("{param_id}")
    public Response modifySaleMethod(@Context final HttpServletRequest req, @PathParam("param_id") int paramId, String jsonData) {
        return ServerUtils.genericUserMethod(req, paramId, jsonData, () -> {
            ResponseBuilder response = Response.status(Status.BAD_REQUEST);

            // Si la informacion que recibe es nula, no se procesa nada.
            if (jsonData.length() != 0) {
                // Convertimos la información JSON recibida en un objeto.
                final JsonObject jsonRequest = Json.createReader(new StringReader(jsonData)).readObject();

                // Adquirimos una conexión de ventas
                SalesMgt salesMgt = ServerPools.getController().acquireSales();

                try  {
                    // Incializamos el objeto.
                    Sale saleAux = salesMgt.get(paramId);

                    // Agregamos la información al usuario.
                    saleAux.setStampRef(new Date(jsonRequest.isNull("stampRef") ? Long.valueOf(jsonRequest.getString("stampRef")) : saleAux.getStampRef().getTime()));
                    saleAux.setUserId(jsonRequest.isNull("userId") ? jsonRequest.getInt("userId") : saleAux.getUserId());
                    saleAux.setProdId(jsonRequest.isNull("prodId") ? jsonRequest.getInt("prodId") : saleAux.getProdId());
                    saleAux.setDiscId(jsonRequest.isNull("discId") ? jsonRequest.getInt("discId") : saleAux.getDiscId());
  
                    // Agregamos el usuario a la lista.
                    salesMgt.modify(saleAux);

                    // Si todo ha ido bien hasta ahora, lanzamos la respuesta 200 OK.
                    response = Response.status(Status.OK);
                } catch (ManagerErrorException e) {
                    // Si no se ha encontrado, lanzamos la respuesta 404 NOT FOUND.
                    ServerApp.getLoggerSystem().debug(e.toString());
                    response = Response.status(Status.NOT_FOUND);
                } finally {
                    // Devolvemos la conexión de ventas
                    ServerPools.getController().releaseSales(salesMgt);
                }                
            }

            return response;
        });
    }

    /**
     * Controlador para eliminar ventas pasados por parametro en la interfaz web
     * http://HOST_URL/api/sales/(id) con el tipo de petición DELETE.
     * 
     * @param req      El header de la petición HTTP.
     * @param paramId  El ID de usuario en la peticion HTTP.
     * @return La respuesta generada por parte de la vista.
     */
    @DELETE
    @Path("{param_id}")
    public Response deleteSaleMethod(@Context final HttpServletRequest req, @PathParam("param_id") int paramId) {
        return ServerUtils.genericUserMethod(req, paramId, null, () -> {
            ResponseBuilder response;

            // Adquirimos una conexión de ventas
            SalesMgt salesMgt = ServerPools.getController().acquireSales();

            try {
                // Obtenemos la venta de la base de datos.
                Sale saleAux = salesMgt.get(paramId);

                // Agregamos la venta a la lista.
                salesMgt.remove(saleAux);

                // Si todo ha ido bien hasta ahora, lanzamos la respuesta 200 OK.
                response = Response.status(Status.OK);

            } catch (ManagerErrorException e) {
                // Si no se ha encontrado, lanzamos la respuesta 404 NOT FOUND.
                ServerApp.getLoggerSystem().debug(e.toString());
                response = Response.status(Status.NOT_FOUND);
            } finally {
                // Devolvemos la conexión de ventas
                ServerPools.getController().releaseSales(salesMgt);
            }

            return response;
        });
    }
}