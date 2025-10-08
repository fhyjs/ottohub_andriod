package org.eu.hanana.reimu.ottohub_andriod.data.base;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.eu.hanana.reimu.ottohub_andriod.MyApp;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.ListFragmentBase;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.Executors;

public abstract class ListViewModelBase<E> extends ViewModel {
    private final MutableLiveData<Resource<List<E>>> videos = new MutableLiveData<>();

    // 封装网络状态的数据包装类
    public static class Resource<T> {
        public final Status status;
        public final T data;
        public final String message;
        public final Throwable msg;

        private Resource(Status status, T data, String message, Throwable msg) {
            this.status = status;
            this.data = data;
            this.message = message;
            this.msg=msg;
        }

        public static <T> Resource<T> loading() {
            return new Resource<>(Status.LOADING, null, null, null);
        }

        public static <T> Resource<T> success(T data) {
            return new Resource<>(Status.SUCCESS, data, null, null);
        }

        public static <T> Resource<T> error(Throwable msg) {
            return new Resource<>(Status.ERROR, null, msg.toString(),msg);
        }

        public enum Status {
            LOADING, SUCCESS, ERROR
        }
    }

    public void loadVideos(ListFragmentBase videoListFragment) {
        if (videoListFragment.error) return;
        if (!isNetworkAvailable()) {
            videos.postValue(Resource.error(new UnknownHostException("网络不可用")));
            return;
        }

        videos.postValue(Resource.loading());
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<E> data = fetchFromNetwork(videoListFragment);
                videos.postValue(Resource.success(data));
            } catch (Exception e) {
                videos.postValue(Resource.error(e));
            }
        });
    }

    public abstract List<E> fetchFromNetwork(ListFragmentBase videoListFragment) throws IOException;

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                MyApp.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    public LiveData<Resource<List<E>>> getVideos() {
        return videos;
    }
}