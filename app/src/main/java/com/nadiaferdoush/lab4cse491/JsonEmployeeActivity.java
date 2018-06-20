package com.nadiaferdoush.lab4cse491;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class JsonEmployeeActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_LOCATION = 900;
    ArrayAdapter<JsonEmployee> mAdapter;
    String name;
    double latitude, longitude;

    private static String url = "http://anontech.info/courses/cse491/employees.json";
    private ListView employeeList;

    public static final String FETCH_FLAG = "fetch_flag";
    JsonEmployee currentJsonEmployee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.employee_list);

        boolean fetchedAlready = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(FETCH_FLAG, false);
        if (!fetchedAlready) {
            new GetContacts().execute();
        }

        AppDatabase db = AppDatabase.getInstance(this);

        final List<JsonEmployee> employees = db.getEmployees();

        employeeList = findViewById(R.id.employee_list_view);
        mAdapter = new JsonEmployeeActivity.EmployeeListAdapter(this, R.layout.employee_list_item, employees);
        employeeList.setAdapter(mAdapter);

        //on click activity
        employeeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                AppDatabase database = AppDatabase.getInstance(JsonEmployeeActivity.this);
                final JsonEmployee em = mAdapter.getItem(position);
                currentJsonEmployee = em;
                Cursor cursor = database.getReadableDatabase().rawQuery("SELECT * FROM jsonEmployee WHERE id = ?", new String[]{String.valueOf(em.id)});
                if (cursor.moveToFirst()) {
                    do {
                        name = cursor.getString(cursor.getColumnIndex("name"));
                        latitude = (cursor.getDouble(cursor.getColumnIndex("latitude")));
                        longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
                    } while (cursor.moveToNext());
                }

                if (latitude != 0 && longitude != 0) {
                    Intent intent = new Intent(JsonEmployeeActivity.this, MapsActivity.class);
                    Bundle extras = new Bundle();
                    extras.putString("Name", name);
                    extras.putDouble("Latitude", latitude);
                    extras.putDouble("Longitude", longitude);
                    intent.putExtras(extras);
                    startActivity(intent);

                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(JsonEmployeeActivity.this);
                    builder.setMessage(R.string.dialog_add_location)
                            .setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    if (ContextCompat.checkSelfPermission(JsonEmployeeActivity.this,
                                            Manifest.permission.ACCESS_FINE_LOCATION)
                                            != PackageManager.PERMISSION_GRANTED) {

                                        ActivityCompat.requestPermissions(JsonEmployeeActivity.this,
                                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                                MY_PERMISSIONS_LOCATION);

                                    } else {
                                        Log.e("LL", "Already granted");
                                        setCurrentLocationToEmployee();
                                    }
                                    return;
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User cancelled the dialog
                                }
                            });

                    AlertDialog alertDialog = builder.create();
                    // show it
                    alertDialog.show();
                }
            }
        });

        //on long click activity
//        employeeList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//
//                AppDatabase database = AppDatabase.getInstance(JsonEmployeeActivity.this);
//                JsonEmployee em = mAdapter.getItem(position);
//                database.getWritableDatabase().delete("jsonEmployee", "id = ?", new String[]{String.valueOf(em.id)});
//                employees.remove(position);
//                mAdapter.notifyDataSetChanged();
//                database.close();
//
//                return false;
//            }
//        });
    }

    @SuppressLint("MissingPermission")
    private void setCurrentLocationToEmployee(){
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Log.e("LL", "Location succ");

                if (location == null) {
                    Log.e("LL", "Location null");
                    return;
                }
                AppDatabase db = AppDatabase.getInstance(JsonEmployeeActivity.this);
                ContentValues cv = new ContentValues();
                cv.put("latitude", location.getLatitude());
                cv.put("longitude", location.getLongitude());
                currentJsonEmployee.setLatitude(location.getLatitude());
                currentJsonEmployee.setLongitude(location.getLongitude());
                db.getWritableDatabase().update("jsonEmployee", cv, "id = ?", new String[]{String.valueOf(currentJsonEmployee.id)});
                mAdapter.notifyDataSetChanged();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("LL", "permission granted");
                    setCurrentLocationToEmployee();
                } else {
                    Log.e("LL", "permission denied");

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    public static class EmployeeListAdapter extends ArrayAdapter<JsonEmployee> {

        public EmployeeListAdapter(Context context, int resource, List<JsonEmployee> objects) {
            super(context, resource, objects);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = LayoutInflater.from(getContext()).inflate(R.layout.employee_list_item, null);

            JsonEmployee jEmployee = getItem(position);
            TextView employeeNameView = (TextView) v.findViewById(R.id.tv_employee_name);
            employeeNameView.setText(jEmployee.getName());

            TextView employeeBirthDateView = (TextView) v.findViewById(R.id.tv_latitude);
            employeeBirthDateView.setText(String.valueOf(jEmployee.getLatitude()));

            TextView employeeEmailView = (TextView) v.findViewById(R.id.tv_longitude);
            employeeEmailView.setText(String.valueOf(jEmployee.getLongitude()));

            return v;
        }
    }

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
                    JSONArray contacts = new JSONArray(jsonStr);
                    AppDatabase db = AppDatabase.getInstance(JsonEmployeeActivity.this);

                    // looping through All Contacts
                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);

                        String name = c.getString("name");
                        double latitude = 0, longitude = 0;

                        if (!c.isNull("location")) {
                            JSONObject location = c.getJSONObject("location");
                            latitude = Double.parseDouble(location.getString("latitude"));
                            longitude = Double.parseDouble(location.getString("longitude"));
                        } else {
                        }
                        if (name.length() > 0) {
                            JsonEmployee jsonEmployee = new JsonEmployee(name, latitude, longitude);
                            db.insertJsonEmployee(jsonEmployee);
                        }
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
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            AppDatabase db = AppDatabase.getInstance(JsonEmployeeActivity.this);

            final List<JsonEmployee> employees = db.getEmployees();
            mAdapter.clear();
            mAdapter.addAll(employees);
            mAdapter.notifyDataSetChanged();

            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(JsonEmployeeActivity.this).edit();
            editor.putBoolean(FETCH_FLAG, true);
            editor.commit();
        }
    }
}
