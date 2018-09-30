package com.example.game.frisk;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;

public class UserHome extends AppCompatActivity {

    Button  sign_out;
    Button file_select;
    TextView user_name;
    ListView list;
    ArrayList<String> arrayList;
    ArrayAdapter<String> arrayAdapter;
    Uri uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        user_name=(TextView) findViewById(R.id.user_name);

        user_name.setText("WELCOME "+getIntent().getStringExtra("name").toString());

        list=(ListView)findViewById(R.id.list);
        arrayList=new ArrayList<>();
        arrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,arrayList);
        list.setAdapter(arrayAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String fname=(String)adapterView.getItemAtPosition(i);
                    Intent j=new Intent(UserHome.this,SingleFile.class);
                    j.putExtra("fname",fname);
                    startActivity(j);
            }
        });

        file_select=(Button) findViewById(R.id.file_select);
        sign_out=(Button) findViewById(R.id.sign_out);
        sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sign_out();
            }
        });

        file_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent,100);
            }
        });
    }
    public void sign_out(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        FirebaseAuth.getInstance().signOut();
        mGoogleSignInClient.signOut();
        Intent i=new Intent(UserHome.this,MainActivity.class);
        startActivity(i);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==100){
            uri = data.getData();
            String path="";
                FTPTask ftpTask=new FTPTask();
                ftpTask.execute();

//            Log.d("", "File Uri: " + uri.toString());
        }
    }


    public class FTPTask extends AsyncTask<Void,Void,String>{

        String name="";
        @Override
        protected String doInBackground(Void... voids) {
//            Uri muri-uri[0];
            File sdcard = Environment.getExternalStorageDirectory();

//            Toast.makeText(UserHome.this,uri.getPath(),Toast.LENGTH_LONG).show();

            Log.d("Path: ",uri.getPath());

            FTPClient con = null;

            try
            {
                con = new FTPClient();
                con.connect("10.2.138.225",8080);

                if (con.login("user", "user"))
                {
//                    Toast.makeText(UserHome.this,"Logged in",Toast.LENGTH_LONG).show();
                    Log.d("Loggin: ","True");

                    con.enterLocalPassiveMode(); // important!
                    con.setFileType(FTP.BINARY_FILE_TYPE);
                    String data = uri.getPath();

                    Log.d("Directory:",con.printWorkingDirectory());

                    String[] x=uri.getPath().split("/");
                    name=x[x.length-1];

                    FileInputStream in = new FileInputStream(new File(data));
                    boolean result = con.storeFile("/"+name, in);
                    in.close();
                    if (result){ Log.v("upload result", "succeeded");}
                    else{
//                        Toast.makeText(UserHome.this,"Failed",Toast.LENGTH_LONG).show();
                        Log.d("upload result: ","failed");
                    }
                    con.logout();
                    con.disconnect();
                }
                else{
//                    Toast.makeText(UserHome.this,"Log in",Toast.LENGTH_LONG).show();
                    Log.d("Loggin: ","False");
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return name;
        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(UserHome.this,"Added "+s,Toast.LENGTH_LONG).show();
//            arrayList.add(s);
//            arrayAdapter.clear();
            arrayAdapter.add(s);

        }
    }
}
