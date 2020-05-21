package io.Mauzo.Server.Controllers;

import io.Mauzo.Server.Managers.Connections;
import io.Mauzo.Server.Managers.InformsMgt;
import io.Mauzo.Server.ServerApp;
import io.Mauzo.Server.ServerUtils;
import io.Mauzo.Server.Templates.Inform;
import org.springframework.stereotype.Component;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import io.Mauzo.Server.Managers.ManagersIntf.ManagerErrorException;


@Component
@Path("/informs")
public class InformsCtrl {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInformMethod(@Context final HttpServletRequest req) {
        return ServerUtils.genericUserMethod(req, null, null, () -> {
            JsonArrayBuilder jsonResponse = Json.createArrayBuilder();

            InformsMgt informsMgt = Connections.getController().acquireInforms();

            try {

                for (Inform inform : informsMgt.getList()) {
                    JsonObjectBuilder jsonObj = Json.createObjectBuilder();

                    jsonObj.add("id", inform.getId());
                    jsonObj.add("nSales", inform.getnSales());
                    jsonObj.add("nRefunds", inform.getnRefunds());
                    jsonObj.add("nDiscounts", inform.getnDiscounts());
                    //TODO: Hashmap
                    jsonObj.add("dStart", inform.getdStart().getTime());
                    jsonObj.add("dEnd", inform.getdEnd().getTime());

                    jsonResponse.add(jsonObj);
                }

            } finally {
                Connections.getController().releaseInforms(informsMgt);
            }

            return Response.ok(jsonResponse.build().toString(), MediaType.APPLICATION_JSON);
        });
    }

    @GET
    @Path("{param_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDiscount(@Context final HttpServletRequest req, @PathParam("param_id") int param) {
        return ServerUtils.genericAdminMethod(req, param, null, () -> {
            ResponseBuilder response = null;
            InformsMgt informsMgt = Connections.getController().acquireInforms();

            try {
                JsonObjectBuilder jsonResponse = Json.createObjectBuilder();
                Inform inform = informsMgt.get(param);

                jsonResponse.add("id", inform.getId());
                jsonResponse.add("nSales", inform.getnSales());
                jsonResponse.add("nRefunds", inform.getnRefunds());
                jsonResponse.add("nDiscounts", inform.getnDiscounts());
                //TODO: Hashmap
                jsonResponse.add("dStart", inform.getdStart().getTime());
                jsonResponse.add("dEnd", inform.getdEnd().getTime());

                response = Response.ok(jsonResponse.build().toString(), MediaType.APPLICATION_JSON);
            } catch (ManagerErrorException e) {
                ServerApp.getLoggerSystem().debug(e.toString());
                response = Response.status(Status.NOT_FOUND);
            } finally {
                Connections.getController().releaseInforms(informsMgt);
            }

            return response;
        });
    }
}