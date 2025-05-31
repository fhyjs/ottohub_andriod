package org.eu.hanana.reimu.ottohub_andriod.util;

import android.widget.MediaController;

import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class VlcMediaControl implements MediaController.MediaPlayerControl {
    private final MediaPlayer mp;
    private Consumer<Integer> seeker;

    public void setSeeker(Consumer<Integer> seeker) {
        this.seeker = seeker;
    }

    public VlcMediaControl(MediaPlayer mediaPlayer){
        this.mp=mediaPlayer;
    }
    @Override
    public void start() {
        mp.play();
    }

    @Override
    public void pause() {
        mp.pause();
    }

    @Override
    public int getDuration() {
        return (int) mp.getLength();
    }

    @Override
    public int getCurrentPosition() {
        return (int) mp.getTime();
    }

    @Override
    public void seekTo(int pos) {
        if (seeker!=null){
            seeker.accept(pos);
        }else {
            mp.setTime(pos);
        }
    }

    @Override
    public boolean isPlaying() {
        return mp.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        if (mp == null) return 0;

        // 获取缓冲百分比
        Media media = (Media) mp.getMedia();
        if (media != null) {
            Media.Stats stats = media.getStats();
            if (stats != null) {
                // 计算缓冲百分比
                if (stats.readBytes > 0 && stats.demuxReadBytes > 0) {
                    return (int) ((float) stats.readBytes / stats.demuxReadBytes * 100);
                }
            }
        }
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
}
