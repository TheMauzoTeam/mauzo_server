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
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSalesMethod(@Context final HttpServletRequest req) {
        return ServerUtils.genericUserMethod(req, null, null, () -> {
            JsonArrayBuilder jsonResponse = Json.createArrayBuilder();

            // Adquirimos una conexión de ventas
            SalesMgt salesMgt = ServerPools.getController().acquireSales();

            try {
                for (Sale sale : salesMgt.getList()) {
                    // Inicializamos los objetos a usar.
                    JsonObjectBuilder jsonObj = Json.createObjectBuilder();
    
                    // Construimos el objeto Json con los atributo de la venta.
                    jsonObj.add("id", sale.getId());
                    jsonObj.add("stampRef", sale.getStampRef().getTime());
                    jsonObj.add("userId", sale.getUserId());
                    jsonObj.add("prodId", sale.getProdId());
                    jsonObj.add("discId", sale.getDiscId());
                    jsonObj.add("refundId", sale.getRefundId());
    
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
                    saleAux.setUserId(jsonRequest.getInt("userId"));
                    saleAux.setDiscId(jsonRequest.getInt("discId"));
                    saleAux.setProdId(jsonRequest.getInt("prodId"));
                    saleAux.setRefundId(jsonRequest.getInt("refundId"));
                    saleAux.setStampRef(new Date(Long.valueOf(jsonRequest.getString("stampRef"))));
                
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
                jsonResponse.add("discId", saleAux.getDiscId());
                jsonResponse.add("refundId", saleAux.getRefundId());

                response = Response.ok(jsonResponse.build().toString(), MediaType.APPLICATION_JSON);
            } catch (ManagerErrorException e) {
                // Si no se ha encontrado, lanzamos la respuesta 404 NOT FOUND.
                ServerApp.getLoggerSystem().info(e.toString());
                response = Response.status(Status.NOT_FOUND);
            } finally {
                // Devolvemos la conexión de ventas
                ServerPools.getController().releaseSales(salesMgt);
            }

            return response;
        });
    }

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
                    saleAux.setRefundId(jsonRequest.isNull("refundId") ? jsonRequest.getInt("refundId") : saleAux.getRefundId());
  
                    // Agregamos el usuario a la lista.
                    salesMgt.modify(saleAux);

                    // Si todo ha ido bien hasta ahora, lanzamos la respuesta 200 OK.
                    response = Response.status(Status.OK);
                } catch (ManagerErrorException e) {
                    // Si no se ha encontrado, lanzamos la respuesta 404 NOT FOUND.
                    ServerApp.getLoggerSystem().info(e.toString());
                    response = Response.status(Status.NOT_FOUND);
                } finally {
                    // Devolvemos la conexión de ventas
                    ServerPools.getController().releaseSales(salesMgt);
                }                
            }

            return response;
        });
    }

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
                ServerApp.getLoggerSystem().info(e.toString());
                response = Response.status(Status.NOT_FOUND);
            } finally {
                // Devolvemos la conexión de ventas
                ServerPools.getController().releaseSales(salesMgt);
            }

            return response;
        });
    }
}