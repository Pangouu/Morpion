package TP4;

import TP4.control.Actions;
import TP4.control.Reseau;
import TP4.control.ReseauWeb;
import TP4.model.Joueurs;
import TP4.view.Fenetre;

import javax.swing.*;

public class Main {
    private static final int PORT_PAR_DEFAUT = 5555;

    public static void main(String[] args) {
        String[] choix = {
                "Local (meme ecran)",
                "En ligne : creer une partie (code)",
                "En ligne : rejoindre avec un code",
                "Avance : connexion directe par IP (heberger)",
                "Avance : connexion directe par IP (rejoindre)"};
        int mode = JOptionPane.showOptionDialog(null, "Mode de jeu ?", "FGES Morpion",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, choix, choix[0]);
        if (mode < 0) return;

        int nbJoueurs = 2; // la connexion directe par IP reste a 2
        if (mode == 0 || mode == 1) {
            nbJoueurs = demanderNbJoueurs();
            if (nbJoueurs == 0) return;
        }

        if (mode == 0) {
            creerFenetre(nbJoueurs).setVisible(true);
            return;
        }

        // l'invite d'un salon ne connait le nombre de joueurs qu'apres l'avoir rejoint :
        // la fenetre est creee plus tard, d'ou cette reference
        Fenetre[] laFenetre = new Fenetre[1];
        Reseau.Recepteur recepteur = new Reseau.Recepteur() {
            @Override
            public void recu(String message) {
                if (laFenetre[0] != null) Actions.recevoir(laFenetre[0].getLaGrille(), message);
            }

            @Override
            public void deconnecte() {
                if (laFenetre[0] == null) return;
                laFenetre[0].afficherMessage("JOUEUR DECONNECTE");
                JOptionPane.showMessageDialog(laFenetre[0], "Un joueur s'est deconnecte.");
            }
        };

        if (mode == 1) { // salon en ligne : creer
            ReseauWeb r = new ReseauWeb('X', recepteur, ReseauWeb.nouveauCode(), nbJoueurs);
            Fenetre f = laFenetre[0] = creerFenetre(nbJoueurs);
            brancher(f, r);
            f.setTitle("FGES Morpion - salon " + r.getCode() + " (" + equipe('X') + ")");
            f.afficherMessage(enAttente(r, 1));
            f.setVisible(true);
            r.creerSalon(() -> f.afficherJoueur('X'), presents -> f.afficherMessage(enAttente(r, presents)));
        } else if (mode == 2) { // salon en ligne : rejoindre
            String code = JOptionPane.showInputDialog(null, "Code du salon :");
            if (code == null || code.isBlank()) return;
            ReseauWeb r = new ReseauWeb('?', recepteur, code.trim().toUpperCase(), 0);
            try {
                if (!r.rejoindreSalon()) {
                    JOptionPane.showMessageDialog(null, "Aucun salon " + code.trim().toUpperCase()
                            + " ne repond, ou il est deja complet. Verifiez le code et que l'hote a bien cree sa partie.");
                    System.exit(0);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Connexion impossible : " + e.getMessage());
                System.exit(0);
            }
            Fenetre f = laFenetre[0] = creerFenetre(r.getNbJoueurs());
            brancher(f, r);
            f.setTitle("FGES Morpion - salon " + r.getCode() + " (" + equipe(r.getRole()) + ")");
            f.afficherMessage("EN ATTENTE DES AUTRES JOUEURS...");
            f.setVisible(true);
            r.setSurDemarrage(() -> f.afficherJoueur('X'));
        } else if (mode == 3) { // direct : heberger
            Reseau r = new Reseau('X', recepteur);
            Fenetre f = laFenetre[0] = creerFenetre(2);
            brancher(f, r);
            int port = demanderPort();
            f.setTitle("FGES Morpion - HOTE (rouges)");
            f.afficherMessage("EN ATTENTE D'UN ADVERSAIRE (port " + port + ")...");
            f.setVisible(true);
            r.heberger(port,
                    () -> f.afficherJoueur('X'),
                    () -> {
                        f.afficherMessage("HEBERGEMENT IMPOSSIBLE");
                        JOptionPane.showMessageDialog(f, "Impossible d'ouvrir le port " + port + ".");
                    });
        } else { // direct : rejoindre
            String adresse = JOptionPane.showInputDialog(null, "Adresse IP de l'hote :", "localhost");
            if (adresse == null) return;
            int port = demanderPort();
            Reseau r = new Reseau('O', recepteur);
            try {
                r.rejoindre(adresse.trim(), port);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Connexion impossible : " + e.getMessage());
                System.exit(0);
            }
            Fenetre f = laFenetre[0] = creerFenetre(2);
            brancher(f, r);
            f.setTitle("FGES Morpion - INVITE (jaunes)");
            f.afficherJoueur('X');
            f.setVisible(true);
        }
    }

    /** 2 joueurs : grille 3x3 ; 4 joueurs : grille 7x7, fenetre plus grande. */
    private static Fenetre creerFenetre(int nbJoueurs) {
        return (nbJoueurs == 2) ? new Fenetre(500, 500, 2) : new Fenetre(800, 750, nbJoueurs);
    }

    private static void brancher(Fenetre f, Reseau r) {
        f.getLaGrille().getMonEcouteur().setReseau(r);
        f.setMonRole(r.getRole());
    }

    private static String equipe(char role) {
        return Joueurs.nom(role).toLowerCase() + "s";
    }

    private static String enAttente(ReseauWeb r, int presents) {
        return "CODE DU SALON : " + r.getCode() + " - EN ATTENTE (" + presents + "/" + r.getNbJoueurs() + ")...";
    }

    /** Renvoie 2 ou 4, 0 si la fenetre est fermee. */
    private static int demanderNbJoueurs() {
        String[] choix = {"2 joueurs (3x3)", "4 joueurs (7x7, 4 alignes)"};
        int c = JOptionPane.showOptionDialog(null, "Nombre de joueurs ?", "FGES Morpion",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, choix, choix[0]);
        if (c < 0) return 0;
        return (c == 0) ? 2 : 4;
    }

    private static int demanderPort() {
        String s = JOptionPane.showInputDialog(null, "Port :", "" + PORT_PAR_DEFAUT);
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return PORT_PAR_DEFAUT;
        }
    }
}
