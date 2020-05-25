/**
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
package io.Mauzo.Server.Managers;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import io.Mauzo.Server.ServerUtils;
import io.Mauzo.Server.Templates.Product;

/**
 * Clase para gestionar los productos en la base de datos.
 *
 * La utilidad de esta de clase es poder implementar métodos que permitan hacer operaciones CRUD.
 * Para lograr eficiencia en las consultas utilizamos PreparedStatement, esto nos permite introducir las variables que necesitamos en la base de datos.
 *
 * @author lluminar Lidia Martínez
 */
public class ProductsMgt implements ManagersIntf<Product>{
    private final PreparedStatement addQuery;
    private final PreparedStatement getIdQuery;
    private final PreparedStatement getNameQuery;
    private final PreparedStatement getListQuery;
    private final PreparedStatement modifyQuery;
    private final PreparedStatement removeQuery;

    ProductsMgt(Connection connection) throws SQLException {
        addQuery = connection.prepareStatement("INSERT INTO Products ( ProdCode, ProdName, ProdPrice, ProdDesc, ProdPic) VALUES ( ?, ?, ?, ?, ?);");
        getIdQuery = connection.prepareStatement("SELECT * FROM Products WHERE id = ?;");
        getNameQuery = connection.prepareStatement("SELECT * FROM Products WHERE prodCode = ?;");
        getListQuery = connection.prepareStatement("SELECT * FROM Products");
        modifyQuery = connection.prepareStatement("UPDATE Products SET ProdCode = ?, prodName = ?, prodPrice = ?, prodDesc = ?, ProdPic = ? WHERE id = ?");
        removeQuery = connection.prepareStatement("DELETE FROM Products WHERE id = ?;");
    }

    /**
     * Método que añade productos a la base de datos.
     *
     * @param product El producto a añadir.
     * @Exception SQLException Excepcion de la consulta SQL
     */
    @Override
    public void add(Product product) throws SQLException{
            //Asociamos los valores
            addQuery.setString(1, product.getCode());
            addQuery.setString(2, product.getName());
            addQuery.setDouble(3,product.getPrice());
            addQuery.setString(4, product.getDescription());

            if(product.getPicture() != null) {
                addQuery.setBytes(5, ServerUtils.imageToByteArray(product.getPicture(), "png"));
            } else {
                addQuery.setNull(5, Types.BINARY);
            }

            //Ejecutamos la sentencia SQl
            addQuery.execute();
    }

    /**
     *Método para obtener el producto a partir de un id
     *
     * @param id    ID del objeto en la base de datos.
     * @return el producto en forma de objeto
     * @throws SQLException Excepción de la consuulta SQL
     * @throws ManagerErrorException Excepción dado al no poder encontrar el producto
     */
    @Override
    public Product get(int id) throws SQLException, ManagerErrorException {
            Product product = null;

            //Asociamos los valores
             getIdQuery.setInt(1, id);

             //Ejecutamos la sentencia y conseguimos el resto de datos relacionados
             try (ResultSet resultSet = getIdQuery.executeQuery()){
                 if (!(resultSet.isLast())) {
                     while (resultSet.next()) {
                         product = new Product();

                         product.setId(resultSet.getInt("id"));
                         product.setCode(resultSet.getString("ProdCode"));
                         product.setName(resultSet.getString("prodName"));
                         product.setPrice(resultSet.getFloat("prodPrice"));
                         product.setDescription(resultSet.getString("prodDesc"));
                         product.setPicture(ServerUtils.imageFromByteArray(resultSet.getBytes("prodPic")));
                     }
                 } else
                     throw new ManagerErrorException("No se ha encontrado el producto");
             }

             return product;
    }

    /**
     * Método para obtener el producto a partir de su nombre
     *
     * @param name  El nombre del objeto en la base de datos.
     * @return El producto en forma de objeto
     * @throws SQLException Excepción en la consulta SQL
     * @throws ManagerErrorException Excepción dada en caso de no encontrar el producto
     */
    public Product get(String name) throws SQLException, ManagerErrorException {
           Product product = null;

           getNameQuery.setString(1, name);

            try (ResultSet resultSet = getNameQuery.executeQuery()){
                if (resultSet.next()) {

                    product = new Product();

                    product.setId(resultSet.getInt("id"));
                    product.setCode(resultSet.getString("ProdCode"));
                    product.setDescription(resultSet.getString("prodDesc"));
                    product.setPrice(resultSet.getFloat("prodPrice"));
                    product.setName(resultSet.getString("prodName"));
                    product.setPicture(ServerUtils.imageFromByteArray(resultSet.getBytes("prodPic")));

                } else
                    throw new ManagerErrorException("No se ha encontrado el producto");

            }

           return product;
    }

    /**
     * Método para obtener la lista de productos de la base de datos
     *
     * @return La lista de productos
     * @throws SQLException Excepción en la consulta SQL
     */
    @Override
    public List<Product> getList() throws SQLException {
            List<Product> products = null;

            try(ResultSet resultSet = getListQuery.executeQuery()) {
                products = new ArrayList<>();

                while (resultSet.next()) {
                    Product product = new Product();

                    product.setId(resultSet.getInt("id"));
                    product.setName(resultSet.getString("prodName"));
                    product.setPrice(resultSet.getFloat("prodPrice"));
                    product.setCode(resultSet.getString("ProdCode"));
                    product.setDescription(resultSet.getString("prodDesc"));
                    product.setPicture(ServerUtils.imageFromByteArray(resultSet.getBytes("prodPic")));

                    products.add(product);
                }
            }

            return products;
    }

    /**
     * Método para poder modificar el producto en la base de datos
     *
     * @param obj El producto en la base de datos
     * @throws SQLException Excepción en la consulta SQL
     * @throws ManagerErrorException Excepción que se da al no encontrar el producto
     */
    @Override
    public void modify(Product obj) throws SQLException, ManagerErrorException {
            modifyQuery.setString(1, obj.getCode());
            modifyQuery.setString(2, obj.getName());
            modifyQuery.setDouble(3, obj.getPrice());
            modifyQuery.setString(4,obj.getDescription());

            // Este es un posible valor nulo.
           if(obj.getPicture() != null) {
                modifyQuery.setBytes(5, ServerUtils.imageToByteArray(obj.getPicture(), "png"));
           } else {
                modifyQuery.setNull(5, Types.BINARY);
           }

            modifyQuery.setInt(6,obj.getId());

           if(modifyQuery.executeUpdate() == 0)
                throw new ManagerErrorException("No se ha encontrado el producto durante la actualización del mismo.");
    }

    /**
     * Método para eliminar el producto
     * @param obj producto de la base de datos
     * @throws SQLException Excepción de la consulta SQL
     * @throws ManagerErrorException Excepción dada al no encontrar el producto
     */
    @Override
    public void remove(Product obj) throws SQLException, ManagerErrorException {
        removeQuery.setInt(1, obj.getId());

        // Ejecutamos la sentencia sql.
        if(removeQuery.executeUpdate() == 1)
            throw new ManagerErrorException("No se ha encontrado el producto");
    }
}
