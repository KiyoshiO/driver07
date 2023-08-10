package jp.etaxi.driver07;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class API {
    OkHttpClient client;
    public API() {
        client = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public void call_POSTApi (String baseurl,ArrayList<ParameterModel> params, String method , Callback callback) {
        String url = baseurl + method;
        FormBody.Builder formBuilder = new FormBody.Builder();
        for(int i=0; i<params.size(); i++){
            formBuilder.add(params.get(i).key, params.get(i).value);
        }
        RequestBody formBody = formBuilder.build();

        Request request = new Request.Builder().url(url).post(formBody).build();
        client.newCall(request).enqueue(callback);
    }

    public void call_POSTApi_withjson (String baseurl, String method, String json, Callback callback) {
        String url = baseurl + method;
         final MediaType JSON
                = MediaType.parse("application/json");
        RequestBody body = RequestBody.create( json,JSON); // new
        // RequestBody body = RequestBody.create(JSON, json); // old
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public void call_GETApi(String baseurl, ArrayList<ParameterModel> params, String method, Callback callback) {

        /*HttpUrl.Builder urlBuilder = HttpUrl.parse("http://battuta.medunes.net/api/region/" + countryCode + "/all/?").newBuilder();
        urlBuilder.addQueryParameter("key", "11ba4fd65274254b4f4a5b0619b59a57");*/
        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseurl + method).newBuilder();
        for(int i=0; i<params.size(); i++){
            urlBuilder.addQueryParameter(params.get(i).key, params.get(i).value);
        }
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder().url(url).get().build();
        client.newCall(request).enqueue(callback);
    }


    public void callImageuploadwithsingleimage (String baseurl, String method, ArrayList<ParameterModel> params, boolean isAvatar ,File avatarImg, Callback callback) {
        String url = baseurl + method;
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        for(int i=0; i<params.size(); i++){
            builder.addFormDataPart(params.get(i).key, params.get(i).value);
        }

        if (isAvatar){
            builder.addFormDataPart("file", avatarImg.getName(), RequestBody.create(MediaType.parse("multipart/form-data"), avatarImg));
        }

        MultipartBody requestBody = builder.build();
        Request request = new Request.Builder().url(url).post(requestBody).build();
        client.newCall(request).enqueue(callback);
    }


    public void call_Uploadfilewithparams(String baseurl, String method,ArrayList<ParameterModel> params, ArrayList<File> files,
                                          Callback callback) {
        String url = baseurl + method;
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        for(int i=0; i<params.size(); i++){
            builder.addFormDataPart(params.get(i).key, params.get(i).value);
        }

        for (File file : files){
            builder.addFormDataPart("file[]", file.getName(), RequestBody.create(MediaType.parse("multipart/form-data"), file));
        }

        MultipartBody requestBody = builder.build();
        Request request = new Request.Builder().url(url).post(requestBody).build();
        client.newCall(request).enqueue(callback);
    }


}
