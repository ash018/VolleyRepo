package info.mis.motorequipment.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.thomashaertel.widget.MultiSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import info.mis.motorequipment.Entity.LocationX;
import info.mis.motorequipment.R;
import info.mis.motorequipment.app.AppConfig;
import info.mis.motorequipment.app.AppController;
import info.mis.motorequipment.helper.ForwardActivityAdapter;
import info.mis.motorequipment.helper.GPSTracker;
import info.mis.motorequipment.helper.GPSTracker2;
import info.mis.motorequipment.helper.SQLiteHandler;
import info.mis.motorequipment.helper.SessionManager;

import static com.google.android.gms.location.LocationRequest.create;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;
import static info.mis.motorequipment.app.AppController.TAG;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;


public class PendingServiceDetailActivity extends ListActivity {

    private EditText ModelEdit, RemarkEdit, ChassisEdit, HRMEdit, ServiceTypeEdit;
    private MultiSpinner forwardTo;
    private TextView serviceTime,ownerTxt,Assigned,CompanyNameTxt;
    private Button btnForward;

    private ProgressDialog pDialog;

    private SessionManager session;

    private static final int REQUEST_PERMISSION_LOCATION = 255; // int should be between 0 and 255

    GPSTracker gps;

    private ArrayList permissionsToRequest = new ArrayList();
    private ArrayList permissionsRejected = new ArrayList();
    private ArrayList permissions = new ArrayList();

    private final static int ALL_PERMISSIONS_RESULT = 101;
    GPSTracker locationTrack;

    private LocationRequest mLocationRequest;

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    private AlertDialog.Builder builder1;

    private SQLiteHandler db;

    private boolean[] selectedItems;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_service_detail);

        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);

        Bundle bundle = getIntent().getExtras();

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        final String serviceId = bundle.getString("ServiceID");
        final String RoleId = bundle.getString("RoleId");
        final String UserName = bundle.getString("UserName");
        final String UserId = bundle.getString("UserId");
        final String ServiceColor = bundle.getString("ServiceColor");

        final LocationX[] lx = new LocationX[1];

        serviceTime = (TextView) findViewById(R.id.ServiceTime);
        ownerTxt = (TextView) findViewById(R.id.txt2);
        Assigned =(TextView) findViewById(R.id.Assigned);
        CompanyNameTxt = (TextView) findViewById(R.id.CompanyName);

        ModelEdit = (EditText) findViewById(R.id.Model);
        RemarkEdit = (EditText) findViewById(R.id.remarks);
        ChassisEdit = (EditText) findViewById(R.id.chassis);
        HRMEdit = (EditText) findViewById(R.id.hrm);
        ServiceTypeEdit = (EditText) findViewById(R.id.serviceType);


        ListView lv = (ListView) findViewById(android.R.id.list);
        lv.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        // forwardTo = (MultiSpinner) findViewById(R.id.forwardTo);

        setServiceRequest(serviceId,UserId);

        Intent intent = getIntent();

        String[] userNameArray = intent.getStringArrayExtra("userNameArray");
        String[] RoleArray = intent.getStringArrayExtra("RoleNameArray");
        final String[] StaffIdArray = intent.getStringArrayExtra("StaffIdArray");

//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(PendingServiceDetailActivity.this, android.R.layout.simple_dropdown_item_1line, userNameArray);
//        forwardTo.setAdapter(adapter,false,onSelectedListener);

        final ForwardActivityAdapter adapter = new ForwardActivityAdapter(this, userNameArray,RoleArray,StaffIdArray);
        setListAdapter(adapter);

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage(this.getResources().getString(R.string.gps_network_not_enabled));
            dialog.setPositiveButton(this.getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);


                }
            });
            dialog.setNegativeButton(this.getString(R.string.Cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub

                }
            });
            dialog.show();
        }

        View linearLayout = findViewById(R.id.tID2);


        int bId = 2;
        btnForward = new Button(this);
        if (RoleId.equals("1") || RoleId.equals("2") || RoleId.equals("3") || (RoleId.equals("5") && ServiceColor.equals("2"))) {
            btnForward.setText("Forward");
        }
        if((ServiceColor.equals("2") && (RoleId.equals("3"))) || (ServiceColor.equals("1") && RoleId.equals("5"))){

            btnForward.setEnabled(false);
            btnForward.setClickable(false);
            btnForward.setVisibility(View.INVISIBLE);

            AlertDialog alertDialog = new AlertDialog.Builder(PendingServiceDetailActivity.this).create();
            alertDialog.setTitle("Status");
            alertDialog.setMessage("Job Is in Process. Thank you!");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }

        if(ServiceColor.equals("3") ){
            btnForward.setEnabled(false);
            btnForward.setClickable(false);

            btnForward.setVisibility(View.GONE);
            AlertDialog alertDialog = new AlertDialog.Builder(PendingServiceDetailActivity.this).create();
            alertDialog.setTitle("Status");
            alertDialog.setMessage("Job Is Done. Thank you!");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();

        }
        else{
            btnForward.setText("Done");
        }
        btnForward.setTextColor(getResources().getColor(R.color.white));
        btnForward.setId(bId);
        btnForward.setLayoutParams(new android.support.v7.app.ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT));
        btnForward.setBackgroundColor(getResources().getColor(R.color.bg_login));

        ((LinearLayout) linearLayout).addView(btnForward);


        btnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Toast.makeText(getApplicationContext(), String.valueOf(lx.getLatitude()), Toast.LENGTH_SHORT).show();

                String Model = ModelEdit.getText().toString();
                String Chassis = ChassisEdit.getText().toString();
                String HRM = HRMEdit.getText().toString();
                String Remark = RemarkEdit.getText().toString();


                if ( RoleId.equals("3")) {
                    ArrayList<String> assignList = adapter.getData();

                   // String stuffId = StaffIdArray[(int) forwardTo.getSelected()];
                    //Log.d("Selected",forwardTo.);
                    requestForwardSubmit(RoleId, UserName, UserId, serviceId, assignList, Model, Chassis, HRM, Remark);
                }

                if(RoleId.equals("1") || RoleId.equals("2")){
                    ArrayList<String> assignList = adapter.getData();

                    if(assignList.size()!=0) {
                        requestForwardSubmit(RoleId, UserName, UserId, serviceId, assignList, Model, Chassis, HRM, Remark);
                    }
                    else{
                        AlertDialog alertDialog = new AlertDialog.Builder(PendingServiceDetailActivity.this).create();
                        alertDialog.setTitle("Status");
                        alertDialog.setMessage("Please Select Anyone from Forwarding List");
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                    }
                }

                else {
                    locationTrack = new GPSTracker(PendingServiceDetailActivity.this);
                    if(locationTrack.canGetLocation()){
                        double longitude = locationTrack.getLongitude();
                        double latitude = locationTrack.getLatitude();
                        //Toast.makeText(getApplicationContext(), "Latitude:" + latitude + "Longitude:" + longitude, Toast.LENGTH_LONG).show();
                        askPermissions();

                        startLocationUpdates(RoleId, UserName, UserId, serviceId, Model, Chassis, HRM, Remark);

                    }
                    else{
                        locationTrack.showSettingsAlert();
                    }

                }

            }
        });



    }

    private MultiSpinner.MultiSpinnerListener onSelectedListener = new MultiSpinner.MultiSpinnerListener() {
        public void onItemsSelected(boolean[] selected) {
            // Do something here with the selected items

            Log.d("Selected Func","");
        }
    };

    // Trigger new location updates at interval
    @SuppressLint("MissingPermission")
    public void startLocationUpdates(final String RoleId,final String UserName,final String UserId,final String serviceId,final String Model,final String Chassis,final String HRM,final String Remark) {

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        final LocationX[] lx = new LocationX[1];

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                public void onLocationResult(LocationResult locationResult) {

                // do work here
                lx[0] = onLocationChanged(locationResult.getLastLocation());
                String lat = lx[0].getLatitude();
                String lng = lx[0].getLongitude();

                Log.d("INSIDE setOnClick",lat);
                        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
                        Log.d("PermissionX",lat+lng);
                        if(!lat.equals("0.0") && !lng.equals("0.0") && !lat.equals("") && !lng.equals("") ) {
                            Log.d("PermissionX",String.valueOf(permissionCheck));
                            requestSubmitWithGPS(RoleId, UserName, UserId, serviceId, Model, Chassis, HRM, Remark, lat, lng);
                        }
                else{
                    new AlertDialog.Builder(getApplicationContext())
                            .setTitle("Attention")
                            .setMessage("Please Enable GPS")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).setNegativeButton("No", null).show();
                }

            }
        },
        Looper.myLooper());

    }

    public void askPermissions(){
        int completed = 0;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ){//Can add more as per requirement
            completed = 0;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                    123);
        }else if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                ){
            completed = 1;

        }



    }

    public LocationX onLocationChanged(Location location) {
        // New location has now been determined
        String msg = "Updated Location!!!: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
       // Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        LocationX lx = new LocationX();
        lx.setLatitude(Double.toString(location.getLatitude()));
        lx.setLongitude(Double.toString(location.getLongitude()));
        Log.d("On Location Result-->",msg);

        return lx;
        // You can now create a LatLng Object for use with maps

    }

    private void setServiceRequest(String serviceId,String UserId) {
        String tag_string_req = "req_assign";

        StringRequest strReq = new StringRequest(Request.Method.GET,
                AppConfig.PENDING_SERVICE + "?ServiceId=" + String.valueOf(serviceId)+"&UserId="+String.valueOf(UserId), new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Getting Pending Service: " + response.toString());
//                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    //boolean error = jObj.getBoolean("error");
                    String status = jObj.getString("StatusCode");
                    Log.d("Pending--->", status);
                    // Check for error node in json
                    if (status.equals("200")) {

                        // user successfully logged in
                        // Create login session
//                        Toast.makeText(getApplicationContext(),"Successfully Logged In",Toast.LENGTH_SHORT);
//                        session.setLogin(true);
                        String StatusMessage = jObj.getString("StatusMessage");
                        JSONArray statusMessageArray = new JSONArray(StatusMessage);

                        String ServiceType = statusMessageArray.getJSONObject(0).getString("ServiceType");
                        String Model = statusMessageArray.getJSONObject(0).getString("Model");
                        String Remark = statusMessageArray.getJSONObject(0).getString("Remark");
                        String ContactPersonType = statusMessageArray.getJSONObject(0).getString("ContactPersonType");
                        String CompanyName = statusMessageArray.getJSONObject(0).getString("CompanyName");
                        String Chassis = statusMessageArray.getJSONObject(0).getString("Chassis");
                        String ContactPerson = statusMessageArray.getJSONObject(0).getString("ContactPerson");
                        String ServiceTime = statusMessageArray.getJSONObject(0).getString("ServiceTime");
                        String HRM = statusMessageArray.getJSONObject(0).getString("HRM");
                        String assigned = statusMessageArray.getJSONObject(0).getString("Assigned");


                        ModelEdit.setText(Model);
                        RemarkEdit.setText(Remark);
                        ChassisEdit.setText(Chassis);
                        HRMEdit.setText(HRM);
                        ServiceTypeEdit.setText(ServiceType);

                        serviceTime.setText(ServiceTime);
                        ownerTxt.setText(ContactPerson);
                        Assigned.setText(assigned);
                        CompanyNameTxt.setText(CompanyName);


                    } else {
                        // Error in login. Get the error message
                        String statusCode = jObj.getString("StatusCode");
                        String errorMsg = jObj.getString("StatusMessage");
                        Toast.makeText(getApplicationContext(), "ERROR IN LOGIN", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
//                hideDialog();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        requestQueue.add(strReq);

        // Adding request to request queue

        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

        requestQueue.getCache().clear();

    }

    private void requestForwardSubmit(final String RoleId,final String UserName,final String UserId,final String serviceId,final ArrayList<String> assignList,final String Model,final String Chassis,final String HRM,final String Remark) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        //pDialog.setMessage("SENDING TO SERVER ...");
        //showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.FORWARD_SUBMIT_POST, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                //hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    //boolean error = jObj.getBoolean("error");
                    String status = jObj.getString("StatusCode");
                    Log.d("Status--->",status);
                    // Check for error node in json
                    if (status.equals("200")) {
                        // user successfully logged in
                        // Create login session
                        Toast.makeText(getApplicationContext(),"Successfully Logged In",Toast.LENGTH_SHORT);
                        //session.setLogin(true);


//
                        Bundle bundle = new Bundle();
                        bundle.putString("RoleId",RoleId);
                        bundle.putString("UserName",UserName);
                        bundle.putString("UserId",UserId);



                        // Launch main activity
                        Intent intent = new Intent(PendingServiceDetailActivity.this,MainActivity.class);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        finish();
                    } else {

                        Toast.makeText(getApplicationContext(),"ERROR IN LOGIN",Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                //hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("entryBy",UserId);
                params.put("serviceId", serviceId);
                params.put("staffId", assignList.toString());
                params.put("model", Model);
                params.put("chassis", Chassis);
                params.put("hrm", HRM);
                params.put("remarks", Remark);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void requestSubmitWithGPS(final String RoleId,final String UserName,final String UserId,final String serviceId,final String Model,final String Chassis,final String HRM,final String Remark,final String latitude,final String longitude) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("SENDING TO SERVER ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.JOB_DONE, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                //hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    //boolean error = jObj.getBoolean("error");
                    String status = jObj.getString("StatusCode");
                    Log.d("Status--->",status);
                    // Check for error node in json
                    if (status.equals("200")) {

//
                        Bundle bundle = new Bundle();
                        bundle.putString("RoleId",RoleId);
                        bundle.putString("UserName",UserName);
                        bundle.putString("UserId",UserId);




                        // Launch main activity
                        Intent intent = new Intent(PendingServiceDetailActivity.this,MainActivity.class);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(),"ERROR IN LOGIN",Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                //hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("entryBy",UserId);
                params.put("serviceId", serviceId);
                params.put("model", Model);
                params.put("chassis", Chassis);
                params.put("hrm", HRM);
                params.put("remarks", Remark);
                params.put("latitude", String.valueOf(latitude));
                params.put("longitude", String.valueOf(longitude));

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

//    private void showDialog() {
//        if (!pDialog.isShowing())
//            pDialog.show();
//    }
//
//    private void hideDialog() {
//        if (pDialog.isShowing())
//            pDialog.dismiss();
//    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menup, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.mybutton) {
            logoutUser();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        final Bundle bundle = getIntent().getExtras();
        final String RoleId = bundle.getString("RoleId");
        final String UserName = bundle.getString("UserName");
        final String UserId = bundle.getString("UserId");


        bundle.putString("RoleId",RoleId);
        bundle.putString("UserName",UserName);
        bundle.putString("UserId",UserId);


        Intent intent = new Intent(PendingServiceDetailActivity.this, PendingServiceActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    private void logoutUser() {
//        session.setLogin(false);
//
//        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(PendingServiceDetailActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
