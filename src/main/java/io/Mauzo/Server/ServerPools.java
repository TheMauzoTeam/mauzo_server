package io.Mauzo.Server;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.Mauzo.Server.Managers.UsersMgt;
import io.Mauzo.Server.Managers.SalesMgt;
import io.Mauzo.Server.Managers.RefundsMgt;
import io.Mauzo.Server.Managers.ProductsMgt;
import io.Mauzo.Server.Managers.InformsMgt;
import io.Mauzo.Server.Managers.DiscountsMgt;

import org.springframework.beans.factory.annotation.Value;

public class ServerPools {
    private static ServerPools controller = null;

    @Value("${mauzo.max_parallel_type_connections}")
    private int maxConnections = 0;

    private final List<UsersMgt> usersConnList = new ArrayList<>();
    private final List<SalesMgt> salesConnList = new ArrayList<>();
    private final List<RefundsMgt> refundsConnList = new ArrayList<>();
    private final List<ProductsMgt> productsConnList = new ArrayList<>();
    //private final List<InformsMgt> informsConnList = new ArrayList<>();
    private final List<DiscountsMgt> discountsConnList = new ArrayList<>();

    public ServerPools() throws SQLException {
        for (int i = 0; i < maxConnections; i++) {
            usersConnList.add(new UsersMgt(ServerApp.setConnection()));
            salesConnList.add(new SalesMgt(ServerApp.setConnection()));
            refundsConnList.add(new RefundsMgt(ServerApp.setConnection()));
            productsConnList.add(new ProductsMgt(ServerApp.setConnection()));
            //informsConnList.add(new InformsMgt(ServerApp.setConnection()));
            discountsConnList.add(new DiscountsMgt(ServerApp.setConnection()));
        }
    }

    public UsersMgt acquireUsers() {
        return usersConnList.get(getIndexRandom());
    }

    public SalesMgt acquireSales() {
        return salesConnList.get(getIndexRandom());
    }

    public RefundsMgt acquireRefunds() {
        return refundsConnList.get(getIndexRandom());
    }

    public ProductsMgt acquireProducts() {
        return productsConnList.get(getIndexRandom());
    }

    /*
    public InformsMgt acquireInforms() {
        return informsConnList.get(getIndexRandom());
    }
    */

    public DiscountsMgt acquireDiscounts() {
        return discountsConnList.get(getIndexRandom());
    }

    private int getIndexRandom() {
        return (new Random().nextInt() % maxConnections);
    }

    public static ServerPools getController() throws SQLException {
        if (controller == null)
            controller = new ServerPools();

        return controller;
    }
}