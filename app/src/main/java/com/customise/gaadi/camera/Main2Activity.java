package com.customise.gaadi.camera;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;


public class Main2Activity extends AppCompatActivity {

    ImageView  imageView;
    ProgressDialog progressDialog;
    EditText edtPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        edtPlace = (EditText) findViewById(R.id.edtPlace);
        imageView = (ImageView) findViewById(R.id.imgPlace);

    }

    public void onClick(View view){

        if(edtPlace.getText().toString().trim().length()<=0){
            edtPlace.setError("Empty string");
            return;
        }

        new AsyncTask<Void,Void,String>(){

            @Override
            protected void onPreExecute() {
                imageView.setImageResource(R.drawable.default_placeholder);
                progressDialog = new ProgressDialog(Main2Activity.this);
                progressDialog.setMessage("loading");
                progressDialog.show();
            }

            @Override
            protected void onPostExecute(String parser) {
                progressDialog.dismiss();
                Glide.with(Main2Activity.this).load("https://" + parser)
                        .transition(new DrawableTransitionOptions().crossFade())
                        .placeholder(com.scanlibrary.R.drawable.default_placeholder)
                        .centerCrop()
                        .error(R.drawable.gcloud_placeholder)
                        .into(imageView);

                Toast.makeText(Main2Activity.this,parser,Toast.LENGTH_SHORT).show();
            }

            @Override
            protected String doInBackground(Void... params) {
               return fillImages();
            }
        }.execute();

    }


    private String fillImages(){
        try {
            int count = 0;
            String FMCurl = "https://en.wikipedia.org/wiki/" + edtPlace.getText().toString().trim();

            URL url = new URL(FMCurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.setRequestMethod("GET");
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.flush();
            int returnCode = conn.getResponseCode();
            System.out.println(returnCode);
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            StringBuilder stringBuilder = new StringBuilder("");
            String output = "";
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                stringBuilder.append(output);
                if(output.contains(".jpg\"") || output.contains(".jpeg\"")){
                    count = count + 1;
                }

                if(count ==10){
                    return parseImage(stringBuilder.toString(), "0");
                }
            }
            return parseImage(stringBuilder.toString(), "0");
        } catch (Exception e) {
            return e.toString();
        }
    }

    private String parseImage(String wikiData, String id){
        try {
            String parser = wikiData.substring(wikiData.lastIndexOf("src=\"//upload.wikimedia.org"));
            int indes;
            if(parser.contains(".jpg\"")) {
                indes = parser.indexOf(".jpg\"");
                parser = parser.substring(0, indes + 4);
            }else if(parser.contains(".jpeg\"")){
                indes = parser.indexOf(".jpeg\"");
                parser = parser.substring(0, indes + 5);
            }else if(parser.contains(".png\"")){
                indes = parser.indexOf(".png\"");
                parser = parser.substring(0, indes + 4);
            }
            parser = parser.substring(7, parser.length());
            return parser;
        }catch (Exception e){
            return e.toString();
        }



    }

}
