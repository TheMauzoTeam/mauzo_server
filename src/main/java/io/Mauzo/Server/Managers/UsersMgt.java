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
package io.Mauzo.Server.Managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import io.Mauzo.Server.ServerUtils;
import io.Mauzo.Server.Templates.User;

/**
 * Clase para gestionar los usuarios respecto a la base de datos.
 * 
 * La utilidad de esta clase es poder implementar métodos que permitan
 * hacer operaciones CRUD con la base de datos mediante el lenguaje SQL,
 * para lograr una eficiencia en las consultas, utilizamos Consultas Preparadas,
 * esto nos ayuda a que solo tengamos que introducir las variables que deseamos 
 * a la base de datos.
 * 
 * @author Neirth Sergio Martínez
 */
public class UsersMgt implements ManagersIntf<User> {
    // Dejamos preparadas las consultas
    private final PreparedStatement addQuery;
    private final PreparedStatement getIdQuery;
    private final PreparedStatement getNameQuery;
    private final PreparedStatement getListQuery;
    private final PreparedStatement modifyQuery;
    private final PreparedStatement deleteQuery;

    /**
     * Constructor donde se obtiene una connexion y se prepara 
     * las preparared statements para poder realizar las consultas.
     * 
     * @throws SQLException Excepcion en la consulta SQL.
     */
    UsersMgt(Connection conn) throws SQLException {  
        // Dejamos las consultas preparadas
        addQuery = conn.prepareStatement("INSERT INTO Users (firstname, lastname, username, email, password, isAdmin, userPic) VALUES (?, ?, ?, ?, ?, ?, ?);");
        getIdQuery = conn.prepareStatement("SELECT * FROM Users WHERE id = ?;");
        getNameQuery = conn.prepareStatement("SELECT * FROM Users WHERE username = ?;");
        getListQuery = conn.prepareStatement("SELECT * FROM Users;");
        modifyQuery = conn.prepareStatement("UPDATE Users SET firstname = ?, lastname = ?, username = ?, email = ?, password = ?, isAdmin = ?, userPic = ? WHERE id = ?;");
        deleteQuery = conn.prepareStatement("DELETE FROM Users WHERE id = ?;");
    }

    /**
     * Método para añadir usuarios a la base de datos.
     * 
     * @param user El usuario encapsulado en un objeto.
     * @throws SQLException Excepcion en la consulta SQL.
     */
    @Override
    public void add(User user) throws SQLException {
        // Asociamos los valores respecto a la sentencia sql.
        addQuery.setString(1, user.getFirstName());
        
        addQuery.setString(2, user.getLastName());
        addQuery.setString(3, user.getUsername());
        addQuery.setString(4, user.getEmail());
        addQuery.setString(5, user.getPassword());
        addQuery.setBoolean(6, user.isAdmin());

        // Este es un posible valor nulo.
        if(user.getUserPic() != null) {
            addQuery.setBytes(7, ServerUtils.imageToByteArray(user.getUserPic(), "png"));
        } else {
            addQuery.setNull(7, Types.BINARY);
        }

        // Ejecutamos la sentencia sql.
        addQuery.execute();
    }

    /**
     * Método para obtener en forma de objeto el usuario, a partir de un id de
     * usuario, el usuario encapsulado.
     * 
     * @param id El id de usuario.
     * @return El usuario encapsulado en forma de objeto.
     * @throws SQLException Excepcion en la consulta SQL.
     * @throws ManagerErrorException Excepcion dada al no encontrar el usuario solicitado.
     */
    @Override
    public User get(int id) throws SQLException, ManagerErrorException {
        // Preparamos una instancia del objeto a devolver
        User user = null;

        // Asociamos los valores respecto a la sentencia sql.
        getIdQuery.setInt(1, id);

        // Ejecutamos la sentencia sql y recuperamos lo que nos ha retornado.
        try (ResultSet rs = getIdQuery.executeQuery()) {
            if (rs.next()) {
                user = new User();

                user.setId(rs.getInt("id"));
                user.setAdmin(rs.getBoolean("isAdmin"));
                user.setEmail(rs.getString("email"));
                user.setFirstName(rs.getString("firstname"));
                user.setLastName(rs.getString("lastname"));
                user.setPassword(rs.getString("password"));
                user.setUsername(rs.getString("username"));
                user.setUserPic(ServerUtils.imageFromByteArray(rs.getBytes("userPic")));
            } else { 
                throw new ManagerErrorException("No se ha encontrado el usuario");
            }
        }

        return user;
    }

    /**
     * Método para obtener en forma de objeto el usuario, a partir de un nombre de
     * usuario, el usuario encapsulado.
     * 
     * @param username El nombre de usuario.
     * @return El usuario encapsulado en forma de objeto.
     * @throws SQLException Excepcion en la consulta SQL.
     * @throws ManagerErrorException Excepcion dada al no encontrar el usuario solicitado.
     */
    public User get(String username) throws SQLException, ManagerErrorException {
        // Preparamos una instancia del objeto a devolver
        User user = null;

        // Asociamos los valores respecto a la sentencia sql.
        getNameQuery.setString(1, username);

        // Ejecutamos la sentencia sql y recuperamos lo que nos ha retornado.
        try (ResultSet rs = getNameQuery.executeQuery()) {
            if (rs.next()) {
                user = new User();

                user.setId(rs.getInt("id"));
                user.setAdmin(rs.getBoolean("isAdmin"));
                user.setEmail(rs.getString("email"));
                user.setFirstName(rs.getString("firstname"));
                user.setLastName(rs.getString("lastname"));
                user.setPassword(rs.getString("password"));
                user.setUsername(rs.getString("username"));
                user.setUserPic(ServerUtils.imageFromByteArray(rs.getBytes("userPic")));
            } else {
                throw new ManagerErrorException("No se ha encontrado el usuario");
            }
        }

        return user;
    }

    /**
     * Método para obtener en forma de lista de usuarios, los usuarios presentes
     * en la base de datos.
     * 
     * @return El listado de usuarios.
     * @throws SQLException Excepcion en la consulta SQL.
     */
    @Override
    public List<User> getList() throws SQLException {
        // Preparamos una instancia del objeto a devolver
        List<User> usersList = null;

        // Lanzamos la consulta SQL y generamos la lista de usuarios.
        try(ResultSet rs = getListQuery.executeQuery()) {
            usersList = new ArrayList<>();

            while (rs.next()) {
                User user = new User();

                user.setId(rs.getInt("id"));
                user.setAdmin(rs.getBoolean("isAdmin"));
                user.setEmail(rs.getString("email"));
                user.setFirstName(rs.getString("firstname"));
                user.setLastName(rs.getString("lastname"));
                user.setPassword(rs.getString("password"));
                user.setUsername(rs.getString("username"));
                user.setUserPic(ServerUtils.imageFromByteArray(rs.getBytes("userPic")));

                usersList.add(user);
            }
        }

        return usersList;
    }

    /**
     * Método para actualizar el usuario en la base de datos.
     * 
     * @param user El usuario encapsulado en un objeto.
     * @throws SQLException Excepcion en la consulta SQL.
     * @throws ManagerErrorException Excepcion dada al no encontrar el usuario solicitado.
     */
    @Override
    public void modify(User user) throws SQLException, ManagerErrorException {
        // Asociamos los valores respecto a la sentencia sql.
        modifyQuery.setString(1, user.getFirstName());
        modifyQuery.setString(2, user.getLastName());
        modifyQuery.setString(3, user.getUsername());
        modifyQuery.setString(4, user.getEmail());
        modifyQuery.setString(5, user.getPassword());
        modifyQuery.setBoolean(6, user.isAdmin());
        
        // Este es un posible valor nulo.
        if(user.getUserPic() != null) {
            modifyQuery.setBytes(7, ServerUtils.imageToByteArray(user.getUserPic(), "png"));
        } else {
            modifyQuery.setNull(7, Types.BINARY);
        }

        modifyQuery.setInt(8, user.getId());

        // Ejecutamos la sentencia sql.
        if(modifyQuery.executeUpdate() == 0) 
            throw new ManagerErrorException("No se ha encontrado el usuario durante la actualización del mismo.");
    }
    
    /**
     * Método para eliminar el usuario en la base de datos.
     * 
     * @param user El usuario encapsulado en un objeto.
     * @throws SQLException Excepcion en la consulta SQL.
     * @throws ManagerErrorException Excepcion dada al no encontrar el usuario solicitado.
     */
    @Override
    public void remove(User user) throws SQLException, ManagerErrorException {
        // Preparamos la sentencia sql.
        deleteQuery.setInt(1, user.getId());

        // Ejecutamos la sentencia sql.
        if(deleteQuery.executeUpdate() == 0) 
            throw new ManagerErrorException("No se ha encontrado el usuario durante la eliminación del mismo.");
    }
}