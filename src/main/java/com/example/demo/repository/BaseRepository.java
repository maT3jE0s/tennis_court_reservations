package com.example.demo.repository;

import java.util.List;

import com.example.demo.entity.BaseEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public abstract class BaseRepository<T extends BaseEntity> {
    @PersistenceContext
    protected EntityManager em;

    private final Class<T> entityClass;

    protected BaseRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public T save(T entity) {
        if (entity.getId() == null) {
            em.persist(entity);
            return entity;
        }
        return em.merge(entity);
    }

    public List<T> findAll() {
        return em.createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e WHERE e.deleted = false",
            entityClass)
            .getResultList();
    }

    public T findById(Long id) {
        T entity = em.find(entityClass, id);
        return entity != null && entity.isDeleted() ? null : entity;
    }

    public void delete(Long id) {
        T entity = findById(id);
        
        if(entity != null) {
            entity.setDeleted(true);
            em.merge(entity);
        }
    }
}
