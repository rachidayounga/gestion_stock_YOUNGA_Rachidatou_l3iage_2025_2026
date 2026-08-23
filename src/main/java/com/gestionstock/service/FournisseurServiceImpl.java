package com.gestionstock.service;

import com.gestionstock.model.Fournisseur;
import com.gestionstock.util.JPAUtil;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class FournisseurServiceImpl implements FournisseurService {
    @Override
    public List<Fournisseur> findAllFournisseurs() {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createQuery(
                    "SELECT f FROM Fournisseur f " +
                            "ORDER BY f.nom",
                    Fournisseur.class
            ).getResultList();
        }
    }

    @Override
    public Optional<Fournisseur> findById(int id) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return Optional.ofNullable(em.find(Fournisseur.class, id));
        }
    }

    @Override
    public void addFournisseur(Fournisseur f) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(f);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback(); // pour enlever ce qu'on avait insérer sur la base données
            throw new RuntimeException("Erreur lors de la sauvegarde du fournisseur");
        } finally {
            em.close();
        }
    }

    @Override
    public void updateFournisseur(Fournisseur f) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(f);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback(); // pour enlever ce qu'on avait insérer sur la base données
            throw new RuntimeException("Erreur lors de la modification du fournisseur");
        } finally {
            em.close();
        }

    }
    @Override
    public Long countProduitsByFournisseur(int fournisseurId){
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createQuery(
                    "SELECT COUNT(*) FROM Produit p " +
                            "WHERE p.fournisseur.id = :fourId " ,
                    Long.class
            ).setParameter("fourId", fournisseurId).getSingleResult();
        }

    }
    @Override
    public void deleteFournisseur( int id){
        EntityManager em = JPAUtil.getEntityManager();
        Fournisseur fournisseur = em.find(Fournisseur.class, id);
        Long fourratache= countProduitsByFournisseur(id);
        if(fourratache>0){
            throw new RuntimeException("ce fournisseur ne peux etre supprimer car elle est rattache a "+fourratache+"produit");
        }
        try {
            em.getTransaction().begin();
            //Optional<Produit> produitOptional = findById(id);
            //correction
            // if(produitOptional.isPresent()) em.remove(produitOptional);
            if (fournisseur != null) {
                em.remove(fournisseur);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Erreur lors de la suppression du fournisseur");
        } finally {
            em.close();
        }
    }

}
