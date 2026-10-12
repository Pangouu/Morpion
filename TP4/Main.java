package TP4;

import TP4.control.Actions;
import TP4.control.Reseau;
import TP4.control.ReseauWeb;
import TP4.view.Fenetre;

import javax.swing.*;

public class Main {
    private static final int PORT_PAR_DEFAUT = 5555;

    public static void main(String[] args) {
        String[] choix = {
                "Local (2 joueurs, meme ecran)",
                "En ligne : creer une partie (code)",
                "En ligne : rejoindre avec un code",
                "Avance : connexion directe par IP (1heberger)",
                "Avance : connexion directe par IP (rejoindre)"};
        int mode = JOptionPane.showOptionDialog(null, "Mode de jeu ?", "FGES Morpion",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, choix, choix[0]);
        if (mode < 0) return;

        Fenetre f = new Fenetre(500, 500);
        if (mode == 0) {
            f.setVisible(true);
            return;
        }

        boolean hote = (mode == 1 || mode == 3);
        char role = hote ? 'X' : 'O';
        Reseau.Recepteur recepteur = new Reseau.Recepteur() {
            @Override
            public void recu(String message) {
                Actions.recevoir(f.getLaGrille(), message);
            }

            @Override
            public void deconnecte() {
                f.afficherMessage("ADVERSAIRE DECONNECTE");
                JOptionPane.showMessageDialog(f, "L'adversaire s'est deconnecte.");
            }
        };

        if (mode == 1) { // salon en ligne : creer
            ReseauWeb r = new ReseauWeb(role, recepteur, ReseauWeb.nouveauCode());
            brancher(f, r);
            f.setTitle("FGES Morpion - salon " + r.getCode() + " (rouges)");
            f.afficherMessage("CODE DU SALON : " + r.getCode() + " - EN ATTENTE...");
            f.setVisible(true);
            r.creerSalon(() -> f.afficherJoueur('X'));
        } else if (mode == 2) { // salon en ligne : rejoindre
            String code = JOptionPane.showInputDialog(null, "Code du salon :");
            if (code == null || code.isBlank()) return;
            ReseauWeb r = new ReseauWeb(role, recepteur, code.trim().toUpperCase());
            try {
                if (!r.rejoindreSalon()) {
                    JOptionPane.showMessageDialog(null, "Aucun salon " + code.trim().toUpperCase()
                            + " ne repond. Verifiez le code et que l'hote a bien cree sa partie.");
                    System.exit(0);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Connexion impossible : " + e.getMessage());
                System.exit(0);
            }
            brancher(f, r);
            f.setTitle("FGES Morpion - salon " + r.getCode() + " (jaunes)");
            f.afficherJoueur('X');
            f.setVisible(true);
        } else if (mode == 3) { // direct : heberger
            Reseau r = new Reseau(role, recepteur);
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
            Reseau r = new Reseau(role, recepteur);
            try {
                r.rejoindre(adresse.trim(), port);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Connexion impossible : " + e.getMessage());
                System.exit(0);
            }
            brancher(f, r);
            f.setTitle("FGES Morpion - INVITE (jaunes)");
            f.afficherJoueur('X');
            f.setVisible(true);
        }
    }

    private static void brancher(Fenetre f, Reseau r) {
        f.getLaGrille().getMonEcouteur().setReseau(r);
        f.setMonRole(r.getRole());
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
