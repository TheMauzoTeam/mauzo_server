package io.Mauzo.Server;

// Paquetes relativos a la inicialización del servidor
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.logging.Logger;

// Clases controladoras de las interfaces web expuestas.
import io.Mauzo.Server.Controllers.DiscountsCtrl;
import io.Mauzo.Server.Controllers.InformsCtrl;
import io.Mauzo.Server.Controllers.LoginCtrl;
import io.Mauzo.Server.Controllers.ProductsCtrl;
import io.Mauzo.Server.Controllers.SalesCtrl;
import io.Mauzo.Server.Controllers.UsersCtrl;
import io.Mauzo.Server.Controllers.RefundsCtrl;

@Configuration
@SpringBootApplication
// Esto que voy a decir, ..., es un poco pro, ..., pero en fin, ... Aquí tienes una limitación muy severa,
// y es que sólo permites una conxión al mismo tiempo. Para permitir muchas, necesitas algunas cosas que me
// habréis de preguntar en clase
public class ServerApp {
    private static Logger loggerSystem = Logger.getLogger("MauzoServer");

    /**
     * Método principal que inicializa el servidor Spring Boot,
     * el cual luego invocará a los métodos y clases que se han
     * ido desarrollando a lo largo y ancho del proyecto.
     * 
     * @param args  Los argumentos que recibe el servidor.
     */
    public static void main(String[] args) {
        SpringApplication.run(ServerApp.class, args);
    }

    /**
     * Método dedicado a la configuración y mapeo de las vistas usadas en el
     * servidor.
     * 
     * @return Configuración del servidor.
     */
    @Bean
    public ResourceConfig resourceConfig() {
        ResourceConfig config = new ResourceConfig();

        // Clases mapeadas en el server/api
        config.register(UsersCtrl.class);
        config.register(LoginCtrl.class);
        config.register(SalesCtrl.class);

        // Clases deshabilitadas
        // config.register(RefundsCtrl.class);
        // config.register(ProductsCtrl.class);
        // config.register(InformsCtrl.class);
        // config.register(DiscountsCtrl.class);

        return config;
    }

    /**
     * Getter para devolver el objeto que se está utilizando para registrar los
     * eventos del servidor por la consola del sistema.
     * 
     * @return Devuelve el objeto usado como Logger.
     */
    public static Logger getLoggerSystem() {
        return loggerSystem;
    }
}