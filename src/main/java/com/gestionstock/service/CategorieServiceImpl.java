package com.gestionstock.service;

import com.gestionstock.model.Categorie;
import com.gestionstock.util.JPAUtil;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
public class CategorieServiceImpl implements CategorieService {
    @Override
    public List<Categorie> findAllCategories() {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createQuery(
                    "SELECT c FROM Categorie c " +
                            "ORDER BY c.nom",
                    Categorie.class
            ).getResultList();
        }
    }

    @Override
    public Optional<Categorie> findById(int id) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return Optional.ofNullable(em.find(Categorie.class, id));
        }
    }

    @Override
    public void addCategorie(Categorie c) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(c);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback(); // pour enlever ce qu'on avait insérer sur la base données
            throw new RuntimeException("Erreur lors de la sauvegarde de la categorie");
        } finally {
            em.close();
        }
    }

    @Override
    public void updateCategorie(Categorie c) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(c);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback(); // pour enlever ce qu'on avait insérer sur la base données
            throw new RuntimeException("Erreur lors de la modification de la categories");
        } finally {
            em.close();
        }

    }
    @Override
    public Long countProduitsByCategorie(int categorieId){
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createQuery(
                    "SELECT COUNT(*) FROM Produit p " +
                            "WHERE p.categorie.id = :catId " ,
                            Long.class
            ).setParameter("catId", categorieId).getSingleResult();
        }

    }
    @Override
    public void deleteCategorie( int id){
        EntityManager em = JPAUtil.getEntityManager();
        Categorie categorie = em.find(Categorie.class, id);
       Long catratache= countProduitsByCategorie(id);
       if(catratache>0){
           throw new RuntimeException("ce categorie ne peux etre supprimer car elle est rattache a "+catratache+"produit");
       }
        try {
            em.getTransaction().begin();
            //Optional<Produit> produitOptional = findById(id);
            //correction
            // if(produitOptional.isPresent()) em.remove(produitOptional);
            if (categorie != null) {
                em.remove(categorie);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Erreur lors de la suppression de la categorie");
        } finally {
            em.close();
        }
    }

}
