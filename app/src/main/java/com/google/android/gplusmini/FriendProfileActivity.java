package com.google.android.gplusmini;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.plusDomains.PlusDomains;
import com.google.api.services.plusDomains.model.Circle;
import com.google.api.services.plusDomains.model.PeopleFeed;
import com.google.api.services.plusDomains.model.Person;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class FriendProfileActivity extends ActionBarActivity {

    String token;
    String friendId, circleId, friendName, circleName;

    protected ImageView profilePic;
    protected TextView fname, occupation, organization, aboutMe, education;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        /*if this activity is called using back button then bundle would be null and the values
        should be fetched from sharedpreferences object otherwise fetch from bundle object*/
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            token = sharedPref.getString("token", "");
            circleId = sharedPref.getString("circleId", "");
            friendId = sharedPref.getString("friendId", "");
            friendName = sharedPref.getString("friendName", "");
            circleName = sharedPref.getString("circleName", "");
        } else if (extras != null) {
            token = extras.getString("token");
            circleId = extras.getString("circleId");
            friendId = extras.getString("friendId");
            friendName = extras.getString("friendName");
            circleName = extras.getString("circleName");
        }
        actionBar.setTitle(friendName);
        profilePic = (ImageView) findViewById(R.id.friendprofilepic);
        fname = (TextView) findViewById(R.id.friendname);
        occupation = (TextView) findViewById(R.id.friendoccupation);
        education = (TextView) findViewById(R.id.friendeducation);
        organization = (TextView) findViewById(R.id.friendorganization);
        aboutMe = (TextView) findViewById(R.id.friendaboutme);

        // Create object of SharedPreferences.
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        //now get Editor
        SharedPreferences.Editor editor = sharedPref.edit();
        //put your value
        editor.putString("friendId", friendId);
        editor.putString("circleName", circleName);
        editor.putString("friendName", friendName);
        //commits your edits
        editor.commit();

        new FriendTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_friend_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_email:
                openEmail();
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openEmail() {
        // Create object of SharedPreferences.
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        //now get Editor
        SharedPreferences.Editor editor = sharedPref.edit();
        //put your value
        editor.putString("token", token);
        editor.putString("circleId", circleId);
        editor.putString("friendId", friendId);
        editor.putString("friendName", friendName);
        //commits your edits
        editor.commit();

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("plain/text");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"recipient@example.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "subject of email");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "body of email");
        startActivity(emailIntent);
    }

    /*FriendTask is used to fetch friend's profile information*/
    private class FriendTask extends AsyncTask<String, Void, String> {
        Map<String, String> profileInfo = new HashMap<String, String>();
        protected Bitmap image;

        @Override
        protected String doInBackground(String... params) {
            GoogleCredential googleCredential = new GoogleCredential().setAccessToken(token);
            PlusDomains plusDomains = new PlusDomains.Builder(new NetHttpTransport(), new JacksonFactory(), googleCredential).setApplicationName("GPlusLab").build();

            try {
                Person person = plusDomains.people().get(friendId).execute();
                profileInfo.put("displayName", person.getDisplayName());
                if (person.getOccupation() != null) {
                    profileInfo.put("occupation", person.getOccupation());
                } else if (person.getOccupation() == null) {
                    profileInfo.put("occupation", "None");
                }
                if (person.getOrganizations() != null) {
                    String organization = "";
                    String education = "";
                    for (int i = 0; i < person.getOrganizations().size(); i++) {
                        if (education.isEmpty()) {
                            if (person.getOrganizations().get(i).getType().equals("school") && person.getOrganizations().get(i).getEndDate() == null) {
                                education += "Attends " + person.getOrganizations().get(i).getName();
                            }
                            if (person.getOrganizations().get(i).getType().equals("school") && person.getOrganizations().get(i).getEndDate() != null) {
                                education += "Attended " + person.getOrganizations().get(i).getName();
                            }
                        }
                        if (organization.isEmpty()) {
                            if (person.getOrganizations().get(i).getType().equals("work") && person.getOrganizations().get(i).getEndDate() == null) {
                                organization += "Works at " + person.getOrganizations().get(i).getName();
                            } else if (person.getOrganizations().get(i).getType().equals("work") && person.getOrganizations().get(i).getEndDate() != null) {
                                organization += "Worked at " + person.getOrganizations().get(i).getName();
                            }
                        }
                    }
                    profileInfo.put("organization", organization);
                    profileInfo.put("education", education);
                } else {
                    profileInfo.put("organization", "None");
                    profileInfo.put("education", "None");
                }
                if (person.getAboutMe() != null) {
                    profileInfo.put("aboutMe", person.getAboutMe());
                } else if (person.getAboutMe() == null) {
                    profileInfo.put("aboutMe", "None");
                }
                try {
                    String profilePicURL = person.getImage().getUrl().substring(0,
                            person.getImage().getUrl().length() - 6);
                    InputStream in = new java.net.URL(profilePicURL).openStream();
                    image = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    Circle circle = plusDomains.circles().get(circleId).execute();
                    if (circle != null) {
                        //to fetch people in circle
                        PlusDomains.People.ListByCircle listPeople = plusDomains.people().listByCircle(circleId);
                        PeopleFeed peopleFeed = listPeople.execute();

                        // This example only displays one page of results.
                        if (peopleFeed.getItems() != null && peopleFeed.getItems().size() > 0) {
                            for (Person person : peopleFeed.getItems()) {
                                if (person.getId().equals(friendId)) {
                                    profileInfo.put("displayName", person.getDisplayName());
                                    if (person.getOccupation() != null) {
                                        profileInfo.put("occupation", person.getOccupation());
                                    } else if (person.getOccupation() == null) {
                                        profileInfo.put("occupation", "None");
                                    }
                                    if (person.getOrganizations() != null) {
                                        String organization = "";
                                        String education = "";
                                        for (int i = 0; i < person.getOrganizations().size(); i++) {
                                            if (education.isEmpty()) {
                                                if (person.getOrganizations().get(i).getType().equals("school") && person.getOrganizations().get(i).getEndDate() == null) {
                                                    education += "Attends " + person.getOrganizations().get(i).getName();
                                                }
                                                if (person.getOrganizations().get(i).getType().equals("school") && person.getOrganizations().get(i).getEndDate() != null) {
                                                    education += "Attended " + person.getOrganizations().get(i).getName();
                                                }
                                            }
                                            if (organization.isEmpty()) {
                                                if (person.getOrganizations().get(i).getType().equals("work") && person.getOrganizations().get(i).getEndDate() == null) {
                                                    organization += "Works at " + person.getOrganizations().get(i).getName();
                                                } else if (person.getOrganizations().get(i).getType().equals("work") && person.getOrganizations().get(i).getEndDate() != null) {
                                                    organization += "Worked at " + person.getOrganizations().get(i).getName();
                                                }
                                            }
                                        }
                                        profileInfo.put("organization", organization);
                                        profileInfo.put("education", education);
                                    } else {
                                        profileInfo.put("organization", "None");
                                        profileInfo.put("education", "None");
                                    }
                                    if (person.getAboutMe() != null) {
                                        profileInfo.put("aboutMe", person.getAboutMe());
                                    } else if (person.getAboutMe() == null) {
                                        profileInfo.put("aboutMe", "None");
                                    }
                                    try {
                                        String profilePicURL = person.getImage().getUrl().substring(0,
                                                person.getImage().getUrl().length() - 6);
                                        InputStream in = new java.net.URL(profilePicURL).openStream();
                                        image = BitmapFactory.decodeStream(in);
                                    } catch (Exception e2) {
                                        Log.e("Error", e2.getMessage());
                                        e2.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            return "";
        }

        @Override
        protected void onPostExecute(String listOfCircles) {
            super.onPostExecute(listOfCircles);

            fname.setText(profileInfo.get("displayName"));
            if (!profileInfo.get("occupation").equals("None")) {
                occupation.setText(profileInfo.get("occupation"));
            }
            if (!profileInfo.get("education").equals("None")) {
                education.setText(profileInfo.get("education"));
            }
            if (!profileInfo.get("organization").equals("None")) {
                organization.setText(profileInfo.get("organization"));
            }
            if (!profileInfo.get("aboutMe").equals("None")) {
                aboutMe.setText(profileInfo.get("aboutMe"));
            }
            Bitmap resized = Bitmap.createScaledBitmap(image, 200, 200, true);
            Bitmap conv_image = getRoundedRectBitmap(resized, 100);
            profilePic.setImageBitmap(conv_image);
        }

        /*getRoundedRectBitmap(...) converts the rectangular image into round image for UI*/
        public Bitmap getRoundedRectBitmap(Bitmap bitmap, int pixels) {
            Bitmap result = null;
            try {
                result = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(result);

                int color = 0xff424242;
                Paint paint = new Paint();
                Rect rect = new Rect(0, 0, 200, 200);

                paint.setAntiAlias(true);
                canvas.drawARGB(0, 0, 0, 0);
                paint.setColor(color);
                canvas.drawCircle(100, 100, 100, paint);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                canvas.drawBitmap(bitmap, rect, rect, paint);

            } catch (NullPointerException e) {
            } catch (OutOfMemoryError o) {
            }
            return result;
        }

    }
}
