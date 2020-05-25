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

/**
 * Clase controladora de informes, la cual gestiona las operaciones CRUD
 * con la base de datos a traves de una interfaz Rest API.
 *
 * Existen dos formas de acceder a los datos, de manera general, la cual
 * te mostrará todos los datos existentes en la base de datos en relación
 * a lo solicitado, y de manera concreta, la cual vas a poder acceder a la
 * unidad de información solicitada.
 *
 * @author Ant04X Antonio Izquierdo
 */
@Component
@Path("/informs")
public class InformsCtrl {
    /**
     * Controlador que permite a un usuario obtener un listado de informes dentro
     * del servidor, permitiendo asi obtener de manera dinamica los informes validos u
     * otros usuarios validos dentro del sistema.
     *
     * El contenido que recibirá esta vista http es mediante una peticion GET con
     * la estructura de atributos de id, nSales, nRefunds, nDiscounts, dStart y dEnd.
     *
     * @param req      El header de la petición HTTP.
     * @return La respuesta generada por parte de la vista.
     */
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

    /**
     * Controlador que permite a un usuario obtener un informe especifica dentro
     * del servidor, permitiendo asi obtener de manera dinamica el informe requeria
     * como parametro en la interfaz web.
     *
     * El contenido que recibirá esta vista http es mediante una peticion GET con
     * la estructura de atributos de id, nSales, nRefunds, nDiscounts, dStart y dEnd.
     *
     * @param req      El header de la petición HTTP.
     * @param param  El ID de usuario en la peticion HTTP.
     * @return La respuesta generada por parte de la vista.
     */
    @GET
    @Path("{param_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInform(@Context final HttpServletRequest req, @PathParam("param_id") int param) {

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