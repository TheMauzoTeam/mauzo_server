package io.Mauzo.Server;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

import io.Mauzo.Server.Managers.UsersMgt;
import io.Mauzo.Server.Managers.SalesMgt;
import io.Mauzo.Server.Managers.RefundsMgt;
import io.Mauzo.Server.Managers.ProductsMgt;
import io.Mauzo.Server.Managers.InformsMgt;
import io.Mauzo.Server.Managers.DiscountsMgt;

import org.springframework.beans.factory.annotation.Value;

public class ServerPools {
    private static ServerPools controller = null;

    //@Value("${mauzo.max_parallel_type_connections}")
    private int maxConnections = 3;

    private final Semaphore uSemaphore = new Semaphore(maxConnections);
    private final Semaphore sSemaphore = new Semaphore(maxConnections);
    private final Semaphore rSemaphore = new Semaphore(maxConnections);
    private final Semaphore pSemaphore = new Semaphore(maxConnections);
    //private final Semaphore iSemaphore = new Semaphore(maxConnections);
    //private final Semaphore dSemaphore = new Semaphore(maxConnections);

    private final List<UsersMgt> usersConnList = new ArrayList<>();
    private final List<SalesMgt> salesConnList = new ArrayList<>();
    private final List<RefundsMgt> refundsConnList = new ArrayList<>();
    private final List<ProductsMgt> productsConnList = new ArrayList<>();
    //private final List<InformsMgt> informsConnList = new ArrayList<>();
    //private final List<DiscountsMgt> discountsConnList = new ArrayList<>();

    public ServerPools() throws SQLException {
        for (int i = 0; i < maxConnections; i++) {
            usersConnList.add(new UsersMgt(ServerApp.setConnection()));
            salesConnList.add(new SalesMgt(ServerApp.setConnection()));
            refundsConnList.add(new RefundsMgt(ServerApp.setConnection()));
            productsConnList.add(new ProductsMgt(ServerApp.setConnection()));
            //informsConnList.add(new InformsMgt(ServerApp.setConnection()));
            //discountsConnList.add(new DiscountsMgt(ServerApp.setConnection()));
        }
    }

    public UsersMgt acquireUsers() throws InterruptedException {
        uSemaphore.acquire();
        return usersConnList.get(getIndexRandom());
    }

    public SalesMgt acquireSales() throws InterruptedException {
        sSemaphore.acquire();
        return salesConnList.get(getIndexRandom());
    }

    public RefundsMgt acquireRefunds() throws InterruptedException {
        rSemaphore.acquire();
        return refundsConnList.get(getIndexRandom());
    }

    public ProductsMgt acquireProducts() throws InterruptedException {
        pSemaphore.acquire();
        return productsConnList.get(getIndexRandom());
    }

    /*
    public InformsMgt acquireInforms() throws InterruptedException {
        iSemaphore.acquire();
        return informsConnList.get(getIndexRandom());
    }

    public DiscountsMgt acquireDiscounts() throws InterruptedException {
        dSemaphore.acquire();
        return discountsConnList.get(getIndexRandom());
    }
    */

    public void releaseUsers(UsersMgt users) {
        usersConnList.add(users);
        uSemaphore.release();
    }

    public void releaseSales(SalesMgt sales) {
        salesConnList.add(sales);
        sSemaphore.release();
    }

    public void releaseRefunds(RefundsMgt refunds) {
        refundsConnList.add(refunds);
        rSemaphore.release();
    }  

    public void releaseProducts(ProductsMgt products) {
        productsConnList.add(products);
        pSemaphore.release();
    }

    /*
    public void releaseInforms(InformsMgt informs) {
        informsConnList.add(informs);
        iSemaphore.release();
    }

    public void releaseDiscounts(DiscountsMgt discounts) {
        discountsConnList.add(discounts);
        dSemaphore.release();
    }
    */

    private int getIndexRandom() {
        return (new Random().nextInt(maxConnections));
    }

    public static ServerPools getController() throws SQLException {
        if (controller == null)
            controller = new ServerPools();

        return controller;
    }
}