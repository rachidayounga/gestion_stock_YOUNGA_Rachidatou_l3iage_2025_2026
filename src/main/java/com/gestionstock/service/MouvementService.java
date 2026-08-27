package com.gestionstock.service;
import com.gestionstock.model.Mouvement;
import com.gestionstock.model.Produit;
import com.gestionstock.model.enums.TypeMouvement;

import java.util.List;
import java.util.Optional;
public interface MouvementService {
    List<Mouvement> findAllMouvements();
    Optional<Mouvement> findById(int id);
    List<Mouvement> findByProduit(int produitId);
    void addMouvement(Produit produit, TypeMouvement type, int quantite, String motif);
    long countEntreesDuJour();
    long countSortiesDuJour();
}
