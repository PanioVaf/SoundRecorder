package com.gluonapplication;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public class AudioPlayer implements LineListener {

    boolean playCompleted;

    boolean isStopped;

    List<File> getAllFilesFromResource()
    {
        List<File> collect = new ArrayList<>();
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            URL resource = classLoader.getResource("records");
            assert resource != null;
            collect = Files.walk(Paths.get(resource.toURI()))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.toList());
        } catch (URISyntaxException | IOException | NullPointerException e) {
            e.printStackTrace();
        }
       
        return collect;
    }

    void play(List<File> results) throws UnsupportedAudioFileException,
            IOException, LineUnavailableException{
        File audioFile = new File(results.get(0).toURI());
        AudioInputStream audioStream = AudioSystem
                .getAudioInputStream(audioFile);
        AudioFormat format = audioStream.getFormat();
        DataLine.Info info = new DataLine.Info(Clip.class, format);
        Clip audioClip = (Clip) AudioSystem.getLine(info);
        audioClip.addLineListener(this);
        audioClip.open(audioStream);
        audioClip.start();
        playCompleted = false;

        while (!playCompleted) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                if (isStopped) {
                    audioClip.stop();
                    break;
                }
            }
        }

        audioClip.close();
    }


    public void stop() {
        isStopped = true;
    }

    @Override
    public void update(LineEvent event) {
        LineEvent.Type type = event.getType();
        if (type == LineEvent.Type.STOP) {
            playCompleted = true;
        }
    }
}