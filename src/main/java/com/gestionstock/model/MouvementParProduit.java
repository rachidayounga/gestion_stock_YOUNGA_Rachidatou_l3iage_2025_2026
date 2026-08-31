package com.gestionstock.model;
public class MouvementParProduit {
    private Produit produit;
     private Long quantitetotale;

    public MouvementParProduit() {
    }

    public MouvementParProduit(Produit produit, Long quntitetotale) {
        this.produit = produit;
        this.quantitetotale = quntitetotale;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public Long getQuntitetotale() {
        return quantitetotale;
    }

    public void setQuntitetotale(Long quntitetotale) {
        this.quantitetotale = quntitetotale;
    }
}
