package com.gestionstock.controller;

import com.gestionstock.model.Fournisseur;
import com.gestionstock.model.Utilisateur;
import com.gestionstock.model.enums.RoleUtilisateur;
import com.gestionstock.service.UtilisateurService;
import com.gestionstock.service.UtilisateurServiceImpl;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class GestionComptesController {

    @FXML
    private TableView<Utilisateur> tableUtilisateurs;
    @FXML
    private TableColumn<Utilisateur, String> colonneNom;
    @FXML
    private TableColumn<Utilisateur, String> colonneEmail;
    @FXML
    private TableColumn<Utilisateur, RoleUtilisateur> colonneRole;
    @FXML
    private TableColumn<Utilisateur, String> colonneStatut;
    @FXML
    private TableColumn<Utilisateur, Void> colonneActions;

    private final UtilisateurService utilisateurService = new UtilisateurServiceImpl();
    private ObservableList<Utilisateur> listeUtilisateurs;

    @FXML
    public void initialize() {
        configurerColonnes();
        chargerDonnees();
    }

    private void configurerColonnes() {
        colonneNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colonneEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colonneRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        colonneStatut.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().isActif() ? "Actif" : "Inactif")
        );

        colonneActions.setCellFactory(colonne -> new CelluleActionsUtilisateur(this));
    }

    private void chargerDonnees() {
        List<Utilisateur> utilisateurs = utilisateurService.findAllUtilisateurs();
        listeUtilisateurs = FXCollections.observableArrayList(utilisateurs);
        tableUtilisateurs.setItems(listeUtilisateurs);
    }

    // Appelée depuis CelluleActionsUtilisateur quand on clique sur le bouton toggle
    void basculerStatut(Utilisateur utilisateur) {
        utilisateurService.activerDesactiver(utilisateur.getId(), !utilisateur.isActif());
        chargerDonnees();
    }


    }