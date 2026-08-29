package com.gestionstock.controller;

import com.gestionstock.model.Utilisateur;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;

/**
 * Cellule personnalisée pour la colonne "Actions" du tableau des utilisateurs.
 * Affiche un seul bouton permettant d'activer ou de désactiver un compte.
 */
public class CelluleActionsUtilisateur extends TableCell<Utilisateur, Void> {

    // Le bouton dont le texte change selon le statut de l'utilisateur (Activer/Désactiver)
    private final Button boutonBasculer = new Button();

    // Conteneur qui accueille le bouton (permet d'ajouter facilement d'autres boutons plus tard)
    private final HBox conteneur = new HBox(5, boutonBasculer);

    // Référence vers le contrôleur pour pouvoir appeler basculerStatut()
    private final GestionComptesController controleur;

    public CelluleActionsUtilisateur(GestionComptesController controleur) {
        this.controleur = controleur;

        // Action déclenchée au clic : on récupère l'utilisateur correspondant
        // à la ligne courante, puis on demande au contrôleur de changer son statut
        boutonBasculer.setOnAction(event -> {
            Utilisateur utilisateur = getTableView().getItems().get(getIndex());
            controleur.basculerStatut(utilisateur);
        });
    }

    @Override
    protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);

        // Cas où la ligne est vide (pas de données) ou l'index ne correspond plus
        // à un utilisateur valide
        if (empty || getTableView().getItems().get(getIndex()) == null) {
            setGraphic(null);
        } else {
            // On récupère l'utilisateur de la ligne pour adapter le texte du bouton
            Utilisateur utilisateur = getTableView().getItems().get(getIndex());
            boutonBasculer.setText(utilisateur.isActif() ? "Désactiver" : "Activer");
            setGraphic(conteneur);
        }
    }
}