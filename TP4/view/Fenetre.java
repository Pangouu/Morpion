package TP4.view;

import TP4.control.Actions;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Fenetre extends JFrame implements ActionListener {
    private int largeur;
    private int hauteur;
    private Grille laGrille;
    private Score leScoreRouge;
    private Score leScoreJaune;
    private JButton reset;
    private JPanel panneauJoueur;
    private JLabel labelJoueur;
    private char monRole = '?';
    private java.awt.Image logo;

    public Fenetre(int W, int H) {
        this.largeur = W;
        this.hauteur = H;
        setSize(W, H);
        setTitle("FGES Morpion");

        Dimension laDimension = getToolkit().getScreenSize();
        setLocation((int) (laDimension.getWidth() - largeur) / 2, (int) (laDimension.getHeight() - hauteur) / 2);

        try {
            logo = javax.imageio.ImageIO.read(new java.io.File("barca.png"));
        } catch (Exception e) {
            System.out.println("Logo introuvable : " + e.getMessage());
        }

        // fond du Barca : rayures blaugrana (bleu / grenat)
        setContentPane(new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int w = Math.max(getWidth() / 10, 1);
                for (int x = 0, i = 0; x < getWidth(); x += w, i++) {
                    g.setColor(i % 2 == 0 ? new Color(0x004D98) : new Color(0xA50044));
                    g.fillRect(x, 0, w, getHeight());
                }
                if (logo != null) { // logo centre, proportions conservees
                    int taille = Math.min(getWidth(), getHeight());
                    int lw = logo.getWidth(null), lh = logo.getHeight(null);
                    double r = Math.min((double) taille / lw, (double) taille / lh);
                    int dw = (int) (lw * r), dh = (int) (lh * r);
                    g.drawImage(logo, (getWidth() - dw) / 2, (getHeight() - dh) / 2, dw, dh, null);
                }
            }
        });
        setLayout(new BorderLayout());

        laGrille = new Grille(this);
        this.add(laGrille, BorderLayout.CENTER);

        leScoreRouge = new Score(1);
        leScoreJaune = new Score(2);
        this.add(leScoreRouge, BorderLayout.WEST);
        this.add(leScoreJaune, BorderLayout.EAST);

        reset = new JButton("RESET");
        reset.addActionListener(this);
        this.add(reset, BorderLayout.SOUTH);

        // etape 17 : indicateur du joueur courant
        panneauJoueur = new JPanel();
        labelJoueur = new JLabel();
        panneauJoueur.add(labelJoueur);
        this.add(panneauJoueur, BorderLayout.NORTH);
        afficherJoueur('X');

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Actions.action2();
            }
        });
    }

    /** Colorie le bandeau nord selon le joueur qui doit jouer ('X' = rouge, 'O' = jaune). */
    public void afficherJoueur(char xo) {
        String qui = (xo == 'X') ? "ROUGE" : "JAUNE";
        panneauJoueur.setBackground(xo == 'X' ? Color.red : Color.yellow);
        if (monRole == '?') labelJoueur.setText("JOUEUR " + qui + ", JOUEZ");
        else if (monRole == xo) labelJoueur.setText("A VOUS DE JOUER (" + qui + ")");
        else labelJoueur.setText("TOUR DE L'ADVERSAIRE (" + qui + ")");
    }

    public void afficherMessage(String message) {
        panneauJoueur.setBackground(Color.lightGray);
        labelJoueur.setText(message);
    }

    /** Role en ligne : 'X' (rouge), 'O' (jaune), '?' = partie locale. */
    public void setMonRole(char role) { this.monRole = role; }

    public void afficherFinDePartie() {
        panneauJoueur.setBackground(Color.lightGray);
        labelJoueur.setText("PARTIE TERMINEE");
    }

    public Grille getLaGrille() { return laGrille; }
    public Score getScoreRouge() { return leScoreRouge; }
    public Score getScoreJaune() { return leScoreJaune; }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        Actions.action1(laGrille);
    }
}
