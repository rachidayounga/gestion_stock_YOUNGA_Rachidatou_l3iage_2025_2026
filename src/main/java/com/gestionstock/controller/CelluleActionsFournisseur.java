package com.gestionstock.controller;
import com.gestionstock.model.Fournisseur;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;

public class CelluleActionsFournisseur extends TableCell<Fournisseur, Void> {
    private final Button boutonModifier = new Button("Modifier");
    private final Button boutonSupprimer = new Button("Supprimer");
    private final HBox conteneur = new HBox(5, boutonModifier, boutonSupprimer);
    private final FournisseurController controleur;

    public CelluleActionsFournisseur(FournisseurController controleur) {
        this.controleur = controleur;
        boutonModifier.setOnAction(event -> {
            Fournisseur fournisseur = getTableView().getItems().get(getIndex());
            controleur.entrerEnModeModification(fournisseur);
        });
        boutonSupprimer.setOnAction(event -> {
            Fournisseur fournisseur = getTableView().getItems().get(getIndex());
            controleur.supprimerFournisseur(fournisseur);
        });
    }

    @Override
    protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(empty ? null : conteneur);
    }
}