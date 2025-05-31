package org.eu.hanana.reimu.ottohub_andriod.data.video;

import static android.widget.Toast.LENGTH_LONG;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.eu.hanana.reimu.lib.ottohub.api.video.VideoListResult;
import org.eu.hanana.reimu.lib.ottohub.api.video.VideoResult;
import org.eu.hanana.reimu.ottohub_andriod.MyApp;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.ui.video.VideoListFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class VideoViewModel extends ViewModel {
    private final MutableLiveData<Resource<List<VideoCard>>> videos = new MutableLiveData<>();

    // 封装网络状态的数据包装类
    public static class Resource<T> {
        public final Status status;
        public final T data;
        public final String message;

        private Resource(Status status, T data, String message) {
            this.status = status;
            this.data = data;
            this.message = message;
        }

        public static <T> Resource<T> loading() {
            return new Resource<>(Status.LOADING, null, null);
        }

        public static <T> Resource<T> success(T data) {
            return new Resource<>(Status.SUCCESS, data, null);
        }

        public static <T> Resource<T> error(String msg) {
            return new Resource<>(Status.ERROR, null, msg);
        }

        public enum Status {
            LOADING, SUCCESS, ERROR
        }
    }

    public void loadVideos(VideoListFragment videoListFragment) {
        if (videoListFragment.error) return;
        if (!isNetworkAvailable()) {
            videos.postValue(Resource.error("网络不可用"));
            return;
        }

        videos.postValue(Resource.loading());
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<VideoCard> data = fetchFromNetwork(videoListFragment);
                videos.postValue(Resource.success(data));
            } catch (IOException e) {
                videos.postValue(Resource.error("网络请求失败：" + e.getMessage()));
            } catch (Exception e) {
                videos.postValue(Resource.error("发生未知错误:"+e));
            }
        });
    }

    private List<VideoCard> fetchFromNetwork(VideoListFragment videoListFragment) throws IOException {
        ArrayList<VideoCard> objects = new ArrayList<>();
        VideoListResult videoListResult = null;

        if (videoListFragment.selectedButton.getTag().equals(videoListFragment.buttonLabels[0])){
            videoListResult = MyApp.getInstance().getOttohubApi().getVideoApi().random_video_list(12);
        }else if (videoListFragment.selectedButton.getTag().equals(videoListFragment.buttonLabels[1])){
            videoListResult = MyApp.getInstance().getOttohubApi().getVideoApi().new_video_list(videoListFragment.currentPage*12,12);
        }else if (videoListFragment.selectedButton.getTag().equals(videoListFragment.buttonLabels[2])){
            videoListResult = MyApp.getInstance().getOttohubApi().getVideoApi().popular_video_list(7,videoListFragment.currentPage*12,12);
        }else if (videoListFragment.selectedButton.getTag().equals(videoListFragment.buttonLabels[3])){
            videoListResult = MyApp.getInstance().getOttohubApi().getVideoApi().popular_video_list(30,videoListFragment.currentPage*12,12);
        }else if (videoListFragment.selectedButton.getTag().equals(videoListFragment.buttonLabels[4])){
            videoListResult = MyApp.getInstance().getOttohubApi().getVideoApi().popular_video_list(90,videoListFragment.currentPage*12,12);
        }else if (videoListFragment.selectedButton.getTag().equals(videoListFragment.buttonLabels[5])){
            videoListResult = MyApp.getInstance().getOttohubApi().getVideoApi().category_video_list(1,12);
        }else if (videoListFragment.selectedButton.getTag().equals(videoListFragment.buttonLabels[6])){
            videoListResult = MyApp.getInstance().getOttohubApi().getVideoApi().category_video_list(2,12);
        }else if (videoListFragment.selectedButton.getTag().equals(videoListFragment.buttonLabels[7])){
            videoListResult = MyApp.getInstance().getOttohubApi().getVideoApi().category_video_list(3,12);
        }else if (videoListFragment.selectedButton.getTag().equals(videoListFragment.buttonLabels[8])){
            videoListResult = MyApp.getInstance().getOttohubApi().getVideoApi().category_video_list(4,12);
        }else if (videoListFragment.selectedButton.getTag().equals(videoListFragment.buttonLabels[9])){
            videoListResult = MyApp.getInstance().getOttohubApi().getVideoApi().category_video_list(5,12);
        }else if (videoListFragment.selectedButton.getTag().equals(videoListFragment.buttonLabels[10])){
            videoListResult = MyApp.getInstance().getOttohubApi().getVideoApi().category_video_list(6,12);
        }else if (videoListFragment.selectedButton.getTag().equals(videoListFragment.buttonLabels[11])){
            videoListResult = MyApp.getInstance().getOttohubApi().getVideoApi().category_video_list(7,12);
        }else if (videoListFragment.selectedButton.getTag().equals(videoListFragment.buttonLabels[12])){
            videoListResult = MyApp.getInstance().getOttohubApi().getVideoApi().category_video_list(0,12);
        }

        if (videoListResult.video_list != null) {
            for (VideoResult videoResult : videoListResult.video_list) {
                objects.add(new VideoCard(
                        videoResult.cover_url,
                        videoResult.avatar_url,
                        videoResult.title,
                        videoResult.time,
                        videoResult.username,
                        videoListFragment.getContext().getString(R.string.video_card_info_short,videoResult.view_count,videoResult.like_count,videoResult.favorite_count),
                        videoResult.vid
                ));
            }
        }
        return objects;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                MyApp.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    public LiveData<Resource<List<VideoCard>>> getVideos() {
        return videos;
    }
}