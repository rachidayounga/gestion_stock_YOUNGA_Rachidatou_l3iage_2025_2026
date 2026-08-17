package com.gestionstock.service;

import com.gestionstock.model.Produit;
import com.gestionstock.model.Utilisateur;

import java.util.Optional;
import com.gestionstock.util.JPAUtil;
import jakarta.persistence.EntityManager;
import org.mindrot.jbcrypt.BCrypt;
import java.util.List;

public class UtilisateurServiceImpl implements UtilisateurService {
    @Override
    public Optional<Utilisateur> verifierIdentifiants(String email, String motDePasseEnClair) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            List<Utilisateur> resultats = em.createQuery(
                    "SELECT u FROM Utilisateur u WHERE u.email = :email",
                    Utilisateur.class
            ).setParameter("email", email).getResultList();

            if (resultats.isEmpty()) {
                return Optional.empty();
            } else {
                Utilisateur utilisateur = resultats.get(0);

                if (!utilisateur.isActif()) {
                    return Optional.empty();
                }

                if (BCrypt.checkpw(motDePasseEnClair, utilisateur.getMotDePasseHash())) {
                    return Optional.of(utilisateur);
                } else {
                    return Optional.empty();
                }


            }
        }

    }
    @Override
    public void addUtilisateur(Utilisateur u) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(u);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Erreur lors de la sauvegarde de l'utilisateur");
        } finally {
            em.close();
        }
    }
    @Override
    public List<Utilisateur> findAllUtilisateurs() {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createQuery(
                    "SELECT u FROM Utilisateur u " ,


                    Utilisateur.class
            ).getResultList();
        }
    }

    @Override
    public void activerDesactiver(Long id, boolean actif) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            Utilisateur utilisateur = em.find(Utilisateur.class, id);
            if (utilisateur != null) {
                utilisateur.setActif(actif);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Erreur lors de la modification du statut de l'utilisateur");
        } finally {
            em.close();
        }
    }


}

