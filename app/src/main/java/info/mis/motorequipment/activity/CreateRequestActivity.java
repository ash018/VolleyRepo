package info.mis.motorequipment.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.codetroopers.betterpickers.timepicker.TimePickerBuilder;
import com.codetroopers.betterpickers.timepicker.TimePickerDialogFragment;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.mis.motorequipment.Entity.EquipmentRequest;
import info.mis.motorequipment.R;
import info.mis.motorequipment.app.AppConfig;
import info.mis.motorequipment.app.AppController;
import info.mis.motorequipment.helper.ForwardActivityAdapter;
import info.mis.motorequipment.helper.SQLiteHandler;
import info.mis.motorequipment.helper.SessionManager;

import static info.mis.motorequipment.app.AppController.TAG;

public class CreateRequestActivity extends AppCompatActivity implements
        View.OnClickListener{


    private MaterialBetterSpinner contact_person_spinner, service_type_spinner ;

    private TextView txtDate,txtTime;
    private EditText company_name,owner,model,chassis,hrm,remarks;

    private SQLiteHandler db;
    private SessionManager session;

    private ProgressDialog pDialog;

    java.sql.Time timeValue;
    private Button btntimepicker,btndatepicker,btnRegister;
    SimpleDateFormat format;
    Calendar c;
    int mYear, mMonth, mDay, mHour, mMinute;

    SimpleDateFormat formatter;

    String[] SPINNER_DATA = {"SCHEDULE","WARRANTY","PAID","COMMISSIONING"};
    String[] CONTACT_PERSON = {"OWNER","OPERATOR","ENGINEER"};

    private android.app.AlertDialog.Builder builder1;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_request);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        txtDate = (TextView) findViewById(R.id.txtdate);
        txtTime = (TextView) findViewById(R.id.txttime);
        company_name = (EditText) findViewById(R.id.company_name);
        owner = (EditText) findViewById(R.id.owner);
        model = (EditText) findViewById(R.id.model);
        chassis = (EditText) findViewById(R.id.chassis);
        hrm = (EditText) findViewById(R.id.hrm);
        remarks = (EditText) findViewById(R.id.remarks);
        btndatepicker = (Button) findViewById(R.id.btndatepicker);
        btntimepicker = (Button) findViewById(R.id.btntimepicker);

        final Spinner contact_person_spinner = (Spinner) findViewById(R.id.contact_person);
        final Spinner service_type_spinner = (Spinner) findViewById(R.id.service_type);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(CreateRequestActivity.this, android.R.layout.simple_dropdown_item_1line, SPINNER_DATA);
        ArrayAdapter<String> adapterConatct = new ArrayAdapter<String>(CreateRequestActivity.this, android.R.layout.simple_dropdown_item_1line, CONTACT_PERSON);

        contact_person_spinner.setAdapter(adapterConatct);
        service_type_spinner.setAdapter(adapter);

        btndatepicker.setOnClickListener(this);
        btntimepicker.setOnClickListener(this);

        View linearLayout =  findViewById(R.id.tID2);


//        ActionBar actionBar = getActionBar();
//        actionBar.setIcon(R.drawable.logout);



        builder1 = new android.app.AlertDialog.Builder(this);


        int bId = 2;
        btnRegister = new Button(this);
        btnRegister.setText("Forward");
        btnRegister.setTextColor(getResources().getColor(R.color.white));
        btnRegister.setId(bId);
        btnRegister.setLayoutParams(new android.support.v7.app.ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,ActionBar.LayoutParams.WRAP_CONTENT));
        btnRegister.setBackgroundColor(getResources().getColor(R.color.bg_login));

        ((LinearLayout) linearLayout).addView(btnRegister);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String Date = txtDate.getText().toString();
                final String Time = txtTime.getText().toString();
                final String Datetime =Date + " " + Time;


                final String companyName = company_name.getText().toString();
                final String Owner = owner.getText().toString();
                final String Model = model.getText().toString();
                final String Chassis = chassis.getText().toString();
                final String Hrm = hrm.getText().toString();
                final String Remarks = remarks.getText().toString();
                final String contact_person = String.valueOf(contact_person_spinner.getSelectedItem());
                final String service_type = String.valueOf(service_type_spinner.getSelectedItem());

                Bundle bundle = getIntent().getExtras();
                final String RoleId = bundle.getString("RoleId");
                final String UserName = bundle.getString("UserName");
                final String UserId = bundle.getString("UserId");


                if(!RoleId.isEmpty() && !Date.isEmpty() && !Time.isEmpty() && !companyName.isEmpty() && !contact_person.isEmpty() && !Owner.isEmpty()){
                    getAssignedUser(RoleId,UserName,UserId,companyName,Owner,Model,Chassis,Hrm,Remarks,Datetime,contact_person,service_type);
                }

                else{
                    openDialog();
                }

                //submitRequest(companyName,Owner,Model,Chassis,Hrm,Remarks,Datetime,contact_person,service_type);
            }
        });

        //super.onBackPressed();

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

    public void openDialog() {

        new AlertDialog.Builder(this)
                .setTitle("Attention")
                .setMessage("Please Provide All the Information")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setNegativeButton("No", null).show();


    }

    private void getAssignedUser(final String RoleId,final String UserName,final String UserId,final String companyName,final String Owner,final String Model,final String Chassis,final String Hrm,final String Remarks,final String Datetime,final String contact_person,final String service_type) {

        String tag_string_req = "req_assign";

        pDialog.setMessage("Getting Users to Assign ...");
        showDialog();

        String config = null;
        if(RoleId.equals("1")) {
            config = AppConfig.REQUEST_SEND_1;
        }
        else{
            config = AppConfig.REQUEST_SEND_2_3+"="+RoleId;
        }

        Log.d("Role",RoleId);

        StringRequest strReq = new StringRequest(Request.Method.GET,
                config, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Create Activity Response: " + response.toString());
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
//                        Toast.makeText(getApplicationContext(),"Successfully Logged In",Toast.LENGTH_SHORT);
//                        session.setLogin(true);

                        // Now store the user in SQLite
                        String StatusMessage = jObj.getString("StatusMessage");

                        JSONArray statusMessageArray = new JSONArray(StatusMessage);

                        String [] userName = new String[]{};
                        String [] RoleNameArr = new String[]{};
                        String [] StaffIdArr = new String[]{};


                        List<String> listFromArray = Arrays.asList(userName);
                        List<String> listFromArrayRole = Arrays.asList(RoleNameArr);
                        List<String> listFromArrayStaffIdArr = Arrays.asList(StaffIdArr);

                        List<String> tempListUsername = new ArrayList<String>(listFromArray);
                        List<String> tempListRole = new ArrayList<String>(listFromArrayRole);
                        List<String> tempListStaffId = new ArrayList<String>(listFromArrayStaffIdArr);


                        for(int i=0;i<statusMessageArray.length();i++){
                            String RoleId = statusMessageArray.getJSONObject(i).getString("RoleId");
                            String UserName = statusMessageArray.getJSONObject(i).getString("UserName");
                            String UserId = statusMessageArray.getJSONObject(i).getString("UserId");
                            String RoleName = statusMessageArray.getJSONObject(i).getString("RoleId__RoleName");
                            String StaffId = statusMessageArray.getJSONObject(i).getString("StaffId");

                            tempListUsername.add(UserName);
                            tempListRole.add(RoleName);
                            tempListStaffId.add(StaffId);
                            //String roleID =  StatusMessage.getString(0);
                            Log.d("roleID",RoleId);
                            Log.d("UserName",UserName);
                            Log.d("UserId",UserId);
                            Log.d("RoleName",RoleName);
                            Log.d("StaffId",StaffId);
                        }

                        String[] tempArray = new String[tempListUsername.size()];
                        String[] tempArrayRole = new String[tempListRole.size()];
                        String[] tempArrayStaffId = new String[tempListStaffId.size()];

                        userName = tempListUsername.toArray(tempArray);
                        RoleNameArr = tempListRole.toArray(tempArrayRole);
                        StaffIdArr = tempListStaffId.toArray(tempArrayStaffId);


                        Bundle bundle = getIntent().getExtras();

                        bundle.putString("RoleId",RoleId);
                        bundle.putString("UserName",UserName);
                        bundle.putString("UserId",UserId);

                        bundle.putString("companyName",companyName);
                        bundle.putString("Owner",Owner);
                        bundle.putString("Model",Model);
                        bundle.putString("Chassis",Chassis);
                        bundle.putString("Hrm",Hrm);
                        bundle.putString("Remarks",Remarks);
                        bundle.putString("Datetime",Datetime);
                        bundle.putString("contact_person",contact_person);
                        bundle.putString("service_type",service_type);

                        Intent intent = new Intent(CreateRequestActivity.this, ForwardActivity.class);
                        intent.putExtra("userNameArray", userName);
                        intent.putExtra("RoleNameArray",RoleNameArr);
                        intent.putExtra("StaffIdArray",StaffIdArr);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        finish();

                    } else {
                        // Error in login. Get the error message
                        String statusCode = jObj.getString("StatusCode");
                        String errorMsg = jObj.getString("StatusMessage");
                        Toast.makeText(getApplicationContext(),"ERROR IN LOGIN",Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        },new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) ;
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
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
//        session.setLogin(false);
//
//        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(CreateRequestActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
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

        Intent intent = new Intent(CreateRequestActivity.this, MainActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        if (v == btndatepicker) {
            // Get Current Date
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);


            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {

                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");


                            try {
                                txtDate.setText("");
                                Date parsedFrom = sdf.parse(String.valueOf(dayOfMonth)+"/"+String.valueOf(monthOfYear+1)+"/"+String.valueOf(year));

                                String now = String.valueOf(mYear)+String.valueOf(mMonth+1)+String.valueOf(mDay);
                                int length = (int)(Math.log10(dayOfMonth)+1);
                                String parseDate = "";
                                if(length<2){
                                    parseDate = String.valueOf(year)+String.valueOf(monthOfYear+1)+'0'+String.valueOf(dayOfMonth);
                                }
                                else{
                                    parseDate = String.valueOf(year)+String.valueOf(monthOfYear+1)+String.valueOf(dayOfMonth);
                                }


                                int nowV = Integer.parseInt(now);
                                int parseV = Integer.parseInt(parseDate);

                                Log.d("nowV",String.valueOf(nowV) + now + "->"+ new Date().getYear()+ "->" + new Date().getMonth() + "->" + new Date().getDay());
                                Log.d("parseV",String.valueOf(parseV) + parseDate + "->" + String.valueOf(parsedFrom.getYear())+String.valueOf(parsedFrom.getMonth())+String.valueOf(parsedFrom.getDay()));

                                if(parseV >= nowV){
                                    Log.d("right",String.valueOf(nowV)+String.valueOf(parseV));
                                    txtDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                                }
                                else{
                                    builder1.setMessage("You can not pick previous date for Service Assignment");
                                    builder1.setCancelable(true);

                                    builder1.setPositiveButton(
                                            "Dismiss",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                }
                                            });

                                    android.app.AlertDialog alert11 = builder1.create();
                                    alert11.show();
                                    Log.d("left",String.valueOf(nowV)+String.valueOf(parseV));
                                    Toast.makeText(getApplicationContext(),"You can not pick previous date for Service Assignment",Toast.LENGTH_LONG);

                                }

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }



                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }

        if (v == btntimepicker) {

            // Get Current Time
            final Calendar c = Calendar.getInstance();
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {

                            txtTime.setText(hourOfDay + ":" + minute);
                        }
                    }, mHour, mMinute, false);
            timePickerDialog.show();
        }


    }
}
