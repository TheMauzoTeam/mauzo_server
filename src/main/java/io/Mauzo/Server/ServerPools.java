package io.Mauzo.Server;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import io.Mauzo.Server.Managers.UsersMgt;
import io.Mauzo.Server.Managers.SalesMgt;
import io.Mauzo.Server.Managers.RefundsMgt;
import io.Mauzo.Server.Managers.ProductsMgt;
import io.Mauzo.Server.Managers.InformsMgt;
import io.Mauzo.Server.Managers.DiscountsMgt;

public class ServerPools {
    private static ServerPools controller = null;

    private final int maxConnections = 3;

    private final Semaphore uSemaphore = new Semaphore(maxConnections);
    private final Semaphore sSemaphore = new Semaphore(maxConnections);
    private final Semaphore rSemaphore = new Semaphore(maxConnections);
    private final Semaphore pSemaphore = new Semaphore(maxConnections);
    // private final Semaphore iSemaphore = new Semaphore(maxConnections);
    // private final Semaphore dSemaphore = new Semaphore(maxConnections);

    private final List<UsersMgt> uConnectionList = new ArrayList<>();
    private final List<SalesMgt> sConnectionList = new ArrayList<>();
    private final List<RefundsMgt> rConnectionList = new ArrayList<>();
    private final List<ProductsMgt> pConnectionList = new ArrayList<>();
    // private final List<InformsMgt> iConnectionList = new ArrayList<>();
    // private final List<DiscountsMgt> dConnectionList = new ArrayList<>();

    /**
     * Constructor privado del cual inicializa los objetos que manejarán las
     * conexiones de los gestores a la base de datos proporcionado como URL JBDC.
     * 
     * @throws SQLException Puede lanzar alguna excepción si ocurre algún problema
     *                      con la base de datos.
     */
    private ServerPools() throws SQLException {       
        for (int i = 0; i < maxConnections; i++) {
            uConnectionList.add(new UsersMgt(ServerApp.setConnection()));
            sConnectionList.add(new SalesMgt(ServerApp.setConnection()));
            rConnectionList.add(new RefundsMgt(ServerApp.setConnection()));
            pConnectionList.add(new ProductsMgt(ServerApp.setConnection()));
            // iConnectionList.add(new InformsMgt(ServerApp.setConnection()));
            // dConnectionList.add(new DiscountsMgt(ServerApp.setConnection()));
        }
    }

    public UsersMgt acquireUsers() throws InterruptedException {
        uSemaphore.acquire();

        UsersMgt aux = uConnectionList.get(0);
        uConnectionList.remove(0);

        return aux;
    }

    public SalesMgt acquireSales() throws InterruptedException {
        sSemaphore.acquire();

        SalesMgt aux = sConnectionList.get(0);
        sConnectionList.remove(0);

        return aux;
    }

    public RefundsMgt acquireRefunds() throws InterruptedException {
        rSemaphore.acquire();

        RefundsMgt aux = rConnectionList.get(0);
        rConnectionList.remove(0);

        return aux;
    }

    public ProductsMgt acquireProducts() throws InterruptedException {
        pSemaphore.acquire();

        ProductsMgt aux = pConnectionList.get(0);
        rConnectionList.remove(0);

        return aux;
    }

    // public InformsMgt acquireInforms() throws InterruptedException {
    //     iSemaphore.acquire();

    //     InformsMgt aux = iConnectionList.get(0);
    //     iConnectionList.remove(0);

    //     return aux;
    // }

    // public DiscountsMgt acquireDiscounts() throws InterruptedException {
    //     dSemaphore.acquire();

    //     DiscountsMgt aux = dConnectionList.get(0);
    //     dConnectionList.remove(0);

    //     return aux;
    // }

    public void releaseUsers(UsersMgt users) {
        uConnectionList.add(users);
        uSemaphore.release();
    }

    public void releaseSales(SalesMgt sales) {
        sConnectionList.add(sales);
        sSemaphore.release();
    }

    public void releaseRefunds(RefundsMgt refunds) {
        rConnectionList.add(refunds);
        rSemaphore.release();
    }

    public void releaseProducts(ProductsMgt products) {
        pConnectionList.add(products);
        pSemaphore.release();
    }

    public static ServerPools getController() throws SQLException {
        if (controller == null)
            controller = new ServerPools();

        return controller;
    }
}