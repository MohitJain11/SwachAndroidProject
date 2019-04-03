package com.mohit.swach;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mohit.swach.extra.Generic;
import com.mohit.swach.extra.URL;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class CommitteDetailsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    TextView tv_header_text;
    private ProgressDialog progressDialog;
    String []centerName;
    String centerNameSelected = "";
    TextView tv_member_1, tv_member_2, tv_member_3, tv_member_4, tv_member_5, tv_member_6, tv_member_7;
    TextView tv_chairman_name;
    LinearLayout ll_member_list;
    Button button_search_member;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_committe_details);

        tv_header_text = findViewById(R.id.tv_header_text);
        tv_header_text.setText("Committee Details");

        progressDialog = new ProgressDialog(CommitteDetailsActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading");

        centerName = new String[Generic.centerDataList.size()];
        for(int i=0;i<Generic.centerDataList.size();i++){
            centerName[i] = Generic.centerDataList.get(i).CentreName;
        }
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(CommitteDetailsActivity.this);
        ArrayAdapter aa = new ArrayAdapter(CommitteDetailsActivity.this, android.R.layout.simple_spinner_item, centerName);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(aa);

        tv_chairman_name = findViewById(R.id.tv_chairman_name);
        tv_member_1 = findViewById(R.id.tv_member_1);
        tv_member_2 = findViewById(R.id.tv_member_2);
        tv_member_3 = findViewById(R.id.tv_member_3);
        tv_member_4 = findViewById(R.id.tv_member_4);
        tv_member_5 = findViewById(R.id.tv_member_5);
        tv_member_6= findViewById(R.id.tv_member_6);
        tv_member_7 = findViewById(R.id.tv_member_7);
        ll_member_list = findViewById(R.id.ll_member_list);
        ll_member_list.setVisibility(View.GONE);

        button_search_member = findViewById(R.id.button_search_member);
        button_search_member.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_chairman_name.setText("");
                tv_member_1.setText("");
                tv_member_2.setText("");
                tv_member_3.setText("");
                tv_member_4.setText("");
                tv_member_5.setText("");
                tv_member_6.setText("");
                tv_member_7.setText("");
                ll_member_list.setVisibility(View.GONE);
                new GetListCenterMember().execute();
            }
        });
    }

    //Performing action onItemSelected and onNothing selected
    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
//        Toast.makeText(getApplicationContext(), centerName[position], Toast.LENGTH_LONG).show();
        centerNameSelected = centerName[position];
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }


    public String getCenterId(String centerName) {
        for (int i = 0; i < Generic.centerDataList.size(); i++) {
            try {
                if (centerName.equals(Generic.centerDataList.get(i).CentreName)){
                    return ""+Generic.centerDataList.get(i).CentreID;
                }
            } catch (Exception e) {
                return "";
            }
        }
        return "";
    }

    class GetListCenterMember extends AsyncTask<String, Void, Void> {
        JSONObject jsonobject, returnValue;
        Boolean status;
        JSONArray returnData;
        Boolean isExceptionOccured;
        String errorMessage, message;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(String... strings) {
            HttpClient httpclient = new DefaultHttpClient();
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            HttpPost httppost = new HttpPost(URL.GetListCenterMember);
            httppost.setHeader("Content-Type", "application/json");
            try {
                JSONObject json = new JSONObject();

                json.put("CentreID", getCenterId(centerNameSelected));
                json.put("CID", 0);

                httppost.setEntity(new ByteArrayEntity(json.toString().replaceAll("\\\\", "").replaceAll("\"\"", "\"").getBytes("UTF8")));
                String responseBody = httpclient.execute(httppost, responseHandler);
                jsonobject = new JSONObject(responseBody);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                progressDialog.dismiss();
//                Toast.makeText(LoginActivity.this, "Something went worng!", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                progressDialog.dismiss();
//                Toast.makeText(LoginActivity.this, "Something went worng!", Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                e.printStackTrace();
                progressDialog.dismiss();
//                Toast.makeText(LoginActivity.this, "Something went worng!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                progressDialog.dismiss();
//                Toast.makeText(LoginActivity.this, "Something went worng!", Toast.LENGTH_SHORT).show();
            } finally {
                progressDialog.dismiss();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                if (jsonobject != null) {
                    isExceptionOccured = jsonobject.getBoolean("IsError");
                    status = jsonobject.getBoolean("Status");
                    if (status && !isExceptionOccured) {
                        returnData = new JSONArray(jsonobject.getString("ReturnValue"));
                        returnValue = returnData.getJSONObject(0);
                        tv_chairman_name.setText(returnValue.getString("Chairman"));
                        tv_member_1.setText(returnValue.getString("Member1"));
                        tv_member_2.setText(returnValue.getString("Member2"));
                        tv_member_3.setText(returnValue.getString("Member3"));
                        tv_member_4.setText(returnValue.getString("Member4"));
                        tv_member_5.setText(returnValue.getString("Member5"));
                        tv_member_6.setText(returnValue.getString("Member6"));
                        tv_member_7.setText(returnValue.getString("Member7"));
                        ll_member_list.setVisibility(View.VISIBLE);
                        progressDialog.dismiss();
                    } else {
                        errorMessage = jsonobject.getString("ErrorMessage");
                        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                } else {
                    Toast.makeText(CommitteDetailsActivity.this, "Please try again later!", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                progressDialog.dismiss();
            } finally {
                progressDialog.dismiss();
            }
        }
    }


}
