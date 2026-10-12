package TP4.control;

import TP4.model.Joueurs;
import TP4.view.Fenetre;
import TP4.view.Grille;
import TP4.view.PlayWave;
import TP4.view.Score;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EcouteurBouton implements ActionListener {
    private char XO = 'X';
    private Grille saGrille;
    private boolean jeuTermine;
    private PlayWave playWave;
    private Reseau reseau; // null = partie locale sur le meme ecran

    public EcouteurBouton(Grille g) {
        saGrille = g;
        jeuTermine = false;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (jeuTermine) return;
        JButton leBoutonClique = (JButton) actionEvent.getSource();
        if (leBoutonClique.isSelected()) return; // un seul clic par bouton

        if (reseau != null) {
            // en ligne : il faut etre connecte et que ce soit son tour
            if (!reseau.isConnecte() || XO != reseau.getRole()) return;
            JButton[][] T = saGrille.getLesBoutons();
            int n = saGrille.getTaille();
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    if (T[i][j] == leBoutonClique) reseau.envoyer("MOVE " + i + " " + j);
        }
        jouerCoup(leBoutonClique);
    }

    /** Coup venant d'un autre joueur. */
    public void coupAdverse(int i, int j) {
        if (jeuTermine) return;
        JButton b = saGrille.getLesBoutons()[i][j];
        if (!b.isSelected()) jouerCoup(b);
    }

    private void jouerCoup(JButton leBoutonClique) {
        System.out.println(" clic sur bouton");
        playWave = new PlayWave("smb_kick.wav");
        playWave.start();
        leBoutonClique.setFont(saGrille.getPolice());
        leBoutonClique.setForeground(Joueurs.COULEURS[Joueurs.indice(XO)]);
        leBoutonClique.setText("" + XO);
        leBoutonClique.setSelected(true);
        XO = Joueurs.suivant(XO, saGrille.getNbJoueurs());

        Fenetre f = saGrille.getSaFenetre();
        int gagnant = detecterVictoire();
        if (gagnant >= 0) {
            playWave = new PlayWave("power-up.wav");
            playWave.start();
            jeuTermine = true;
            Score s = f.getScore(gagnant);
            s.setValeur(s.getValeur() + 1);
            s.getLeScore().setText("" + s.getValeur());
            f.afficherFinDePartie();
            JOptionPane.showMessageDialog(null, "Victoire des " + Joueurs.NOMS[gagnant] + "S");
        } else if (grillePleine()) {
            jeuTermine = true;
            f.afficherFinDePartie();
            JOptionPane.showMessageDialog(null, "Match nul");
        } else {
            f.afficherJoueur(XO);
        }
    }

    /** Renvoie l'indice du gagnant (voir Joueurs), -1 sinon. Colorie les cases gagnantes. */
    private int detecterVictoire() {
        JButton[][] T = this.saGrille.getLesBoutons();
        int n = saGrille.getTaille();
        int k = saGrille.getAligner();
        // couleur de surbrillance : le vert est deja un joueur a 4
        Color surbrillance = (saGrille.getNbJoueurs() == 2) ? Color.green : Color.magenta;

        // a partir de chaque case : horizontale, verticale et les 2 diagonales
        int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                String t = T[i][j].getText();
                if (t.equals("")) continue;
                for (int[] d : directions) {
                    int fi = i + (k - 1) * d[0], fj = j + (k - 1) * d[1];
                    if (fi < 0 || fi >= n || fj < 0 || fj >= n) continue;
                    boolean aligne = true;
                    for (int m = 1; m < k && aligne; m++)
                        aligne = T[i + m * d[0]][j + m * d[1]].getText().equals(t);
                    if (aligne) {
                        for (int m = 0; m < k; m++) {
                            JButton b = T[i + m * d[0]][j + m * d[1]];
                            b.setContentAreaFilled(true);
                            b.setBackground(surbrillance);
                        }
                        return Joueurs.indice(t.charAt(0));
                    }
                }
            }
        }
        return -1;
    }

    private boolean grillePleine() {
        for (JButton[] ligne : saGrille.getLesBoutons())
            for (JButton b : ligne)
                if (!b.isSelected()) return false;
        return true;
    }

    public void setJeuTermine(boolean jeuTermine) { this.jeuTermine = jeuTermine; }
    public void setXO(char XO) { this.XO = XO; }
    public void setReseau(Reseau reseau) { this.reseau = reseau; }
    public Reseau getReseau() { return reseau; }
}
