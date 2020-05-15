package io.Mauzo.Server.Controllers;

import io.Mauzo.Server.Managers.ManagersIntf;
import io.Mauzo.Server.Managers.RefundsMgt;

import io.Mauzo.Server.ServerApp;
import io.Mauzo.Server.ServerPools;
import io.Mauzo.Server.ServerUtils;
import io.Mauzo.Server.Templates.Refund;
import org.springframework.stereotype.Component;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.StringReader;
import java.util.Date;

@Component
@Path("/Refunds")
public class RefundsCtrl {
    /**
     * Controlador que permite a un administrador obtener un listado de reembolsos dentro
     * del servidor.
     *
     * El contenido que recibirá esta vista http es mediante una petición GET con
     * la estructura de atributos de id, dateRefund, userId ,saleId.
     *
     * @param req
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRefundsMethod(@Context final HttpServletRequest req) {
        return ServerUtils.genericUserMethod(req, null, null, () -> {
            JsonArrayBuilder jsonResponse = Json.createArrayBuilder();

            RefundsMgt refundsMgt = ServerPools.getController().acquireRefunds();

            for (Refund refund : refundsMgt.getList()) {
                // Inicializamos los objetos a usar.
                JsonObjectBuilder jsonObj = Json.createObjectBuilder();

                // Construimos el objeto Json con los atributo de la venta.
                jsonObj.add("id", refund.getId());
                jsonObj.add("dateRefund", refund.getDateRefund().getTime());
                jsonObj.add("userId", refund.getUserId());
                jsonObj.add("saleId", refund.getSaleId());
                // Lo añadimos al Json Array.
                jsonResponse.add(jsonObj);
            }

            ServerPools.getController().releaseRefunds(refundsMgt);

            return Response.ok(jsonResponse.build().toString());
        });
    }

    /**
     * Controlador que permite a un administrador registrar reembolsos dentro del
     * servidor.
     *
     * El contenido que recibirá esta vista http es mediante una peticion POST con
     * la estructura de atributos de id, dateRefund, userId ,saleId.
     *
     * @param req      El header de la petición HTTP.
     * @param jsonData El body de la petición HTTP.
     * @return La respuesta generada por parte de la vista.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerRefund(@Context final HttpServletRequest req, String jsonData) {
        return ServerUtils.genericAdminMethod(req, null, jsonData, () -> {
            Response.ResponseBuilder response = Response.status(Response.Status.BAD_REQUEST);

            // Si la informacion que recibe es nula, no se procesa nada.
            if (jsonData.length() != 0) {
                // Convertimos la información JSON recibida en un objeto.
                final JsonObject jsonRequest = Json.createReader(new StringReader(jsonData)).readObject();

                // Adquirimos una conexión de reembolsos
                RefundsMgt refundsMgt = ServerPools.getController().acquireRefunds();

                try {
                    // Incializamos el objeto.
                    Refund refund = new Refund();

                    // Agregamos la información al reembolso.
                    refund.setId(jsonRequest.getInt("id"));
                    refund.setDateRefund(new Date(Long.valueOf(jsonRequest.getString("dateRefund"))));
                    refund.setUserId(jsonRequest.getInt("refundId"));
                    refund.setSaleId(jsonRequest.getInt("saleId"));

                    // Agregamos el reembolso a la lista.
                    refundsMgt.add(refund);
                } finally {
                    // Devolvemos la conexión de reembolsos
                    ServerPools.getController().releaseRefunds(refundsMgt);
                }

                // Si todo ha ido bien hasta ahora, lanzamos la respuesta 200 OK.
                response = Response.status(Response.Status.OK);
            }

            return response;
        });
    }

    /**
     * Controlador que permite a un administrador obtener un reembolso especifico dentro
     * del servidor, permitiendo asi obtener de manera dinámica el reembolso requerido
     * como parámetro en la interfaz web.
     *
     * El contenido que recibirá esta vista http es mediante una petición GET con
     * la estructura de atributos de id, dateRefund, userId ,saleId.
     *
     * @param req El header de la petición HTTP.
     * @param paramRefundId  El ID del reembolso en la peticion HTTP.
     * @return La respuesta generada por parte de la vista.
     */
    @GET
    @Path("{param_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRefund(@Context final HttpServletRequest req, @PathParam("param_id") int paramRefundId) {
        return ServerUtils.genericAdminMethod(req, paramRefundId, null, () -> {
            Response.ResponseBuilder response = null;

            // Adquirimos una conexión de reembolsos
            RefundsMgt refundsMgt = ServerPools.getController().acquireRefunds();

            try {
                // Inicializamos los objetos a usar.
                JsonObjectBuilder jsonResponse = Json.createObjectBuilder();
                Refund refund = refundsMgt.get(paramRefundId);

                // Generamos un JSON con los atributos del reembolso.
                jsonResponse.add("id", refund.getId());
                jsonResponse.add("dateRefund", refund.getDateRefund().getTime());
                jsonResponse.add("userId", refund.getUserId());
                jsonResponse.add("saleId", refund.getSaleId());

                // Lanzamos la respuesta 200 OK si todo ha ido bien.
                response = Response.ok(jsonResponse.build().toString(), MediaType.APPLICATION_JSON);
            } catch (ManagersIntf.ManagerErrorException e) {
                // Si no se ha encontrado, lanzamos la respuesta 404 NOT FOUND.
                ServerApp.getLoggerSystem().info(e.toString());
                response = Response.status(Response.Status.NOT_FOUND);
            } finally {
                // Devolvemos la conexión de productos
                ServerPools.getController().releaseRefunds(refundsMgt);
            }

            return response;
        });
    }

    /**
     * Controlador que gestiona las actualizaciones de información de los reembolsos
     * cuya información se recibe mediante una peticion PUT a la interfaz web
     * http://HOST_URL/api/refunds/(id)
     *
     * @param req El header de la petición HTTP.
     * @param paramId  El ID del reembolso en la petición HTTP.
     * @param jsonData El body de la petición HTTP.
     * @return La respuesta generada por parte de la vista.
     */
    @PUT
    @Path("{param_id}")
    public Response modifyRefund(@Context final HttpServletRequest req, @PathParam("param_id") int paramId, String jsonData) {
        return ServerUtils.genericUserMethod(req, paramId, jsonData, () -> {
            Response.ResponseBuilder response = Response.status(Response.Status.BAD_REQUEST);

            // Si la informacion que recibe es nula, no se procesa nada.
            if (jsonData.length() != 0) {
                // Convertimos la información JSON recibida en un objeto.
                final JsonObject jsonRequest = Json.createReader(new StringReader(jsonData)).readObject();

                // Adquirimos una conexión de reembolsos
                RefundsMgt refundsMgt = ServerPools.getController().acquireRefunds();

                try  {
                    // Incializamos el objeto.
                    Refund refund = refundsMgt.get(paramId);

                    // Agregamos la información al reembolso.
                    refund.setId(jsonRequest.isNull("id") ? jsonRequest.getInt("id") : refund.getId());
                    refund.setDateRefund(new Date(jsonRequest.isNull("dateRefund") ? Long.valueOf(jsonRequest.getString("dateRefund")) : refund.getDateRefund().getTime()));
                    refund.setUserId(jsonRequest.isNull("userId") ? jsonRequest.getInt("userId") : refund.getUserId());
                    refund.setSaleId(jsonRequest.isNull("saleId") ? jsonRequest.getInt("saleId") : refund.getSaleId());

                    // Agregamos el reembolso a la lista.
                    refundsMgt.modify(refund);

                    // Si todo ha ido bien hasta ahora, lanzamos la respuesta 200 OK.
                    response = Response.status(Response.Status.OK);
                } catch (ManagersIntf.ManagerErrorException e) {
                    // Si no se ha encontrado, lanzamos la respuesta 404 NOT FOUND.
                    ServerApp.getLoggerSystem().info(e.toString());
                    response = Response.status(Response.Status.NOT_FOUND);
                } finally {
                    // Devolvemos la conexión de reembolsos
                    ServerPools.getController().releaseRefunds(refundsMgt);
                }
            }
            return response;
        });
    }

    /**
     * Controlador para eliminar reembolsos pasados por parámetro en la interfaz web
     * http://HOST_URL/api/refunds/(id) con el tipo de petición DELETE.
     *
     * @param req El header de la petición HTTP.
     * @param paramId  El ID del reembolso en la peticion HTTP.
     * @return La respuesta generada por parte de la vista.
     */
    @DELETE
    @Path("{param_id}")
    public Response deleteRefund(@Context final HttpServletRequest req, @PathParam("param_id") int paramId) {
        return ServerUtils.genericAdminMethod(req, paramId, null, () -> {
            Response.ResponseBuilder response;

            // Adquirimos una conexión de reembolsos
            RefundsMgt refundsMgt = ServerPools.getController().acquireRefunds();

            try {
                // Obtenemos el reembolso de la base de datos.
                Refund refund = refundsMgt.get(paramId);

                // Agregamos el reembolso a la lista.
                refundsMgt.remove(refund);

                // Si todo ha ido bien hasta ahora, lanzamos la respuesta 200 OK.
                response = Response.status(Response.Status.OK);
            } catch (ManagersIntf.ManagerErrorException e) {
                // Si no se ha encontrado, lanzamos la respuesta 404 NOT FOUND.
                ServerApp.getLoggerSystem().info(e.toString());
                response = Response.status(Response.Status.NOT_FOUND);
            } finally {
                // Devolvemos la conexión de reembolsos
                ServerPools.getController().releaseRefunds(refundsMgt);
            }

            return response;
        });
    }
}