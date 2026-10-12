package TP4.control;

import javax.swing.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Partie en ligne via un salon (code a 6 caracteres) relaye par le service public ntfy.sh :
 * aucune redirection de port ni IP a donner, ca marche derriere n'importe quelle box.
 * Chaque message envoye est "idClient|message" ; on ignore ses propres messages.
 */
public class ReseauWeb extends Reseau {
    private static final String BASE = "https://ntfy.sh/";
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final String code;
    private final String topic;
    private final String id = Long.toString(System.nanoTime(), 36);
    private final HttpClient client = HttpClient.newHttpClient();
    private final CountDownLatch abonne = new CountDownLatch(1);
    private final CountDownLatch accueilli = new CountDownLatch(1);
    private volatile boolean connecte = false;
    private volatile boolean arret = false;
    private Runnable surConnexion;

    public ReseauWeb(char role, Recepteur recepteur, String code) {
        super(role, recepteur);
        this.code = code;
        this.topic = "morpion-fges-" + code;
    }

    public static String nouveauCode() {
        SecureRandom r = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) sb.append(ALPHABET.charAt(r.nextInt(ALPHABET.length())));
        return sb.toString();
    }

    public String getCode() { return code; }

    @Override
    public boolean isConnecte() { return connecte; }

    /** Cree le salon et attend l'invite (surConnexion est appele dans l'EDT). */
    public void creerSalon(Runnable surConnexion) {
        this.surConnexion = surConnexion;
        ecouter();
    }

    /** Rejoint un salon ; renvoie false si personne ne repond. */
    public boolean rejoindreSalon() throws Exception {
        ecouter();
        if (!abonne.await(8, TimeUnit.SECONDS)) return false;
        for (int essai = 0; essai < 3; essai++) {
            publier("HELLO");
            if (accueilli.await(3, TimeUnit.SECONDS)) return true;
        }
        arret = true;
        return false;
    }

    private void ecouter() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (connecte) {
                try { publierSync("BYE"); } catch (Exception e) { /* tant pis */ }
            }
        }));
        Thread t = new Thread(() -> {
            while (!arret) {
                try {
                    HttpRequest req = HttpRequest.newBuilder(URI.create(BASE + topic + "/raw")).GET().build();
                    HttpResponse<java.util.stream.Stream<String>> rep = client.send(req, HttpResponse.BodyHandlers.ofLines());
                    abonne.countDown();
                    rep.body().forEach(this::traiter);
                } catch (Exception e) {
                    if (arret) return;
                    try { Thread.sleep(1500); } catch (InterruptedException ie) { return; }
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void traiter(String ligne) {
        int k = ligne.indexOf('|');
        if (k < 0) return;
        if (ligne.substring(0, k).equals(id)) return; // mon propre message
        String msg = ligne.substring(k + 1);
        switch (msg) {
            case "HELLO":
                if (getRole() == 'X' && !connecte) {
                    connecte = true;
                    publier("WELCOME");
                    SwingUtilities.invokeLater(surConnexion);
                }
                break;
            case "WELCOME":
                if (getRole() == 'O' && !connecte) {
                    connecte = true;
                    accueilli.countDown();
                }
                break;
            case "BYE":
                if (connecte) {
                    connecte = false;
                    SwingUtilities.invokeLater(recepteur::deconnecte);
                }
                break;
            default:
                if (connecte) SwingUtilities.invokeLater(() -> recepteur.recu(msg));
        }
    }

    private HttpRequest requete(String msg) {
        return HttpRequest.newBuilder(URI.create(BASE + topic))
                .POST(HttpRequest.BodyPublishers.ofString(id + "|" + msg)).build();
    }

    private void publier(String msg) {
        client.sendAsync(requete(msg), HttpResponse.BodyHandlers.discarding());
    }

    private void publierSync(String msg) throws Exception {
        client.send(requete(msg), HttpResponse.BodyHandlers.discarding());
    }

    @Override
    public void envoyer(String message) {
        if (connecte) publier(message);
    }
}
