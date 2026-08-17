package com.gestionstock.service;
import com.gestionstock.model.Produit;
import com.gestionstock.model.Utilisateur;

import java.util.List;
import java.util.Optional;
public interface UtilisateurService {
    Optional<Utilisateur> verifierIdentifiants(String email, String motDePasseEnClair);
    List<Utilisateur> findAllUtilisateurs();
    void addUtilisateur(Utilisateur u);
    void activerDesactiver(Long id, boolean actif);
}
