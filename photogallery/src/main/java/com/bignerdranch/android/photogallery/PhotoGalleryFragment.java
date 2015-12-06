package com.bignerdranch.android.photogallery;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * @author Zhuo
 *         2015/12/1
 */
public class PhotoGalleryFragment extends Fragment {

    private static final String TAG = "PhotoGalleryFragment";

    GridView mGridView;
    private ArrayList<GalleryItem> mGalleryItems;
    private ThumbnailDownloader<ImageView> mThumbnailDownloader;
    private LruCache<String, Bitmap> mBitmapLruCache;

    private int current_page = 1;
    private int fetched_page = 0;
    private int scrollPosition = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        new FetchItemsTask().execute(current_page);


        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mBitmapLruCache = new LruCache<String, Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };

        mThumbnailDownloader = new ThumbnailDownloader<ImageView>(new Handler(), mBitmapLruCache);
        mThumbnailDownloader.setListener(new ThumbnailDownloader.Listener<ImageView>() {
            @Override
            public void onThumbnailDownloaded(ImageView imageView, Bitmap bitmap) {
                if (isVisible()) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        });
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG, "Background Thread Started");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mGalleryItems = new ArrayList<>();

        mGridView = (GridView) v.findViewById(R.id.gridView);

        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount == totalItemCount
                        && totalItemCount > 0 && current_page == fetched_page) {
                    scrollPosition = firstVisibleItem + 3;
                    new FetchItemsTask().execute(++current_page);
                }
            }
        });

        setupAdapter();

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
        Log.i(TAG, "Background Thread Quited");
    }

    private class FetchItemsTask extends AsyncTask<Integer, Void, ArrayList<GalleryItem>> {

        @Override
        protected ArrayList<GalleryItem> doInBackground(Integer... params) {
            return new FlickrFetchr().fetchItem(params[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<GalleryItem> galleryItems) {
            if (mGalleryItems != null) {
                mGalleryItems.addAll(galleryItems);
            } else {
                mGalleryItems = galleryItems;
            }
            setupAdapter();
            fetched_page++;
        }
    }

    private void setupAdapter() {
        if (getActivity() == null || mGridView == null) {
            return;
        }
        if (mGalleryItems != null) {
//            mGridView.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout
//                    .simple_gallery_item, mGalleryItems));
            mGridView.setAdapter(new GalleryItemAdapter(mGalleryItems));
            mGridView.setSelection(scrollPosition);
        } else {
            mGridView.setAdapter(null);
        }
    }

    private class GalleryItemAdapter extends ArrayAdapter<GalleryItem> {

        public GalleryItemAdapter(ArrayList<GalleryItem> objects) {
            super(getActivity(), 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.gallery_item, null);
            }

            ImageView imageView = (ImageView) convertView.findViewById(R.id.gallery_item_imageView);
            imageView.setImageResource(R.mipmap.brian_up_close);
            GalleryItem item = getItem(position);
            mThumbnailDownloader.queueThumbnail(imageView, item.getUrl());

//            为前十个和后十个GalleryItem预加载bitmap
//            写的好蠢= =

            String id;
            String url;
            if (mGalleryItems.size() > 1) {
                int endPos = (position - 10 < 0) ? 0 : (position - 10);
                for (int i = position - 1; i >= endPos; i--) {
                    if (i < mGalleryItems.size()) {
                        url = mGalleryItems.get(i).getUrl();
                        id = mGalleryItems.get(i).getId();
                        if (url != null) {
                            mThumbnailDownloader.queueThumbnailCache(id, url);
                        }
                    }
                }
//                因为分页了，所以不用判断是否到最后，但是分页好不好用并没测试
//                不用分页的话，应该用i<=mGalleryItems.size()就行了
                for (int i = position + 1; i <= position + 10; i++) {
                    if (i < mGalleryItems.size()) {
                        url = mGalleryItems.get(i).getUrl();
                        id = mGalleryItems.get(i).getId();
                        if (url != null) {
                            mThumbnailDownloader.queueThumbnailCache(id, url);
                        }
                    }
                }
            }

            return convertView;
        }
    }
}
