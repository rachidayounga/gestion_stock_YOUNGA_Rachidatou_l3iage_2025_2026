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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MouvementController {

    // Colonnes et tableau liés au fx:id du fichier Mouvement.fxml
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

    // Champ de recherche texte (par nom de produit)
    @FXML
    TextField champRecherche;

    // Les 3 boutons radio du filtre par type, liés au même ToggleGroup dans le FXML
    // (un seul peut être sélectionné à la fois)
    @FXML
    private RadioButton radioToutes;
    @FXML
    private RadioButton radioEntrees;
    @FXML
    private RadioButton radioSorties;

    // Sélecteurs de date pour filtrer par période (bornes inclusives)
    @FXML
    private DatePicker dateDebut;
    @FXML
    private DatePicker dateFin;

    private final MouvementService mouvementService = new MouvementServiceImpl();

    // Format d'affichage des dates dans la colonne "Date" du tableau
    private final DateTimeFormatter formatDate = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    //  liste qu'on applique les filtres à chaque appel de appliquerFiltres()
    private ObservableList<Mouvement> listeMouvements;

    // Appelée automatiquement par JavaFX juste après le chargement du FXML
    @FXML
    public void initialize() {
        configurerColonnes();
        chargerDonnees();
    }

    // Indique à chaque colonne du TableView comment récupérer sa valeur depuis un objet Mouvement
    private void configurerColonnes() {
        //  transformer
        // la donnée brute (LocalDateTime, objet Produit) en texte affichable
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

    // Charge tous les mouvements depuis la base et les affiche dans le tableau
    // (appelée au démarrage, et après ajout d'un nouveau mouvement)
    private void chargerDonnees() {
        List<Mouvement> mouvements = mouvementService.findAllMouvements();
        listeMouvements = FXCollections.observableArrayList(mouvements);
        tableMouvements.setItems(listeMouvements);
    }

    // Appelée à chaque changement d'un des filtres (texte, type, période)
    // Combine les 3 critères et met à jour le contenu du tableau
    @FXML
    private void appliquerFiltres() {
        // --- Filtre texte : nom du produit doit contenir ce qui est tapé ---
        String recherche = champRecherche.getText();
        String rechercheMinuscule = (recherche == null) ? "" : recherche.trim().toLowerCase();
        // Si le champ est vide, rechercheMinuscule = "" et contains("") vaut toujours true,
        // donc aucun mouvement n'est exclu par ce critère
        // --- Filtre type : null = "Toutes" (aucune restriction), sinon ENTRE ou SORTIE ---
        TypeMouvement typeFiltre;
        if (radioEntrees.isSelected()) {
            typeFiltre = TypeMouvement.ENTRE;
        } else if (radioSorties.isSelected()) {
            typeFiltre = TypeMouvement.SORTIE;
        } else {
            typeFiltre = null; // radioToutes sélectionné
        }

        // --- Filtre période
        LocalDate debut = dateDebut.getValue();
        LocalDate fin = dateFin.getValue();

        // On reconstruit la liste filtrée à partir de la liste complète (listeMouvements),
        ObservableList<Mouvement> resultats = listeMouvements.filtered(mouvement -> {

            // Condition 1 : le nom du produit contient le texte recherché
            boolean correspondTexte = mouvement.getProduit() != null
                    && mouvement.getProduit().getNom() != null
                    && mouvement.getProduit().getNom().toLowerCase().contains(rechercheMinuscule);

            // Condition 2 : le type du mouvement correspond au filtre
            boolean correspondType = typeFiltre == null || mouvement.getType() == typeFiltre;

            // Condition 3 : la date du mouvement est comprise entre debut et fin
            boolean correspondPeriode = true;
            if (mouvement.getDateMouvement() != null) {
                LocalDate dateMvt = mouvement.getDateMouvement().toLocalDate();
                if (debut != null && dateMvt.isBefore(debut)) {
                    correspondPeriode = false; // mouvement antérieur à la date de début choisie
                }
                if (fin != null && dateMvt.isAfter(fin)) {
                    correspondPeriode = false; // mouvement postérieur à la date de fin choisie
                }
            }
            // Le mouvement n'est gardé que si les 3 conditions sont vraies en même temps
            return correspondTexte && correspondType && correspondPeriode;
        });

        tableMouvements.setItems(resultats);
    }

    // Ouvre le formulaire modal d'ajout de mouvement, et recharge le tableau
    // une fois la fenêtre fermée (que l'ajout ait réussi ou non)
    @FXML
    private void ouvrirDialogAjout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gestionstock/AddMouvement.fxml"));
            Parent racine = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Nouveau mouvement");
            stage.initModality(Modality.WINDOW_MODAL); // bloque l'accès à la fenêtre principale tant que celle-ci est ouverte
            stage.initOwner(tableMouvements.getScene().getWindow());
            stage.setScene(new Scene(racine));

            stage.showAndWait(); // attend la fermeture avant de continuer

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