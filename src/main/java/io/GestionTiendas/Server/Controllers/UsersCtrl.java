package io.GestionTiendas.Server.Controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import io.GestionTiendas.Server.ServerApp;
import io.GestionTiendas.Server.Models.Users;

public class UsersCtrl {
    private static List<Users> usersList = null;

    public static void addUser(Users user) {
        usersList.add(user);
    }
    
    public static Users getUser(String username) {
        Users user = null;

        for (int i = 0; i < usersList.size(); i++) {
            user = usersList.get(i);

            if (user.getUsername().equals(username)) {
                break;
            } else {
                user = null;
            }
        }

        return user;
    }

    public static void removeUser(String username) {
        Users user = null;

        for (int i = 0; i < usersList.size(); i++) {
            user = usersList.get(i);

            if (user.getUsername().equals(username)) {
                usersList.remove(i);
            }
        }
    }

    // TODO: Repasar esta funcion para obtener el listado
    public static void pullUsers() throws SQLException {
        if(usersList == null) {
            // Conectamos con la base de datos en caso de no haberlo hecho.
            if(ServerApp.getConnection() == null)
                ServerApp.setConnection();

            // Guardamos el puntero de conexion con la base de datos.
            final Connection mainSql = ServerApp.getConnection();

            // Preparamos la consulta de verificación.
            final PreparedStatement statementSql = mainSql.prepareStatement("SELECT * FROM Users");

            // Consultamos a la base de datos y volcamos la información a la lista.
            try(ResultSet rs = statementSql.executeQuery()) {
                usersList = new ArrayList<>();

                while(rs.next()) {
                    Users userAux = new Users();

                    userAux.setId(rs.getInt("id"));
                    userAux.setEmail(rs.getString("email"));
                    userAux.setFirstName(rs.getString("firstname"));
                    userAux.setLastName(rs.getString("lastname"));
                    userAux.setPassword(rs.getString("password"));

                    usersList.add(userAux);
                }
            }
        }
    }

    // TODO: Preparar funcion para sincronizar los cambios con la base de datos.
    public static void pushUsers() throws SQLException {}
}