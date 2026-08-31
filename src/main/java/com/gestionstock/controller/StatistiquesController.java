package com.gestionstock.controller;

import com.gestionstock.model.Categorie;
import com.gestionstock.model.MouvementParProduit;
import com.gestionstock.model.enums.TypeMouvement;
import com.gestionstock.service.ProduitService;
import com.gestionstock.service.ProduitServiceImpl;
import com.gestionstock.service.MouvementService;
import com.gestionstock.service.MouvementServiceImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

public class StatistiquesController {

    @FXML
    private DatePicker dateDebut;
    @FXML
    private DatePicker dateFin;

    @FXML
    private Label labelValeurTotaleStock;
    @FXML
    private Label labelProduitLePlusMouvemente;

    @FXML
    private BarChart<String, Number> barChartMouvements;
    @FXML
    private PieChart pieChartCategories;

    private final ProduitService produitService = new ProduitServiceImpl();
    private final MouvementService mouvementService = new MouvementServiceImpl();

    // Correspondance numéro de mois -> abréviation affichée sur le graphique
    private static final String[] NOMS_MOIS = {
            "Jan", "Fév", "Mar", "Avr", "Mai", "Jun",
            "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc"
    };

    @FXML
    public void initialize() {
        // Valeur totale du stock et camembert par catégorie reflètent l'état ACTUEL du
        // stock : ils ne dépendent pas de la période choisie, donc chargés une seule fois ici.
        chargerValeurStock();
        chargerPieChart();

        // Produit le plus mouvementé et graphique par mois dépendent de la période -> null,null = tout l'historique / année en cours
        chargerProduitMax(null, null);
        chargerBarChart(null, null);
    }

    private void chargerValeurStock() {
        double valeurStock = produitService.calculerValeurTotaleStock();
        labelValeurTotaleStock.setText(String.format("%.0f FCFA", valeurStock));
    }

    private void chargerProduitMax(LocalDateTime debut, LocalDateTime fin) {
        Optional<MouvementParProduit> produitMax = mouvementService.findProduitLePlusMouvemente(debut, fin);
        if (produitMax.isPresent()) {
            MouvementParProduit resultat = produitMax.get();
            labelProduitLePlusMouvemente.setText(
                    resultat.getProduit().getNom() + " — " + resultat.getQuntitetotale() + " unités"
            );
        } else {
            labelProduitLePlusMouvemente.setText("Aucun mouvement enregistré");
        }
    }

    private void chargerBarChart(LocalDateTime debut, LocalDateTime fin) {
        Map<Integer, Long> entreesParMois = mouvementService.compterMouvementsParMois(TypeMouvement.ENTRE, debut, fin);
        Map<Integer, Long> sortiesParMois = mouvementService.compterMouvementsParMois(TypeMouvement.SORTIE, debut, fin);

        XYChart.Series<String, Number> serieEntrees = new XYChart.Series<>();
        serieEntrees.setName("Entrées");

        XYChart.Series<String, Number> serieSorties = new XYChart.Series<>();
        serieSorties.setName("Sorties");

        // On parcourt les mois réellement renvoyés (12 mois par défaut, ou seulement
        // ceux couverts par la période choisie) plutôt qu'une plage 1..12 fixe.
        for (Integer mois : entreesParMois.keySet()) {
            String nomMois = NOMS_MOIS[mois - 1];
            serieEntrees.getData().add(new XYChart.Data<>(nomMois, entreesParMois.get(mois)));
            serieSorties.getData().add(new XYChart.Data<>(nomMois, sortiesParMois.get(mois)));
        }

        barChartMouvements.getData().clear();
        barChartMouvements.getData().addAll(serieEntrees, serieSorties);
    }

    private void chargerPieChart() {
        Map<Categorie, Double> valeurParCategorie = produitService.calculerValeurStockParCategorie();

        ObservableList<PieChart.Data> donnees = FXCollections.observableArrayList();
        for (Map.Entry<Categorie, Double> entry : valeurParCategorie.entrySet()) {
            String nomCategorie = entry.getKey().getNom();
            donnees.add(new PieChart.Data(nomCategorie, entry.getValue()));
        }

        pieChartCategories.setData(donnees);
    }

    @FXML
    private void appliquerFiltrePeriode() {
        LocalDate debut = dateDebut.getValue();
        LocalDate fin = dateFin.getValue();

        if (debut == null || fin == null) {
            afficherAlerte("Veuillez choisir une date de début et une date de fin.");
            return;
        }
        if (debut.isAfter(fin)) {
            afficherAlerte("La date de début doit être avant (ou égale à) la date de fin.");
            return;
        }

        LocalDateTime debutPeriode = debut.atStartOfDay();
        // borne exclusive : on inclut toute la journée de "fin", donc jusqu'au lendemain minuit
        LocalDateTime finPeriode = fin.plusDays(1).atStartOfDay();

        chargerProduitMax(debutPeriode, finPeriode);
        chargerBarChart(debutPeriode, finPeriode);
    }

    private void afficherAlerte(String message) {
        Alert alerte = new Alert(Alert.AlertType.WARNING);
        alerte.setHeaderText(null);
        alerte.setContentText(message);
        alerte.showAndWait();
    }
}