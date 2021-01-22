package com.gluonapplication;

import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.Icon;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;


public class BasicView extends View {
    private final SoundRecordingUtil recorder = new SoundRecordingUtil();
    private final AudioPlayer player = new AudioPlayer();
    private Thread playbackThread;
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    String pathSaveRecord = String.format("src/main/resources/records/%s.wav", timestamp);

    boolean isRecording = false;
    boolean isPlaying = false;
    boolean savedRecords = false;
    public int i  = 0;

    Button buttonRecord = new Button("Record");
    Button buttonPlay = new Button("Play");
    Button buttonStop = new Button("Stop");
    Label recordLabel = new Label();

    public BasicView() {

        ImageView covidImage = new ImageView(new Image(BasicView.class.getResourceAsStream("/icon.png")));
        covidImage.setFitHeight(120);
        covidImage.setFitWidth(120);

        Label titleLabel = new Label("Sound Recorder");
        titleLabel.setStyle("-fx-font-weight:bold; -fx-font-family: Courier; -fx-font-size: 20");

        recordLabel.setStyle("-fx-font-size: 25");

        buttonRecord.setGraphic(new Icon(MaterialDesignIcon.MIC));
        buttonRecord.setStyle("-fx-font-weight:bold; -fx-background-radius:15; -fx-background-color: #FF6347");

        buttonPlay.setGraphic(new Icon(MaterialDesignIcon.PLAY_ARROW));
        buttonPlay.setStyle("-fx-font-weight:bold; -fx-background-radius:15; -fx-background-color: #6495ED");

        buttonStop.setGraphic(new Icon(MaterialDesignIcon.STOP));
        buttonStop.setStyle("-fx-font-weight:bold; -fx-background-radius:15; -fx-background-color: #20B2AA");

        buttonStop.setOnMouseClicked(e -> {
            if (isRecording) {
                stopRecording();
            }
            if (isPlaying) {
                stopPlaying();
            }
        });

        buttonRecord.setOnMouseClicked(e -> {
            if (!isRecording) {
                startRecording();
            }
        });

        buttonPlay.setOnMouseClicked(e -> {
            if (!isPlaying && savedRecords) {
                playBack();
            }
        });

        VBox controls = new VBox(20, covidImage, titleLabel, buttonRecord, buttonStop, buttonPlay, recordLabel);
        controls.setAlignment(Pos.CENTER);

        setCenter(controls);
    }

    private void startRecording() {
        Thread recordThread = new Thread(() -> {
            try {
                isRecording = true;
                buttonRecord.setDisable(true);
                buttonPlay.setDisable(true);
                recorder.start();
            } catch (LineUnavailableException | IOException e) {
                e.printStackTrace();
            }
        });
        recordThread.start();
    }

    private void stopRecording() {
        i = 0;
        isRecording = false;
        try {
            buttonRecord.setDisable(false);
            buttonPlay.setDisable(false);
            recorder.stop();
            saveFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopPlaying() {
        player.stop();
        playbackThread.interrupt();
    }

    private void playBack() {

        List<File> results = player.getAllFilesFromResource();
        isPlaying = true;
        playbackThread = new Thread(() -> {
            try {
                buttonPlay.setDisable(true);
                buttonRecord.setDisable(true);
                player.play(results);
                buttonPlay.setDisable(false);
                buttonRecord.setDisable(false);
                isPlaying = false;

            } catch (UnsupportedAudioFileException | LineUnavailableException | IOException ex) {
                ex.printStackTrace();
            }
        });

        playbackThread.start();
    }

    @Override
    protected void updateAppBar(AppBar appBar) {
        appBar.setTitleText("Sound Recorder");
    }

    private void saveFile() throws IOException {
        File wavFile = new File(pathSaveRecord);
        recorder.save(wavFile);
        savedRecords = true;
    }
}
