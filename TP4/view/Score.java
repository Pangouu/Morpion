package TP4.view;

import javax.swing.*;
import java.awt.*;

public class Score extends JPanel {
    private int valeur;
    private JLabel leScore;
    public static Font mafaconDecrire = new Font("TimesRoman", Font.BOLD, 60);

    public Score(int couleur) {
        leScore = new JLabel("" + valeur);
        if (couleur == 1) leScore.setForeground(Color.red);
        else leScore.setForeground(Color.yellow);
        leScore.setFont(mafaconDecrire);
        this.setOpaque(false);
        this.add(leScore);
    }

    public int getValeur() { return valeur; }
    public void setValeur(int valeur) { this.valeur = valeur; }
    public JLabel getLeScore() { return leScore; }
}
