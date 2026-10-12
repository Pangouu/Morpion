package TP4.view;

import javax.swing.*;
import java.awt.*;

public class Score extends JPanel {
    private int valeur;
    private JLabel leScore;
    public static Font mafaconDecrire = new Font("TimesRoman", Font.BOLD, 60);

    public Score(Color couleur) {
        leScore = new JLabel("" + valeur);
        leScore.setForeground(couleur);
        leScore.setFont(mafaconDecrire);
        this.setOpaque(false);
        this.add(leScore);
    }

    public int getValeur() { return valeur; }
    public void setValeur(int valeur) { this.valeur = valeur; }
    public JLabel getLeScore() { return leScore; }
}
