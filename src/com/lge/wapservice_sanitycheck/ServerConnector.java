
package com.lge.wapservice_sanitycheck;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import android.util.Log;

public class ServerConnector {
    private ArrayList<String[]> data = new ArrayList<String[]>();
    private HttpPost request;
    
    public ServerConnector(){}
    
    public ServerConnector(String addr){
        request = new HttpPost(addr);
    }
    
    private Vector<NameValuePair> setPostParams(){
        Vector<NameValuePair> params = new Vector<NameValuePair>();
        
        if (data != null) {
            for(int i = 0; i<data.size(); i++){
                params.add(new BasicNameValuePair(data.get(i)[0], data.get(i)[1]));
            }
        }
        return params;
    }
    
    private void setEntity(Vector<NameValuePair> params) throws UnsupportedEncodingException{
        HttpEntity entity = null;
        entity = new UrlEncodedFormEntity(params,"utf-8");
        request.setEntity(entity);
    }

    //최종 전송
    public String send() throws IOException {
        setEntity(setPostParams());

        HttpClient client = new DefaultHttpClient();
        ResponseHandler<String> reshandler = new BasicResponseHandler();
        data.clear();
        return client.execute(request, reshandler);
    }
    
    //값 설정
    public ServerConnector add(String key, String value) {
        data.add(new String[]{key, value});
        return this;
    }
    
    public void setAddress(String addr) {
        request = new HttpPost(addr);
    }
}
