
package info.mis.motorequipment.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import info.mis.motorequipment.R;
import info.mis.motorequipment.app.AppConfig;
import info.mis.motorequipment.app.AppController;
import info.mis.motorequipment.helper.SQLiteHandler;
import info.mis.motorequipment.helper.SessionManager;

public class LoginActivity extends Activity {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private Button btnLogin;
    private Button btnLinkToRegister;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.btnLogin);

        isNetworkConnectionAvailable();
        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Session manager
        session = new SessionManager(getApplicationContext());


        // Check if user is already logged in or not

        if (session.isLoggedIn()) {
             //User is already logged in. Take him to main activity
            String RoleId = session.getRoleId();
            String UserName = session.getUserName();
            String UserId = session.getUserId();

            Bundle bundle = new Bundle();
            bundle.putString("RoleId",RoleId);
            bundle.putString("UserName",UserName);
            bundle.putString("UserId",UserId);

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
            Log.d("session","loggedin");
        }
        else{
            Log.d("session","loggedout");
        }

        // Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                // Check for empty data in the form
                if (!email.isEmpty() && !password.isEmpty()) {
                    // login user
                    checkLogin(email, password);
                } else {
                    // Prompt user to enter credentials
                    Toast.makeText(getApplicationContext(),
                            "Please enter the credentials!", Toast.LENGTH_LONG)
                            .show();
                }
            }

        });



    }

    public void checkNetworkConnection(){
        AlertDialog.Builder builder =new AlertDialog.Builder(this);
        builder.setTitle("No internet Connection");
        builder.setMessage("Please turn on internet connection to continue");
        builder.setNegativeButton("close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public boolean isNetworkConnectionAvailable(){
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnected();
        if(isConnected) {
            Log.d("Network", "Connected");
            return true;
        }
        else{
            checkNetworkConnection();
            Log.d("Network","Not Connected");
            return false;
        }
    }

    /**
     * function to verify login details in mysql db
     * */
    private void checkLogin(final String email, final String password) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("Logging in ...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_LOGIN, new Response.Listener<String>() {

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

                        // Now store the user in SQLite
                        String StatusMessage = jObj.getString("StatusMessage");

                        JSONArray statusMessageArray = new JSONArray(StatusMessage);

                        String RoleId = statusMessageArray.getJSONObject(0).getString("RoleId");
                        String UserName = statusMessageArray.getJSONObject(0).getString("UserName");
                        String UserId = statusMessageArray.getJSONObject(0).getString("UserId");

                        //String roleID =  StatusMessage.getString(0);
                        Log.d("roleID",RoleId);


                        Date currentTime = Calendar.getInstance().getTime();
                        String created_at = currentTime.toString();
//
//                        // Inserting row in users table
//                        db.addUser(UserName, UserId, RoleId, created_at);

                        Bundle bundle = new Bundle();
                        bundle.putString("RoleId",RoleId);
                        bundle.putString("UserName",UserName);
                        bundle.putString("UserId",UserId);

                        session.setRoleId(RoleId);
                        session.setUserName(UserName);
                        session.setUserId(UserId);

                        // Launch main activity
                        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
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
                params.put("userId", email);
                params.put("password", password);

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

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
