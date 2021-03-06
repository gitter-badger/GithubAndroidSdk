package com.alorma.github.basesdk.client;

import android.content.Context;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.alorma.github.basesdk.ApiClient;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.converter.Converter;

public abstract class BaseClient<K> implements Callback<K>, RequestInterceptor, RestAdapter.Log {

    private StoreCredentials storeCredentials;

    protected final Context context;
    private OnResultCallback<K> onResultCallback;
    protected Handler handler;
    private ApiClient client;

    public BaseClient(Context context, ApiClient client) {
        this.client = client;
        this.context = context.getApplicationContext();
        storeCredentials = new StoreCredentials(context);
        try {
            handler = new Handler();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private RestAdapter getRestAdapter() {
        RestAdapter.Builder restAdapterBuilder = new RestAdapter.Builder()
                .setClient(new OkClient())
                .setEndpoint(client.getApiEndpoint())
                .setRequestInterceptor(this)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setLog(this);

        if (customConverter() != null) {
            restAdapterBuilder.setConverter(customConverter());
        }

        return restAdapterBuilder.build();
    }

    public void execute() {
        if (getToken() != null) {
            executeService(getRestAdapter());
        }
    }

    public K executeSync() {
        if (getToken() != null) {
            return executeSync(getRestAdapter());
        }
        return null;
    }

    protected K executeSync(RestAdapter restAdapter) {
        return null;
    }

    protected Converter customConverter() {
        return null;
    }

    protected abstract void executeService(RestAdapter restAdapter);

    @Override
    public void success(final K k, final Response response) {
        if (handler != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    sendResponse(k, response);
                }
            });
        } else {
            sendResponse(k, response);
        }
    }

    private void sendResponse(K k, Response response) {
        if (onResultCallback != null) {
            onResultCallback.onResponseOk(k, response);
        }
    }

    @Override
    public void failure(final RetrofitError error) {
        if (handler != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    sendError(error);
                }
            });
        } else {
            sendError(error);
        }
    }

    private void sendError(RetrofitError error) {
        if (error.getResponse() != null && error.getResponse().getStatus() == 401) {
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
            manager.sendBroadcast(new UnAuthIntent(storeCredentials.token()));
        } else {
            if (onResultCallback != null) {
                onResultCallback.onFail(error);
            }
        }
    }

    public OnResultCallback<K> getOnResultCallback() {
        return onResultCallback;
    }

    public void setOnResultCallback(OnResultCallback<K> onResultCallback) {
        this.onResultCallback = onResultCallback;
    }


    protected String getToken() {
        return storeCredentials.token();
    }

    public Context getContext() {
        return context;
    }

    public interface OnResultCallback<K> {
        void onResponseOk(K k, Response r);

        void onFail(RetrofitError error);
    }

    public ApiClient getClient() {
        return client;
    }

    public void setStoreCredentials(StoreCredentials storeCredentials) {
        this.storeCredentials = storeCredentials;
    }

}
