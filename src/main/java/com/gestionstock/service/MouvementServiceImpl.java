package com.gestionstock.service;


import com.gestionstock.model.Mouvement;
import com.gestionstock.model.enums.TypeMouvement;

import com.gestionstock.model.Produit;
import com.gestionstock.util.JPAUtil;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
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
    public List<Mouvement> findByProduit(int produitId) {
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
        LocalDateTime dateMouvement = LocalDateTime.now();
        EntityManager em = JPAUtil.getEntityManager();
        try {
            // 1. Recharger le produit à jour depuis la base
            Produit produitDB = em.find(Produit.class, produit.getId());

            // 2. Vérifications métier
            if (type == TypeMouvement.SORTIE && (motif == null || motif.isEmpty())) {
                throw new RuntimeException("Le motif est obligatoire pour une sortie");
            }

            if (type == TypeMouvement.SORTIE && produitDB.getQuantiteStock() < quantite) {
                throw new RuntimeException("Stock insuffisant");
            }

            em.getTransaction().begin();

            // 3. Calculer et appliquer la nouvelle quantité
            int nouvelleQuantite;
            if (type == TypeMouvement.ENTRE) {
                nouvelleQuantite = produitDB.getQuantiteStock() + quantite;
                produitDB.setQuantiteStock(nouvelleQuantite);
            } else {
                nouvelleQuantite = produitDB.getQuantiteStock() - quantite;
                produitDB.setQuantiteStock(nouvelleQuantite);

            }

            // 4. Créer et persister le mouvement
            Mouvement mouvement = new Mouvement(produitDB, type, quantite, motif, dateMouvement);
            em.persist(mouvement);

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("erreur lors de l'enregistrement du mouvement:la quantite demande est superieur au stock");
        } finally {
            em.close();
        }
    }

    @Override
    public long countEntreesDuJour() {
        try (EntityManager em = JPAUtil.getEntityManager()) {

            // On calcule les bornes de la journée en cours :
            // debutJour = aujourd'hui à 00:00:00
            // finJour   = demain à 00:00:00 (donc "avant finJour" = "avant demain minuit")
            LocalDateTime debutJour = LocalDate.now().atStartOfDay();
            LocalDateTime finJour = debutJour.plusDays(1);

            return em.createQuery(
                            // On compte les lignes
                            "SELECT COUNT(*) FROM Mouvement m " +
                                    // Filtre 1 : uniquement les mouvements de type ENTRE
                                    "WHERE m.type = :typeMouvement " +
                                    // Filtre 2 : uniquement ceux dont la date tombe dans la journée en cours
                                    "AND m.dateMouvement >= :debut AND m.dateMouvement < :fin",
                            Long.class
                    )
                    // Chaque :xxx de la requête doit avoir son setParameter correspondant,

                    .setParameter("typeMouvement", TypeMouvement.ENTRE)
                    .setParameter("debut", debutJour)
                    .setParameter("fin", finJour)
                    .getSingleResult();
        }
    }

    @Override
    public long countSortiesDuJour() {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            // On calcule les bornes de la journée en cours :
            // debutJour = aujourd'hui à 00:00:00
            // finJour   = demain à 00:00:00 (donc "avant finJour" = "avant demain minuit")
            LocalDateTime debutJour = LocalDate.now().atStartOfDay();
            LocalDateTime finJour = debutJour.plusDays(1);
            return em.createQuery(
                            // On compte les lignes
                            "SELECT COUNT(*) FROM Mouvement m " +
                                    // Filtre 1 : uniquement les mouvements de type ENTRE
                                    "WHERE m.type = :typeMouvement " +
                                    // Filtre 2 : uniquement ceux dont la date tombe dans la journée en cours
                                    "AND m.dateMouvement >= :debut AND m.dateMouvement < :fin",
                            Long.class
                    )
                    // Chaque :xxx de la requête doit avoir son setParameter correspondant,

                    .setParameter("typeMouvement", TypeMouvement.SORTIE)
                    .setParameter("debut", debutJour)
                    .setParameter("fin", finJour)
                    .getSingleResult();
        }
    }
}




