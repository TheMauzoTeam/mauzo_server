package io.Mauzo.Server;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.Mauzo.Server.Controllers.DiscountsCtrl;
import io.Mauzo.Server.Controllers.InformsCtrl;
import io.Mauzo.Server.Controllers.LoginCtrl;
import io.Mauzo.Server.Controllers.ProductsCtrl;
import io.Mauzo.Server.Controllers.SalesCtrl;
import io.Mauzo.Server.Controllers.UsersCtrl;
import io.Mauzo.Server.Controllers.RefundsCtrl;

@Configuration
@SpringBootApplication
@ApplicationPath("/api")
public class ServerApp {
    @Bean
    ResourceConfig resourceConfig() {
        ResourceConfig config = new ResourceConfig();

        // Clases mapeadas en el server/api
        config.register(UsersCtrl.class);
        config.register(LoginCtrl.class);
        
        // Clases deshabilitadas
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