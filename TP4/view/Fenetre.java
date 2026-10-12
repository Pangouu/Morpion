package TP4.view;

import TP4.control.Actions;
import TP4.model.Joueurs;

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
    private Score[] lesScores;
    private JButton reset;
    private JPanel panneauJoueur;
    private JLabel labelJoueur;
    private char monRole = '?';
    private java.awt.Image logo;

    public Fenetre(int W, int H, int nbJoueurs) {
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

        laGrille = new Grille(this, nbJoueurs);
        this.add(laGrille, BorderLayout.CENTER);

        // scores : joueurs pairs a gauche (rouge, vert), impairs a droite (jaune, bleu)
        lesScores = new Score[nbJoueurs];
        JPanel gauche = new JPanel(new GridLayout(nbJoueurs / 2, 1));
        JPanel droite = new JPanel(new GridLayout(nbJoueurs / 2, 1));
        gauche.setOpaque(false);
        droite.setOpaque(false);
        for (int k = 0; k < nbJoueurs; k++) {
            lesScores[k] = new Score(Joueurs.COULEURS[k]);
            (k % 2 == 0 ? gauche : droite).add(lesScores[k]);
        }
        this.add(gauche, BorderLayout.WEST);
        this.add(droite, BorderLayout.EAST);

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

    /** Colorie le bandeau nord selon le joueur qui doit jouer (couleur de son symbole). */
    public void afficherJoueur(char xo) {
        String qui = Joueurs.nom(xo);
        panneauJoueur.setBackground(Joueurs.COULEURS[Joueurs.indice(xo)]);
        if (monRole == '?') labelJoueur.setText("JOUEUR " + qui + ", JOUEZ");
        else if (monRole == xo) labelJoueur.setText("A VOUS DE JOUER (" + qui + ")");
        else labelJoueur.setText("TOUR DE L'ADVERSAIRE (" + qui + ")");
    }

    public void afficherMessage(String message) {
        panneauJoueur.setBackground(Color.lightGray);
        labelJoueur.setText(message);
    }

    /** Role en ligne : symbole du joueur (voir Joueurs), '?' = partie locale. */
    public void setMonRole(char role) { this.monRole = role; }

    public void afficherFinDePartie() {
        panneauJoueur.setBackground(Color.lightGray);
        labelJoueur.setText("PARTIE TERMINEE");
    }

    public Grille getLaGrille() { return laGrille; }
    /** Score du joueur d'indice k (voir Joueurs). */
    public Score getScore(int k) { return lesScores[k]; }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        Actions.action1(laGrille);
    }
}
