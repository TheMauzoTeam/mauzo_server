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


import io.Mauzo.Server.Managers.Connections;
import io.Mauzo.Server.Managers.ManagersIntf;
import io.Mauzo.Server.ServerApp;
import org.springframework.stereotype.Component;
import io.Mauzo.Server.ServerUtils;
import io.Mauzo.Server.Templates.Product;
import io.Mauzo.Server.Managers.ProductsMgt;

/**
 * Clase controladora de productos que gestiona las operaciones CRUD con la base de datos a través de una interfaz Rest API.
 *
 * Existen dos formas de acceder a los datos, de manera general, la cual
 * te mostrará todos los datos existentes en la base de datos en relación
 * a lo solicitado, y de manera concreta, la cual vas a poder acceder a la
 * unidad de información solicitada.
 *
 * @author lluminar Lidia Martínez
 */
@Component
@Path("/products")
public class ProductsCtrl {
    /**
     * Controlador que permite a un administrador obtener un listado de productos dentro
     * del servidor, permitiendo así, obtener de manera dinámica los productos válidos.
     *
     * El contenido que recibirá esta vista http es mediante una peticion GET con
     * la estructura de atributos de prodId, prodName, prodCode, prodDesc, prodPrice, prodPic.
     *
     * @param req Nos da la cabecera de la consulta
     * @return Devuelve una respuesta HTTP
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProductsMethod(@Context final HttpServletRequest req) {
        return ServerUtils.genericUserMethod(req, null, null, () -> {
            JsonArrayBuilder jsonResponse = Json.createArrayBuilder();

            ProductsMgt productsMgt = Connections.getController().acquireProducts();

            for (Product product : productsMgt.getList()) {
                // Inicializamos los objetos a usar.
                JsonObjectBuilder jsonObj = Json.createObjectBuilder();

                // Construimos el objeto Json con los atributo de la venta.
                jsonObj.add("prodId", product.getId());
                jsonObj.add("prodCode", product.getCode());
                jsonObj.add("prodName", product.getName());
                jsonObj.add("prodDesc", product.getDescription());
                jsonObj.add("prodPrice", product.getPrice());
                jsonObj.add("prodPic", ServerUtils.byteArrayToBase64(ServerUtils.imageToByteArray(product.getPicture(),"png")));
                // Lo añadimos al Json Array.
                jsonResponse.add(jsonObj);
            }

            Connections.getController().releaseProducts(productsMgt);

            return Response.ok(jsonResponse.build().toString());
        });
    }

    /**
     * Controlador que permite a un administrador registrar productos dentro del
     * servidor, permitiendo así, agregar de manera dinámica productos válidos.
     *
     * El contenido que recibirá esta vista http es mediante una peticion POST con
     * la estructura de atributos de prodId, prodCode, prodName, prodDesc, prodPrice, prodPic.
     *
     * @param req      El header de la petición HTTP.
     * @param jsonData El body de la petición HTTP.
     * @return La respuesta generada por parte de la vista.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addProductMethod(@Context final HttpServletRequest req, String jsonData) {
        return ServerUtils.genericUserMethod(req, null, jsonData, () -> {
            ResponseBuilder response = Response.status(Status.BAD_REQUEST);

            ProductsMgt productsMgt = Connections.getController().acquireProducts();

            // Si la informacion que recibe es nula, no se procesa nada
            if (jsonData.length() != 0) {
                // Convertimos la información JSON recibida en un objeto.
                final JsonObject jsonRequest = Json.createReader(new StringReader(jsonData)).readObject();

                // Incializamos el objeto.
                Product product = new Product();

                // Agregamos la información de la venta.
                product.setId(jsonRequest.getInt("prodId"));
                product.setName(jsonRequest.getString("prodName"));
                product.setDescription(jsonRequest.getString("prodDesc"));
                product.setCode(jsonRequest.getString("prodCode"));
                product.setPrice(Float.valueOf(jsonRequest.getString("prodPrice")));

                // Agregamos la venta a la lista.
                productsMgt.add(product);

                // Si todo ha ido bien hasta ahora, lanzamos la respuesta 200 OK.
                response = Response.status(Status.OK);
            }

            Connections.getController().releaseProducts(productsMgt);

            return response;
        });
    }

    /**
     * Controlador que permite a un administrador obtener un producto especifico dentro
     * del servidor, permitiendo asi obtener de manera dinámica el producto requerido
     * como parámetro en la interfaz web.
     *
     * El contenido que recibirá esta vista http es mediante una petición GET con
     * la estructura de atributos de prodId, prodName, prodCode, prodDesc, prodPrice, prodPic.
     *
     * @param req      El header de la petición HTTP.
     * @param paramProductId  El ID del producto en la peticion HTTP.
     * @return La respuesta generada por parte de la vista.
     */
    @GET
    @Path("{param_prodId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProduct(@Context final HttpServletRequest req, @PathParam("param_prodId") int paramProductId) {
        return ServerUtils.genericAdminMethod(req, paramProductId, null, () -> {
            ResponseBuilder response = null;

            // Adquirimos una conexión de productos
            ProductsMgt productsMgt = Connections.getController().acquireProducts();

            try {
                // Inicializamos los objetos a usar.
                JsonObjectBuilder jsonResponse = Json.createObjectBuilder();
                Product product = productsMgt.get(paramProductId);

                // Generamos un JSON con los atributos del producto.
                jsonResponse.add("prodId", product.getId());
                jsonResponse.add("prodName", product.getName());
                jsonResponse.add("prodCode", product.getCode());
                jsonResponse.add("prodPrice", product.getPrice());
                jsonResponse.add("prodDesc", product.getDescription());
                jsonResponse.add("prodPic", ServerUtils.byteArrayToBase64(ServerUtils.imageToByteArray(product.getPicture(), "png")));

                // Lanzamos la respuesta 200 OK si todo ha ido bien.
                response = Response.ok(jsonResponse.build().toString(), MediaType.APPLICATION_JSON);
            } catch (ManagersIntf.ManagerErrorException e) {
                // Si no se ha encontrado, lanzamos la respuesta 404 NOT FOUND.
                ServerApp.getLoggerSystem().info(e.toString());
                response = Response.status(Status.NOT_FOUND);
            } finally {
                // Devolvemos la conexión de productos
                Connections.getController().releaseProducts(productsMgt);
            }

            return response;
        });
    }

    /**
     * Controlador que gestiona las actualizaciones de información de los productos
     * cuya información se recibe mediante una peticion PUT a la interfaz web
     * http://HOST_URL/api/products/(prodId)
     *
     * @param req      El header de la petición HTTP.
     * @param paramProdId  El ID del producto en la peticion HTTP.
     * @param jsonData El body de la petición HTTP.
     * @return La respuesta generada por parte de la vista.
     */
    @PUT
    @Path("{param_prodId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modifyProduct(@Context final HttpServletRequest req, @PathParam("paramProdId") int paramProdId, String jsonData) {
        return ServerUtils.genericAdminMethod(req, paramProdId, jsonData, () -> {
            ResponseBuilder response = Response.status(Status.BAD_REQUEST);

            // Si la informacion que recibe es nula, no se procesa nada.
            if (jsonData.length() != 0) {
                // Convertimos la información JSON recibida en un objeto.
                final JsonObject jsonRequest = Json.createReader(new StringReader(jsonData)).readObject();

                // Adquirimos una conexión de productos
                ProductsMgt productsMgt = Connections.getController().acquireProducts();

                try {
                    // Incializamos el objeto.
                    Product product = productsMgt.get(paramProdId);

                    // Agregamos la información al producto.
                    product.setName(jsonRequest.isNull("prodName") ? product.getName() : jsonRequest.getString("prodName"));
                    product.setCode(jsonRequest.isNull("prodCode") ? product.getCode() : jsonRequest.getString("prodCode"));
                    product.setPrice(jsonRequest.isNull("prodPrice") ? product.getPrice() : Float.valueOf(jsonRequest.getString("prodPrice")));
                    product.setDescription(jsonRequest.isNull("prodDesc") ? product.getDescription() : jsonRequest.getString("prodDesc"));
                    product.setPicture(jsonRequest.isNull("prodPic") ? product.getPicture() : ServerUtils.imageFromByteArray(ServerUtils.byteArrayFromBase64(jsonRequest.getString("prodPic"))));

                    // Agregamos el producto a la lista.
                    productsMgt.modify(product);

                    // Si todo ha ido bien hasta ahora, lanzamos la respuesta 200 OK.
                    response = Response.status(Status.OK);
                } catch (ManagersIntf.ManagerErrorException e) {
                    // Si no se ha encontrado, lanzamos la respuesta 404 NOT FOUND.
                    ServerApp.getLoggerSystem().info(e.toString());
                    response = Response.status(Status.NOT_FOUND);
                } finally {
                    // Devolvemos la conexión de productos
                    Connections.getController().releaseProducts(productsMgt);
                }
            }

            return response;
        });
    }

    /**
     * Controlador para eliminar productos pasados por parámetro en la interfaz web
     * http://HOST_URL/api/products/(prodId) con el tipo de petición DELETE.
     *
     * @param req El header de la petición HTTP.
     * @param paramProductId  El ID del producto en la peticion HTTP.
     * @return La respuesta generada por parte de la vista.
     */
    @DELETE
    @Path("{param_prodId}")
    public Response deleteProduct(@Context final HttpServletRequest req, @PathParam("param_productId") int paramProductId) {
        return ServerUtils.genericAdminMethod(req, paramProductId, null, () -> {
            ResponseBuilder response;

            // Adquirimos una conexión de productos
            ProductsMgt productsMgt = Connections.getController().acquireProducts();

            try {
                // Obtenemos el producto de la base de datos.
                Product product = productsMgt.get(paramProductId);

                // Agregamos el producto a la lista.
                productsMgt.remove(product);

                // Si todo ha ido bien hasta ahora, lanzamos la respuesta 200 OK.
                response = Response.status(Status.OK);
            } catch (ManagersIntf.ManagerErrorException e) {
                // Si no se ha encontrado, lanzamos la respuesta 404 NOT FOUND.
                ServerApp.getLoggerSystem().info(e.toString());
                response = Response.status(Status.NOT_FOUND);
            } finally {
                // Devolvemos la conexión de usuarios
                Connections.getController().releaseProducts(productsMgt);
            }

            return response;
        });
    }
}
