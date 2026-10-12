package TP4.control;

import TP4.model.Joueurs;

import javax.swing.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.IntConsumer;

/**
 * Partie en ligne via un salon (code a 6 caracteres) relaye par le service public ntfy.sh :
 * aucune redirection de port ni IP a donner, ca marche derriere n'importe quelle box.
 * Chaque message envoye est "idClient|message" ; on ignore ses propres messages.
 * L'hote (X) accueille les invites un par un : HELLO -> "WELCOME idInvite indiceRole nbJoueurs"
 * (ou "FULL idInvite" si le salon est complet), puis "START" quand tout le monde est la.
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
    private volatile boolean connecte = false; // la partie a commence
    private volatile boolean enSalon = false;  // au moins un autre joueur est la
    private volatile boolean arret = false;
    private volatile boolean complet = false;
    private volatile boolean demarre = false;
    private volatile int nbJoueurs;
    private final Map<String, Integer> invites = new HashMap<>(); // hote : id invite -> indice joueur
    private Runnable surConnexion;
    private IntConsumer surArrivee;
    private volatile Runnable surDemarrage;

    /** Hote : role 'X' et nombre de joueurs. Invite : role '?' et 0, l'hote les donne a l'arrivee. */
    public ReseauWeb(char role, Recepteur recepteur, String code, int nbJoueurs) {
        super(role, recepteur);
        this.code = code;
        this.topic = "morpion-fges-" + code;
        this.nbJoueurs = nbJoueurs;
    }

    public static String nouveauCode() {
        SecureRandom r = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) sb.append(ALPHABET.charAt(r.nextInt(ALPHABET.length())));
        return sb.toString();
    }

    public String getCode() { return code; }
    public int getNbJoueurs() { return nbJoueurs; }

    @Override
    public boolean isConnecte() { return connecte; }

    /**
     * Cree le salon et attend les invites (dans l'EDT : surArrivee recoit le nombre de joueurs
     * presents a chaque arrivee, surConnexion est appele quand le salon est complet).
     */
    public void creerSalon(Runnable surConnexion, IntConsumer surArrivee) {
        this.surConnexion = surConnexion;
        this.surArrivee = surArrivee;
        ecouter();
    }

    /** Rejoint un salon ; renvoie false si personne ne repond ou s'il est complet. */
    public boolean rejoindreSalon() throws Exception {
        ecouter();
        if (!abonne.await(8, TimeUnit.SECONDS)) return false;
        for (int essai = 0; essai < 3; essai++) {
            publier("HELLO");
            if (accueilli.await(3, TimeUnit.SECONDS) && !complet) return true;
            if (complet) break;
        }
        arret = true;
        return false;
    }

    /** Invite : action (dans l'EDT) quand l'hote lance la partie. */
    public void setSurDemarrage(Runnable surDemarrage) {
        this.surDemarrage = surDemarrage;
        if (demarre) SwingUtilities.invokeLater(surDemarrage);
    }

    private void ecouter() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (enSalon) {
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
        String expediteur = ligne.substring(0, k);
        if (expediteur.equals(id)) return; // mon propre message
        String msg = ligne.substring(k + 1);
        String[] p = msg.split(" ");
        switch (p[0]) {
            case "HELLO":
                if (getRole() == 'X') accueillir(expediteur);
                break;
            case "WELCOME":
                if (getRole() == '?' && p.length == 4 && p[1].equals(id)) {
                    setRole(Joueurs.SYMBOLES[Integer.parseInt(p[2])]);
                    nbJoueurs = Integer.parseInt(p[3]);
                    enSalon = true;
                    connecte = true;
                    accueilli.countDown();
                }
                break;
            case "FULL":
                if (getRole() == '?' && p.length == 2 && p[1].equals(id)) {
                    complet = true;
                    accueilli.countDown();
                }
                break;
            case "START":
                if (getRole() != 'X' && connecte && !demarre) {
                    demarre = true;
                    SwingUtilities.invokeLater(() -> {
                        if (surDemarrage != null) surDemarrage.run();
                    });
                }
                break;
            case "BYE":
                if (enSalon) {
                    enSalon = false;
                    connecte = false;
                    SwingUtilities.invokeLater(recepteur::deconnecte);
                }
                break;
            default:
                if (connecte) SwingUtilities.invokeLater(() -> recepteur.recu(msg));
        }
    }

    /** Hote : donne un role a l'invite, et lance la partie quand le salon est complet. */
    private void accueillir(String invite) {
        Integer indice = invites.get(invite);
        if (indice == null) {
            if (invites.size() >= nbJoueurs - 1) {
                publier("FULL " + invite);
                return;
            }
            indice = invites.size() + 1;
            invites.put(invite, indice);
            enSalon = true;
        }
        int presents = invites.size() + 1;
        try {
            // envois synchrones : WELCOME doit arriver avant START
            publierSync("WELCOME " + invite + " " + indice + " " + nbJoueurs);
            if (presents == nbJoueurs && !connecte) {
                publierSync("START");
                connecte = true;
                SwingUtilities.invokeLater(surConnexion);
            } else if (presents < nbJoueurs && surArrivee != null) {
                SwingUtilities.invokeLater(() -> surArrivee.accept(presents));
            }
        } catch (Exception e) {
            System.out.println("Envoi impossible : " + e.getMessage());
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
