package com.gestionstock.controller;

import com.gestionstock.model.Categorie;
import com.gestionstock.model.Fournisseur;
import com.gestionstock.model.Produit;
import com.gestionstock.service.CategorieService;
import com.gestionstock.service.CategorieServiceImpl;
import com.gestionstock.service.FournisseurService;
import com.gestionstock.service.FournisseurServiceImpl;
import com.gestionstock.service.ProduitService;
import com.gestionstock.service.ProduitServiceImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddProduitController {

    @FXML
    private TextField champNom;
    @FXML
    private ComboBox<Categorie> comboCategorie;
    @FXML
    private ComboBox<Fournisseur> comboFournisseur;
    @FXML
    private TextField champPrix;
    @FXML
    private TextField champPrixPromo;
    @FXML
    private TextField champStock;
    @FXML
    private TextField champStockMin;

    private final CategorieService categorieService = new CategorieServiceImpl();
    private final FournisseurService fournisseurService = new FournisseurServiceImpl();
    private final ProduitService produitService = new ProduitServiceImpl();

    @FXML
    public void initialize() {
        comboCategorie.setItems(FXCollections.observableArrayList(categorieService.findAllCategories()));
        comboFournisseur.setItems(FXCollections.observableArrayList(fournisseurService.findAllFournisseurs()));
    }

    @FXML
    private void enregistrer() {
        // Validation des champs obligatoires
        if (champNom.getText() == null || champNom.getText().isBlank()) {
            afficherAlerte("Le nom du produit est obligatoire.");
            return;
        }
        if (comboCategorie.getValue() == null) {
            afficherAlerte("La catégorie est obligatoire.");
            return;
        }
        if (champPrix.getText() == null || champPrix.getText().isBlank()) {
            afficherAlerte("Le prix est obligatoire.");
            return;
        }
        if (champStock.getText() == null || champStock.getText().isBlank()) {
            afficherAlerte("Le stock est obligatoire.");
            return;
        }
        if (champStockMin.getText() == null || champStockMin.getText().isBlank()) {
            afficherAlerte("Le stock minimum est obligatoire.");
            return;
        }

        // Conversion des champs numériques, avec gestion des erreurs de saisie
        double prix;
        int stock;
        int stockMin;
        double prixPromo = 0.0;
        try {
            prix = Double.parseDouble(champPrix.getText().trim());
            stock = Integer.parseInt(champStock.getText().trim());
            stockMin = Integer.parseInt(champStockMin.getText().trim());
            if (champPrixPromo.getText() != null && !champPrixPromo.getText().isBlank()) {
                prixPromo = Double.parseDouble(champPrixPromo.getText().trim());
            }
        } catch (NumberFormatException e) {
            afficherAlerte("Prix, prix promo, stock et stock min. doivent être des nombres valides.");
            return;
        }
        if (prixPromo >0&& prixPromo>=prix) {
            afficherAlerte("le prix de du promo doit etre inferieur au prix normal");
            return;
        }

        Produit produit = new Produit(
                champNom.getText().trim(),
                stock,
                stockMin,
                prix,
                comboCategorie.getValue(),
                comboFournisseur.getValue() // peut être null, Fournisseur optionnel
        );
        produit.setPrixPromo(prixPromo);

        produitService.addProduit(produit);

        fermerFenetre();
    }

    @FXML
    private void annuler() {
        fermerFenetre();
    }

    private void afficherAlerte(String message) {
        Alert alerte = new Alert(Alert.AlertType.WARNING);
        alerte.setTitle("Champ requis");
        alerte.setHeaderText(null);
        alerte.setContentText(message);
        alerte.showAndWait();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) champNom.getScene().getWindow();
        stage.close();
    }
}