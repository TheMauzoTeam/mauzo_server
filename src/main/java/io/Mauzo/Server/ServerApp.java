package io.Mauzo.Server;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import io.Mauzo.Server.Controllers.DiscountsCtrl;
import io.Mauzo.Server.Controllers.InformsCtrl;
import io.Mauzo.Server.Controllers.ProductsCtrl;
import io.Mauzo.Server.Controllers.SalesCtrl;
import io.Mauzo.Server.Controllers.UsersCtrl;
import io.Mauzo.Server.Controllers.RefundsCtrl;

@SpringBootApplication
@ApplicationPath("/api")
public class ServerApp {
    @Bean
    ResourceConfig resourceConfig() {
        ResourceConfig config = new ResourceConfig();

        config.register(UsersCtrl.class);
        //config.register(SalesCtrl.class);
        //config.register(RefundsCtrl.class);
        //config.register(ProductsCtrl.class);
        //config.register(InformsCtrl.class);
        //config.register(DiscountsCtrl.class);

        return config;
    }

    public static void main(String[] args) {
        SpringApplication.run(ServerApp.class, args);
    }
}