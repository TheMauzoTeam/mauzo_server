package io.Mauzo.Server.Managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import io.Mauzo.Server.ServerApp;
import io.Mauzo.Server.Templates.Users;

public class UsersMgt {
    private static UsersMgt controller = null;

    /**
     * Excepcion generica para cuando existe algún problema relativo a los usuarios.
     */
    public static class UserErrorException extends Exception {
        private static final long serialVersionUID = 1L;

        public UserErrorException(String msg) {
            super(msg);
        }
    }

    /**
     * Método para añadir usuarios a la base de datos.
     * 
     * @param user El usuario encapsulado en un objeto.
     * @throws SQLException Excepcion en la consulta SQL.
     */
    public void addUser(Users user) throws SQLException {
        // Guardamos el puntero de conexion con la base de datos.
        final Connection conn = ServerApp.getConnection();

        // Preparamos la consulta sql.
        try (PreparedStatement st = conn.prepareStatement("INSERT INTO Users (firstname, lastname, username, email, password, isAdmin) VALUES (?, ?, ?, ?, ?, ?);")) {
            // Asociamos los valores respecto a la sentencia sql.
            st.setString(1, user.getFirstName());
            st.setString(2, user.getLastName());
            st.setString(3, user.getUsername());
            st.setString(4, user.getEmail());
            st.setString(5, user.getPassword());
            st.setBoolean(6, user.isAdmin());

            // Ejecutamos la sentencia sql.
            st.execute();
        }
    }

    /**
     * Método para obtener en forma de objeto el usuario, a partir de un id de
     * usuario, el usuario encapsulado.
     * 
     * @param id El id de usuario.
     * @return El usuario encapsulado en forma de objeto.
     * @throws SQLException Excepcion en la consulta SQL.
     * @throws UserErrorException Excepcion dada al no encontrar el usuario solicitado.
     */
    public Users getUser(int id) throws SQLException, UserErrorException {
        Users user = null;

        // Guardamos el puntero de conexion con la base de datos.
        final Connection conn = ServerApp.getConnection();

        // Preparamos la consulta sql.
        try (PreparedStatement st = conn.prepareStatement("SELECT * FROM Users WHERE id = ?;")) {
            // Asociamos los valores respecto a la sentencia sql.
            st.setInt(1, id);

            // Ejecutamos la sentencia sql y recuperamos lo que nos ha retornado.
            try (ResultSet rs = st.executeQuery()) {
                if (!(rs.isLast())) {
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
                } else {
                    throw new UserErrorException("No se ha encontrado el usuario");
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
     * @throws UserErrorException Excepcion dada al no encontrar el usuario solicitado.
     */
    public Users getUser(String username) throws SQLException, UserErrorException {
        Users user = null;

        // Guardamos el puntero de conexion con la base de datos.
        final Connection conn = ServerApp.getConnection();

        // Preparamos la consulta sql.
        try (PreparedStatement st = conn.prepareStatement("SELECT * FROM Users WHERE username = ?;")) {
            // Asociamos los valores respecto a la sentencia sql.
            st.setString(1, username);

            // Ejecutamos la sentencia sql y recuperamos lo que nos ha retornado.
            try (ResultSet rs = st.executeQuery()) {
                if (!(rs.isLast())) {
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
                } else {
                    throw new UserErrorException("No se ha encontrado el usuario");
                }
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
    public List<Users> getUsersList() throws SQLException {
        // Guardamos el puntero de conexion con la base de datos.
        final Connection conn = ServerApp.getConnection();
        List<Users> usersList = null;

        // Lanzamos la consulta SQL y generamos la lista de usuarios.
        try(PreparedStatement st = conn.prepareStatement("SELECT * FROM Users")) {
            try(ResultSet rs = st.executeQuery()) {
                usersList = new ArrayList<>();

                while (rs.next()) {
                    Users user = new Users();

                    user.setId(rs.getInt("id"));
                    user.setAdmin(rs.getBoolean("isAdmin"));
                    user.setEmail(rs.getString("email"));
                    user.setFirstName(rs.getString("firstname"));
                    user.setLastName(rs.getString("lastname"));
                    user.setPassword(rs.getString("password"));
                    user.setUsername(rs.getString("username"));

                    usersList.add(user);
                }
            }
        }

        return usersList;
    }

    /**
     * Método para eliminar el usuario en la base de datos.
     * 
     * @param user El usuario encapsulado en un objeto.
     * @throws SQLException Excepcion en la consulta SQL.
     * @throws UserErrorException Excepcion dada al no encontrar el usuario solicitado.
     */
    public void removeUser(Users user) throws SQLException, UserErrorException {
        // Guardamos el puntero de conexion con la base de datos.
        final Connection conn = ServerApp.getConnection();

        // Preparamos la sentencia sql.
        try (PreparedStatement st = conn.prepareStatement("DELETE FROM Users WHERE id = ?;")) {
            st.setInt(1, user.getId());

            // Ejecutamos la sentencia sql.
            if(st.execute() == false) 
                throw new UserErrorException("No se ha encontrado el usuario durante la eliminación del mismo.");
        }
    }

    /**
     * Método para actualizar el usuario en la base de datos.
     * 
     * @param user El usuario encapsulado en un objeto.
     * @throws SQLException Excepcion en la consulta SQL.
     * @throws UserErrorException Excepcion dada al no encontrar el usuario solicitado.
     */
    public void modifyUser(Users user) throws SQLException, UserErrorException {
        // Guardamos el puntero de conexion con la base de datos.
        final Connection conn = ServerApp.getConnection();

        // Preparamos la sentencia sql.
        try (PreparedStatement st = conn.prepareStatement("UPDATE Users SET firstname = ?, lastname = ?, username = ?, email = ?, password = ?, isAdmin = ? WHILE id = ?;")) {

            // Asociamos los valores respecto a la sentencia sql.
            st.setString(1, user.getFirstName());
            st.setString(2, user.getLastName());
            st.setString(3, user.getUsername());
            st.setString(4, user.getEmail());
            st.setString(5, user.getPassword());
            st.setBoolean(6, user.isAdmin());
            st.setInt(7, user.getId());

            // Ejecutamos la sentencia sql.
            if(st.execute() == false) 
                throw new UserErrorException("No se ha encontrado el usuario durante la actualización del mismo.");
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