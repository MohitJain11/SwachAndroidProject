package com.android.swach;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.swach.extra.URL;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;


public class LoginActivity extends AppCompatActivity {

    Button button_login;
    Button button_verify_otp;
    AppCompatEditText password, login_id;
    private ProgressDialog progressDialog;
    LinearLayout ll_login_form, ll_otp_form;
    AppCompatEditText et_otp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences prefs = getSharedPreferences("LoginData", MODE_PRIVATE);
        String restoredText = prefs.getString("isLogin", null);
        if (restoredText != null && restoredText.equals("yes")) {
            Intent intent = new Intent(LoginActivity.this, SubmitDetailActivity.class);
            startActivity(intent);
            finish();
        }

        ll_otp_form = findViewById(R.id.ll_otp_form);
        ll_login_form = findViewById(R.id.ll_login_form);
        ll_login_form.setVisibility(View.VISIBLE);
        ll_otp_form.setVisibility(View.GONE);

        et_otp = findViewById(R.id.et_otp);
        login_id = findViewById(R.id.login_id);
        password = findViewById(R.id.password);

        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading");

        button_login = findViewById(R.id.button_login);
        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new UserLogin().execute();
            }
        });

        button_verify_otp = findViewById(R.id.button_verify_otp);
        button_verify_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new CheckOTP().execute();
            }
        });
    }

    class UserLogin extends AsyncTask<String, Void, Void> {
        JSONObject jsonobject, returnValue;
        Boolean status;
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
            HttpPost httppost = new HttpPost(URL.UserLogin);
            httppost.setHeader("Content-Type", "application/json");
            try {
                JSONObject json = new JSONObject();
                json.put("UserName", login_id.getText().toString());
                json.put("Password", password.getText().toString());
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
                        returnValue = new JSONObject(jsonobject.getString("ReturnValue"));
                        SharedPreferences.Editor editor = getSharedPreferences("LoginData", MODE_PRIVATE).edit();
//                        editor.putString("Role", returnValue.getString("Role"));
                        editor.putString("CordID", returnValue.getString("CordID"));
                        editor.putString("CordName", returnValue.getString("CordName"));
                        editor.putString("MobileNo", returnValue.getString("MobileNo"));
                        editor.putString("UserName", returnValue.getString("UserName"));
                        editor.putString("Password", returnValue.getString("Password"));
                        editor.putString("isLogin", "no");
                        editor.commit();
//                        progressDialog.dismiss();
                        new OtpGenerate().execute();

                    } else {
                        errorMessage = jsonobject.getString("ErrorMessage");
                        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Please try again later!", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                progressDialog.dismiss();
            } finally {
//                progressDialog.dismiss();
            }
        }
    }


    class OtpGenerate extends AsyncTask<String, Void, Void> {
        JSONObject jsonobject, returnValue;
        Boolean status;
        Boolean isExceptionOccured;
        String errorMessage, message;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            progressDialog.show();
        }

        @Override
        protected Void doInBackground(String... strings) {
            HttpClient httpclient = new DefaultHttpClient();
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            HttpPost httppost = new HttpPost(URL.OtpGenerate);
            httppost.setHeader("Content-Type", "application/json");
            try {
                JSONObject json = new JSONObject();
                SharedPreferences prefs = getSharedPreferences("LoginData", MODE_PRIVATE);
                String mobileNumber = prefs.getString("MobileNo", null);
                if (mobileNumber != null) {
                    json.put("MobileNo", mobileNumber);
                } else {
                    json.put("MobileNo", "");
                }
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
                        errorMessage = jsonobject.getString("ErrorMessage");

                        //for Testing
                        et_otp.setText(errorMessage);

                        ll_login_form.setVisibility(View.GONE);
                        ll_otp_form.setVisibility(View.VISIBLE);
                        progressDialog.dismiss();
                    } else {
                        errorMessage = jsonobject.getString("ErrorMessage");
                        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Please try again later!", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                progressDialog.dismiss();
            }
        }
    }


    class CheckOTP extends AsyncTask<String, Void, Void> {
        JSONObject jsonobject, returnValue;
        Boolean status;
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
            HttpPost httppost = new HttpPost(URL.CheckOTP);
            httppost.setHeader("Content-Type", "application/json");
            try {
                JSONObject json = new JSONObject();
                SharedPreferences prefs = getSharedPreferences("LoginData", MODE_PRIVATE);
                String mobileNumber = prefs.getString("MobileNo", null);
                if (mobileNumber != null) {
                    json.put("MobileNo", mobileNumber);
                } else {
                    json.put("MobileNo", "");
                }
                json.put("OTP", et_otp.getText().toString());
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
                        SharedPreferences.Editor editor = getSharedPreferences("LoginData", MODE_PRIVATE).edit();
                        editor.putString("isLogin", "yes");
                        editor.commit();
                        progressDialog.dismiss();
                        Intent intent = new Intent(LoginActivity.this, SubmitDetailActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        errorMessage = jsonobject.getString("ErrorMessage");
                        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Please try again later!", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                progressDialog.dismiss();
            }
        }
    }
}
