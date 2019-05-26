package com.android.swach;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.swach.Adapters.StudentListAdapter;
import com.android.swach.extra.Generic;
import com.android.swach.extra.URL;
import com.android.swach.models.StudentListModel;

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

public class StudentDetailsActivity extends AppCompatActivity {
    TextView tv_header_text;
    private ProgressDialog progressDialog;
    String[] centerName;
    String centerNameSelected = "";
    Button button_student_details;

    LinearLayout ll_no_data_available, ll_student_list;

    /////Student List//////
    private List<StudentListModel> studentList = new ArrayList<>();
    private RecyclerView recycler_view_student_details;
    public static StudentListAdapter studentListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_details);

        centerNameSelected = getIntent().getStringExtra("centerName");

        tv_header_text = findViewById(R.id.tv_header_text);
        tv_header_text.setText("Student Details");

        progressDialog = new ProgressDialog(StudentDetailsActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading");

        ll_student_list = findViewById(R.id.ll_student_list);
        ll_no_data_available = findViewById(R.id.ll_no_data_available);
        ll_no_data_available.setVisibility(View.GONE);
        ll_student_list.setVisibility(View.VISIBLE);

//        centerName = new String[Generic.centerDataList.size()];
//        for (int i = 0; i < Generic.centerDataList.size(); i++) {
//            centerName[i] = Generic.centerDataList.get(i).CentreName;
//        }
//        Spinner spinner = (Spinner) findViewById(R.id.spinner);
//        spinner.setOnItemSelectedListener(StudentDetailsActivity.this);
//        ArrayAdapter aa = new ArrayAdapter(StudentDetailsActivity.this, android.R.layout.simple_spinner_item, centerName);
//        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinner.setAdapter(aa);

        recycler_view_student_details = (RecyclerView) findViewById(R.id.recycler_view_student_details);

        button_student_details = findViewById(R.id.button_student_details);
        button_student_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new GetListStudent().execute();
            }
        });

        new GetListStudent().execute();
    }

//    //Performing action onItemSelected and onNothing selected
//    @Override
//    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
////        Toast.makeText(getApplicationContext(), centerName[position], Toast.LENGTH_LONG).show();
//        centerNameSelected = centerName[position];
//    }
//
//    @Override
//    public void onNothingSelected(AdapterView<?> arg0) {
//        // TODO Auto-generated method stub
//    }

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


    class GetListStudent extends AsyncTask<String, Void, Void> {
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
            HttpPost httppost = new HttpPost(URL.GetListStudent);
            httppost.setHeader("Content-Type", "application/json");
            try {
                JSONObject json = new JSONObject();

                json.put("CentreID", getCenterId(centerNameSelected));
                json.put("ClassID", 0);
                json.put("StudentID", 0);
                json.put("StudentsNo", 0);
                json.put("StudentName", "");
                json.put("AadharNo", "");
                Log.i("json req student", json.toString());
//                httppost.setEntity(new ByteArrayEntity(json.toString().replaceAll("\\\\", "").replaceAll("\"\"", "\"").getBytes("UTF8")));
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
                        returnData = new JSONArray(jsonobject.getString("ReturnValue"));
                        studentList = new ArrayList<>();
                        for (int i = 0; i < returnData.length(); i++) {
                            returnValue = returnData.getJSONObject(i);
                            StudentListModel appliedModel = new StudentListModel();
                            appliedModel.StudentID = returnValue.getString("StudentID");
                            appliedModel.CentreID = returnValue.getString("CentreID");
                            appliedModel.CentreName = returnValue.getString("CentreName");
                            appliedModel.StudentsNo = returnValue.getString("StudentsNo");
                            appliedModel.StudentName = returnValue.getString("StudentName");
                            appliedModel.AadharNo = returnValue.getString("AadharNo");
                            appliedModel.StudentSex = returnValue.getString("StudentSex");
                            appliedModel.StudentFatherName = returnValue.getString("StudentFatherName");
                            appliedModel.StudentDOB = returnValue.getString("StudentDOB");
                            appliedModel.ClassID = returnValue.getString("ClassID");
                            appliedModel.ClassName = returnValue.getString("ClassName");
                            studentList.add(appliedModel);
                        }
                        progressDialog.dismiss();
                        studentListAdapter = new StudentListAdapter(studentList, StudentDetailsActivity.this);
                        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                        recycler_view_student_details.setLayoutManager(mLayoutManager);
                        recycler_view_student_details.setItemAnimator(new DefaultItemAnimator());
                        recycler_view_student_details.setAdapter(studentListAdapter);
                        ll_student_list.setVisibility(View.VISIBLE);
                    } else {
                        ll_no_data_available.setVisibility(View.VISIBLE);
                        errorMessage = jsonobject.getString("ErrorMessage");
                        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                } else {
                    ll_no_data_available.setVisibility(View.VISIBLE);
                    Toast.makeText(StudentDetailsActivity.this, "Please try again later!", Toast.LENGTH_LONG).show();
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
