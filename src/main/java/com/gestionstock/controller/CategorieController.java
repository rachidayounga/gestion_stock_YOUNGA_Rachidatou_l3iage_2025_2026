package com.gestionstock.controller;

import com.gestionstock.model.Categorie;
import com.gestionstock.service.CategorieService;
import com.gestionstock.service.CategorieServiceImpl;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.Optional;

public class CategorieController {

    @FXML
    private TextField champRecherche;
    @FXML
    private TableView<Categorie> tableCategories;
    @FXML
    private TableColumn<Categorie, String> colonneNom;
    @FXML
    private TableColumn<Categorie, String> colonneDescription;
    @FXML
    private TableColumn<Categorie, Long> colonneNbProduits;
    @FXML
    private TableColumn<Categorie, Void> colonneActions;
    @FXML
    private TextField champNom;
    @FXML
    private TextField champDescription;
    @FXML
    private Button boutonEnregistrer;

    private ObservableList<Categorie> listeCategories;
    private final CategorieService categorieService = new CategorieServiceImpl();

    // État : null = mode ajout, non-null = mode modification
    private Categorie categorieUpdate = null;

    @FXML
    public void initialize() {
        colonneNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colonneDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colonneNbProduits.setCellValueFactory(cellData -> {
            Categorie categorie = cellData.getValue();
            long nb = categorieService.countProduitsByCategorie(categorie.getId());
            return new SimpleLongProperty(nb).asObject();
        });

        colonneActions.setCellFactory(colonne -> new CelluleActionsCategorie(this));

        chargerCategories();
    }

    private void chargerCategories() {
        List<Categorie> categories = categorieService.findAllCategories();
        listeCategories = FXCollections.observableArrayList(categories);
        tableCategories.setItems(listeCategories);
    }

    @FXML
    private void rechercherCategories() {
        String recherche = champRecherche.getText();

        if (recherche == null || recherche.isBlank()) {
            tableCategories.setItems(listeCategories);
            return;
        }
        String rechercheMinuscule = recherche.trim().toLowerCase();

        ObservableList<Categorie> resultats = listeCategories.filtered(categorie ->
                (categorie.getNom() != null && categorie.getNom().toLowerCase().contains(rechercheMinuscule))
        );

        tableCategories.setItems(resultats);
    }

    @FXML
    private void enregistrerCategorie() {
        // validation réelle
        if (champNom.getText() == null || champNom.getText().isBlank()) {
            Alert alerte = new Alert(Alert.AlertType.WARNING);
            alerte.setTitle("Champ requis");
            alerte.setHeaderText(null);
            alerte.setContentText("Le nom de la catégorie est obligatoire.");
            alerte.showAndWait();
            return;
        }

        // test AVANT réaffectation, pour savoir dans quel mode on était en entrant
        if (categorieUpdate == null) {
            // mode ajout : nouvel objet
            Categorie nouvelleCategorie = new Categorie(champNom.getText(), champDescription.getText());
            categorieService.addCategorie(nouvelleCategorie);
        } else {
            // mode modification : on met à jour l'objet EXISTANT (conserve son id)
            categorieUpdate.setNom(champNom.getText());
            categorieUpdate.setDescription(champDescription.getText());
            categorieService.updateCategorie(categorieUpdate);
        }

        // rafraîchir + vider
        chargerCategories();
        viderFormulaire();
    }

    @FXML
    private void annulerModification() {
        viderFormulaire();
    }

    private void viderFormulaire() {
        champNom.clear();
        champDescription.clear();
        categorieUpdate = null;
        boutonEnregistrer.setText("Enregistrer");
    }

    // --- Méthodes appelées par les boutons de colonneActions ---
    // Placées ici (niveau du contrôleur, PAS dans la classe anonyme TableCell) :
    // elles portent la logique métier et l'état, pas juste l'affichage d'une cellule.

     void entrerEnModeModification(Categorie categorie) {
        categorieUpdate = categorie;
        champNom.setText(categorie.getNom());
        champDescription.setText(categorie.getDescription());
        boutonEnregistrer.setText("Modifier");
    }

     void supprimerCategorie(Categorie categorie) {
        long nbProduits = categorieService.countProduitsByCategorie(categorie.getId());
        if (nbProduits > 0) {
            Alert alerteInfo = new Alert(Alert.AlertType.INFORMATION);
            alerteInfo.setTitle("Suppression impossible");
            alerteInfo.setHeaderText(null);
            alerteInfo.setContentText("Cette catégorie est rattachée à " + nbProduits
                    + " produit(s). Vous ne pouvez pas la supprimer.");
            alerteInfo.showAndWait();
            return;
        }

        Alert alerteConfirmation = new Alert(Alert.AlertType.CONFIRMATION);
        alerteConfirmation.setTitle("Confirmation de suppression");
        alerteConfirmation.setHeaderText(null);
        alerteConfirmation.setContentText("Voulez-vous vraiment supprimer la catégorie \"" + categorie.getNom() + "\" ?");

        Optional<ButtonType> reponse = alerteConfirmation.showAndWait();

        if (reponse.isPresent() && reponse.get() == ButtonType.OK) {
            categorieService.deleteCategorie(categorie.getId());
            chargerCategories();
        }
    }
}
