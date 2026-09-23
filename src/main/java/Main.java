import app.config.HibernateConfig;
import app.controller.PoemController;
import app.dtos.PoemsDTO;
import io.javalin.Javalin;
import jakarta.persistence.EntityManagerFactory;

public class Main {

    public static void main(String[] args) {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        Javalin app = Javalin.create().start(7070);
        PoemController c = new PoemController(emf);
        c.populateDatabase();

        app.get("/api/v1/poem", c::getAllPoems);
        app.get("/api/v1/poem/{id}", c::getPoemById);

        app.post("/api/v1/poem", c::createPoem);

        app.put("/api/v1/poem/{id}", c::updatePoem);

        app.delete("/api/v1/poem/{id}", c::deletePoem);



    }
}
