package com.google.android.gplusmini;

import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.SignInButton;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.plusDomains.PlusDomains;

import java.io.IOException;

public class LoginActivity extends ActionBarActivity {

    private static final int SOME_REQ_CODE = 1111;
    final int REQUEST_AUTHORIZATION = 2;
    String scopes = "oauth2:" + "https://www.googleapis.com/auth/plus.me " +
            "https://www.googleapis.com/auth/plus.circles.read";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        SignInButton signinbutton = (SignInButton) findViewById(R.id.signin);
        signinbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"}, false, null, null, null, null);
                startActivityForResult(intent, SOME_REQ_CODE);
            }
        });
    }

    protected String accountName = "";
    protected String token = "";

    @Override
    protected void onActivityResult(final int REQ_CODE, final int RESULT_CODE, final Intent data) {
        if (REQ_CODE == SOME_REQ_CODE && RESULT_CODE == RESULT_OK) {
            accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            new AuthTask().execute();
        } else if (REQ_CODE == REQUEST_AUTHORIZATION && RESULT_CODE == RESULT_OK) {
            new AgainAuthTask().execute();
        }
    }

    /*AgainAuthTask is used to redirect the authorized user to Profile activity*/
    private class AgainAuthTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                token = GoogleAuthUtil.getToken(LoginActivity.this,
                        accountName, scopes);
                GoogleCredential googleCredential = new GoogleCredential().setAccessToken(token);
                PlusDomains plusDomains = new PlusDomains.Builder(new NetHttpTransport(), new JacksonFactory(), googleCredential).setApplicationName("GPlusLab").build();
                plusDomains.people().get("me").execute();
                Intent i = new Intent(LoginActivity.this, ProfileActivity.class);
                i.putExtra("token", token);
                startActivity(i);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String token) {

        }
    }

    /*AuthTask is used to Authorize the user*/
    private class AuthTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                token = sharedPref.getString("token", "");
                if (!token.equals("")) {
                    GoogleAuthUtil.clearToken(LoginActivity.this, token);
                }
                token = GoogleAuthUtil.getToken(LoginActivity.this,
                        accountName, scopes);
                GoogleCredential googleCredential = new GoogleCredential().setAccessToken(token);
                PlusDomains plusDomains = new PlusDomains.Builder(new NetHttpTransport(), new JacksonFactory(), googleCredential).setApplicationName("GPlusLab").build();
                plusDomains.people().get("me").execute();
                return token;
            } catch (UserRecoverableAuthException e) {
                startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
            } catch (GoogleAuthException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String token) {
            if (token != null) {
                Intent i = new Intent(LoginActivity.this, ProfileActivity.class);
                i.putExtra("token", token);
                startActivity(i);
            } else {
                Toast.makeText(LoginActivity.this, "There is some problem with the Login", Toast.LENGTH_LONG);
            }
        }
    }

    ;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
