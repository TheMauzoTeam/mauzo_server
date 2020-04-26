package io.Mauzo.Server.Managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import io.Mauzo.Server.ServerApp;
import io.Mauzo.Server.ServerUtils;
import io.Mauzo.Server.Templates.User;

public class UsersMgt implements ManagersIntf<User> {
    private static UsersMgt controller = null;

    /**
     * Método para añadir usuarios a la base de datos.
     * 
     * @param user El usuario encapsulado en un objeto.
     * @throws SQLException Excepcion en la consulta SQL.
     */
    @Override
    public void add(User user) throws SQLException {
        // Guardamos el puntero de conexion con la base de datos.
        final Connection conn = ServerApp.getConnection();

        // Preparamos la consulta sql.
        // Las prepared statement no son de usar y tirar
        try (PreparedStatement st = conn.prepareStatement("INSERT INTO User (firstname, lastname, username, email, password, isAdmin, userPic) VALUES (?, ?, ?, ?, ?, ?, ?);")) {
           
            // Asociamos los valores respecto a la sentencia sql.
            st.setString(1, user.getFirstName());
            st.setString(2, user.getLastName());
            st.setString(3, user.getUsername());
            st.setString(4, user.getEmail());
            st.setString(5, user.getPassword());
            st.setBoolean(6, user.isAdmin());
            st.setBytes(7, ServerUtils.imageToByteArray(user.getUserPic(), "png"));

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
     * @throws ManagerErrorException Excepcion dada al no encontrar el usuario solicitado.
     */
    @Override
    public User get(int id) throws SQLException, ManagerErrorException {
        // Preparamos una instancia del objeto a devolver
        User user = null;

        // Guardamos el puntero de conexion con la base de datos.
        final Connection conn = ServerApp.getConnection();

        // Preparamos la consulta sql.
        try (PreparedStatement st = conn.prepareStatement("SELECT * FROM User WHERE id = ?;")) {
            // Asociamos los valores respecto a la sentencia sql.
            st.setInt(1, id);

            // Ejecutamos la sentencia sql y recuperamos lo que nos ha retornado.
            try (ResultSet rs = st.executeQuery()) {
                if (!(rs.isLast()))
                    while (rs.next()) {
                        user = new User();

                        user.setId(rs.getInt("id"));
                        user.setAdmin(rs.getBoolean("isAdmin"));
                        user.setEmail(rs.getString("email"));
                        user.setFirstName(rs.getString("firstname"));
                        user.setLastName(rs.getString("lastname"));
                        user.setPassword(rs.getString("password"));
                        user.setUsername(rs.getString("username"));
                        user.setUserPic(ServerUtils.imageFromByteArray(rs.getBytes("userPic")));
                    }
                else 
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

        // Guardamos el puntero de conexion con la base de datos.
        final Connection conn = ServerApp.getConnection();

        // Preparamos la consulta sql.
        try (PreparedStatement st = conn.prepareStatement("SELECT * FROM Users WHERE username = ?;")) {
            // Asociamos los valores respecto a la sentencia sql.
            st.setString(1, username);

            // Ejecutamos la sentencia sql y recuperamos lo que nos ha retornado.
            try (ResultSet rs = st.executeQuery()) {
                if (!(rs.isLast()))
                    while (rs.next()) {
                        user = new User();

                        user.setId(rs.getInt("id"));
                        user.setAdmin(rs.getBoolean("isAdmin"));
                        user.setEmail(rs.getString("email"));
                        user.setFirstName(rs.getString("firstname"));
                        user.setLastName(rs.getString("lastname"));
                        user.setPassword(rs.getString("password"));
                        user.setUsername(rs.getString("username"));
                        user.setUserPic(ServerUtils.imageFromByteArray(rs.getBytes("userPic")));
                    }
                else
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

        // Guardamos el puntero de conexion con la base de datos.
        final Connection conn = ServerApp.getConnection();

        // Lanzamos la consulta SQL y generamos la lista de usuarios.
        try(PreparedStatement st = conn.prepareStatement("SELECT * FROM Users")) {
            try(ResultSet rs = st.executeQuery()) {
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
        // Guardamos el puntero de conexion con la base de datos.
        final Connection conn = ServerApp.getConnection();

        // Preparamos la sentencia sql.
        try (PreparedStatement st = conn.prepareStatement("UPDATE Users SET firstname = ?, lastname = ?, username = ?, email = ?, password = ?, isAdmin = ?, userPic = ? WHILE id = ?;")) {
            // Asociamos los valores respecto a la sentencia sql.
            st.setString(1, user.getFirstName());
            st.setString(2, user.getLastName());
            st.setString(3, user.getUsername());
            st.setString(4, user.getEmail());
            st.setString(5, user.getPassword());
            st.setBoolean(6, user.isAdmin());
            st.setBytes(7, ServerUtils.imageToByteArray(user.getUserPic(), "png"));
            st.setInt(8, user.getId());

            // Ejecutamos la sentencia sql.
            if(st.execute() == false) 
                throw new ManagerErrorException("No se ha encontrado el usuario durante la actualización del mismo.");
        }
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
        // Guardamos el puntero de conexion con la base de datos.
        final Connection conn = ServerApp.getConnection();

        // Preparamos la sentencia sql.
        try (PreparedStatement st = conn.prepareStatement("DELETE FROM Users WHERE id = ?;")) {
            st.setInt(1, user.getId());

            // Ejecutamos la sentencia sql.
            if(st.execute() == false) 
                throw new ManagerErrorException("No se ha encontrado el usuario durante la eliminación del mismo.");
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