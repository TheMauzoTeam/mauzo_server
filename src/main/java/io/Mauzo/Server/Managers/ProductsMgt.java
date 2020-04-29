package io.Mauzo.Server.Managers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import io.Mauzo.Server.ServerUtils;
import io.Mauzo.Server.Templates.Product;
import io.Mauzo.Server.ServerApp;

public class ProductsMgt implements ManagersIntf<Product>{
    private static ProductsMgt controller = null;


    //remove, modify, get, add, getProductsList

    /**
     * Método que añade productos a la base de datos.
     *
     * @param product El producto a añadir.
     * @Exception SQLException Excepcion de la consulta SQL
     */
    @Override
    public void add(Product product) throws SQLException{
        final Connection connection = ServerApp.getConnection();

        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO Products (id, code, ProdName, ProdPrice, ProdDesc, ProdPic) VALUES (?, ?, ?, ?, ?, ?);")) {
            //Asociamos los valores
            statement.setInt(1, product.getId());
            statement.setString(2, product.getCode());
            statement.setString(3, product.getName());
            statement.setDouble(4,product.getPrice());
            statement.setString(5, product.getDescription());
            statement.setBytes(6, ServerUtils.imageToByteArray(product.getPicture(),"png"));
            //Ejecutamos la sentencia SQl
            statement.execute();
        }
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
         final Connection connection = ServerApp.getConnection();

         try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM Products WHERE id = ?;")){
             //Asociamos los valores
             statement.setInt(1, id);

             //Ejecutamos la sentencia y conseguimos el resto de datos relacionados
             try (ResultSet resultSet = statement.executeQuery()){
                 if (!(resultSet.isLast())) {
                     while (resultSet.next()) {
                         product = new Product();

                         product.setId(resultSet.getInt("id"));
                         product.setCode(resultSet.getString("code"));
                         product.setName(resultSet.getString("prodName"));
                         product.setPrice(resultSet.getFloat("prodPrice"));
                         product.setDescription(resultSet.getString("prodDesc"));
                         product.setPicture(ServerUtils.imageFromByteArray(resultSet.getBytes("prodPic")));

                     }

                 }
                 else
                     throw new ManagerErrorException("No se ha encontrado el producto");
             }

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

       final Connection connection = ServerApp.getConnection();

       try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM Products WHERE prodName = ?;")){
            statement.setString(1, name);

            try (ResultSet resultSet = statement.executeQuery()){
                if (!resultSet.isLast()){
                    while (resultSet.next()){
                        product = new Product();

                        product.setId(resultSet.getInt("id"));
                        product.setCode(resultSet.getString("code"));
                        product.setDescription(resultSet.getString("prodDesc"));
                        product.setPrice(resultSet.getFloat("prodPrice"));
                        product.setName(resultSet.getString("prodName"));
                        product.setPicture(ServerUtils.imageFromByteArray(resultSet.getBytes("prodPic")));
                    }
                }else {
                    throw new ManagerErrorException("No se ha encontrado el producto");
                }
            }
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
        final Connection connection = ServerApp.getConnection();
        List<Product> products = null;

        try(PreparedStatement statement = connection.prepareStatement("SELECT * FROM Products")){
            try(ResultSet resultSet = statement.executeQuery()) {
                products = new ArrayList<>();

                while (resultSet.next()) {
                    Product product = new Product();

                    product.setId(resultSet.getInt("id"));
                    product.setName(resultSet.getString("prodName"));
                    product.setPrice(resultSet.getFloat("prodPrice"));
                    product.setCode(resultSet.getString("code"));
                    product.setDescription(resultSet.getString("prodDesc"));
                    product.setPicture(ServerUtils.imageFromByteArray(resultSet.getBytes("prodPic")));

                    products.add(product);
                }
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
        final Connection connection = ServerApp.getConnection();

        //Preparamos la sentencia sql
        try (PreparedStatement statement = connection.prepareStatement("UPDATE Users SET code = ?, prodName = ?, prodPrice = ?, prodDesc = ? WHILE id = ?")){

            statement.setString(1, obj.getCode());
            statement.setString(2, obj.getName());
            statement.setDouble(3, obj.getPrice());
            statement.setString(4,obj.getDescription());
            statement.setBytes(7,ServerUtils.imageToByteArray(obj.getPicture(),"png"));
        }
    }

    /**
     * Método para eliminar el producto
     * @param obj producto de la base de datos
     * @throws SQLException Excepción de la consulta SQL
     * @throws ManagerErrorException Excepción dada al no encontrar el producto
     */
    @Override
    public void remove(Product obj) throws SQLException, ManagerErrorException {
        final Connection connection = ServerApp.getConnection();

        try (PreparedStatement st = connection.prepareStatement("DELETE FROM Products WHERE id = ?;")) {
            st.setInt(1, obj.getId());

            // Ejecutamos la sentencia sql.
            if(st.execute() == false)
                throw new ManagerErrorException("No se ha encontrado el producto");
        }
    }

    /**
     * Método para recuperar el controlador de la clase ProductsMgt.
     *
     * @return El controlador de la clase ProductsMgt
     */
    public static ProductsMgt getController() {
        if (controller == null)
            controller = new ProductsMgt();

        return controller;
    }
}
