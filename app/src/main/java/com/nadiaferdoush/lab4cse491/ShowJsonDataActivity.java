package com.nadiaferdoush.lab4cse491;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static com.nadiaferdoush.lab4cse491.JsonEmployeeActivity.FETCH_FLAG;
import static com.nadiaferdoush.lab4cse491.R.id.location;

public class ShowJsonDataActivity extends AppCompatActivity {

    private ListView lv;

    // URL to get contacts JSON
    private static String url = "http://anontech.info/courses/cse491/employees.json";

    ArrayList<HashMap<String, String>> contactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_list_data);

        contactList = new ArrayList<>();

        lv = findViewById(R.id.list);

        boolean fetchedAlready = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(FETCH_FLAG, false);
        if (!fetchedAlready) {
            new GetContacts().execute();
        }
    }

    /**
     * Async task class to get json by making HTTP call
     */
    private class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);

            if (jsonStr != null) {
                try {
                    // Getting JSON Array node
                    JSONArray contacts  = new JSONArray(jsonStr);

                    // looping through All Contacts
                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);

                        String name = c.getString("name");


                        HashMap<String, String> contact = new HashMap<>();

                        contact.put("name", name);

                        if (!c.isNull("location")) {
                            JSONObject location = c.getJSONObject("location");
                            contact.put("location", "latitude is " + location.getString("latitude") + " longitude is " + location.getString("longitude"));
                        } else {
                            contact.put("location", "Not available");
                        }

                        // adding contact to contact list
                        contactList.add(contact);
                    }
                } catch (final JSONException e) {
                    System.out.println("Error");
                }
            } else {
                System.out.println("Error");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            ListAdapter adapter = new SimpleAdapter(
                    ShowJsonDataActivity.this, contactList,
                    R.layout.json_list_item, new String[]{"name", "location"}, new int[]{R.id.name, location});

            lv.setAdapter(adapter);
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(ShowJsonDataActivity.this).edit();
            editor.putBoolean(FETCH_FLAG, true);
            editor.commit();
        }

    }
}
