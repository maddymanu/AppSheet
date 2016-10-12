package com.appsheet.adityabansal.appsheet;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {

    //Queue for all Http requests.
    RequestQueue queue;

    //Contains details from all user ids
    JSONArray peeps = new JSONArray();

    //List of List, where each list is userIds from GET request
    JSONArray data = new JSONArray();

    //List of all details, sorted by age
    JSONArray sortedPeeps = new JSONArray();

    //List for final 5, with valid numbers
    JSONArray final_five = new JSONArray();

    //Sorted final 5
    JSONArray final_sorted = new JSONArray();

    //How big the array should be
    int size = 0;


    //URLs
    String url_orig = "https://appsheettest1.azurewebsites.net/sample/list";
    String url_token = "https://appsheettest1.azurewebsites.net/sample/list?token=";
    String url_detail = "http://appsheettest1.azurewebsites.net/sample/detail/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Creating a new RequestQueue
        queue = Volley.newRequestQueue(this);

        //Start by getting data from the default URL
        getData(url_orig, "");


    }

    /*
    GET method for /list and tokenizer.
     */
    private void getData(String url, String token) {
        String url_used = url + token;
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url_used, (String) null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //Once data is received, process it in another function
                        processData(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error", error.toString());

                    }
                });

        queue.add(jsObjRequest);

    }

    /*
    Once the data is received, this function is used to
     */
    public void processData(JSONObject respone) {
        //Try getting the list of ids, and appending it to the JSONArray
        try {
            JSONArray currData = respone.getJSONArray("result");
            data.put(currData);

        } catch (JSONException e) {
        }

        //If token is available, call the getData function again ^
        try {
            String token = respone.get("token").toString();
            getData(url_token, token);


        } catch (JSONException e) {

            //If token is NOT available, that means we have reached the end, and can move on
            //In this case, go over each ID, and call getIndividual for it

            for (int i = 0; i < data.length(); i++) {
                try {
                    JSONArray curr = (JSONArray) data.get(i);
                    size += curr.length();
                    for (int j = 0; j < curr.length(); j++) {
                        getIndividual((int) curr.get(j));
                    }

                } catch (JSONException e2) {
                }
            }
        }
    }

    /*
    GET method for each userID detail.
     */
    public void getIndividual(int id) {
        String url_used = url_detail + id;
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url_used, (String) null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //process data for each individual
                        processIndividual(response);

                        //if details for each ID is received, move on to showing the data
                        if (peeps.length() == size) {
                            completedGettingUserData();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });
        queue.add(jsObjRequest);
    }

    /*
    put each person in the peeps array if its not null
     */
    public void processIndividual(JSONObject person) {
        if (person != null) {
            peeps.put(person);
        }
    }

    /*
    Method is called AFTER all the user details have been gathered
     */
    public void completedGettingUserData() {

        sortByAge();

        //counter to keep track of which person is next to be checked for valid phone number
        int index = 0;

        //Putting 5 valid phone numbered users into final_five
        while (final_five.length() < 5) {
            try {
                JSONObject currPerson = sortedPeeps.getJSONObject(index);
                if (validNum(currPerson.getString("number")) == true) {
                    final_five.put(currPerson);

                }
                index++;

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        //Sorting output list by name
        sortByName();


        //Putting information in Custom Adapter
        ArrayList<User> arrayOfUsers = User.fromJson(final_sorted);
        UserAdapter adapter = new UserAdapter(getApplicationContext(), arrayOfUsers);
        ListView listView = (ListView) findViewById(R.id.lvItems);
        listView.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /*
    Helper function to sort by age
     */
    public void sortByAge() {

        List<JSONObject> jsonValues = new ArrayList<JSONObject>();
        for (int i = 0; i < peeps.length(); i++) {
            try {
                jsonValues.add(peeps.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Collections.sort(jsonValues, new Comparator<JSONObject>() {
            private static final String KEY_NAME = "age";

            @Override
            public int compare(JSONObject a, JSONObject b) {
                int valA = -1;
                int valB = -1;

                try {
                    valA = (int) a.get(KEY_NAME);
                    valB = (int) b.get(KEY_NAME);
                } catch (JSONException e) {
                }

                int cmp = valA > valB ? +1 : valA < valB ? -1 : 0;
                return cmp;


            }
        });


        for (int i = 0; i < peeps.length(); i++) {
            sortedPeeps.put(jsonValues.get(i));
        }

    }

    /*
    Helper function to sort by name
     */
    public void sortByName() {
        List<JSONObject> jsonValuesNew = new ArrayList<JSONObject>();
        for (int i = 0; i < final_five.length(); i++) {
            try {
                jsonValuesNew.add(final_five.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Collections.sort(jsonValuesNew, new Comparator<JSONObject>() {
            private static final String KEY_NAME = "name";

            @Override
            public int compare(JSONObject a, JSONObject b) {
                String valA = new String();
                String valB = new String();

                try {
                    valA = (String) a.get(KEY_NAME);
                    valB = (String) b.get(KEY_NAME);
                } catch (JSONException e) {
                }

                return valA.compareTo(valB);


            }
        });

        for (int i = 0; i < final_five.length(); i++) {
            final_sorted.put(jsonValuesNew.get(i));
        }


    }

    /*
    Helper function to check if the phone number matches a pattern
     */
    public boolean validNum(String num) {
        Pattern pattern = Pattern.compile("\\d{3}-\\d{3}-\\d{4}");
        Matcher matcher = pattern.matcher(num);

        if (matcher.matches()) {
            return true;
        } else {
            return false;
        }
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

