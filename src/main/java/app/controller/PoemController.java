package app.controller;

import app.daos.PoemDAO;
import app.dtos.PoemNoIdDTO;
import app.dtos.PoemsDTO;
import app.entities.Poem;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class PoemController {
    private final List<PoemsDTO> poems = new ArrayList<>();
    PoemDAO poemDAO;
    PoemNoIdDTO poemNoIdDTO;

    public PoemController(EntityManagerFactory emf) {
        poemDAO = new PoemDAO(emf);
    }

    public void populateDatabase() {
        List<PoemNoIdDTO> poemsToCreate = new ArrayList<>(List.of(
                new PoemNoIdDTO("The Old Pond", "Old pond A frog jumps into the water Sound of the splash", "Matsuo Basho"),
                new PoemNoIdDTO("Spring Rain", "A gentle spring rain Washing green leaves on the branch Earth smells sweet and fresh", "Traditional"),
                new PoemNoIdDTO("Summer Breeze", "Cool wind in the trees Leaves dance in the golden light Day begins to fade", "Traditional"),
                new PoemNoIdDTO("Autumn Moon", "Bright moon in the skySilver light upon the ground Night is calm and still", "Traditional"),
                new PoemNoIdDTO("Winter Snow", "White snow falls on pine Silent world in ice and cold Rest till winter ends", "Traditional")
        ));

        for (PoemNoIdDTO dto : poemsToCreate) {
            Poem poem = Poem.builder()
                    .title(dto.title())
                    .content(dto.content())
                    .author(dto.author())
                    .build();
            poemDAO.create(poem);
        }
        log.info("Database populated with {} poems", poemsToCreate.size());
    }

    public void getAllPoems(Context ctx) {
        List<Poem> poems = poemDAO.getAll();
        ctx.json(poems);
        log.info("All poems retrieved");
    }

    public void getPoemById(Context ctx) {
        // int id = Integer.parseInt(ctx.pathParam("id"));
        int id = ctx.pathParamAsClass("id", Integer.class)
                .check(value -> value > 0, "ID must be positive")
                .get();

        Poem poem = poemDAO.getById(id);
        if (poem == null) {
            ctx.status(404);
            ctx.json("Poem not found");
            log.info("Poem with id {} not found", id);
            return;
        }
        ctx.json(poem);
    }

    public void createPoem(Context ctx) {
        PoemNoIdDTO poem = ctx.bodyValidator(PoemNoIdDTO.class)
                .check(p -> p.title() != null && !p.title().isBlank(), "Title is required")
                .check(p -> p.content() != null && !p.content().isBlank(), "Content is required")
                .check(p -> p.author() != null && !p.author().isBlank(), "Author is required")
                .get();

//        if (poem == null) {
//            ctx.status(400);
//            ctx.json("Poem is required");
//            return;
//        }

        Poem newPoem = Poem.builder()
                .title(poem.title())
                .content(poem.content())
                .author(poem.author())
                .build();
        poemDAO.create(newPoem);
        log.info("Created new poem with id {}, and title {}", newPoem.getId(), newPoem.getTitle());
        ctx.status(201);
        ctx.json(newPoem);
    }

    public void updatePoem(Context ctx) {
        // int id = Integer.parseInt(ctx.pathParam("id"));
        int id = ctx.pathParamAsClass("id", Integer.class)
                .check(value -> value > 0, "ID must be positive")
                .get();

        Poem poem = poemDAO.getById(id);
        if (poem == null) {
            ctx.status(404);
            ctx.json("Poem not found");
            return;
        }

        PoemNoIdDTO updatedPoem = ctx.bodyValidator(PoemNoIdDTO.class)
                .check(p -> p.title() != null && !p.title().isBlank(), "Title is required")
                .check(p -> p.content() != null && !p.content().isBlank(), "Content is required")
                .check(p -> p.author() != null && !p.author().isBlank(), "Author is required")
                .get();

        Poem updatePoem = Poem.builder()
                        .id(poem.getId())
                        .title(updatedPoem.title())
                        .content(updatedPoem.content())
                        .author(updatedPoem.author())
                        .build();

        poemDAO.update(updatePoem);
        log.info("Updated poem with id {} from Title: {} -> {} | Content: {} -> {} | Author {} -> {}",
                updatePoem.getId(), poem.getTitle(), updatePoem.getTitle(),
                poem.getContent(), updatePoem.getContent(), poem.getAuthor(), updatePoem.getAuthor());
        ctx.status(200).json(updatePoem);
    }

    public void deletePoem(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class)
                .check(value -> value > 0, "ID must be positive")
                .get();

        if (poemDAO.getById(id) == null) {
            ctx.status(404);
            ctx.json("Poem not found");
            log.info("Poem with id {} not found", id);
            return;
        }
        poemDAO.delete(id);
        ctx.status(200).json("Poem deleted");
        log.info("Poem with id {} deleted", id);
    }
}
