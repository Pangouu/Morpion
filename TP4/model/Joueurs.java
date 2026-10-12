package TP4.model;

import java.awt.*;

/** Les 4 joueurs possibles : symbole, nom et couleur. Une partie a 2 utilise les 2 premiers. */
public class Joueurs {
    public static final char[] SYMBOLES = {'X', 'O', '△', '□'}; // X O triangle carre
    public static final String[] NOMS = {"ROUGE", "JAUNE", "VERT", "BLEU"};
    public static final Color[] COULEURS = {Color.red, Color.yellow, Color.green, new Color(0x00CFFF)};

    /** Indice du joueur (0 a 3) a partir de son symbole, -1 si inconnu. */
    public static int indice(char symbole) {
        for (int k = 0; k < SYMBOLES.length; k++)
            if (SYMBOLES[k] == symbole) return k;
        return -1;
    }

    /** Symbole du joueur qui joue apres celui-ci. */
    public static char suivant(char symbole, int nbJoueurs) {
        return SYMBOLES[(indice(symbole) + 1) % nbJoueurs];
    }

    public static String nom(char symbole) {
        return NOMS[indice(symbole)];
    }
}
