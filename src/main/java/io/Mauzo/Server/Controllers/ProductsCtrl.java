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
import io.Mauzo.Server.ServerUtils;
import io.Mauzo.Server.ServerPools;
import io.Mauzo.Server.Templates.Product;
import io.Mauzo.Server.Managers.ProductsMgt;


@Component
@Path("/products")
public class ProductsCtrl {
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Response getProductsMethod(@Context final HttpServletRequest req) {
            return ServerUtils.genericUserMethod(req, null, null, () -> {
                JsonArrayBuilder jsonResponse = Json.createArrayBuilder();

                ProductsMgt productsMgt = ServerPools.getController().acquireProducts();

                for (Product product : productsMgt.getList()) {
                    // Inicializamos los objetos a usar.
                    JsonObjectBuilder jsonObj = Json.createObjectBuilder();

                    // Construimos el objeto Json con los atributo de la venta.
                    jsonObj.add("id", product.getId());
                    jsonObj.add("ProdCode", product.getCode());
                    jsonObj.add("prodName", product.getName());
                    jsonObj.add("prodDesc", product.getDescription());
                    jsonObj.add("prodPrice", product.getPrice());
                    jsonObj.add("prodPic", ServerUtils.byteArrayToBase64(ServerUtils.imageToByteArray(product.getPicture(),"png")));
                    // Lo añadimos al Json Array.
                    jsonResponse.add(jsonObj);
                }

                ServerPools.getController().releaseProducts(productsMgt);

                return Response.ok(jsonResponse.build().toString());
            });
        }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addSalesMethod(@Context final HttpServletRequest req, String jsonData) {
        return ServerUtils.genericUserMethod(req, null, jsonData, () -> {
            ResponseBuilder response = Response.status(Status.BAD_REQUEST);

            ProductsMgt productsMgt = ServerPools.getController().acquireProducts();

            // Si la informacion que recibe es nula, no se procesa nada
            if (jsonData.length() != 0) {
                // Convertimos la información JSON recibida en un objeto.
                final JsonObject jsonRequest = Json.createReader(new StringReader(jsonData)).readObject();

                // Incializamos el objeto.
                Product product = new Product();

                // Agregamos la información de la venta.
                product.setId(jsonRequest.getInt("id"));
                product.setName(jsonRequest.getString("prodName"));
                product.setDescription(jsonRequest.getString("prodDesc"));
                product.setCode(jsonRequest.getString("prodCode"));
                product.setPrice(Float.valueOf(jsonRequest.getString("prodPrice")));

                // Agregamos la venta a la lista.
                productsMgt.add(product);

                // Si todo ha ido bien hasta ahora, lanzamos la respuesta 200 OK.
                response = Response.status(Status.OK);
            }

            ServerPools.getController().releaseProducts(productsMgt);

            return response;
        });
    }
}
