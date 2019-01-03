package info.mis.motorequipment.activity;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import java.util.HashMap;

import java.util.Map;

import info.mis.motorequipment.R;
import info.mis.motorequipment.app.AppConfig;
import info.mis.motorequipment.app.AppController;
import info.mis.motorequipment.helper.ForwardActivityAdapter;
import info.mis.motorequipment.helper.SQLiteHandler;
import info.mis.motorequipment.helper.SessionManager;

import static info.mis.motorequipment.app.AppController.TAG;

public class ForwardActivity extends ListActivity   {

    private TextView txtName,txtEmail;
    private Button btnSubmit,btnLogout;

    private SQLiteHandler db;
    private SessionManager session;

    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forward);

        txtName = (TextView) findViewById(R.id.name);
        txtEmail = (TextView) findViewById(R.id.email);
        btnSubmit = (Button) findViewById(R.id.btn);
//        btnLogout = (Button) findViewById(R.id.btnLogout);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        db = new SQLiteHandler(getApplicationContext());
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        Bundle bundle = getIntent().getExtras();

        final String RoleId = bundle.getString("RoleId");
        final String UserName = bundle.getString("UserName");
        final String UserId = bundle.getString("UserId");

        txtName.setText(UserName);
        txtEmail.setText(UserId);



        // Logout button click event
//        btnLogout.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                logoutUser();
//            }
//
//        });



        final String companyName = bundle.getString("companyName");
        final String Owner = bundle.getString("Owner");
        final String Model = bundle.getString("Model");
        final String Chassis = bundle.getString("Chassis");
        final String Hrm = bundle.getString("Hrm");
        final String Remarks = bundle.getString("Remarks");
        final String Datetime = bundle.getString("Datetime");
        final String contact_person = bundle.getString("contact_person");
        final String service_type = bundle.getString("service_type");

        String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
                "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
                "Linux", "OS/2" };

        Intent intent = getIntent();

        String [] userNameArray = intent.getStringArrayExtra("userNameArray");
        String [] RoleArray = intent.getStringArrayExtra("RoleNameArray");
        String [] StaffIdArray = intent.getStringArrayExtra("StaffIdArray");


        final ForwardActivityAdapter adapter = new ForwardActivityAdapter(this, userNameArray,RoleArray,StaffIdArray);
        setListAdapter(adapter);




        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> assignList = adapter.getData();
                ArrayList roleList = adapter.getRoleList();

                int engCount = 0,speCount = 0, mecCount=0;

                for(int i=0;i<roleList.size();i++){
                    Log.d("automated",String.valueOf(roleList.get(i)));
                    if(String.valueOf(roleList.get(i)).trim().equals("Engineer")){
                        engCount = engCount + 1;
                    }
                    if(String.valueOf(roleList.get(i)).trim().equals("Sr.SPE")){
                        speCount = speCount + 1;
                    }
                    if(String.valueOf(roleList.get(i)).trim().equals("Mechanic")){
                        mecCount = mecCount + 1;
                    }
                }

                Log.d("count!!","count"+String.valueOf(engCount)+"spe"+String.valueOf(speCount));

                if(speCount==0 && RoleId.equals("1")){
                    AlertDialog alertDialog = new AlertDialog.Builder(ForwardActivity.this).create();
                    alertDialog.setTitle("Status");
                    alertDialog.setMessage("Please Select At least one Sr. SPE");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();

                    return;
                }

                if(RoleId.equals("2") && (speCount==0 || mecCount == 0)){
                    AlertDialog alertDialog = new AlertDialog.Builder(ForwardActivity.this).create();
                    alertDialog.setTitle("Status");
                    alertDialog.setMessage("Please Select At least one Sr. SPE & One Mechanic");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();

                    return;
                }

                if(RoleId.equals("3") && engCount==0){
                    AlertDialog alertDialog = new AlertDialog.Builder(ForwardActivity.this).create();
                    alertDialog.setTitle("Status");
                    alertDialog.setMessage("Please Select At least one Engineer");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();

                    return;
                }

                if(RoleId.equals("3") && engCount==0 && speCount==0){
                    AlertDialog alertDialog = new AlertDialog.Builder(ForwardActivity.this).create();
                    alertDialog.setTitle("Status");
                    alertDialog.setMessage("Please Select At least one Responsible Person");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();

                    return;
                }

                else{
                    requestSubmit(RoleId,UserName,UserId,companyName,Owner,Model,Chassis,Hrm,Remarks,Datetime,contact_person,service_type,assignList);
                    Log.d("ELSE INSIDE","count"+String.valueOf(engCount)+"spe"+String.valueOf(speCount));
                }

            }
        });

    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menup, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_label) {
            logoutUser();
            Log.d("logout","logout has happend");
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestSubmit(final String RoleId,final String UserName,final String UserId,final String companyName,final String owner,final String model,final String chassis,final String hrm,final String remarks,final String datetime, final String contact_person, final String service_type,final ArrayList<String> assignList) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("SENDING TO SERVER ...");
        showDialog();



        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.SAVE_SERVICE, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                hideDialog();

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
                        session.setLogin(true);


                        Bundle bundle = new Bundle();
                        bundle.putString("RoleId",RoleId);
                        bundle.putString("UserName",UserName);
                        bundle.putString("UserId",UserId);



                        // Launch main activity
                        Intent intent = new Intent(ForwardActivity.this,MainActivity.class);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        finish();
                    } else {
                        // Error in login. Get the error message
//                        String statusCode = jObj.getString("StatusCode");
//                        String errorMsg = jObj.getString("StatusMessage");
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
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("entryBy",UserId);
                params.put("companyName", companyName);
                params.put("owner", owner);
                params.put("model", model);
                params.put("chassis", chassis);
                params.put("hrm", hrm);
                params.put("remarks", remarks);
                params.put("DateTime", datetime);
                params.put("ContactPerson", contact_person);
                params.put("ServiceType", service_type);
                params.put("assignList", assignList.toString());

                Log.d("assignlist",assignList.toString());

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
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

        Intent intent = new Intent(ForwardActivity.this, CreateRequestActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }


    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(ForwardActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


}
