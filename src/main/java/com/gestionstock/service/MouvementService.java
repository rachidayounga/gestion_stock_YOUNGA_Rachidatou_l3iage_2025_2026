package com.gestionstock.service;
import com.gestionstock.model.Mouvement;
import com.gestionstock.model.MouvementParProduit;
import com.gestionstock.model.Produit;
import com.gestionstock.model.enums.TypeMouvement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
public interface MouvementService {
    List<Mouvement> findAllMouvements();
    Optional<Mouvement> findById(int id);
    List<Mouvement> findByProduit(int produitId);
    void addMouvement(Produit produit, TypeMouvement type, int quantite, String motif);
    long countEntreesDuJour();
    long countSortiesDuJour();
    // debut/fin nullables : si null, comportement par défaut (année en cours / tout l'historique)
    Map<Integer, Long> compterMouvementsParMois(TypeMouvement type, LocalDateTime debut, LocalDateTime fin);
    Optional<MouvementParProduit> findProduitLePlusMouvemente(LocalDateTime debut, LocalDateTime fin);
}