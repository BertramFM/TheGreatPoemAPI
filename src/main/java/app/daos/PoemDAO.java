package app.daos;

import app.entities.Poem;
import app.exceptions.ApiException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class PoemDAO implements GenericDAO<Poem, Integer> {
    private final EntityManagerFactory emf;

    public PoemDAO(EntityManagerFactory emf){
        this.emf = emf;
    }

    @Override
    public Poem create(Poem poem) {
        if (poem == null) {
            throw new ApiException(400, "Poem is required");
        }
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            try {
                em.persist(poem);
                em.getTransaction().commit();
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new ApiException(500, "Create Poem failed: " + e.getMessage());
            }
        }
        return poem;
    }

    @Override
    public Poem getById(Integer id) {
        if (id == null) {
            throw new ApiException(400, "Poem id is required");
        }
        try (EntityManager em = emf.createEntityManager()) {
            try {
                return em.find(Poem.class, id);
            } catch (PersistenceException e) {
                throw new ApiException(500, "Get Poem failed: " + e.getMessage());
            }
        }
    }

    @Override
    public List<Poem> getAll() {
        try (EntityManager em = emf.createEntityManager()) {
            try {
                TypedQuery<Poem> query = em.createQuery("SELECT f FROM Poem f", Poem.class);
                return query.getResultList();
            } catch (PersistenceException e) {
                throw new ApiException(500, "Get Poem failed: " + e.getMessage());
            }
        }
    }

    @Override
    public Poem update(Poem poem) {

        if (poem == null || poem.getId() == null) {
            throw new ApiException(400, "Movie id is required");
        }

        Poem updated;
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            try {
                Poem existing = em.find(Poem.class, poem.getId());
                if (existing == null) {
                    throw new ApiException(404, "Movie not found");
                }
                updated = em.merge(poem);
                em.getTransaction().commit();
            } catch (PersistenceException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new ApiException(500, "Update Movie failed: " + e.getMessage());
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            }
        }
        return updated;
    }

    @Override
    public boolean delete(Integer id) {

        if (id == null) {
            throw new ApiException(400, "Movie id is required");
        }

        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            try {
                Poem poemToRemove = em.find(Poem.class, id);
                if (poemToRemove != null) {
                    em.remove(poemToRemove);
                } else {
                    throw new ApiException(404, "Poem not found");
                }
                em.getTransaction().commit();
            } catch (PersistenceException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new ApiException(500, "Delete Movie failed: " + e.getMessage());
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            }
        }
        return true;
    }
}
