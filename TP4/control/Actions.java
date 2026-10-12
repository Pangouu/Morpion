package TP4.control;

import TP4.view.Grille;

import javax.swing.*;

public class Actions {
    /** Clic sur RESET : vide la grille (et previent l'adversaire en ligne). */
    public static void action1(Grille g) {
        Reseau r = g.getMonEcouteur().getReseau();
        if (r != null) r.envoyer("RESET");
        reinitialiser(g);
    }

    private static void reinitialiser(Grille g) {
        System.out.println(" RESET ");
        int n = g.getTaille();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                JButton b = g.getLesBoutons()[i][j];
                b.setText("");
                b.setSelected(false);
                b.setBackground(UIManager.getColor("Button.background"));
                b.setContentAreaFilled(false);
            }
        }
        g.getMonEcouteur().setJeuTermine(false);
        g.getMonEcouteur().setXO('X');
        g.getSaFenetre().afficherJoueur('X');
    }

    /** Message recu de l'adversaire. */
    public static void recevoir(Grille g, String message) {
        String[] p = message.split(" ");
        if (p[0].equals("MOVE") && p.length == 3) {
            g.getMonEcouteur().coupAdverse(Integer.parseInt(p[1]), Integer.parseInt(p[2]));
        } else if (p[0].equals("RESET")) {
            reinitialiser(g);
        }
    }

    /** Fermeture de la fenetre. */
    public static void action2() {
        System.out.println("bye bye ");
        System.exit(0);
    }
}
