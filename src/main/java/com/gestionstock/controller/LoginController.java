package com.gestionstock.controller;
import com.gestionstock.service.UtilisateurService;
import javafx.fxml.FXML;
import com.gestionstock.service.UtilisateurServiceImpl;
import com.gestionstock.model.Utilisateur;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import com.gestionstock.util.SessionUtilisateur;



import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.Optional;
public class LoginController {
    @FXML
    TextField champEmail;
    @FXML
    PasswordField champMotDePasse;
    @FXML
    Button btnConnexion;
    @FXML
    Label labelErreur;
    private final UtilisateurService utilisateurService = new UtilisateurServiceImpl();

    @FXML
    private void seConnecter() {
        String email = champEmail.getText();
        String motDePasse = champMotDePasse.getText();
        // 2. Appeler le service qui vérifie en base (avec BCrypt)
        Optional<Utilisateur> resultat = utilisateurService.verifierIdentifiants(email, motDePasse);
        if (resultat.isEmpty()) {
            labelErreur.setText("Email ou mot de passe incorrect");
            return; // on arrête ici
        }
        Utilisateur utilisateurConnecte = resultat.get();
        SessionUtilisateur.setUtilisateurCourant(utilisateurConnecte);
        //charger main.fxml et remplacer la scène
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/gestionstock/main.fxml")
            );
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(
                    getClass().getResource("/com/gestionstock/style.css").toExternalForm()
            );

            Stage stage = (Stage) btnConnexion.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Gestion Stock IAGE");
        } catch (IOException e) {
            labelErreur.setText("Erreur lors du chargement de l'application");
            e.printStackTrace();
        }

    }

}
