package TP4.view;

import TP4.control.EcouteurBouton;

import javax.swing.*;
import java.awt.*;

public class Grille extends JPanel {
    private JButton[][] lesBoutons;
    private EcouteurBouton monEcouteur;
    private Fenetre saFenetre;
    private int nbJoueurs;
    private int taille;   // la grille fait taille x taille
    private int aligner;  // nombre de symboles a aligner pour gagner
    private Font police;

    /** 2 joueurs : 3x3, 3 alignes. 4 joueurs : 7x7, 4 alignes. */
    public Grille(Fenetre f, int nbJoueurs) {
        saFenetre = f;
        this.nbJoueurs = nbJoueurs;
        this.taille = (nbJoueurs == 2) ? 3 : 7;
        this.aligner = (nbJoueurs == 2) ? 3 : 4;
        this.police = (nbJoueurs == 2) ? Score.mafaconDecrire : new Font("TimesRoman", Font.BOLD, 36);
        this.lesBoutons = new JButton[taille][taille];
        this.setOpaque(false);
        this.setLayout(new GridLayout(taille, taille));
        monEcouteur = new EcouteurBouton(this);
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                this.lesBoutons[i][j] = new JButton();
                this.lesBoutons[i][j].setContentAreaFilled(false);
                this.add(this.lesBoutons[i][j]);
                this.lesBoutons[i][j].addActionListener(monEcouteur);
            }
        }
    }

    public JButton[][] getLesBoutons() { return lesBoutons; }
    public EcouteurBouton getMonEcouteur() { return monEcouteur; }
    public Fenetre getSaFenetre() { return saFenetre; }
    public int getNbJoueurs() { return nbJoueurs; }
    public int getTaille() { return taille; }
    public int getAligner() { return aligner; }
    public Font getPolice() { return police; }
}
