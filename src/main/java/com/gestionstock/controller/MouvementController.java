package com.gestionstock.controller;

import com.gestionstock.model.Mouvement;
import com.gestionstock.model.Produit;
import com.gestionstock.model.enums.TypeMouvement;
import com.gestionstock.service.MouvementService;
import com.gestionstock.service.MouvementServiceImpl;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MouvementController {

    @FXML
    TableView<Mouvement> tableMouvements;
    @FXML
    TableColumn<Mouvement, String> colonneDate;
    @FXML
    TableColumn<Mouvement, String> colonneProduit;
    @FXML
    TableColumn<Mouvement, TypeMouvement> colonneType;
    @FXML
    TableColumn<Mouvement, Integer> colonneQuantite;
    @FXML
    TableColumn<Mouvement, String> colonneMotif;
    @FXML
    TextField champRecherche;

    private final MouvementService mouvementService = new MouvementServiceImpl();
    private final DateTimeFormatter formatDate = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private ObservableList<Mouvement> listeMouvements;

    @FXML
    public void initialize() {
        configurerColonnes();
        chargerDonnees();
    }

    private void configurerColonnes() {
        colonneDate.setCellValueFactory(data -> {
            var date = data.getValue().getDateMouvement();
            return new SimpleStringProperty(date != null ? date.format(formatDate) : "");
        });
        colonneProduit.setCellValueFactory(data -> {
            Produit p = data.getValue().getProduit();
            return new SimpleStringProperty(p != null ? p.getNom() : "");
        });
        colonneType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colonneQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colonneMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));
    }

    private void chargerDonnees() {
        List<Mouvement> mouvements = mouvementService.findAllMouvements();
        listeMouvements = FXCollections.observableArrayList(mouvements);
        tableMouvements.setItems(listeMouvements);
    }

    @FXML
    private void rechercherMouvements() {
        String recherche = champRecherche.getText();

        if (recherche == null || recherche.isBlank()) {
            tableMouvements.setItems(listeMouvements);
            return;
        }

        String rechercheMinuscule = recherche.trim().toLowerCase();

        ObservableList<Mouvement> resultats = listeMouvements.filtered(mouvement ->
                mouvement.getProduit() != null
                        && mouvement.getProduit().getNom() != null
                        && mouvement.getProduit().getNom().toLowerCase().contains(rechercheMinuscule)
        );

        tableMouvements.setItems(resultats);
    }

    @FXML
    private void ouvrirDialogAjout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gestionstock/AddMouvement.fxml"));
            Parent racine = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Nouveau mouvement");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tableMouvements.getScene().getWindow());
            stage.setScene(new Scene(racine));

            stage.showAndWait();

            chargerDonnees();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alerte = new Alert(Alert.AlertType.ERROR);
            alerte.setTitle("Erreur");
            alerte.setHeaderText(null);
            alerte.setContentText("Impossible d'ouvrir le formulaire de mouvement.");
            alerte.showAndWait();
        }
    }
}