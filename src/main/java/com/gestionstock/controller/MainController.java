package com.gestionstock.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import com.gestionstock.util.SessionUtilisateur;
import javafx.stage.Stage;
import java.io.IOException;

/*
    -@FXML: Annotation qui connecte un attribut Java à un composant déclaré dans le fichier XML via son fx:id
    -initialize(): méthode spéciale appelée automatiquement par JavaFx après le chargement du FXML
 */
public class MainController {
    @FXML
    private StackPane contenuPrincipale;
    @FXML
    private Button btnDeconnexion;
    @FXML
    private Button btnGestionCompte;


    @FXML
    public void initialize() {
        if(SessionUtilisateur.estAdmin()){
            btnGestionCompte.setVisible(true);

        }else {
            //setVisible(false) propriete java qui cache un boutton
            btnGestionCompte.setVisible(false);
            //manager reorganiser liberer espace
            btnGestionCompte.setManaged(false);
        }
        afficherDashboard();}

    @FXML
    private void afficherDashboard() {
        contenuPrincipale.getChildren().clear();
        contenuPrincipale.getChildren().add(new Label("Dashboard"));

    }

    @FXML
    private void afficherProduits() {
        chargerVue("/com/gestionstock/produits.fxml");
    }

    @FXML
    private void afficherCategories() {
        chargerVue("/com/gestionstock/Categorie.fxml");
    }

    @FXML
    private void afficherFournisseurs() {
        chargerVue("/com/gestionstock/fournisseur.fxml");
    }

    private void chargerVue(String cheminFxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(cheminFxml)
            );
            Node vue = loader.load();
            contenuPrincipale.getChildren().clear();
            contenuPrincipale.getChildren().add(vue);
        } catch(Exception e){
            e.printStackTrace();
        }
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

            Stage stage = (Stage) btnDeconnexion.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Gestion Stock IAGE");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    @FXML
    private void afficherMouvements() {
        chargerVue("/com/gestionstock/Mouvement.fxml");
    }
    @FXML
    private void gererComptes() {
        System.out.println("");
    }

}
