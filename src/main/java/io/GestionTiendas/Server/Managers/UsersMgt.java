package io.GestionTiendas.Server.Managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import io.GestionTiendas.Server.ServerApp;
import io.GestionTiendas.Server.Templates.Users;

public class UsersMgt {
    private static UsersMgt controller = null;

    /**
     * Método para añadir usuarios a la base de datos.
     * 
     * @param user El usuario encapsulado en un objeto.
     * @throws SQLException Excepcion en la consulta SQL.
     */
    public void addUser(Users user) throws SQLException {
        // Guardamos el puntero de conexion con la base de datos.
        final Connection mainSql = ServerApp.getConnection();

        // Preparamos la consulta sql.
        // Las prepared statement no son de usar y tirar
        try (PreparedStatement statementSql = mainSql.prepareStatement(
                "INSERT INTO Users (firstname, lastname, username, email, password, isAdmin) VALUES (?, ?, ?, ?, ?, ?);")) {
            // Asociamos los valores respecto a la sentencia sql.
            statementSql.setString(1, user.getFirstName());
            statementSql.setString(2, user.getLastName());
            statementSql.setString(3, user.getUsername());
            statementSql.setString(4, user.getEmail());
            statementSql.setString(5, user.getPassword());
            statementSql.setBoolean(6, user.isAdmin());

            // Ejecutamos la sentencia sql.
            statementSql.execute();
        }
    }

    /**
     * Método para obtener en forma de objeto el usuario, a partir de un id de
     * usuario, el usuario encapsulado.
     * 
     * @param id El id de usuario.
     * @return El usuario encapsulado en forma de objeto.
     * @throws SQLException Excepcion en la consulta SQL.
     */
    public Users getUser(int id) throws SQLException {
        Users user = null;

        // Guardamos el puntero de conexion con la base de datos.
        final Connection mainSql = ServerApp.getConnection();

        // Preparamos la consulta sql.
        try (PreparedStatement statementSql = mainSql.prepareStatement("SELECT * FROM Users WHERE id = ?;")) {
            // Asociamos los valores respecto a la sentencia sql.
            statementSql.setInt(1, id);

            // Ejecutamos la sentencia sql y recuperamos lo que nos ha retornado.
            try (ResultSet rs = statementSql.executeQuery()) {
                while (rs.next()) {
                    user = new Users();

                    user.setId(rs.getInt("id"));
                    user.setAdmin(rs.getBoolean("isAdmin"));
                    user.setEmail(rs.getString("email"));
                    user.setFirstName(rs.getString("firstname"));
                    user.setLastName(rs.getString("lastname"));
                    user.setPassword(rs.getString("password"));
                    user.setUsername(rs.getString("username"));
                }
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
     */
    public Users getUser(String username) throws SQLException {
        Users user = null;

        // Guardamos el puntero de conexion con la base de datos.
        final Connection mainSql = ServerApp.getConnection();

        // Preparamos la consulta sql.
        try (PreparedStatement statementSql = mainSql.prepareStatement("SELECT * FROM Users WHERE username = ?;")) {
            // Asociamos los valores respecto a la sentencia sql.
            statementSql.setString(1, username);

            // Ejecutamos la sentencia sql y recuperamos lo que nos ha retornado.
            try (ResultSet rs = statementSql.executeQuery()) {
                while (rs.next()) {
                    user = new Users();

                    user.setId(rs.getInt("id"));
                    user.setAdmin(rs.getBoolean("isAdmin"));
                    user.setEmail(rs.getString("email"));
                    user.setFirstName(rs.getString("firstname"));
                    user.setLastName(rs.getString("lastname"));
                    user.setPassword(rs.getString("password"));
                    user.setUsername(rs.getString("username"));
                }
            }
        }

        return user;
    }

    /**
     * Método para eliminar el usuario en la base de datos.
     * 
     * @param user El usuario encapsulado en un objeto.
     * @throws SQLException Excepcion en la consulta SQL.
     */
    public void removeUser(Users user) throws SQLException {
        // Guardamos el puntero de conexion con la base de datos.
        final Connection mainSql = ServerApp.getConnection();

        // Preparamos la sentencia sql.
        try (PreparedStatement statementSql = mainSql.prepareStatement("SELECT * FROM Users WHERE id = ?;")) {
            statementSql.setInt(1, user.getId());

            // Ejecutamos la sentencia sql.
            statementSql.execute();
        }
    }

    /**
     * Método para actualizar el usuario en la base de datos.
     * 
     * @param user El usuario encapsulado en un objeto.
     * @throws SQLException Excepcion en la consulta SQL.
     */
    public void modifyUser(Users user) throws SQLException {
        // Guardamos el puntero de conexion con la base de datos.
        final Connection mainSql = ServerApp.getConnection();

        // Preparamos la sentencia sql.
        try (PreparedStatement statementSql = mainSql.prepareStatement(
                "UPDATE Users SET firstname = ?, lastname = ?, username = ?, email = ?, password = ?, isAdmin = ? WHILE id = ?;")) {

            // Asociamos los valores respecto a la sentencia sql.
            statementSql.setString(1, user.getFirstName());
            statementSql.setString(2, user.getLastName());
            statementSql.setString(3, user.getUsername());
            statementSql.setString(4, user.getEmail());
            statementSql.setString(5, user.getPassword());
            statementSql.setBoolean(6, user.isAdmin());
            statementSql.setInt(7, user.getId());

            // Ejecutamos la sentencia sql.
            statementSql.execute();
        }
    }

    /**
     * Método para recuperar el controlador de la clase UsersMgt.
     * 
     * @return El controlador de la clase UsersMgt.
     */
    public static UsersMgt getController() {
        if (controller == null)
            controller = new UsersMgt();

        return controller;
    }
}