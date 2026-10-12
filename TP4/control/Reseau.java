package TP4.control;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Connexion TCP entre 2 joueurs. L'hote joue les rouges (X), l'invite les jaunes (O).
 * Messages echanges (une ligne chacun) : "MOVE i j" et "RESET".
 */
public class Reseau {
    public interface Recepteur {
        void recu(String message);
        void deconnecte();
    }

    private char role;
    protected final Recepteur recepteur;
    private Socket socket;
    private PrintWriter out;
    private volatile boolean connecte = false;

    public Reseau(char role, Recepteur recepteur) {
        this.role = role;
        this.recepteur = recepteur;
    }

    public char getRole() { return role; }
    protected void setRole(char role) { this.role = role; }
    public boolean isConnecte() { return connecte; }

    /** Attend un adversaire en arriere-plan, puis appelle surConnexion (dans l'EDT). */
    public void heberger(int port, Runnable surConnexion, Runnable surErreur) {
        Thread t = new Thread(() -> {
            try (ServerSocket serveur = new ServerSocket(port)) {
                ouvrir(serveur.accept());
                SwingUtilities.invokeLater(surConnexion);
            } catch (IOException e) {
                System.out.println("Hebergement impossible : " + e.getMessage());
                SwingUtilities.invokeLater(surErreur);
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public void rejoindre(String hote, int port) throws IOException {
        ouvrir(new Socket(hote, port));
    }

    private void ouvrir(Socket s) throws IOException {
        socket = s;
        out = new PrintWriter(new OutputStreamWriter(s.getOutputStream()), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        connecte = true;
        Thread lecteur = new Thread(() -> {
            try {
                String ligne;
                while ((ligne = in.readLine()) != null) {
                    final String m = ligne;
                    SwingUtilities.invokeLater(() -> recepteur.recu(m));
                }
            } catch (IOException e) {
                // connexion coupee
            }
            connecte = false;
            SwingUtilities.invokeLater(recepteur::deconnecte);
        });
        lecteur.setDaemon(true);
        lecteur.start();
    }

    public void envoyer(String message) {
        if (connecte && out != null) out.println(message);
    }
}
