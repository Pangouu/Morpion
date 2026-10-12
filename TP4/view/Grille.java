package TP4.view;

import TP4.control.EcouteurBouton;

import javax.swing.*;
import java.awt.*;

public class Grille extends JPanel {
    private JButton[][] lesBoutons;
    private EcouteurBouton monEcouteur;
    private Fenetre saFenetre;

    public Grille(Fenetre f) {
        saFenetre = f;
        this.lesBoutons = new JButton[3][3];
        this.setOpaque(false);
        this.setLayout(new GridLayout(3, 3));
        monEcouteur = new EcouteurBouton(this);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
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
}
