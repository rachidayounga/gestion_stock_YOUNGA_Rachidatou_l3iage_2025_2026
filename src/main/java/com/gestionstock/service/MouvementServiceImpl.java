package com.gestionstock.service;


import com.gestionstock.model.Mouvement;
import com.gestionstock.model.Produit;

import com.gestionstock.model.enums.TypeMouvement;
import com.gestionstock.model.MouvementParProduit;
import java.util.HashMap;
import com.gestionstock.util.JPAUtil;
import jakarta.persistence.EntityManager;
import java.util.LinkedHashMap;
import java.util.Map;
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
                            "SELECT COUNT(*) FROM Mouvement m " +
                                    "WHERE m.type = :typeMouvement " +
                                    "AND m.dateMouvement >= :debut AND m.dateMouvement < :fin",
                            Long.class
                    )
                    .setParameter("typeMouvement", TypeMouvement.ENTRE)
                    .setParameter("debut", debutJour)
                    .setParameter("fin", finJour)
                    .getSingleResult();
        }
    }

    @Override
    public long countSortiesDuJour() {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            LocalDateTime debutJour = LocalDate.now().atStartOfDay();
            LocalDateTime finJour = debutJour.plusDays(1);
            return em.createQuery(
                            "SELECT COUNT(*) FROM Mouvement m " +
                                    "WHERE m.type = :typeMouvement " +
                                    "AND m.dateMouvement >= :debut AND m.dateMouvement < :fin",
                            Long.class
                    )
                    .setParameter("typeMouvement", TypeMouvement.SORTIE)
                    .setParameter("debut", debutJour)
                    .setParameter("fin", finJour)
                    .getSingleResult();
        }
    }

    @Override
    public Map<Integer, Long> compterMouvementsParMois(TypeMouvement type, LocalDateTime debut, LocalDateTime fin) {
        //LinkedHashMap :permet de trier par ordre javier,fevrier...
        Map<Integer, Long> resultat = new LinkedHashMap<>();

        // Pas de période choisie -> comportement par défaut : les 12 mois de l'année en cours
        LocalDate curseur;
        LocalDate dernierMois;
        if (debut == null || fin == null) {
            int annee = LocalDate.now().getYear();
            curseur = LocalDate.of(annee, 1, 1);
            dernierMois = LocalDate.of(annee, 12, 1);
        } else {
            curseur = debut.toLocalDate().withDayOfMonth(1);
            dernierMois = fin.toLocalDate().withDayOfMonth(1);
        }

        try (EntityManager em = JPAUtil.getEntityManager()) {
            while (!curseur.isAfter(dernierMois)) {
                // Premier jour du mois à minuit
                LocalDateTime debutMois = curseur.atStartOfDay();
                // Premier jour du mois suivant à minuit (borne exclusive)
                LocalDateTime finMois = debutMois.plusMonths(1);

                // Si une période est choisie, on ne compte que l'intersection [mois] ∩ [période]
                LocalDateTime borneDebut = (debut != null && debut.isAfter(debutMois)) ? debut : debutMois;
                LocalDateTime borneFin = (fin != null && fin.isBefore(finMois)) ? fin : finMois;

                long count = em.createQuery(
                                "SELECT COUNT(*) FROM Mouvement m " +
                                        "WHERE m.type = :typeMouvement " +
                                        "AND m.dateMouvement >= :debut AND m.dateMouvement < :fin",
                                Long.class
                        )
                        .setParameter("typeMouvement", type)
                        .setParameter("debut", borneDebut)
                        .setParameter("fin", borneFin)
                        .getSingleResult();

                // dépasse un an, deux mois de même numéro s'écraseraient : cas non géré ici,
                // le filtre est prévu pour une période à l'intérieur d'une même année.
                resultat.put(curseur.getMonthValue(), count);
                curseur = curseur.plusMonths(1);
            }
        }

        return resultat;
    }

    @Override
    public Optional<MouvementParProduit> findProduitLePlusMouvemente(LocalDateTime debut, LocalDateTime fin) {
        List<Mouvement> mouvements;

        if (debut == null || fin == null) {
            mouvements = findAllMouvements();
        } else {
            try (EntityManager em = JPAUtil.getEntityManager()) {
                mouvements = em.createQuery(
                        "SELECT m FROM Mouvement m " +
                                "WHERE m.dateMouvement >= :debut AND m.dateMouvement < :fin " +
                                "ORDER BY m.dateMouvement DESC",
                        Mouvement.class
                ).setParameter("debut", debut).setParameter("fin", fin).getResultList();
            }
        }

        return trouverProduitLePlusMouvemente(mouvements);
    }

    // Regroupe les quantités mouvementées par produit et retourne celui qui a le plus fort total.
    // Extrait en méthode privée pour être réutilisé, que la liste soit filtrée par période ou non.
    private Optional<MouvementParProduit> trouverProduitLePlusMouvemente(List<Mouvement> mouvements) {
        Map<Produit, Long> totauxParProduit = new HashMap<>();
        for (Mouvement m : mouvements) {
            Produit produit = m.getProduit();
            long quantite = m.getQuantite();

            long totalActuel = totauxParProduit.getOrDefault(produit, 0L);
            totauxParProduit.put(produit, totalActuel + quantite);
        }

        if (totauxParProduit.isEmpty()) {
            return Optional.empty();
        }

        Produit produitMax = null;
        long quantiteMax = -1L;

        for (Map.Entry<Produit, Long> entry : totauxParProduit.entrySet()) {
            if (entry.getValue() > quantiteMax) {
                quantiteMax = entry.getValue();
                produitMax = entry.getKey();
            }
        }

        return Optional.of(new MouvementParProduit(produitMax, quantiteMax));
    }
}