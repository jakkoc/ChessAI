package chess;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

public class SoundEffect {
    Clip clip;

    public void setFile(String soundFileName) throws Exception {
        File file = new File(soundFileName);
        AudioInputStream sound = AudioSystem.getAudioInputStream(file);
        clip = AudioSystem.getClip();
        clip.open(sound);
    }

    public void play() {
        clip.setFramePosition(0);
        clip.start();
    }
}

