package info.mis.motorequipment.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.mis.motorequipment.Entity.PendingSrevice;
import info.mis.motorequipment.R;
import info.mis.motorequipment.app.AppConfig;
import info.mis.motorequipment.app.AppController;
import info.mis.motorequipment.helper.ForwardActivityAdapter;
import info.mis.motorequipment.helper.JSONParse;
import info.mis.motorequipment.helper.PendingServiceActivityAdapter;
import info.mis.motorequipment.helper.RecyclerTouchListener;
import info.mis.motorequipment.helper.SQLiteHandler;
import info.mis.motorequipment.helper.SessionManager;
import info.mis.motorequipment.helper.SpacesItemDecoration;

import static info.mis.motorequipment.app.AppController.TAG;

public class PendingServiceActivity extends AppCompatActivity{

    private TextView txtName,txtEmail,txtFromdate,txtTodate;
    private Button btnLogout;
    private Button btnFromdatepicker,btnTodatepicker,submitDate;

    private ProgressDialog pDialog;

    private SQLiteHandler db;
    private SessionManager session;

    int mYear, mMonth, mDay, mHour, mMinute;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    List<PendingSrevice> mDataset;

    private AlertDialog.Builder builder1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_service);

        mRecyclerView = (RecyclerView) findViewById(R.id.main_list);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));


        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

//        txtName = (TextView) findViewById(R.id.name);
//        txtEmail = (TextView) findViewById(R.id.email);
//        txtFromdate = (TextView) findViewById(R.id.txtFromdate);
//        txtTodate = (TextView) findViewById(R.id.txtTodate);


        btnFromdatepicker = (Button) findViewById(R.id.btnFromdatepicker);
        btnTodatepicker = (Button) findViewById(R.id.btnTodatepicker);
        submitDate = (Button) findViewById(R.id.submitDate);



//        btnLogout = (Button) findViewById(R.id.btnLogout);

        builder1 = new AlertDialog.Builder(this);

        Bundle bundle = getIntent().getExtras();
        final String RoleId = bundle.getString("RoleId");
        final String UserName = bundle.getString("UserName");
        final String UserId = bundle.getString("UserId");

//        txtName.setText(UserName);
//        txtEmail.setText(UserId);

        db = new SQLiteHandler(getApplicationContext());
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

//        // Logout button click event
//        btnLogout.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                logoutUser();
//            }
//
//        });

        final String[] fromDate = new String[1];
        final String[] toDate = new String[1];


        btnFromdatepicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(PendingServiceActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                //txtFromdate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                                btnFromdatepicker.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                                //fromDate[0] = txtFromdate.getText().toString();
                                fromDate[0] = btnFromdatepicker.getText().toString();

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        btnTodatepicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(PendingServiceActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                //txtTodate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                                btnTodatepicker.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                                //toDate[0] = txtTodate.getText().toString();
                                toDate[0] = btnTodatepicker.getText().toString();
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });



            Log.d("No Problem", "Show Alert....");
            submitDate.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {


                    try {
                        String fDate = fromDate[0].toString();
                        String tDate = toDate[0].toString();

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Date parsedFrom = sdf.parse(fDate);
                        Date parsedTo = sdf.parse(tDate);
                        if (!fDate.equals("") && !tDate.equals("")) {
                            sendDate(RoleId, UserId, UserName, fDate, tDate);
                        } else {
                            Log.d("Problem", "Show Alert...."+String.valueOf(parsedFrom.compareTo(parsedTo)));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });





//        submitDate.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.d("Problem","Show Alert--->");
//                if(!fromDate[0].toString().equals("") && !toDate[0].toString().equals("")) {
//
//                    String fDate = fromDate[0].toString();
//                    String tDate = toDate[0].toString();
//
//                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//
//                    try {
//                        Date parsedFrom = sdf.parse(fDate);
//                        Date parsedTo = sdf.parse(tDate);
//                        if (parsedFrom.compareTo(parsedTo) == -1 && !fDate.equals("") && !tDate.equals("")) {
//                            sendDate(RoleId, UserId, UserName, fDate, tDate);
//                        } else {
//                            Log.d("Problem", "Show Alert....");
//                        }
//                    } catch (ParseException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                else{
//                    Log.d("Problem","Show Alert--->");
//                }
//
//
//            }
//        });


    }

    private void goToDetailActivity(final int ServiceId, final String RoleId, final String UserName, String UserId,int ServiceColor){
        final Bundle bundle = new Bundle();
        bundle.putString("ServiceID", String.valueOf(ServiceId));
        bundle.putString("RoleId",RoleId);
        bundle.putString("UserName",UserName);
        bundle.putString("UserId",UserId);
        bundle.putString("ServiceColor",String.valueOf(ServiceColor));

        String tag_string_req = "req_assign";
        String tag_string_role = "role_assign";


        Log.d("ServiceID-->",String.valueOf(ServiceId));




        StringRequest roleReq = new StringRequest(Request.Method.GET,
                AppConfig.GET_ROLE_DATA+"?RoleId="+RoleId, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Getting Pending Service: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    //boolean error = jObj.getBoolean("error");
                    String status = jObj.getString("StatusCode");
                    Log.d("Pending--->",status);
                    // Check for error node in json
                    if (status.equals("200")) {

                        String StatusMessage = jObj.getString("StatusMessage");
                        Log.d("StatusMessage-->",StatusMessage);
                        String[] userName = new String[]{};
                        String[] RoleNameArr = new String[]{};
                        String[] StaffIdArr = new String[]{};
                        if(!StatusMessage.equals("NoNeed")) {
                            JSONArray statusMessageArray = new JSONArray(StatusMessage);

                            List<String> listFromArray = Arrays.asList(userName);
                            List<String> listFromArrayRole = Arrays.asList(RoleNameArr);
                            List<String> listFromArrayStaffIdArr = Arrays.asList(StaffIdArr);

                            List<String> tempListUsername = new ArrayList<String>(listFromArray);
                            List<String> tempListRole = new ArrayList<String>(listFromArrayRole);
                            List<String> tempListStaffId = new ArrayList<String>(listFromArrayStaffIdArr);


                            for (int i = 0; i < statusMessageArray.length(); i++) {
                                String RoleId = statusMessageArray.getJSONObject(i).getString("RoleId__RoleName");
                                String UserName = statusMessageArray.getJSONObject(i).getString("UserName");
                                String UserId = statusMessageArray.getJSONObject(i).getString("UserId");
                                String RoleName = statusMessageArray.getJSONObject(i).getString("RoleId");
                                String StaffId = statusMessageArray.getJSONObject(i).getString("StaffId");

                                tempListUsername.add(UserName);
                                tempListRole.add(RoleName);
                                tempListStaffId.add(StaffId);
                                //String roleID =  StatusMessage.getString(0);
                                Log.d("roleID", RoleId);
                                Log.d("UserName", UserName);
                                Log.d("UserId", UserId);
                                Log.d("RoleName", RoleName);
                                Log.d("StaffId", StaffId);
                            }

                            String[] tempArray = new String[tempListUsername.size()];
                            String[] tempArrayRole = new String[tempListRole.size()];
                            String[] tempArrayStaffId = new String[tempListStaffId.size()];

                            userName = tempListUsername.toArray(tempArray);
                            RoleNameArr = tempListRole.toArray(tempArrayRole);
                            StaffIdArr = tempListStaffId.toArray(tempArrayStaffId);
                            Intent intent = new Intent(PendingServiceActivity.this,PendingServiceDetailActivity.class);
                            intent.putExtra("userNameArray", userName);
                            intent.putExtra("RoleNameArray",RoleNameArr);
                            intent.putExtra("StaffIdArray",StaffIdArr);
                            intent.putExtras(bundle);
                            startActivity(intent);
                            finish();
                        }
                        else{
                            Intent intent = new Intent(PendingServiceActivity.this,PendingServiceDetailActivity.class);
                            intent.putExtra("userNameArray", userName);
                            intent.putExtra("RoleNameArray",RoleNameArr);
                            intent.putExtra("StaffIdArray",StaffIdArr);
                            intent.putExtras(bundle);
                            startActivity(intent);
                            finish();
                        }

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
        });



        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(roleReq);
        //requestQueue.add(strReq);

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(roleReq, tag_string_role);
//        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

        requestQueue.getCache().clear();


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

    private void sendDate(final String RoleId, final String UserId, final String UserName ,final String fromDate, final String toDate){


        String tag_string_req = "req_assign";

        pDialog.setMessage("Getting Users to Assign ...");
        showDialog();

        String config = null;


        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.PENDING_SERVICE, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Getting Pending Service: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    //boolean error = jObj.getBoolean("error");
                    String status = jObj.getString("StatusCode");
                    Log.d("Pending--->",status);
                    // Check for error node in json
                    if (status.equals("200")) {

                        JSONParse pj = new JSONParse(response);
                        pj.parseJSON();
                        mDataset = pj.getPendingServices();
                        Log.d("mDataset",String.valueOf(mDataset.size()));
                        mAdapter = new PendingServiceActivityAdapter(mDataset);

                        if(mDataset.size()==0){

                            builder1.setMessage("No History Found on This Date Range");
                            builder1.setCancelable(true);

                            builder1.setPositiveButton(
                                    "Dismiss",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                            });

                            AlertDialog alert11 = builder1.create();
                            alert11.show();
                        }

                        mRecyclerView.setAdapter(mAdapter);
                        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mRecyclerView, new RecyclerTouchListener.ClickListener() {
                            @Override
                            public void onClick(View view, int position) {
                                PendingSrevice pendingSrevice =   mDataset.get(position);
                                int ServiceColor = mDataset.get(position).getServiceColor();
                                goToDetailActivity(pendingSrevice.getServiceId(),RoleId,UserName,UserId,ServiceColor);

                            }

                            @Override
                            public void onLongClick(View view, int position) {

                            }
                        }));


                        mAdapter.notifyDataSetChanged();



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
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                Log.d("DataXXXXX",RoleId+UserId+fromDate+toDate);
                params.put("roleId", RoleId);
                params.put("userId", UserId);
                params.put("startDate", fromDate);
                params.put("endDate", toDate);

                return params;
            }

        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(strReq);
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

    @Override
    public void onBackPressed() {
        final Bundle bundle = getIntent().getExtras();
        final String RoleId = bundle.getString("RoleId");
        final String UserName = bundle.getString("UserName");
        final String UserId = bundle.getString("UserId");


        bundle.putString("RoleId",RoleId);
        bundle.putString("UserName",UserName);
        bundle.putString("UserId",UserId);

        Intent intent = new Intent(PendingServiceActivity.this, MainActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(PendingServiceActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


}
