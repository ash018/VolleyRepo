package info.mis.motorequipment.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.util.HashMap;


import info.mis.motorequipment.R;
import info.mis.motorequipment.app.AppConfig;
import info.mis.motorequipment.helper.SQLiteHandler;
import info.mis.motorequipment.helper.SessionManager;

import static android.provider.LiveFolders.INTENT;

public class MainActivity extends AppCompatActivity {

	private TextView txtName;
	private TextView txtEmail;
	private Button btnLogout,createRequestBtn,serviceHistoryBtn;

	private SQLiteHandler db;
	private SessionManager session;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		txtName = (TextView) findViewById(R.id.name);
		txtEmail = (TextView) findViewById(R.id.email);
//		btnLogout = (Button) findViewById(R.id.btnLogout);
		createRequestBtn = (Button) findViewById(R.id.CreateRequest);
        serviceHistoryBtn = (Button) findViewById(R.id.ServiceHistory);



        // SqLite database handler
		db = new SQLiteHandler(getApplicationContext());

		// session manager
		session = new SessionManager(getApplicationContext());

		if (!session.isLoggedIn()) {
			logoutUser();
		}

		// Fetching user details from SQLite
//		HashMap<String, String> user = db.getUserDetails();
		final Bundle bundle = getIntent().getExtras();
		final String RoleId = bundle.getString("RoleId");
		final String UserName = bundle.getString("UserName");
		final String UserId = bundle.getString("UserId");


		bundle.putString("RoleId",RoleId);
		bundle.putString("UserName",UserName);
		bundle.putString("UserId",UserId);


		// Displaying the user details on the screen
		txtName.setText(UserName);
		txtEmail.setText(UserId);

		if(RoleId.equals("4") || RoleId.equals("5")){
			serviceHistoryBtn.setText("Receive Request");
		}

		// Logout button click event
//		btnLogout.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				logoutUser();
//			}
//
//		});



		//Create Request Button Click Event
        createRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            	if(RoleId.equals("1") || RoleId.equals("2") || RoleId.equals("3")){
					Intent intent = new Intent(MainActivity.this, CreateRequestActivity.class);
					intent.putExtras(bundle);
					startActivity(intent);
					finish();
				}
				else{
					openDialog();
				}
            }
        });

		serviceHistoryBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
//                if(RoleId.equals("1")){
////                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.ADMIN_REPORT));
//					Intent intent = new Intent(MainActivity.this,AdminReport.class);
//                    intent.putExtras(bundle);
//                    startActivity(intent);
//                    finish();
//
////					Intent i = new Intent(Intent.ACTION_VIEW);
////					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////					i.setData(Uri.parse(AppConfig.ADMIN_REPORT));
////					startActivity(i);
//                }
//                else {
			Intent intent = new Intent(MainActivity.this, PendingServiceActivity.class);
			intent.putExtras(bundle);
			startActivity(intent);
			finish();
//                }
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

	/**
	 * Logging out the user. Will set isLoggedIn flag to false in shared
	 * preferences Clears the user data from sqlite users table
	 * */
	private void logoutUser() {
		session.setLogin(false);

		db.deleteUsers();

		// Launching the login activity
		Intent intent = new Intent(MainActivity.this, LoginActivity.class);
		startActivity(intent);
		finish();
	}

	public void openDialog() {
//		final Dialog dialog = new Dialog(MainActivity.this); // Context, this, etc.
//		dialog.setContentView(R.layout.dialog_demo);
//		dialog.setTitle(R.string.attention);
//		dialog.show();


		new AlertDialog.Builder(this)
				.setTitle("Attention")
				.setMessage("You cannot create request.")
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}).setNegativeButton("No", null).show();


	}
}
