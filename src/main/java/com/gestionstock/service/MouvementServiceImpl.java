package com.gestionstock.service;


import com.gestionstock.model.Mouvement;
import com.gestionstock.model.enums.TypeMouvement;

import com.gestionstock.model.Produit;
import com.gestionstock.util.JPAUtil;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class MouvementServiceImpl implements MouvementService {
    @Override
    public List<Mouvement> findAllMouvements() {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createQuery(
                    "SELECT m FROM Mouvement m " +
                            "ORDER BY m.dateMouvement DESC",
                    Mouvement.class
            ).getResultList();
        }
    }
    @Override
    public Optional<Mouvement> findById(int id) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return Optional.ofNullable(em.find(Mouvement.class, id));
        }
    }
    @Override
    public List<Mouvement> findByProduit(int produitId){
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createQuery(
                    "SELECT m FROM Mouvement m " +
                            "WHERE m.produit.id = :proId " +
                            "ORDER BY m.dateMouvement DESC",
                    Mouvement.class
            ).setParameter("proId", produitId).getResultList();
        }
    }
    @Override
    public void addMouvement(Produit produit, TypeMouvement type, int quantite, String motif) {
        LocalDateTime dateMouvement= LocalDateTime.now();
        EntityManager em = JPAUtil.getEntityManager();
        try {
            // 1. Recharger le produit à jour depuis la base
            Produit produitDB = em.find(Produit.class, produit.getId());

            // 2. Vérifications métier
            if (type == TypeMouvement.SORTIE && (motif == null || motif.isEmpty())) {
                throw new RuntimeException("Le motif est obligatoire pour une sortie");
            }

            if (type == TypeMouvement.SORTIE && produitDB.getQuantiteStock()< quantite) {
                throw new RuntimeException("Stock insuffisant");
            }

            em.getTransaction().begin();

            // 3. Calculer et appliquer la nouvelle quantité
            int nouvelleQuantite ;
            if(type==TypeMouvement.ENTRE){
                nouvelleQuantite =produitDB.getQuantiteStock()+quantite;
                produitDB.setQuantiteStock(nouvelleQuantite);
            }else {
                nouvelleQuantite = produitDB.getQuantiteStock()-quantite;
                produitDB.setQuantiteStock(nouvelleQuantite);

            }

            // 4. Créer et persister le mouvement
            Mouvement mouvement = new Mouvement(produitDB, type, quantite, motif,dateMouvement);
            em.persist(mouvement);

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("erreur lors de l'enregistrement du mouvement");
        } finally {
            em.close();
        }
    }

}
