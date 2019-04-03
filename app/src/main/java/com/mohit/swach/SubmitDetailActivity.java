package com.mohit.swach;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mohit.swach.extra.Generic;
import com.mohit.swach.extra.URL;
import com.mohit.swach.models.CenterListModel;

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
import java.util.ArrayList;
import java.util.List;

public class SubmitDetailActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Button button_save;
    private ProgressDialog progressDialog;
    TextView tv_header_text;
    TextView tv_committee_details, tv_sutdent_details;
    LinearLayout ll_menu_icon;
    EditText et_remark3, et_remark1, et_remark2;
    EditText et_student_number;
    String centerNameSelected;
    private List<CenterListModel> centerList;
    String[] centerName;
    String resultImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_detail);

//        SharedPreferences prefs = getSharedPreferences("LoginData", MODE_PRIVATE);
//        String restoredText = prefs.getString("isLogin", null);
//        if (restoredText != null && restoredText.equals("yes")) {
//            Intent intent = new Intent(LoginActivity.this, SubmitDetailActivity.class);
//            startActivity(intent);
//            finish();
//        }

        tv_header_text = findViewById(R.id.tv_header_text);
        tv_header_text.setText("Swach");
        ll_menu_icon = findViewById(R.id.ll_menu_icon);
        ll_menu_icon.setVisibility(View.VISIBLE);

        ll_menu_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

        progressDialog = new ProgressDialog(SubmitDetailActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading");

        tv_committee_details = findViewById(R.id.tv_committee_details);
        tv_sutdent_details = findViewById(R.id.tv_sutdent_details);

        tv_committee_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SubmitDetailActivity.this, CommitteDetailsActivity.class);
                startActivity(intent);
            }
        });

        tv_sutdent_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SubmitDetailActivity.this, StudentDetailsActivity.class);
                startActivity(intent);
            }
        });

        et_student_number = findViewById(R.id.et_student_number);
        et_remark3 = findViewById(R.id.et_remark3);
        et_remark2 = findViewById(R.id.et_remark2);
        et_remark1 = findViewById(R.id.et_remark1);


        button_save = findViewById(R.id.button_save);
        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SubmitDetailActivity.this, Upload.class);
                startActivityForResult(intent, 1);
            }
        });

        new GetListCentre().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            resultImage = data.getStringExtra("resultImage");
            if(resultImage != null && !resultImage.equals("")){
                new DailyVisitSave().execute();
            }
        }

    }

    //Performing action onItemSelected and onNothing selected
    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
        centerNameSelected = centerName[position];
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_main, menu);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == R.id.action_logout) {
//            logout();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    private void logout() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SubmitDetailActivity.this);
        // Setting Dialog Title
        alertDialog.setTitle("Log Out");
        // Setting Dialog Message
        alertDialog.setMessage("Are you sure, you want to logout?");
        // Setting Icon to Dialog
//                alertDialog.setIcon(R.drawable.tick);
        alertDialog.setPositiveButton("Log Out", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = getSharedPreferences("LoginData", MODE_PRIVATE).edit();
                editor.putString("CordID", null);
                editor.putString("CordName", null);
                editor.putString("MobileNo", null);
                editor.putString("UserName", null);
                editor.putString("Password", null);
                editor.putString("isLogin", "no");
                editor.commit();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    class GetListCentre extends AsyncTask<String, Void, Void> {
        JSONObject jsonobject, returnValue;
        Boolean status;
        Boolean isExceptionOccured;
        String errorMessage, message;
        JSONArray centerListData;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(String... strings) {
            HttpClient httpclient = new DefaultHttpClient();
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            HttpPost httppost = new HttpPost(URL.GetListCentre);
            httppost.setHeader("Content-Type", "application/json");
            try {
                JSONObject json = new JSONObject();

//                json.put("PSID", 0);
//                json.put("GPID", 0);
//                json.put("VILLAGEID", 0);
//                json.put("CENTREID", 0);
//                json.put("CENTRE", "");
//                json.put("LATITUDE", "");
//                json.put("LONGITUDE", "");

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
                        centerList = new ArrayList<>();
                        centerListData = new JSONArray(jsonobject.getString("ReturnValue"));
                        centerName = new String[centerListData.length()];
                        for (int i = 0; i < centerListData.length(); i++) {
                            returnValue = centerListData.getJSONObject(i);
                            centerName[i] = returnValue.getString("CentreName");
                            CenterListModel centerListModel = new CenterListModel();
                            centerListModel.CentreID = returnValue.getInt("CentreID");
                            centerListModel.PSID = returnValue.getInt("PSID");
                            centerListModel.GPID = returnValue.getInt("GPID");
                            centerListModel.VillageID = returnValue.getInt("VillageID");
                            centerListModel.CentreName = returnValue.getString("CentreName");
                            centerListModel.DocNo = returnValue.getString("DocNo");
                            centerListModel.DocNo = returnValue.getString("DocNo");
                            centerListModel.FTQualification = returnValue.getString("FTQualification");
                            centerListModel.SecondTeacher = returnValue.getString("SecondTeacher");
                            centerListModel.STQualification = returnValue.getString("STQualification");
                            centerListModel.TypeID = returnValue.getInt("TypeID");
                            centerListModel.MobileNo = returnValue.getString("MobileNo");
                            centerListModel.Latitude = returnValue.getString("Latitude");
                            centerListModel.Longitude = returnValue.getString("Longitude");
                            centerListModel.CordID = returnValue.getInt("CordID");
                            centerList.add(centerListModel);
                        }
                        Generic.centerDataList = centerList;
                        //Getting the instance of Spinner and applying OnItemSelectedListener on it
                        Spinner spinner = (Spinner) findViewById(R.id.spinner);
                        spinner.setOnItemSelectedListener(SubmitDetailActivity.this);
                        ArrayAdapter aa = new ArrayAdapter(SubmitDetailActivity.this, android.R.layout.simple_spinner_item, centerName);
                        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(aa);
                    } else {
                        errorMessage = jsonobject.getString("ErrorMessage");
                        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(SubmitDetailActivity.this, "Please try again later!", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                progressDialog.dismiss();
            }
        }
    }


    public String getCenterId(String centerName) {
        for (int i = 0; i < Generic.centerDataList.size(); i++) {
            try {
                if (centerName.equals(Generic.centerDataList.get(i).CentreName)) {
                    return "" + Generic.centerDataList.get(i).CentreID;
                }
            } catch (Exception e) {
                return "";
            }
        }
        return "";
    }


    class DailyVisitSave extends AsyncTask<String, Void, Void> {
        JSONObject jsonobject, returnValue;
        Boolean status;
        JSONArray returnData;
        Boolean isExceptionOccured;
        String errorMessage, message;
        JSONArray imageList;
        JSONObject imageData = new JSONObject();
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(String... strings) {
            HttpClient httpclient = new DefaultHttpClient();
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            HttpPost httppost = new HttpPost(URL.DailyVisitSave);
            httppost.setHeader("Content-Type", "application/json");
            try {
                JSONObject json = new JSONObject();
                imageList = new JSONArray();
                imageData = new JSONObject();
                json.put("DailyVisitID", 0);
                json.put("CentreID", getCenterId(centerNameSelected));
                json.put("Remark1", et_remark1.getText().toString());
                json.put("Remark2", et_remark2.getText().toString());
                json.put("Remark3", et_remark3.getText().toString());
                json.put("Remark4", et_student_number.getText().toString());
                json.put("VisitDate", 0);
                json.put("UserID", 0);
                imageData.put("TypeID",1);
                imageData.put("URL_Path", "image/jpeg;base64,"+resultImage);
                imageList.put(imageData);
                json.put("Images", imageList);
                Log.i("save Json", json.toString());
                httppost.setEntity(new ByteArrayEntity(json.toString().getBytes("UTF8")));
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
                        String message = jsonobject.getString("Message");
                        Toast.makeText(SubmitDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                    } else {
                        errorMessage = jsonobject.getString("ErrorMessage");
                        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(SubmitDetailActivity.this, "Please try again later!", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                progressDialog.dismiss();
            }
        }
    }
}
