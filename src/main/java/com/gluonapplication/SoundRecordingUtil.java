package com.gluonapplication;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class SoundRecordingUtil {
	private static final int BUFFER_SIZE = 4096;
	private ByteArrayOutputStream recordBytes;


	private AudioFormat format;

	private TargetDataLine audioLine;

	private boolean isRunning;

	AudioFormat getAudioFormat() {
		float sampleRate = 16000;
		int sampleSizeInBits = 8;
		int channels = 2;
		return new AudioFormat(sampleRate, sampleSizeInBits, channels, true, true);
	}

	public void start() throws LineUnavailableException, IOException {
		format = getAudioFormat();
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

		if (!AudioSystem.isLineSupported(info)) {
			throw new LineUnavailableException("The system does not support the specified format.");}

		audioLine = (TargetDataLine) AudioSystem.getLine(info);
		audioLine.open(format);
		audioLine.start();
		byte[] buffer = new byte[BUFFER_SIZE];
		int bytesRead;
		recordBytes = new ByteArrayOutputStream();
		isRunning = true;
		while (isRunning) {
			bytesRead = audioLine.read(buffer, 0, buffer.length);
			recordBytes.write(buffer, 0, bytesRead);
		}
	}

	public void stop() throws IOException {
		isRunning = false;
		
		if (audioLine != null) {
			audioLine.drain();
			audioLine.close();
		}
	}
	public void save(File wavFile) throws IOException {
		byte[] audioData = recordBytes.toByteArray();
		ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
		AudioInputStream audioInputStream = new AudioInputStream(bais, format,
				audioData.length / format.getFrameSize());
		AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, wavFile);
		audioInputStream.close();
		recordBytes.close();
	}
}