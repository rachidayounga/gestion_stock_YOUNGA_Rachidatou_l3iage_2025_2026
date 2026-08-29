package com.gestionstock.controller;

import com.gestionstock.model.Fournisseur;
import com.gestionstock.service.FournisseurService;
import com.gestionstock.service.FournisseurServiceImpl;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.Optional;

public class FournisseurController {

    @FXML
    private TextField champRecherche;
    @FXML
    private TableView<Fournisseur> tableFournisseurs;
    @FXML
    private TableColumn<Fournisseur, String> colonneNom;
    @FXML
    private TableColumn<Fournisseur, String> colonneEmail;
    @FXML
    private TableColumn<Fournisseur, String> colonneTel;
    @FXML
    private TableColumn<Fournisseur, Long> colonneNbProduits;
    @FXML
    private TableColumn<Fournisseur, Void> colonneActions;
    @FXML
    private TextField champNom;
    @FXML
    private TextField champEmail;
    @FXML
    private TextField champTel;
    @FXML
    private Button boutonEnregistrer;

    private ObservableList<Fournisseur> listeFournisseur;
    private final FournisseurService fournisseurService = new FournisseurServiceImpl();

    // État : null = mode ajout, non-null = mode modification
    private Fournisseur fournisseurUpdate = null;
    String telvalide = "^(77|78|75|76|70)\\d{7}$";
    @FXML
    public void initialize() {
        colonneNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colonneEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colonneTel.setCellValueFactory(new PropertyValueFactory<>("tel"));

        colonneNbProduits.setCellValueFactory(cellData -> {
            Fournisseur fournisseur = cellData.getValue();
            long nb = fournisseurService.countProduitsByFournisseur(fournisseur.getId());
            return new SimpleLongProperty(nb).asObject();
        });

        colonneActions.setCellFactory(colonne -> new CelluleActionsFournisseur(this));

        chargerFournisseur();
    }

    private void chargerFournisseur() {
        List<Fournisseur> fournisseurs = fournisseurService.findAllFournisseurs();
        listeFournisseur = FXCollections.observableArrayList(fournisseurs);
        tableFournisseurs.setItems(listeFournisseur);
    }

    @FXML
    private void rechercherFournisseur() {
        String recherche = champRecherche.getText();

        if (recherche == null || recherche.isBlank()) {
            tableFournisseurs.setItems(listeFournisseur);
            return;
        }
        String rechercheMinuscule = recherche.trim().toLowerCase();

        ObservableList<Fournisseur> resultats = listeFournisseur.filtered(fournisseur ->
                (fournisseur.getNom() != null && fournisseur.getNom().toLowerCase().contains(rechercheMinuscule))
        );

        tableFournisseurs.setItems(resultats);
    }

    @FXML
    private void enregistrerFournisseur() {
        // validation réelle
        if (champNom.getText() == null || champNom.getText().isBlank()) {
            Alert alerte = new Alert(Alert.AlertType.WARNING);
            alerte.setTitle("Champ requis");
            alerte.setHeaderText(null);
            alerte.setContentText("Le nom du fournisseur est obligatoire.");
            alerte.showAndWait();
            return;
        }
        if (champEmail.getText() == null || champEmail.getText().isBlank()) {
            Alert alerte = new Alert(Alert.AlertType.WARNING);
            alerte.setTitle("Champ requis");
            alerte.setHeaderText(null);
            alerte.setContentText("L'email du fournisseur est obligatoire.");
            alerte.showAndWait();
            return;
        }
        if (champTel.getText() == null || champTel.getText().isBlank()) {
            Alert alerte = new Alert(Alert.AlertType.WARNING);
            alerte.setTitle("Champ requis");
            alerte.setHeaderText(null);
            alerte.setContentText("Le téléphone du fournisseur est obligatoire.");
            alerte.showAndWait();
            return;
        }
        if (!champTel.getText().trim().matches(telvalide)){
            Alert alerte = new Alert(Alert.AlertType.WARNING);
            alerte.setTitle("Champ invalide");
            alerte.setHeaderText(null);
            alerte.setContentText("le numero de telephone doit avoir 9 chiffre et commene par 77/78/../..");
            alerte.showAndWait();
            return;
        }

        // test AVANT réaffectation, pour savoir dans quel mode on était en entrant
        if (fournisseurUpdate == null) {
            // mode ajout : nouvel objet
            Fournisseur nouvelleFournisseur = new Fournisseur(champNom.getText(), champEmail.getText(), champTel.getText());
            fournisseurService.addFournisseur(nouvelleFournisseur);
        } else {
            // mode modification : on met à jour l'objet EXISTANT (conserve son id)
            fournisseurUpdate.setNom(champNom.getText());
            fournisseurUpdate.setEmail(champEmail.getText());
            fournisseurUpdate.setTel(champTel.getText());
            fournisseurService.updateFournisseur(fournisseurUpdate);
        }
        // rafraîchir
        chargerFournisseur();
        viderFormulaire();
    }

    @FXML
    private void annulerModification() {
        viderFormulaire();
    }

    private void viderFormulaire() {
        champNom.clear();
        champEmail.clear();
        champTel.clear();
        fournisseurUpdate = null;
        boutonEnregistrer.setText("Enregistrer");
    }

    // --- Méthodes appelées par les boutons de colonneActions ---
    // Placées ici (niveau du contrôleur, PAS dans la classe anonyme TableCell) :
    // elles portent la logique métier et l'état, pas juste l'affichage d'une cellule.

    void entrerEnModeModification(Fournisseur fournisseur) {
        fournisseurUpdate = fournisseur;
        champNom.setText(fournisseur.getNom());
        champEmail.setText(fournisseur.getEmail());
        champTel.setText(fournisseur.getTel());
        boutonEnregistrer.setText("Modifier");
    }

    void supprimerFournisseur(Fournisseur fournisseur) {
        long nbProduits = fournisseurService.countProduitsByFournisseur(fournisseur.getId());
        if (nbProduits > 0) {
            Alert alerteInfo = new Alert(Alert.AlertType.INFORMATION);
            alerteInfo.setTitle("Suppression impossible");
            alerteInfo.setHeaderText(null);
            alerteInfo.setContentText("Ce fournisseur est rattaché à " + nbProduits
                    + " produit(s). Vous ne pouvez pas le supprimer.");
            alerteInfo.showAndWait();
            return;
        }

        Alert alerteConfirmation = new Alert(Alert.AlertType.CONFIRMATION);
        alerteConfirmation.setTitle("Confirmation de suppression");
        alerteConfirmation.setHeaderText(null);
        alerteConfirmation.setContentText("Voulez-vous vraiment supprimer le fournisseur \"" + fournisseur.getNom() + "\" ?");

        Optional<ButtonType> reponse = alerteConfirmation.showAndWait();

        if (reponse.isPresent() && reponse.get() == ButtonType.OK) {
            fournisseurService.deleteFournisseur(fournisseur.getId());
            chargerFournisseur();
        }
    }
}