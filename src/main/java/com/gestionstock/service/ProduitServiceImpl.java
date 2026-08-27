package com.gestionstock.service;

import com.gestionstock.model.Produit;
import com.gestionstock.util.DatabaseConfig;
import com.gestionstock.util.JPAUtil;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProduitServiceImpl implements ProduitService {


    @Override
    public List<Produit> findAllProduits() {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createQuery(
                    "SELECT p FROM Produit p " +
                            "LEFT JOIN FETCH p.categorie " +
                            "LEFT JOIN FETCH p.fournisseur " +
                            "ORDER BY p.nom",
                    Produit.class
            ).getResultList();
        }
    }

    @Override
    public Optional<Produit> findById(int id) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return Optional.ofNullable(em.find(Produit.class, id));
        }
    }

    @Override
    public List<Produit> findByCategorie(int categorieId) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createQuery(
                    "SELECT p FROM Produit p " +
                            "WHERE p.categorie.id = :catId " +
                            "ORDER BY p.nom",
                    Produit.class
            ).setParameter("catId", categorieId).getResultList();
        }
    }

    @Override
    public void addProduit(Produit p) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(p);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback(); // pour enlever ce qu'on avait insérer sur la base données
            throw new RuntimeException("Erreur lors de la sauvegarde du produit");
        } finally {
            em.close();
        }
    }

    @Override
    public void updateProduit(Produit p) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(p);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback(); // pour enlever ce qu'on avait insérer sur la base données
            throw new RuntimeException("Erreur lors de la modification du produit");
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteProduit(int id) {
        EntityManager em = JPAUtil.getEntityManager();
        Produit produit = em.find(Produit.class, id);
        try {
            em.getTransaction().begin();
            //Optional<Produit> produitOptional = findById(id);
            //correction
            // if(produitOptional.isPresent()) em.remove(produitOptional);
            if (produit != null) {
                em.remove(produit);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Erreur lors de la suppression du produit");
        } finally {
            em.close();
        }
    }

    @Override
    public List<Produit> findByStockBas() {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createQuery(
                    "SELECT p FROM Produit p " +
                            "WHERE p.quantiteStock <= p.quantiteMin " +
                            "ORDER BY p.quantiteStock",
                    Produit.class
            ).getResultList();
        }
    }

    @Override
    public long countTotalProduits() {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createQuery(
                    "SELECT COUNT(*) FROM Produit p ",
                    Long.class
            ).getSingleResult();
        }
    }

    //    @Override
//    public double calculerValeurTotaleStock(){
//        try (EntityManager em = JPAUtil.getEntityManager()) {
//            return em.createQuery(
//                  " SELECT SUM(p.quantiteStock * p.prix) FROM Produit p",
//                    Double.class
//            ).getSingleResult();
//        }
//    }
    //garder le resultat dans un variable pour eviter exception quand c'est null
    public double calculerValeurTotaleStock() {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            Double resultat = em.createQuery(
                    "SELECT SUM(p.quantiteStock * p.prix) FROM Produit p",
                    Double.class
            ).getSingleResult();
            if (resultat == null) {
                return 0.0;
            }
            return resultat;

        }
    }
    @Override
    public long countStockBas(){
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createQuery(
                    "SELECT COUNT(*) FROM Produit p " +
                            "WHERE p.quantiteStock <= p.quantiteMin",
                    Long.class
            ).getSingleResult();
        }
    }
}

