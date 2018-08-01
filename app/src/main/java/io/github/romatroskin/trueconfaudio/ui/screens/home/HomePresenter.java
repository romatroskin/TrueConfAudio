package io.github.romatroskin.trueconfaudio.ui.screens.home;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.inject.Inject;

public class HomePresenter implements HomeScreen.Presenter {
    private static final int RECORD_TICK = 1;
    private static final int RECORD_COMPLETE = 2;

    private static final int DEFAULT_BUFFER_SIZE = 2048;
    private static final int DEFAULT_SAMPLE_RATE = 44100;
    private static final int CHANNEL_IN = AudioFormat.CHANNEL_IN_MONO;
    private static final int CHANNEL_OUT = AudioFormat.CHANNEL_OUT_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int MIN_BUFFER_SIZE = AudioRecord.getMinBufferSize(
            DEFAULT_SAMPLE_RATE, CHANNEL_IN, AUDIO_FORMAT
    );

    private final HomeScreen.View view;

    private AudioTrack audioTrack;
    private AudioRecord audioRecord;

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RECORD_TICK:
                    view.setTime((int) msg.obj);
                    break;
                case RECORD_COMPLETE:
                    audioRecord.stop();
                    view.onRecordComplete();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    @Inject
    HomePresenter(HomeView view) {
        this.view = view;
    }

    @Override
    public void onLoad() {
        if(MIN_BUFFER_SIZE != AudioRecord.ERROR_BAD_VALUE && MIN_BUFFER_SIZE != AudioRecord.ERROR) {
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, DEFAULT_SAMPLE_RATE, CHANNEL_IN,
                    AUDIO_FORMAT, Math.max(MIN_BUFFER_SIZE, DEFAULT_BUFFER_SIZE));

            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, DEFAULT_SAMPLE_RATE,
                    CHANNEL_OUT, AUDIO_FORMAT, Math.max(MIN_BUFFER_SIZE, DEFAULT_BUFFER_SIZE),
                    AudioTrack.MODE_STREAM);
        }
    }

    @Override
    public void onSave() {
        if(audioRecord != null) {
            if(audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord.stop();
            }

            if(audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                audioRecord.release();
            }

            audioRecord = null;
        }

        if(audioTrack != null) {
            if(audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                audioTrack.stop();
            }

            if(audioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
                audioTrack.release();
            }

            audioTrack = null;
        }
    }

    @Override
    public void record(String filename) throws FileNotFoundException {
        final DataOutputStream outputStream = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(filename))
        );

        if(audioRecord != null && audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
            audioRecord.startRecording();
            Thread recordingThread = new Thread(() -> {
                short[] tempBuf = new short[MIN_BUFFER_SIZE / 2];
                while (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    audioRecord.read(tempBuf, 0, tempBuf.length);

                    try {
                        for (short shortSample : tempBuf) {
                            outputStream.writeByte(shortSample & 0xFF);
                            outputStream.writeByte((shortSample >> 8) & 0xFF);
                        }

                        int seconds = outputStream.size() / (2 * DEFAULT_SAMPLE_RATE);
                        if (seconds < 10) {
                            Message tickMessage = handler.obtainMessage(RECORD_TICK, seconds);
                            tickMessage.sendToTarget();
                        } else {
                            Message completeMessage = handler.obtainMessage(RECORD_COMPLETE);
                            completeMessage.sendToTarget();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            recordingThread.start();
        }
    }

    @Override
    public void play(String filename) throws FileNotFoundException {
//        final RandomAccessFile randomAccessFile = new RandomAccessFile(filename, "r");
        final File pcmFile = new File(filename);
        final DataInputStream inputStream = new DataInputStream(
                new BufferedInputStream(new FileInputStream(pcmFile))
        );

        if(audioTrack != null && audioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
            audioTrack.play();
            Thread playingThread = new Thread(() -> {
                try {
//                    short[] tmpBuf = new short[MIN_BUFFER_SIZE / 2];
//                    long lastPosition = (randomAccessFile.length() - 2);
//                    for(long i = lastPosition; i >= 0; i-=2) {
//                        randomAccessFile.seek(i);
//                        short sample = randomAccessFile.readShort();
//                        audioTrack.write(new short[]{sample}, 0, 1);
//                    }
                    byte[] pcmData = new byte[(int) pcmFile.length()];
                    int bytesRead = inputStream.read(pcmData, 0, pcmData.length);

                    short[] shorts = new short[bytesRead / 2];
                    ByteBuffer.wrap(pcmData).order(ByteOrder.LITTLE_ENDIAN)
                            .asShortBuffer().get(shorts);

                    for(int i = 0; i < shorts.length / 2; i++) {
                        short temp = shorts[i];
                        shorts[i] = shorts[shorts.length-i-1];
                        shorts[shorts.length-i-1] = temp;
                    }

                    audioTrack.write(shorts, 0, shorts.length);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            playingThread.start();
        }
    }
}
