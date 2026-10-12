package TP4.control;

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
    private Reseau reseau; // null = partie locale a 2 sur le meme ecran

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
            for (int i = 0; i < 3; i++)
                for (int j = 0; j < 3; j++)
                    if (T[i][j] == leBoutonClique) reseau.envoyer("MOVE " + i + " " + j);
        }
        jouerCoup(leBoutonClique);
    }

    /** Coup venant de l'adversaire. */
    public void coupAdverse(int i, int j) {
        if (jeuTermine) return;
        JButton b = saGrille.getLesBoutons()[i][j];
        if (!b.isSelected()) jouerCoup(b);
    }

    private void jouerCoup(JButton leBoutonClique) {
        System.out.println(" clic sur bouton");
        playWave = new PlayWave("smb_kick.wav");
        playWave.start();
        leBoutonClique.setFont(Score.mafaconDecrire);
        if (XO == 'X') {
            leBoutonClique.setForeground(Color.red);
            leBoutonClique.setText("" + 'X');
            XO = 'O';
        } else {
            leBoutonClique.setForeground(Color.yellow);
            leBoutonClique.setText("" + 'O');
            XO = 'X';
        }
        leBoutonClique.setSelected(true);

        Fenetre f = saGrille.getSaFenetre();
        int reponse = detecterVictoire();
        int valeurDuScoreActuelle;
        switch (reponse) {
            case 1:
                playWave = new PlayWave("power-up.wav");
                playWave.start();
                jeuTermine = true;
                valeurDuScoreActuelle = f.getScoreRouge().getValeur();
                f.getScoreRouge().getLeScore().setText("" + (valeurDuScoreActuelle + 1));
                f.getScoreRouge().setValeur(valeurDuScoreActuelle + 1);
                f.afficherFinDePartie();
                JOptionPane.showMessageDialog(null, "Victoire des ROUGES");
                break;
            case 2:
                playWave = new PlayWave("power-up.wav");
                playWave.start();
                jeuTermine = true;
                valeurDuScoreActuelle = f.getScoreJaune().getValeur();
                f.getScoreJaune().getLeScore().setText("" + (valeurDuScoreActuelle + 1));
                f.getScoreJaune().setValeur(valeurDuScoreActuelle + 1);
                f.afficherFinDePartie();
                JOptionPane.showMessageDialog(null, "Victoire des JAUNES");
                break;
            default:
                f.afficherJoueur(XO);
        }
    }

    /** Renvoie 1 si les rouges (X) gagnent, 2 si les jaunes (O) gagnent, 0 sinon. Colorie les cases gagnantes. */
    private int detecterVictoire() {
        JButton[][] T = this.saGrille.getLesBoutons();

        // 3 lignes, 3 colonnes puis 2 diagonales
        int[][][] alignements = {
                {{0, 0}, {0, 1}, {0, 2}}, {{1, 0}, {1, 1}, {1, 2}}, {{2, 0}, {2, 1}, {2, 2}},
                {{0, 0}, {1, 0}, {2, 0}}, {{0, 1}, {1, 1}, {2, 1}}, {{0, 2}, {1, 2}, {2, 2}},
                {{0, 0}, {1, 1}, {2, 2}}, {{0, 2}, {1, 1}, {2, 0}}
        };
        for (int[][] a : alignements) {
            String t0 = T[a[0][0]][a[0][1]].getText();
            String t1 = T[a[1][0]][a[1][1]].getText();
            String t2 = T[a[2][0]][a[2][1]].getText();
            if (!t0.equals("") && t0.equals(t1) && t0.equals(t2)) {
                for (int[] c : a) {
                    T[c[0]][c[1]].setContentAreaFilled(true);
                    T[c[0]][c[1]].setBackground(Color.green);
                }
                return (t0.charAt(0) == 'X') ? 1 : 2;
            }
        }
        return 0;
    }

    public void setJeuTermine(boolean jeuTermine) { this.jeuTermine = jeuTermine; }
    public void setXO(char XO) { this.XO = XO; }
    public void setReseau(Reseau reseau) { this.reseau = reseau; }
    public Reseau getReseau() { return reseau; }
}
