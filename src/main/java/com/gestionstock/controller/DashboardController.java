package com.gestionstock.controller;

import com.gestionstock.model.Categorie;
import com.gestionstock.model.Produit;
import com.gestionstock.service.*;
import com.gestionstock.util.SessionUtilisateur;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class DashboardController {

    @FXML
    private Label labelUtilisateurConnecte;
    @FXML
    private Button boutonDeconnexion;

    @FXML
    private Label labelTotalProduits;
    @FXML
    private Label labelStockBas;
    @FXML
    private Label labelValeurStock;
    @FXML
    private Label labelMouvementsJour;
    @FXML
    private TableView tableStockBas;
    @FXML
    private TableColumn colonneNomProduit;
    @FXML
    private TableColumn colonneQuantiteStock;
    @FXML
    private TableColumn colonneQuantiteMin;
    private final ProduitService produitService = new ProduitServiceImpl();
    private final MouvementService mouvementService = new MouvementServiceImpl();
    private ObservableList<Produit> listeStockBas;



    @FXML
    public void initialize() {
        if (SessionUtilisateur.getUtilisateurCourant() != null) {
            labelUtilisateurConnecte.setText(SessionUtilisateur.getUtilisateurCourant().getNom());
        }
        chargerStatistiques();
        configurerColones();
        chargerProduitsStockBas();
    }

    private void chargerStatistiques() {
        long totalProduits = produitService.countTotalProduits();
        long stockBas = produitService.countStockBas();
        double valeurStock = produitService.calculerValeurTotaleStock();
        long entrees = mouvementService.countEntreesDuJour();
        long sorties = mouvementService.countSortiesDuJour();

        labelTotalProduits.setText(Long.toString(totalProduits));
        labelStockBas.setText(Long.toString(stockBas));
        labelValeurStock.setText(String.format("%.0f", valeurStock));
        labelMouvementsJour.setText(String.format("%d / %d", entrees, sorties));
    }

    @FXML
    private void seDeconnecter() {
        SessionUtilisateur.deconnecter();
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/gestionstock/Login.fxml")
            );
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(
                    getClass().getResource("/com/gestionstock/style.css").toExternalForm()
            );

            Stage stage = (Stage) boutonDeconnexion.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Gestion Stock IAGE");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void configurerColones() {
        colonneNomProduit.setCellValueFactory( new PropertyValueFactory<>("nom"));
        colonneQuantiteStock.setCellValueFactory( new PropertyValueFactory<>("quantiteStock"));
        colonneQuantiteMin.setCellValueFactory( new PropertyValueFactory<>("quantiteMin"));

    }

    private void chargerProduitsStockBas() {
        List<Produit> produits = produitService.findByStockBas();

        listeStockBas = FXCollections.observableArrayList(produits);
        tableStockBas.setItems(listeStockBas);
    }

}