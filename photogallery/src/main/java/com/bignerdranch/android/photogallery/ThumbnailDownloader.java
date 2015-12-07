package com.bignerdranch.android.photogallery;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Zhuo
 *         2015/12/4
 */
public class ThumbnailDownloader<Token> extends HandlerThread {

    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;
    private static final int MESSAGE_CACHING = 1;

    Handler mHandler;
    Handler mResponseHandler;
    Listener mListener;
    LruCache<String, Bitmap> mBitmapLruCache;
    Map<Token, String> requestMap = Collections.synchronizedMap(new HashMap<Token, String>());
    Map<String, String> requestCacheMap = Collections.synchronizedMap(new HashMap<String, String>());

    public ThumbnailDownloader(Handler responseHandler, LruCache<String, Bitmap> lruCache) {
        super(TAG);
        mResponseHandler = responseHandler;
        mBitmapLruCache = lruCache;
    }

    public interface Listener<Token> {
        void onThumbnailDownloaded(Token token, Bitmap bitmap);
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    @SuppressWarnings("uncheck")
                    Token token = (Token) msg.obj;
                    Log.i(TAG, "Got a request for url: " + requestMap.get(token));
                    handleRequest(token);
                } else if (msg.what == MESSAGE_CACHING) {
                    String id = (String) msg.obj;
                    handleRequestCache(id);
                }
            }
        };
    }

    private void handleRequest(final Token token) {

        try {
            final String url = requestMap.get(token);
            if (url == null) {
                return;
            }
            final Bitmap bitmap;

            if (getBitmapFromMemCache(url) != null) {
                bitmap = getBitmapFromMemCache(url);

            } else {

                bitmap = getBitmapFromUrl(url);
                addBitmapToMemCache(url, bitmap);
            }

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (requestMap.get(token) != url) {
                        return;
                    }
                    requestMap.remove(token);
                    mListener.onThumbnailDownloaded(token, bitmap);
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "Error downloading image", e);
        }
    }

    private void handleRequestCache(final String id) {
        try {
            final String url = requestCacheMap.get(id);
            if (url == null) {
                return;
            }
            final Bitmap bitmap;

            if (getBitmapFromMemCache(url) == null) {

                bitmap = getBitmapFromUrl(url);
                addBitmapToMemCache(url, bitmap);

//                如果每个GalleryItem都放20个进map里，就占用太大了，
//                想想应该只能在这个时候将这个请求的记录移除
                requestCacheMap.remove(id);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error downloading image", e);
        }

    }

    public void queueThumbnail(Token token, String url) {
        Log.i(TAG, "Got an url: " + url);
        requestMap.put(token, url);

        mHandler.obtainMessage(MESSAGE_DOWNLOAD, token).sendToTarget();
    }

    public void queueThumbnailCache(String id, String url) {
        requestCacheMap.put(id, url);

        mHandler.obtainMessage(MESSAGE_CACHING, id).sendToTarget();
    }

    public void clearQueue() {
        mHandler.removeMessages(MESSAGE_DOWNLOAD);
        requestMap.clear();
        requestCacheMap.clear();
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void addBitmapToMemCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mBitmapLruCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mBitmapLruCache.get(key);
    }

    private Bitmap getBitmapFromUrl(String url) throws IOException {
        byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
        return BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
    }
}
