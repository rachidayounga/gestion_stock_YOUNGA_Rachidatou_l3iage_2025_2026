package com.gestionstock.controller;
import com.gestionstock.model.Categorie;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;

public class CelluleActionsCategorie extends TableCell<Categorie, Void> {

    private final Button boutonModifier = new Button("Modifier");
    private final Button boutonSupprimer = new Button("Supprimer");
    private final HBox conteneur = new HBox(5, boutonModifier, boutonSupprimer);
    private final CategorieController controleur;

    public CelluleActionsCategorie(CategorieController controleur) {
        this.controleur = controleur;
        boutonModifier.setOnAction(event -> {
            Categorie categorie = getTableView().getItems().get(getIndex());
            controleur.entrerEnModeModification(categorie);
        });
        boutonSupprimer.setOnAction(event -> {
            Categorie categorie = getTableView().getItems().get(getIndex());
            controleur.supprimerCategorie(categorie);
        });
    }

    @Override
    protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(empty ? null : conteneur);
    }
}