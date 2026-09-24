import app.config.HibernateConfig;
import app.controller.PoemController;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.javalin.http.InternalServerErrorResponse;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final Logger debugLogger = LoggerFactory.getLogger("app");


    public static void main(String[] args) {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        Javalin app = Javalin.create().start(7070);
        PoemController c = new PoemController(emf);
        c.populateDatabase();

        log.info("Server started on port 7070");


        app.get("/", ctx -> {
            log.info("Handling request to /");
            ctx.result("Hello, Javalin with Logging!");
        });

        app.get("/api/v1/poem", c::getAllPoems);
        app.get("/api/v1/poem/{id}", c::getPoemById);

        app.post("/api/v1/poem", c::createPoem);

        app.put("/api/v1/poem/{id}", c::updatePoem);

        app.delete("/api/v1/poem/{id}", c::deletePoem);

        app.get("/error", ctx -> {
            log.error("An error endpoint was accessed");
            throw new RuntimeException("This is an intentional error for logging demonstration.");
        });

        // Log the server start
        log.info("Javalin application started on http://localhost:7070");
        debugLogger.debug("Debug log message from Main class during startup");

        // Exception handling example
        app.exception(Exception.class, (e, ctx) -> {
            log.error("An exception occurred: {}", e.getMessage(), e);
            ctx.status(500).result("Internal Server Error");
        });

        app.error(HttpStatus.INTERNAL_SERVER_ERROR, ctx -> {
            log.error("Bummer: {}", ctx.status());
            Map<String, String> msg = Map.of("error", "Internal Server Error, dude!", "status", String.valueOf(ctx.status()));
            throw new InternalServerErrorResponse("Off limits!", msg);
        });
    }
}
