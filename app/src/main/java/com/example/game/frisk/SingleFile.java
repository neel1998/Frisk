package com.example.game.frisk;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SingleFile extends AppCompatActivity {

    TextView text;
    String name;
    Button analysis;
    ListView resultList;
    Integer False=0,True=0;
    ArrayList<String> resultArray;
    ArrayAdapter<String> resultAdapter;
    ProgressBar progress1;
    double per;
    TextView no,perc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_file);
        TextView text=(TextView)findViewById(R.id.text);
        name=getIntent().getStringExtra("fname");
        analysis=(Button)findViewById(R.id.analysis);

        no=(TextView)findViewById(R.id.no);
        perc=(TextView)findViewById(R.id.perc);

        resultList=(ListView)findViewById(R.id.result);
        resultArray=new ArrayList<>();
        resultAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,resultArray);
        progress1=(ProgressBar)findViewById(R.id.progress1);
        resultList.setAdapter(resultAdapter);

        progress1.setVisibility(View.GONE);
        analysis.setVisibility(View.VISIBLE);
        analysis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HttpTask httpTask=new HttpTask();
                progress1.setVisibility(View.VISIBLE);
                analysis.setVisibility(View.GONE);
                httpTask.execute();

            }
        });
    }
    public class HttpTask extends AsyncTask<Void,Void,String>{

        String result_response;
        JSONObject jsonObject=new JSONObject();
            @Override
        protected String doInBackground(Void... voids) {
            try {
                jsonObject.put("fname",name);
                URL url=new URL("http://10.2.138.225:8081");
                final MediaType JSON=MediaType.parse("application/json; charset=utf-8");
                OkHttpClient client=new OkHttpClient();
                RequestBody body= RequestBody.create(JSON,jsonObject.toString());
                Request request=new Request.Builder()
                        .url("http://10.2.138.225:8081")
                        .post(body)
                        .build();
                Response response=client.newCall(request).execute();
                result_response=response.body().string();
            }
            catch (MalformedURLException e) {}
            catch (IOException e) {} catch (JSONException e) {
                e.printStackTrace();
            }
            return result_response;
        }

        @Override
        protected void onPostExecute(String s) {
            progress1.setVisibility(View.GONE);
//            Toast.makeText(SingleFile.this,s,Toast.LENGTH_LONG).show();
            try {
                JSONObject resp=new JSONObject(s);
                JSONObject scans = resp.getJSONObject("scans");
                Iterator<String> keys = scans.keys();

                String res="";
//                boolean det=true;
                JSONObject temp;
                while(keys.hasNext()) {
                    String key = keys.next();
//                    if (scans.get(key) instanceof JSONObject) {
                        temp=scans.getJSONObject(key);
//                    }
                    if(temp.getBoolean("detected")==true) {
                        res ="Anti Virus:" + key + "\ndetected: True";
                        True++;
                    }
                    else{
                        res ="Anti Virus:"+ key + "\ndetected: False";
                        False++;
                    }
                    per=(False/(False+True))*100.0;
                    no.setText(True+" malware detected");
                    perc.setText(String.valueOf(per)+"%");
                    resultAdapter.add(res);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("response",s);
        }
    }
}
