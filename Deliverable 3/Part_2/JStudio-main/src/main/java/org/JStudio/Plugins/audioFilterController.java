package org.JStudio.Plugins;


import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class audioFilterController {
    private final Map<MenuButton, String> filterType = new HashMap<>();
    public String inputFile = "src/main/resources/test_inputs_music.wav";
    public String outputFile = "filtered_output.wav"; //todo create a field for the user to enter the output file name
    @FXML
    private Label cutOffLabel;
    @FXML
    private TextField fieldFrequency;
    @FXML
    private MenuButton optionsCutOff;
    @FXML
    private Button saveBtn, playBtn;
    private float userFrequency;
    private String selectedFilter;
    private Clip audioClip;
    private boolean isPlaying = false;


    // Convert 16-bit PCM byte array to short array
    private static short[] bytesToShorts(byte[] audioBytes) {
        short[] samples = new short[audioBytes.length / 2];
        for (int i = 0; i < samples.length; i++) {
            samples[i] = (short) ((audioBytes[2 * i + 1] << 8) | (audioBytes[2 * i] & 0xFF));
        }
        return samples;
    }

    // Convert short array back to 16-bit PCM byte array
    private static byte[] shortsToBytes(short[] samples) {
        byte[] audioBytes = new byte[samples.length * 2];
        for (int i = 0; i < samples.length; i++) {
            audioBytes[2 * i] = (byte) (samples[i] & 0xFF);
            audioBytes[2 * i + 1] = (byte) ((samples[i] >> 8) & 0xFF);
        }
        return audioBytes;
    }

    // Save byte[] to WAV file
    private static void saveWavFile(String filename, byte[] audioBytes, AudioFormat format) throws IOException {
        File file = new File(filename);
        ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
        AudioInputStream audioStream = new AudioInputStream(bais, format, audioBytes.length / format.getFrameSize());
        AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, file);
    }

    // Play audio from byte[]
    private static void playAudio(byte[] audioBytes, AudioFormat format) throws LineUnavailableException {
        SourceDataLine line = AudioSystem.getSourceDataLine(format);
        line.open(format);
        line.start();
        line.write(audioBytes, 0, audioBytes.length);
        line.drain();
        line.close();
    }

    // low-pass filter implementation: frequency higher than given frequency is cut off
    private static void applyLowPassFilter(short[] samples, float sampleRate, float cutoffFreq) {
        double RC = 1.0 / (2 * Math.PI * cutoffFreq);
        double dt = 1.0 / sampleRate;
        double alpha = dt / (RC + dt);
        double prevSample = 0;
        for (int i = 0; i < samples.length; i++) {
            double filtered = alpha * samples[i] + (1 - alpha) * prevSample;
            prevSample = filtered;
            samples[i] = (short) filtered;
        }
    }

    //  high-pass filter implementationL: frequency lower than given is cut off
    private static void applyHighPassFilter(short[] samples, float sampleRate, float cutoffFreq) {
        double RC = 1.0 / (2 * Math.PI * cutoffFreq);
        double dt = 1.0 / sampleRate;
        double alpha = RC / (RC + dt);
        double prevSample = 0;
        double prevFiltered = 0;
        for (int i = 0; i < samples.length; i++) {
            double newSample = alpha * (prevFiltered + samples[i] - prevSample);
            prevSample = samples[i];
            prevFiltered = newSample;
            samples[i] = (short) newSample;
        }
    }

    @FXML
    public void initialize() {
        setupMenu(optionsCutOff);
        setDefaultMenuSelection(optionsCutOff);

        playBtn.setOnAction(event -> {
            try {
                getText();
                File file = new File(inputFile);
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
                AudioFormat format = audioStream.getFormat();
                byte[] audioBytes = audioStream.readAllBytes();
                audioStream.close();

                short[] samples = bytesToShorts(audioBytes);
                float sampleRate = format.getSampleRate();

                if ("Low".equals(selectedFilter)) {
                    applyLowPassFilter(samples, sampleRate, userFrequency);
                } else if ("High".equals(selectedFilter)) {
                    applyHighPassFilter(samples, sampleRate, userFrequency);
                }

                byte[] filteredBytes = shortsToBytes(samples);

                Task<Void> playTask = new Task<>() {
                    // https://stackoverflow.com/questions/24924414/javafx-task-ending-and-javafx-threading
                    @Override
                    protected Void call() throws Exception {
                        if (isPlaying) {
                            // If playing, stop audio playback
                            audioClip.stop();
                            isPlaying = false;
                        } else {
                            // Start playing the filtered audio in background
                            playAudio(filteredBytes, format);
                            isPlaying = true;
                        }
                        return null;
                    }
                };
                new Thread(playTask).start();


                saveBtn.setOnAction(event1 -> {
                    try {
                        saveWavFile(outputFile, filteredBytes, format);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    private void setupMenu(MenuButton menuButton) {
        for (MenuItem item : menuButton.getItems()) {
            item.setOnAction(event -> {
                menuButton.setText(item.getText());
                filterType.put(menuButton, item.getText());
                selectedFilter = item.getText();
            });
        }
    }

    private void setDefaultMenuSelection(MenuButton menuButton) {
        for (MenuItem item : menuButton.getItems()) {
            if (item.getText().equals("Low")) {
                menuButton.setText(item.getText());
                selectedFilter = item.getText();
                break;
            }
        }
    }

    public void getText() {
        // https://www.squash.io/how-to-use-a-regex-to-only-accept-numbers-0-9/
        if (fieldFrequency.getText().matches("^[0-9]+$")) {
            userFrequency = (float) Double.parseDouble(fieldFrequency.getText());
        }
    }

}