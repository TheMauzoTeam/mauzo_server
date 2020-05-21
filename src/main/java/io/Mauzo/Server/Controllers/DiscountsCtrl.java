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

import io.Mauzo.Server.Managers.Connections;
import io.Mauzo.Server.Managers.DiscountsMgt;
import io.Mauzo.Server.Templates.Discount;
import org.springframework.stereotype.Component;

// Paquetes propios de la aplicación.
import io.Mauzo.Server.ServerUtils;
import io.Mauzo.Server.ServerApp;
import io.Mauzo.Server.Managers.ManagersIntf.ManagerErrorException;
@Component
@Path("/discounts")
public class DiscountsCtrl {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDiscountMethod(@Context final HttpServletRequest req) {
        return ServerUtils.genericUserMethod(req, null, null, () -> {
            JsonArrayBuilder jsonResponse = Json.createArrayBuilder();

            DiscountsMgt discountMgt = Connections.getController().acquireDiscounts();

            try {
                // Recorremos la lista que nos ha entregado el servidor.
                for (Discount discount : discountMgt.getList()) {
                    // Inicializamos los objetos a usar.
                    JsonObjectBuilder jsonObj = Json.createObjectBuilder();

                    // Construimos el objeto Json con los atributo del descuento.
                    jsonObj.add("id", discount.getId());
                    jsonObj.add("codeDisc", discount.getCode());
                    jsonObj.add("descDisc", discount.getDesc());
                    jsonObj.add("pricePerc", discount.getPrizeDisc());

                    // Lo añadimos al Json Array.
                    jsonResponse.add(jsonObj);
                }
            } finally {
                Connections.getController().releaseDiscounts(discountMgt);
            }

            return Response.ok(jsonResponse.build().toString(), MediaType.APPLICATION_JSON);
        });
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addDiscountMethod(@Context final HttpServletRequest req, String jsonData) {
        return ServerUtils.genericAdminMethod(req, null, jsonData, () -> {
            ResponseBuilder response = Response.status(Status.BAD_REQUEST);

            // Si la informacion que recibe es nula, no se procesa nada.
            if (jsonData.length() != 0) {
                // Convertimos la información JSON recibida en un objeto.
                final JsonObject jsonRequest = Json.createReader( new StringReader(jsonData)).readObject();

                DiscountsMgt discountMgt = Connections.getController().acquireDiscounts();

                try {
                    // Inicializamos el objeto
                    Discount disAux = new Discount();

                    // Agregamos la información al descuento.
                    disAux.setCode(jsonRequest.getString("codeDisc"));
                    disAux.setDesc(jsonRequest.getString("descDisc"));
                    disAux.setPriceDisc(Float.valueOf(jsonRequest.getString("pricePerc")));

                    // Agregamos el descuento al la lista.
                    discountMgt.add(disAux);
                } finally {
                    Connections.getController().releaseDiscounts(discountMgt);
                }

                // Si todo ha ido bien hasta ahora, lanzamos la respuesta 200 OK.
                response = Response.status(Status.OK);
            }

            return response;
        });
    }

    @GET
    @Path("{param_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDiscount(@Context final HttpServletRequest req, @PathParam("param_id") int param) {
        return ServerUtils.genericAdminMethod(req, param, null, () -> {
            ResponseBuilder response = null;
            DiscountsMgt discountsMgt = Connections.getController().acquireDiscounts();
            try {
                JsonObjectBuilder jsonResponse = Json.createObjectBuilder();
                Discount discount = discountsMgt.get(param);

                jsonResponse.add("id", discount.getId());
                jsonResponse.add("codeDisc", discount.getId());
                jsonResponse.add("descDisc", discount.getDesc());
                jsonResponse.add("pricePerc", discount.getPrizeDisc());

                response = Response.ok(jsonResponse.build().toString(), MediaType.APPLICATION_JSON);
            } catch (ManagerErrorException e) {
                ServerApp.getLoggerSystem().debug(e.toString());
                response = Response.status(Status.NOT_FOUND);
            } finally {
                Connections.getController().releaseDiscounts(discountsMgt);
            }
            return response;
        });
    }

    @PUT
    @Path("{param_id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modifyDiscount(@Context final HttpServletRequest req, @PathParam("param_id") int paramId, String jsonData) {
        return ServerUtils.genericAdminMethod(req, paramId, jsonData, () -> {
            ResponseBuilder response = Response.status(Status.BAD_REQUEST);

            // Si la infromación que se recibe es nula, no se procesa nada.
            if (jsonData.length() != 0) {
                // Convertimos la información JSON recibida en un objeto.
                final JsonObject jsonRequest = Json.createReader(new StringReader(jsonData)).readObject();

                DiscountsMgt discountMgt = Connections.getController().acquireDiscounts();

                try {
                    // Inicializamos el objeto.
                    Discount disAux = discountMgt.get(paramId);

                    // Agregamos la información al descuento.
                    disAux.setCode(jsonRequest.isNull("codeDisc") ? disAux.getCode() : jsonRequest.getString("codeDisc"));
                    disAux.setDesc(jsonRequest.isNull("descDisc") ? disAux.getDesc() : jsonRequest.getString("descDisc"));
                    disAux.setPriceDisc(jsonRequest.isNull("pricePerc") ? disAux.getPrizeDisc() : Float.valueOf(jsonRequest.getString("pricePerc")));

                    // Agregamos el descuento a la lista.
                    discountMgt.modify(disAux);

                    // Si todo ha salido bien hasta ahora, lanzamos la respuesta 200 OK.
                    response = Response.status(Status.OK);
                } catch (ManagerErrorException e) {
                    // Si nos se ha encontrado, lanzamos la respuesta 404 NOT FOUND.
                    ServerApp.getLoggerSystem().info(e.toString());
                    response = Response.status(Status.NOT_FOUND);
                }

                Connections.getController().releaseDiscounts(discountMgt);
            }

            return response;
        });
    }

    @DELETE
    @Path("{param_id}")
    public Response deleteDiscount(@Context final HttpServletRequest req, @PathParam("param_id") int paramId) {
        return ServerUtils.genericAdminMethod(req, paramId, null, () -> {
            ResponseBuilder response;

            DiscountsMgt discountMgt = Connections.getController().acquireDiscounts();

            try {
                // Obtenemos el descuento de la base de datos.
                Discount disAux = discountMgt.get(paramId);

                // Agregamos el descuento a la lista.
                discountMgt.remove(disAux);

                // Si todo ha salido bien hasta ahora, lanzamos la respuesta 200 OK.
                response = Response.status(Status.OK);
            } catch (ManagerErrorException e) {
                // Si no se ha encontrado, lanzamos la respuesta 404 NOT FOUND.
                ServerApp.getLoggerSystem().info(e.toString());
                response = Response.status(Status.NOT_FOUND);
            }

            Connections.getController().releaseDiscounts(discountMgt);

            return response;
        });
    }
}