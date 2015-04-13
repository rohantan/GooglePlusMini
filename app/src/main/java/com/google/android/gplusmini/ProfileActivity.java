package com.google.android.gplusmini;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
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
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.plusDomains.PlusDomains;
import com.google.api.services.plusDomains.model.Circle;
import com.google.api.services.plusDomains.model.CircleFeed;
import com.google.api.services.plusDomains.model.Person;


public class ProfileActivity extends ActionBarActivity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    protected static String token = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            token = sharedPref.getString("token", "");
        } else if (extras != null) {
            token = extras.getString("token");
            // Create object of SharedPreferences.
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            //now get Editor
            SharedPreferences.Editor editor = sharedPref.edit();
            //put your value
            editor.putString("token", token);
            //commits your edits
            editor.commit();
        }
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
        if (extras == null) {
            actionBar.setSelectedNavigationItem(1);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
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

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // Return a PlaceholderFragment (defined as a static inner class below).

            switch (position) {
                case 0:
                    return new ProfileFragment();

                default:
                    return new CircleFragment();
            }

        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
            }
            return null;
        }
    }

    /*ProfileFragment is used to display the logged in user's profile detail information*/
    public static class ProfileFragment extends Fragment {
        protected Bitmap image;
        protected ImageView profilePic;
        protected TextView name, occupation, education, organization, aboutMe;

        private class ProfileTask extends AsyncTask<String, Void, Map<String, String>> {
            @Override
            protected Map<String, String> doInBackground(String... params) {
                Map<String, String> profileInfo = new HashMap<String, String>();
                GoogleCredential googleCredential = new GoogleCredential().setAccessToken(token);
                PlusDomains plusDomains = new PlusDomains.Builder(new NetHttpTransport(), new JacksonFactory(), googleCredential).setApplicationName("GPlusLab").build();
                try {
                    Person mePerson = plusDomains.people().get("me").execute();
                    profileInfo.put("displayName", mePerson.getDisplayName());
                    profileInfo.put("occupation", mePerson.getOccupation());
                    if (mePerson.getOrganizations() != null) {
                        String organization = "";
                        String education = "";
                        for (int i = 0; i < mePerson.getOrganizations().size(); i++) {
                            if (education.isEmpty()) {
                                if (mePerson.getOrganizations().get(i).getType().equals("school") && mePerson.getOrganizations().get(i).getEndDate() == null) {
                                    education += "Attends " + mePerson.getOrganizations().get(i).getName();
                                }
                                if (mePerson.getOrganizations().get(i).getType().equals("school") && mePerson.getOrganizations().get(i).getEndDate() != null) {
                                    education += "Attended " + mePerson.getOrganizations().get(i).getName();
                                }
                            }
                            if (organization.isEmpty()) {
                                if (mePerson.getOrganizations().get(i).getType().equals("work") && mePerson.getOrganizations().get(i).getEndDate() == null) {
                                    organization += "Works at " + mePerson.getOrganizations().get(i).getName();
                                } else if (mePerson.getOrganizations().get(i).getType().equals("work") && mePerson.getOrganizations().get(i).getEndDate() != null) {
                                    organization += "Worked at " + mePerson.getOrganizations().get(i).getName();
                                }
                            }
                        }
                        profileInfo.put("organization", organization);
                        profileInfo.put("education", education);
                    } else {
                        profileInfo.put("organization", "None");
                        profileInfo.put("education", "None");
                    }
                    if (mePerson.getAboutMe() != null) {
                        profileInfo.put("aboutMe", mePerson.getAboutMe());
                    } else if (mePerson.getAboutMe() == null) {
                        profileInfo.put("aboutMe", "None");
                    }
                    try {
                        String profilePicURL = mePerson.getImage().getUrl().substring(0,
                                mePerson.getImage().getUrl().length() - 6);
                        InputStream in = new java.net.URL(profilePicURL).openStream();
                        image = BitmapFactory.decodeStream(in);

                        Bitmap resized = Bitmap.createScaledBitmap(image, 200, 200, true);
                        Bitmap conv_image = getRoundedRectBitmap(resized, 100);
                        image = conv_image;

                    } catch (Exception e) {
                        Log.e("Error", e.getMessage());
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return profileInfo;
            }

            @Override
            protected void onPostExecute(Map<String, String> profileInfo) {
                super.onPostExecute(profileInfo);

                profilePic.setImageBitmap(image);
                name.setText(profileInfo.get("displayName"));
                occupation.setText(profileInfo.get("occupation"));
                education.setText(profileInfo.get("education"));
                organization.setText(profileInfo.get("organization"));
                aboutMe.setText(profileInfo.get("aboutMe"));

            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
            new ProfileTask().execute();

            profilePic = (ImageView) rootView.findViewById(R.id.profilepic);
            name = (TextView) rootView.findViewById(R.id.name);
            occupation = (TextView) rootView.findViewById(R.id.occupation);
            education = (TextView) rootView.findViewById(R.id.education);
            organization = (TextView) rootView.findViewById(R.id.organization);
            aboutMe = (TextView) rootView.findViewById(R.id.aboutme);

            return rootView;
        }

        /*getRoundedRectBitmap(...) converts the rectangular image into round image for UI*/
        public static Bitmap getRoundedRectBitmap(Bitmap bitmap, int pixels) {
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

    /*CircleFragment is used to display the logged in user's circles list*/
    public static class CircleFragment extends Fragment {
        List<String> listOfCircles = new ArrayList<String>();
        List<String> listOfCircleIds = new ArrayList<String>();
        StableArrayAdapter adapter;
        ListView listView;

        private class CircleTask extends AsyncTask<String, Void, List<String>> {
            @Override
            protected List<String> doInBackground(String... params) {
                GoogleCredential googleCredential = new GoogleCredential().setAccessToken(token);
                PlusDomains plusDomains = new PlusDomains.Builder(new NetHttpTransport(), new JacksonFactory(), googleCredential).setApplicationName("GPlusLab").build();

                try {
                    PlusDomains.Circles.List listCircles = plusDomains.circles().list("me");
                    listCircles.setMaxResults(5L);
                    CircleFeed circleFeed = listCircles.execute();
                    List<Circle> circles = circleFeed.getItems();

                    // Loop until no additional pages of results are available.
                    while (circles != null) {
                        for (Circle circle : circles) {
                            listOfCircles.add(circle.getDisplayName());
                            listOfCircleIds.add(circle.getId());
                        }
                        // When the next page token is null, there are no additional pages of
                        // results. If this is the case, break.
                        if (circleFeed.getNextPageToken() != null) {
                            // Prepare the next page of results
                            listCircles.setPageToken(circleFeed.getNextPageToken());

                            // Execute and process the next page request
                            circleFeed = listCircles.execute();
                            circles = circleFeed.getItems();
                        } else {
                            circles = null;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return listOfCircles;
            }

            @Override
            protected void onPostExecute(final List<String> listOfCircles) {
                super.onPostExecute(listOfCircles);
                list.addAll(listOfCircles);

                adapter = new StableArrayAdapter(getActivity(),
                        android.R.layout.simple_list_item_1, list);
                listView.setAdapter(adapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        Intent i = new Intent(getActivity(), CircleActivity.class);
                        i.putExtra("token", token);
                        i.putExtra("circleId", listOfCircleIds.get(position));
                        i.putExtra("circleName", listOfCircles.get(position));
                        startActivity(i);
                    }
                });
            }
        }

        List<String> list = new ArrayList<String>();

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_circle, container, false);
            new CircleTask().execute();
            listView = (ListView) rootView.findViewById(R.id.circleListView);
            return rootView;
        }

        private class StableArrayAdapter extends ArrayAdapter<String> {
            HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

            public StableArrayAdapter(Context context, int textViewResourceId,
                                      List<String> objects) {
                super(context, textViewResourceId, objects);
                for (int i = 0; i < objects.size(); ++i) {
                    mIdMap.put(objects.get(i), i);
                }
            }

            @Override
            public long getItemId(int position) {
                String item = getItem(position);
                return mIdMap.get(item);
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }

        }
    }
}
