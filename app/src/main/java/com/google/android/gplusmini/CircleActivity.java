package com.google.android.gplusmini;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.plusDomains.PlusDomains;
import com.google.api.services.plusDomains.model.Circle;
import com.google.api.services.plusDomains.model.PeopleFeed;
import com.google.api.services.plusDomains.model.Person;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CircleActivity extends ActionBarActivity {

    ListView list;
    String token;
    String circleId, circleName;
    ArrayList<String> friendNames;
    ArrayList<String> friendIds;
    ArrayList<Bitmap> friendImages;
    CustomList adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cricle);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            token = sharedPref.getString("token", "");
            circleId = sharedPref.getString("circleId", "");
            circleName = sharedPref.getString("circleName", "");
        } else if (extras != null) {
            token = extras.getString("token");
            circleId = extras.getString("circleId");
            circleName = extras.getString("circleName");
        }
        actionBar.setTitle(circleName);
        // Create object of SharedPreferences.
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        //now get Editor
        SharedPreferences.Editor editor = sharedPref.edit();
        //put your value
        editor.putString("token", token);
        editor.putString("circleId", circleId);
        //commits your edits
        editor.commit();

        friendNames = new ArrayList<String>();
        friendIds = new ArrayList<String>();
        friendImages = new ArrayList<Bitmap>();

        new FriendsTask().execute();
        list = (ListView) findViewById(R.id.friendList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cricle, menu);
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

    /*FriendTask lists the friends in particular circle*/
    private class FriendsTask extends AsyncTask<String, Void, Map<String, String>> {
        protected Bitmap image;

        @Override
        protected Map<String, String> doInBackground(String... params) {
            Map<String, String> profileInfo = new HashMap<String, String>();
            GoogleCredential googleCredential = new GoogleCredential().setAccessToken(token);
            PlusDomains plusDomains = new PlusDomains.Builder(new NetHttpTransport(), new JacksonFactory(), googleCredential).setApplicationName("GPlusLab").build();
            try {
                Circle circle = plusDomains.circles().get(circleId).execute();

                // Loop until no additional pages of results are available.
                if (circle != null) {
                    //to fetch people in circle
                    PlusDomains.People.ListByCircle listPeople = plusDomains.people().listByCircle(circleId);
                    listPeople.setMaxResults(100L);
                    PeopleFeed peopleFeed = listPeople.execute();

                    if (peopleFeed.getItems() != null && peopleFeed.getItems().size() > 0) {
                        for (Person person : peopleFeed.getItems()) {
                            friendNames.add(person.getDisplayName());
                            friendIds.add(person.getId());
                            try {
                                String profilePicURL = person.getImage().getUrl().substring(0,
                                        person.getImage().getUrl().length() - 6);
                                InputStream in = new java.net.URL(profilePicURL).openStream();
                                image = BitmapFactory.decodeStream(in);
                            } catch (Exception e) {
                                Log.e("Error", e.getMessage());
                                e.printStackTrace();
                            }
                            friendImages.add(image);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return profileInfo;
        }

        /*This method is executed after doInBackground(). The value returned by doInBackground() is consumed as parameter.*/
        @Override
        protected void onPostExecute(Map<String, String> profileInfo) {
            super.onPostExecute(profileInfo);
            adapter = new
                    CustomList(CircleActivity.this, friendNames, friendImages);
            list.setAdapter(adapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    Intent i = new Intent(CircleActivity.this, FriendProfileActivity.class);
                    i.putExtra("token", token);
                    i.putExtra("circleId", circleId);
                    i.putExtra("circleName", circleName);
                    i.putExtra("friendId", friendIds.get(position));
                    i.putExtra("friendName", friendNames.get(position));
                    startActivity(i);
                }
            });
        }
    }
}