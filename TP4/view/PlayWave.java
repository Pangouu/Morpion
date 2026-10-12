package TP4.view;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

/** Joue un fichier wav dans un Thread separe (le jeu n'attend pas la fin du son). */
public class PlayWave extends Thread {
    private String wavfile;

    public PlayWave(String wavfile) {
        this.wavfile = wavfile;
    }

    @Override
    public void run() {
        try {
            File f = new File(wavfile);
            if (!f.exists()) return; // pas de son si le fichier est absent
            AudioInputStream in = AudioSystem.getAudioInputStream(f);
            Clip clip = AudioSystem.getClip();
            clip.open(in);
            clip.start();
            Thread.sleep(clip.getMicrosecondLength() / 1000 + 100);
            clip.close();
        } catch (Exception e) {
            System.out.println("Son impossible : " + e.getMessage());
        }
    }
}
