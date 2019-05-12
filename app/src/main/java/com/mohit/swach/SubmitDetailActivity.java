package com.mohit.swach;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mohit.swach.Handlers.CenterListHandler;
import com.mohit.swach.Handlers.DailyVisitHandler;
import com.mohit.swach.Handlers.DailyVisitImageHandler;
import com.mohit.swach.extra.Generic;
import com.mohit.swach.extra.NetworkStateReceiver;
import com.mohit.swach.extra.URL;
import com.mohit.swach.models.CenterListModel;
import com.mohit.swach.models.DailyVisitImageModel;
import com.mohit.swach.models.DailyVisitModel;

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

public class SubmitDetailActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, LocationListener, NetworkStateReceiver.NetworkStateReceiverListener {

    private final static int ALL_PERMISSIONS_RESULT = 101;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;
    //////Geo Location
    final String TAG = "GPS";
    Button button_save;
    TextView tv_header_text;
    TextView tv_committee_details, tv_sutdent_details;
    LinearLayout ll_menu_icon;
    EditText et_remark3, et_remark1, et_remark2;
    EditText et_student_number;
    EditText et_remark;
    String centerNameSelected;
    String[] centerName;
    String resultImage[];
    RadioGroup radio_teacher_present, radio_bojan_sanyogi_present, radio_food_present;
    LinearLayout ll_teacher_detail;
    TextView tv_second_teacher_name, tv_second_teacher_edu, tv_second_teacher_mobile;
    TextView tv_first_teacher_name, tv_first_teacher_edu, tv_first_teacher_mobile;
    String userId;
    DailyVisitHandler dailyVisitHandler;
    CenterListHandler centerListHandler;
    DailyVisitImageHandler dailyVisitImageHandler;
    SQLiteDatabase sqLiteDatabase;
    ArrayList<DailyVisitModel> dailyVisitModelArrayList = new ArrayList<DailyVisitModel>();
    ArrayList<DailyVisitImageModel> dailyVisitImageList = new ArrayList<DailyVisitImageModel>();
    /////
    LocationManager locationManager;
    Location loc;
    ArrayList<String> permissions = new ArrayList<>();
    ArrayList<String> permissionsToRequest;
    ArrayList<String> permissionsRejected = new ArrayList<>();
    boolean isGPS = false;
    boolean isNetwork = false;
    boolean canGetLocation = true;
    private ProgressDialog progressDialog;
    private List<CenterListModel> centerList;
    ////Network Detecting
    private NetworkStateReceiver networkStateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_detail);

        dailyVisitHandler = new DailyVisitHandler(getApplicationContext());
        centerListHandler = new CenterListHandler(getApplicationContext());
        dailyVisitImageHandler = new DailyVisitImageHandler(getApplicationContext());
        sqLiteDatabase = dailyVisitHandler.getWritableDatabase();
        sqLiteDatabase.execSQL(DailyVisitHandler.CREATE_QUERY);
        sqLiteDatabase = dailyVisitImageHandler.getWritableDatabase();
        sqLiteDatabase.execSQL(DailyVisitImageHandler.CREATE_QUERY);
        sqLiteDatabase = centerListHandler.getWritableDatabase();
        sqLiteDatabase.execSQL(CenterListHandler.CREATE_QUERY);

        //////Geo Location
        locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
        isGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionsToRequest = findUnAskedPermissions(permissions);
        if (!isGPS && !isNetwork) {
            Log.d(TAG, "Connection off");
            showSettingsAlert();
            getLastLocation();
        } else {
            Log.d(TAG, "Connection on");
            // check permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (permissionsToRequest.size() > 0) {
                    requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]),
                            ALL_PERMISSIONS_RESULT);
                    Log.d(TAG, "Permission requests");
                    canGetLocation = false;
                }
            }
            // get location
            getLocation();
        }
        ////////////

        ////////Network Detecting
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        //////////

        SharedPreferences prefs = getSharedPreferences("LoginData", MODE_PRIVATE);
        String restoredText = prefs.getString("CordID", null);
        userId = restoredText;

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

        ll_teacher_detail = findViewById(R.id.ll_teacher_detail);
        tv_second_teacher_name = findViewById(R.id.tv_second_teacher_name);
        tv_second_teacher_edu = findViewById(R.id.tv_second_teacher_edu);
//        tv_second_teacher_mobile = findViewById(R.id.tv_second_teacher_mobile);
        tv_first_teacher_name = findViewById(R.id.tv_first_teacher_name);
        tv_first_teacher_edu = findViewById(R.id.tv_first_teacher_edu);
//        tv_first_teacher_mobile = findViewById(R.id.tv_first_teacher_mobile);
        ll_teacher_detail.setVisibility(View.GONE);

        tv_committee_details = findViewById(R.id.tv_committee_details);
        tv_sutdent_details = findViewById(R.id.tv_sutdent_details);

        tv_committee_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SubmitDetailActivity.this, CommitteDetailsActivity.class);
                intent.putExtra("centerName", centerNameSelected);
                startActivity(intent);
            }
        });

        tv_sutdent_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SubmitDetailActivity.this, StudentDetailsActivity.class);
                intent.putExtra("centerName", centerNameSelected);
                startActivity(intent);
            }
        });

        et_student_number = findViewById(R.id.et_student_number);
//        et_remark3 = findViewById(R.id.et_remark3);
//        et_remark2 = findViewById(R.id.et_remark2);
//        et_remark1 = findViewById(R.id.et_remark1);
        et_remark = findViewById(R.id.et_remark);

        radio_teacher_present = findViewById(R.id.radio_teacher_present);
        radio_bojan_sanyogi_present = findViewById(R.id.radio_bojan_sanyogi_present);
        radio_food_present = findViewById(R.id.radio_food_present);

        button_save = findViewById(R.id.button_save);
        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean isError = false;
                ////Teacher Present
                if (radio_teacher_present.getCheckedRadioButtonId() != R.id.rb_teacher_yes && radio_teacher_present.getCheckedRadioButtonId() != R.id.rb_teacher_no) {
                    isError = true;
                }
                ////Bhojan Shanjog
                if (radio_bojan_sanyogi_present.getCheckedRadioButtonId() != R.id.rb_bhojan_yes && radio_bojan_sanyogi_present.getCheckedRadioButtonId() != R.id.rb_bhojan_no) {
                    isError = true;
                }
                ////Food Present
                if (radio_food_present.getCheckedRadioButtonId() != R.id.rb_food_yes && radio_food_present.getCheckedRadioButtonId() != R.id.rb_food_no) {
                    isError = true;
                }
                if (et_student_number.getText().toString().equals("")) {
                    isError = true;
                }
                if (isError) {
                    Toast.makeText(SubmitDetailActivity.this, "Please fill all details!", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(SubmitDetailActivity.this, Upload.class);
                    startActivityForResult(intent, 1);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            resultImage = data.getStringArrayExtra("resultImage");
            if (resultImage != null) {
                if (checkInternetConenction())
                    new DailyVisitSave().execute();
                else
                    saveDailyVisitData();
            }
        }

    }

    //Performing action onItemSelected and onNothing selected
    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
        centerNameSelected = centerName[position];
        if (checkInternetConenction()) {
            new GetCentreDetails().execute();
        } else {
            ll_teacher_detail.setVisibility(View.GONE);
        }
    }


    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

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

    private void addCenterListArray() {
        deleteCenterDataList();
        for (int i = 0; i < Generic.centerDataList.size(); i++) {
            CenterListModel centerListModel = Generic.centerDataList.get(i);
            addCenterList(centerListModel);
        }
    }

    public void addCenterList(CenterListModel centerListModel) {
        sqLiteDatabase = centerListHandler.getWritableDatabase();
        centerListHandler.addinnformation(centerListModel, sqLiteDatabase);
    }

    private void deleteCenterDataList() {
        sqLiteDatabase = centerListHandler.getWritableDatabase();
        centerListHandler.deletequery(sqLiteDatabase);
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

    private void saveDailyVisitData() {
        DailyVisitModel dailyVisitModel = new DailyVisitModel();
        dailyVisitModel.DailyVisitID = 0;
        dailyVisitModel.CentreID = getCenterId(centerNameSelected);
        ////Teacher Present
        if (radio_teacher_present.getCheckedRadioButtonId() == R.id.rb_teacher_yes) {
            dailyVisitModel.Remark1 = "Y";
        } else {
            if (radio_teacher_present.getCheckedRadioButtonId() == R.id.rb_teacher_no)
                dailyVisitModel.Remark1 = "N";
            else
                dailyVisitModel.Remark1 = "";
        }
        ////Bhojan Shanjog
        if (radio_bojan_sanyogi_present.getCheckedRadioButtonId() == R.id.rb_bhojan_yes) {
            dailyVisitModel.Remark2 = "Y";
        } else {
            if (radio_bojan_sanyogi_present.getCheckedRadioButtonId() == R.id.rb_bhojan_no)
                dailyVisitModel.Remark2 = "N";
            else
                dailyVisitModel.Remark2 = "";
        }
        ////Food Present
        if (radio_food_present.getCheckedRadioButtonId() == R.id.rb_food_yes) {
            dailyVisitModel.Remark3 = "Y";
        } else {
            if (radio_food_present.getCheckedRadioButtonId() == R.id.rb_food_no)
                dailyVisitModel.Remark3 = "N";
            else
                dailyVisitModel.Remark3 = "";
        }
        dailyVisitModel.Remark5 = et_remark.getText().toString();///Remark
        dailyVisitModel.Remark4 = et_student_number.getText().toString();///Student Present
        dailyVisitModel.VisitDate = "0";
        dailyVisitModel.UserID = userId;
        dailyVisitModel.Latitude = loc.getLatitude() + "";
        dailyVisitModel.Longitude = loc.getLongitude() + "";
        int dailyVisitTableId = addDailyVisit(dailyVisitModel);
        for (int i = 0; i < resultImage.length; i++) {
            if (resultImage[i] != null) {
                DailyVisitImageModel dailyVisitImageModel = new DailyVisitImageModel();
                dailyVisitImageModel.DailyVisitTableId = dailyVisitTableId;
                dailyVisitImageModel.TypeID = 1;
                String image = "image/jpeg;base64," + resultImage[i];
                dailyVisitImageModel.URL_Path = image.replace("\n", "");
                addDailyVisitImage(dailyVisitImageModel);
            }
        }
        Toast.makeText(this, "Order save locally", Toast.LENGTH_SHORT).show();
        et_student_number.setText("");
        et_remark.setText("");
        radio_food_present.clearCheck();
        radio_bojan_sanyogi_present.clearCheck();
        radio_teacher_present.clearCheck();
    }

    public int addDailyVisit(DailyVisitModel dailyVisitModel) {
        sqLiteDatabase = dailyVisitHandler.getWritableDatabase();
        return dailyVisitHandler.addinnformation(dailyVisitModel, sqLiteDatabase);
    }

    public int addDailyVisitImage(DailyVisitImageModel dailyVisitImageModel) {
        sqLiteDatabase = dailyVisitImageHandler.getWritableDatabase();
        return dailyVisitImageHandler.addinnformation(dailyVisitImageModel, sqLiteDatabase);
    }

    ////Geo Location ////////
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged");
        updateUI(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
        getLocation();
    }

    @Override
    public void onProviderDisabled(String s) {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    private void getLocation() {
        try {
            if (canGetLocation) {
                Log.d(TAG, "Can get location");
                if (isGPS) {
                    // from GPS
                    Log.d(TAG, "GPS on");
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    if (locationManager != null) {
                        loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (loc != null)
                            updateUI(loc);
                    }
                } else if (isNetwork) {
                    // from Network Provider
                    Log.d(TAG, "NETWORK_PROVIDER on");
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    if (locationManager != null) {
                        loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (loc != null)
                            updateUI(loc);
                    }
                } else {
                    loc.setLatitude(0);
                    loc.setLongitude(0);
                    updateUI(loc);
                }
            } else {
                Log.d(TAG, "Can't get location");
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void getLastLocation() {
        try {
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, false);
            Location location = locationManager.getLastKnownLocation(provider);
            Log.d(TAG, provider);
            Log.d(TAG, location == null ? "NO LastLocation" : location.toString());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private ArrayList findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList result = new ArrayList();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canAskPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canAskPermission() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case ALL_PERMISSIONS_RESULT:
                Log.d(TAG, "onRequestPermissionsResult");
                for (String perms : permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application.Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.toArray(
                                                        new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }
                } else {
                    Log.d(TAG, "No rejected permissions.");
                    canGetLocation = true;
                    getLocation();
                }
                break;
        }
    }

    public String checkOutDateTime(String bigTime) {
//        final String time = bigTime;
//
//        try {
//            final SimpleDateFormat sdf = new SimpleDateFormat("H:mm");
//            final Date dateObj = sdf.parse(time);
//            return (new SimpleDateFormat("K:mm").format(dateObj));
//        } catch (final ParseException e) {
//            e.printStackTrace();
//        }
//        return "";

        String time[] = bigTime.split(":");
        if (Integer.parseInt(time[0]) > 11) {
            String convertedTime = "" + (Integer.parseInt(time[0]) - 12);
            if ((Integer.parseInt(time[0]) - 12) > 9)
                return "" + convertedTime + ":" + time[1] + " PM";
            else
                return "0" + convertedTime + ":" + time[1] + " PM";
        } else
            return time[0] + ":" + time[1] + " AM";
    }

    public void showSettingsAlert() {
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(this);
        alertDialog.setTitle("GPS is not Enabled!");
        alertDialog.setMessage("Do you want to turn on GPS?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.app.AlertDialog.Builder(SubmitDetailActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void updateUI(Location loc) {
        Log.d(TAG, "updateUI");
//        tvLatitude.setText(Double.toString(loc.getLatitude()));
//        tvLongitude.setText(Double.toString(loc.getLongitude()));
//        tvTime.setText(DateFormat.getTimeInstance().format(loc.getTime()));
//        Toast.makeText(this, DateFormat.getTimeInstance().format(loc.getTime()) + "", Toast.LENGTH_SHORT).show();
//        Toast.makeText(this, loc.getLatitude() + " latitude", Toast.LENGTH_SHORT).show();
//        Toast.makeText(this, loc.getLongitude() + " Longitude", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
        networkStateReceiver.removeListener(this);
        this.unregisterReceiver(networkStateReceiver);
    }

    /////////
    @Override
    public void networkAvailable() {
        Log.d("tommydevall", "I'm in, baby!");
        /* TODO: Your connection-oriented stuff here */
        new GetListCentre().execute();
        syncDailyVisitData();
    }

    @Override
    public void networkUnavailable() {
        Log.d("tommydevall", "I'm dancing with myself");
        /* TODO: Your disconnection-oriented stuff here */
        getCenterListData();
    }

    private boolean checkInternetConenction() {
        @SuppressWarnings("static-access")
        ConnectivityManager connec = (ConnectivityManager) getSystemService(getBaseContext().CONNECTIVITY_SERVICE);
        if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED) {
            return true;
        } else if (
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED) {
            return false;
        }
        return false;
    }

    private void syncDailyVisitData() {
        sqLiteDatabase = dailyVisitHandler.getWritableDatabase();
        Cursor cursor = dailyVisitHandler.getinformation(sqLiteDatabase);
        dailyVisitModelArrayList = new ArrayList<DailyVisitModel>();
        dailyVisitImageList = new ArrayList<DailyVisitImageModel>();
        if (cursor.moveToFirst()) {
            do {
                DailyVisitModel dailyVisitModel = new DailyVisitModel();
                dailyVisitModel.dailyVisitTableId = cursor.getString(cursor.getColumnIndex("DailyVisitTableId"));
                dailyVisitModel.DailyVisitID = cursor.getInt(cursor.getColumnIndex("DailyVisitID"));
                dailyVisitModel.CentreID = cursor.getString(cursor.getColumnIndex("CentreID"));
                dailyVisitModel.Remark1 = cursor.getString(cursor.getColumnIndex("Remark1"));
                dailyVisitModel.Remark2 = cursor.getString(cursor.getColumnIndex("Remark2"));
                dailyVisitModel.Remark3 = cursor.getString(cursor.getColumnIndex("Remark3"));
                dailyVisitModel.Remark5 = cursor.getString(cursor.getColumnIndex("Remark5"));
                dailyVisitModel.Remark4 = cursor.getString(cursor.getColumnIndex("Remark4"));
                dailyVisitModel.VisitDate = cursor.getString(cursor.getColumnIndex("VisitDate"));
                dailyVisitModel.UserID = cursor.getString(cursor.getColumnIndex("UserID"));
                dailyVisitModel.Latitude = cursor.getString(cursor.getColumnIndex("Latitude"));
                dailyVisitModel.Longitude = cursor.getString(cursor.getColumnIndex("Longitude"));
                dailyVisitModelArrayList.add(dailyVisitModel);
                getImageListById(dailyVisitModel.dailyVisitTableId);
            } while (cursor.moveToNext());
        }
        new DailyVisitSaveBulk().execute();
        dailyVisitHandler.close();
    }

    private void getImageListById(String dailyVisitTableId) {
        sqLiteDatabase = dailyVisitImageHandler.getWritableDatabase();
        Cursor cursor = dailyVisitImageHandler.getDataById(sqLiteDatabase, dailyVisitTableId);
        if (cursor.moveToFirst()) {
            do {
                DailyVisitImageModel dailyVisitImageModel = new DailyVisitImageModel();
                dailyVisitImageModel.DailyVisitTableId = cursor.getInt(cursor.getColumnIndex("DailyVisitTableId"));
                dailyVisitImageModel.TypeID = cursor.getInt(cursor.getColumnIndex("TypeID"));
                dailyVisitImageModel.URL_Path = cursor.getString(cursor.getColumnIndex("URL_Path"));
                dailyVisitImageList.add(dailyVisitImageModel);
            } while (cursor.moveToNext());
        }
    }

    private void getCenterListData() {
        ArrayList<CenterListModel> centerList = new ArrayList<CenterListModel>();
        sqLiteDatabase = centerListHandler.getWritableDatabase();
        Cursor cursor = centerListHandler.getinformation(sqLiteDatabase);
        if (cursor.moveToFirst()) {
            do {
                CenterListModel centerListModel = new CenterListModel();
                centerListModel.CentreID = cursor.getInt(cursor.getColumnIndex("CentreID"));
                centerListModel.PSID = cursor.getInt(cursor.getColumnIndex("PSID"));
                centerListModel.GPID = cursor.getInt(cursor.getColumnIndex("GPID"));
                centerListModel.VillageID = cursor.getInt(cursor.getColumnIndex("VillageID"));
                centerListModel.CentreName = cursor.getString(cursor.getColumnIndex("CentreName"));
                centerListModel.DocNo = cursor.getString(cursor.getColumnIndex("DocNo"));
                centerListModel.FirstTeacher = cursor.getString(cursor.getColumnIndex("FirstTeacher"));
                centerListModel.FTQualification = cursor.getString(cursor.getColumnIndex("FTQualification"));
                centerListModel.SecondTeacher = cursor.getString(cursor.getColumnIndex("SecondTeacher"));
                centerListModel.STQualification = cursor.getString(cursor.getColumnIndex("STQualification"));
                centerListModel.TypeID = cursor.getInt(cursor.getColumnIndex("TypeID"));
                centerListModel.MobileNo = cursor.getString(cursor.getColumnIndex("MobileNo"));
                centerListModel.Latitude = cursor.getString(cursor.getColumnIndex("Latitude"));
                centerListModel.Longitude = cursor.getString(cursor.getColumnIndex("Longitude"));
                centerListModel.CordID = cursor.getInt(cursor.getColumnIndex("CordID"));
                centerList.add(centerListModel);
            } while (cursor.moveToNext());
        }
        Generic.centerDataList = centerList;
        centerListHandler.close();
        Log.i("monty CenterList", Generic.centerDataList.get(0).toString());
        centerName = new String[Generic.centerDataList.size()];
        for (int i = 0; i < Generic.centerDataList.size(); i++) {
            centerName[i] = Generic.centerDataList.get(i).CentreName;
        }
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(SubmitDetailActivity.this);
        ArrayAdapter aa = new ArrayAdapter(SubmitDetailActivity.this, android.R.layout.simple_spinner_item, centerName);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(aa);
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
//                json.put("LATITUDE",  loc.getLatitude());
//                json.put("LONGITUDE",  loc.getLongitude());
                json.put("UserID", userId);

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
                        addCenterListArray();
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

    class GetCentreDetails extends AsyncTask<String, Void, Void> {
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
            HttpPost httppost = new HttpPost(URL.GetCentreDetails);
            httppost.setHeader("Content-Type", "application/json");
            try {
                JSONObject json = new JSONObject();

                json.put("CentreID", getCenterId(centerNameSelected));

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
                    returnValue = new JSONObject(jsonobject.getString("ReturnValue"));
                    if (status && !isExceptionOccured) {
                        String firstTeacherName = returnValue.getString("FirstTeacher");
                        String firstTeacherEdu = returnValue.getString("FTQualification");
                        String secondTeacherName = returnValue.getString("SecondTeacher");
                        String secondTeacherEdu = returnValue.getString("STQualification");

                        tv_second_teacher_name.setText(secondTeacherName);
                        tv_second_teacher_edu.setText(secondTeacherEdu);
                        tv_first_teacher_edu.setText(firstTeacherEdu);
                        tv_first_teacher_name.setText(firstTeacherName);
                        ll_teacher_detail.setVisibility(View.VISIBLE);

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

    class DailyVisitSave extends AsyncTask<String, Void, Void> {
        JSONObject jsonobject, returnValue;
        Boolean status;
        JSONArray returnData;
        Boolean isExceptionOccured;
        String errorMessage, message;
        JSONArray imageList;

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
                json.put("DailyVisitID", 0);
                json.put("CentreID", getCenterId(centerNameSelected));
                ////Teacher Present
                if (radio_teacher_present.getCheckedRadioButtonId() == R.id.rb_teacher_yes) {
                    json.put("Remark1", "Y");
                } else {
                    if (radio_teacher_present.getCheckedRadioButtonId() == R.id.rb_teacher_no)
                        json.put("Remark1", "N");
                    else
                        json.put("Remark1", "");
                }
                ////Bhojan Shanjog
                if (radio_bojan_sanyogi_present.getCheckedRadioButtonId() == R.id.rb_bhojan_yes) {
                    json.put("Remark2", "Y");
                } else {
                    if (radio_bojan_sanyogi_present.getCheckedRadioButtonId() == R.id.rb_bhojan_no)
                        json.put("Remark2", "N");
                    else
                        json.put("Remark2", "");
                }
                ////Food Present
                if (radio_food_present.getCheckedRadioButtonId() == R.id.rb_food_yes) {
                    json.put("Remark3", "Y");
                } else {
                    if (radio_food_present.getCheckedRadioButtonId() == R.id.rb_food_no)
                        json.put("Remark3", "N");
                    else
                        json.put("Remark3", "");
                }
                json.put("Remark5", et_remark.getText().toString());///Remark
                json.put("Remark4", et_student_number.getText().toString());///Student Present
                json.put("VisitDate", 0);
                json.put("UserID", userId);
                json.put("Latitude", loc.getLatitude());
                json.put("Longitude", loc.getLongitude());
                for (int i = 0; i < resultImage.length; i++) {
                    if (resultImage[i] != null) {
                        JSONObject imageData = new JSONObject();
                        imageData.put("TypeID", 1);
                        String image = "image/jpeg;base64," + resultImage[i];
                        imageData.put("URL_Path", image.replace("\n", ""));
                        imageList.put(imageData);
                    }
                }
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
                        et_student_number.setText("");
                        et_remark.setText("");
                        radio_food_present.clearCheck();
                        radio_bojan_sanyogi_present.clearCheck();
                        radio_teacher_present.clearCheck();
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

    class DailyVisitSaveBulk extends AsyncTask<String, Void, Void> {
        JSONObject jsonobject, returnValue;
        Boolean status;
        JSONArray returnData;
        Boolean isExceptionOccured;
        String errorMessage, message;
        JSONArray imageList;
        JSONArray dailyVisitArray;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(String... strings) {
            HttpClient httpclient = new DefaultHttpClient();
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            HttpPost httppost = new HttpPost(URL.DailyVisitSaveBulk);
            httppost.setHeader("Content-Type", "application/json");
            try {
                JSONObject json = new JSONObject();
                dailyVisitArray = new JSONArray();
                for (int i = 0; i < dailyVisitModelArrayList.size(); i++) {
                    imageList = new JSONArray();
                    JSONObject dailyVisitJson = new JSONObject();
                    dailyVisitJson.put("DailyVisitID", dailyVisitModelArrayList.get(i).DailyVisitID);
                    dailyVisitJson.put("CentreID", dailyVisitModelArrayList.get(i).CentreID);
                    dailyVisitJson.put("UserID", dailyVisitModelArrayList.get(i).UserID);
                    dailyVisitJson.put("Remark1", dailyVisitModelArrayList.get(i).Remark1);
                    dailyVisitJson.put("Remark2", dailyVisitModelArrayList.get(i).Remark2);
                    dailyVisitJson.put("Remark3", dailyVisitModelArrayList.get(i).Remark3);
                    dailyVisitJson.put("Remark4", dailyVisitModelArrayList.get(i).Remark4);
                    dailyVisitJson.put("Remark5", dailyVisitModelArrayList.get(i).Remark5);
                    dailyVisitJson.put("Latitude", dailyVisitModelArrayList.get(i).Latitude);
                    dailyVisitJson.put("Longitude", dailyVisitModelArrayList.get(i).Longitude);
                    dailyVisitJson.put("WorkFlag", "A");
                    for (int j = 0; j < dailyVisitImageList.size(); j++) {
                        if (dailyVisitModelArrayList.get(i).dailyVisitTableId.equals(dailyVisitImageList.get(j).DailyVisitTableId+"")) {
                            JSONObject dailyVisitImageJson = new JSONObject();
                            dailyVisitImageJson.put("TypeID", dailyVisitImageList.get(j).TypeID);
                            dailyVisitImageJson.put("DailyVisitID", dailyVisitModelArrayList.get(i).DailyVisitID);
                            dailyVisitImageJson.put("URL_Path", dailyVisitImageList.get(j).URL_Path);
                            imageList.put(dailyVisitImageJson);
                        }
                    }
                    dailyVisitJson.put("DailyVisitIVList", imageList);
                    dailyVisitArray.put(dailyVisitJson);
                }
                json.put("visits", dailyVisitArray);
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
                        et_student_number.setText("");
                        et_remark.setText("");
                        radio_food_present.clearCheck();
                        radio_bojan_sanyogi_present.clearCheck();
                        radio_teacher_present.clearCheck();
                        deleteDailyVisit();
                        deleteDailyVisitImage();
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

    private void deleteDailyVisit() {
        sqLiteDatabase = dailyVisitHandler.getWritableDatabase();
        dailyVisitHandler.deletequery(sqLiteDatabase);
    }

    private void deleteDailyVisitImage() {
        sqLiteDatabase = dailyVisitImageHandler.getWritableDatabase();
        dailyVisitImageHandler.deletequery(sqLiteDatabase);
    }
}
