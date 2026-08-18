package com.gestionstock.util;
import com.gestionstock.model.Utilisateur;

    public class SessionUtilisateur {

        // Une seule variable statique : partagée par toute l'appli
        private static Utilisateur utilisateurCourant;
//constructeur
        private SessionUtilisateur() {}

        public static Utilisateur getUtilisateurCourant() {
            return utilisateurCourant;
        }

        public static void setUtilisateurCourant(Utilisateur utilisateur) {
            utilisateurCourant = utilisateur;
        }

        public static void deconnecter() {
            utilisateurCourant = null;
        }

        public static boolean estAdmin() {
            return utilisateurCourant != null
                    && utilisateurCourant.getRole() == com.gestionstock.model.enums.RoleUtilisateur.ADMIN;
        }
    }


