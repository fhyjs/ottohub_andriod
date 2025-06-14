package org.eu.hanana.reimu.ottohub_andriod.data.blog;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.eu.hanana.reimu.lib.ottohub.api.blog.BlogListResult;
import org.eu.hanana.reimu.lib.ottohub.api.blog.BlogResult;
import org.eu.hanana.reimu.ottohub_andriod.MyApp;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.ui.blog.BlogListFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class BlogViewModel extends ViewModel {
    private final MutableLiveData<Resource<List<BlogResult>>> blogs = new MutableLiveData<>();

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

    public void loadVideos(BlogListFragment blogListFragment) {
        if (blogListFragment.error) return;
        if (!isNetworkAvailable()) {
            blogs.postValue(Resource.error("网络不可用"));
            return;
        }

        blogs.postValue(Resource.loading());
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<BlogResult> data = fetchFromNetwork(blogListFragment);
                blogs.postValue(Resource.success(data));
            } catch (IOException e) {
                blogs.postValue(Resource.error("网络请求失败：" + e.getMessage()));
            } catch (Exception e) {
                blogs.postValue(Resource.error("发生未知错误:"+e));
            }
        });
    }

    private List<BlogResult> fetchFromNetwork(BlogListFragment blogListFragment) throws IOException {
        BlogListResult listResult = null;
        if (blogListFragment.uid==null&&blogListFragment.data==null) {
            if (blogListFragment.selectedButton.getTag().equals(blogListFragment.buttonLabels[0])) {
                listResult = MyApp.getInstance().getOttohubApi().getBlogApi().random_blog_list(12);
            } else if (blogListFragment.selectedButton.getTag().equals(blogListFragment.buttonLabels[1])) {
                listResult = MyApp.getInstance().getOttohubApi().getBlogApi().new_blog_list(blogListFragment.currentPage * 12, 12);
            } else if (blogListFragment.selectedButton.getTag().equals(blogListFragment.buttonLabels[2])) {
                listResult = MyApp.getInstance().getOttohubApi().getBlogApi().popular_blog_list(7, blogListFragment.currentPage * 12, 12);
            } else if (blogListFragment.selectedButton.getTag().equals(blogListFragment.buttonLabels[3])) {
                listResult = MyApp.getInstance().getOttohubApi().getBlogApi().popular_blog_list(30, blogListFragment.currentPage * 12, 12);
            } else if (blogListFragment.selectedButton.getTag().equals(blogListFragment.buttonLabels[4])) {
                listResult = MyApp.getInstance().getOttohubApi().getBlogApi().popular_blog_list(90, blogListFragment.currentPage * 12, 12);
            }
        }else {
            if(blogListFragment.uid!=null) {
                listResult = MyApp.getInstance().getOttohubApi().getBlogApi().user_blog_list(blogListFragment.uid, blogListFragment.currentPage * 12, 12);
            }else {
                listResult = MyApp.getInstance().getOttohubApi().getBlogApi().search_blog_list(blogListFragment.data, 36);
                if (blogListFragment.data.toLowerCase(Locale.ROOT).startsWith("ob")) {
                    listResult.blog_list.addAll(0,MyApp.getInstance().getOttohubApi().getBlogApi().id_blog_list(Integer.parseInt(blogListFragment.data.substring(2))).blog_list);
                }
            }
        }
        //username用于存储额外信息.
        for (BlogResult blogResult : listResult.blog_list) {
            blogResult.username=blogListFragment.getString(R.string.video_card_info_short,blogResult.view_count,blogResult.like_count,blogResult.favorite_count);
        }
        return new ArrayList<>(listResult.blog_list);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                MyApp.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    public LiveData<Resource<List<BlogResult>>> getVideos() {
        return blogs;
    }
}
