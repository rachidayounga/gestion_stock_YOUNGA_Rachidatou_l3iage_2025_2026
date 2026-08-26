package com.gestionstock.controller;

import com.gestionstock.model.Produit;
import com.gestionstock.model.Mouvement;
import com.gestionstock.model.enums.TypeMouvement;
import com.gestionstock.service.MouvementService;
import com.gestionstock.service.MouvementServiceImpl;
import com.gestionstock.service.ProduitService;
import com.gestionstock.service.ProduitServiceImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AddMouvementController implements Initializable {

    @FXML
    private ComboBox<Produit> comboProduit;
    @FXML
    private RadioButton radioEntree;
    @FXML
    private RadioButton radioSortie;
    @FXML
    private ToggleGroup groupeTypeMouvement;
    @FXML
    private TextField champQuantite;
    @FXML
    private Label labelMotif;
    @FXML
    private TextField champMotif;
    @FXML
    private Label labelApercuStock;
    @FXML
    private Button boutonAnnuler;
    @FXML
    private Button boutonEnregistrer;

    private final ProduitService produitService = new ProduitServiceImpl();
    private final MouvementService mouvementService = new MouvementServiceImpl();
    private boolean mouvementAjoute = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        comboProduit.setItems(FXCollections.observableArrayList(produitService.findAllProduits()));

        // Listener sur le produit sélectionné
        comboProduit.valueProperty().addListener((obs, ancienProduit, nouveauProduit) -> {
            mettreAJourApercuStock();
        });
        // Listener sur le type sélectionné (ENTRE/SORTIE)
        groupeTypeMouvement.selectedToggleProperty().addListener((obs, ancienToggle, nouveauToggle) -> {
            mettreAJourApercuStock();
            mettreAJourCaractereObligatoireMotif();
        });
        // Listener sur la quantité saisie
        champQuantite.textProperty().addListener((obs, ancienTexte, nouveauTexte) -> {
            mettreAJourApercuStock();
        });

    }

    private void mettreAJourApercuStock() {
        //  on teste la VALEUR sélectionnée du ComboBox
        Produit produitSelectionne = comboProduit.getValue();
        if (produitSelectionne == null) {
            labelApercuStock.setText("Stock actuel : —");
            return;
        }
        // on essaie de parser la quantité ; si le champ est vide ou invalide,
        // on affiche  le stock actuel sans "Nouveau stock"
        String texteQuantite = champQuantite.getText();
        Integer quantiteSaisie = null;
        if (texteQuantite != null && !texteQuantite.isBlank()) {
            try {
                quantiteSaisie = Integer.parseInt(texteQuantite.trim());
            } catch (NumberFormatException e) {
                quantiteSaisie = null;
            }
            double stockActuel = produitSelectionne.getQuantiteStock();
            if (quantiteSaisie == null || quantiteSaisie <= 0) {
                // pas de quantité valide saisie -> on montre juste le stock actuel
                labelApercuStock.setText("Stock actuel : " + stockActuel);
                return;
            }
            double nouveauStock;
            if (radioSortie.isSelected()) {
                nouveauStock = stockActuel - quantiteSaisie;
            } else {
                nouveauStock = stockActuel + quantiteSaisie;
            }

            labelApercuStock.setText("Stock actuel : " + stockActuel + "  →  Nouveau stock : " + nouveauStock);
        }
    }
    // vérifie que le bouton radio soit coché
    private void mettreAJourCaractereObligatoireMotif() {
        if (radioSortie.isSelected()) {
            labelMotif.setText("Motif *");
        } else {
            labelMotif.setText("Motif");
        }
    }

    @FXML
    private void enregistrer() {
        // 1. Validation du produit
        if (comboProduit.getValue() == null) {
            afficherAlerte("Le produit est obligatoire.");
            return;
        }
        //  Validation de la quantité

        int qte;
        try {
            qte = Integer.parseInt(champQuantite.getText().trim());
        } catch (NumberFormatException e) {
            afficherAlerte("La quantité doit être un nombre entier.");
            return;
        }
        if (qte <= 0) {
            afficherAlerte("La quantité doit être supérieure à 0.");
            return;
        }
        // Type de mouvement sélectionné
        TypeMouvement type = radioSortie.isSelected() ? TypeMouvement.SORTIE : TypeMouvement.ENTRE;
        // 4. Motif obligatoire uniquement pour une SORTIE
        String motif = champMotif.getText();
        if (type == TypeMouvement.SORTIE) {
            if (motif == null || motif.isEmpty()) {
                afficherAlerte("Le motif est obligatoire pour une sortie.");
                return;
            }
        }
        //  Produit sélectionné
        Produit produit = comboProduit.getValue();
        // Appel au service, protégé par un try/catch
        try {
            mouvementService.addMouvement(produit, type, qte, motif);
            mouvementAjoute = true;
            fermerFenetre();
        } catch (RuntimeException e) {
            // e.getMessage() contient le message métier levé par le service
            // (ex: "Stock insuffisant", "Le motif est obligatoire pour une sortie")
            afficherAlerte(e.getMessage());
        }
    }

    @FXML
    private void annuler() {
        fermerFenetre();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) boutonAnnuler.getScene().getWindow();
        stage.close();
    }

    public boolean isMouvementAjoute() {
        return mouvementAjoute;
    }

    private void afficherAlerte(String message) {
        Alert alerte = new Alert(Alert.AlertType.WARNING);
        alerte.setTitle("Champ requis");
        alerte.setHeaderText(null);
        alerte.setContentText(message);
        alerte.showAndWait();
    }
}