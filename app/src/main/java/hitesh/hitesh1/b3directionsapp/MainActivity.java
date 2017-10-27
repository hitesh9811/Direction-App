package devanshu.hackveda.b3directionsapp;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.simple.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    EditText et1, et2;
    Button bt;
    ListView lv;
    private String origin;
    private String dest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        et1 = (EditText)findViewById(R.id.editText);
        et2 = (EditText) findViewById(R.id.editText2);
        bt = (Button)findViewById(R.id.button);
        lv = (ListView)findViewById(R.id.listView);
        bt.setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               origin = et1.getText().toString().trim();
                origin = origin.replace(" ", "+");
               dest = et2.getText().toString().trim();
                dest = dest.replace(" ", "+");
                // Execute Asnychronus Task
                new DirectionOp().execute();
            }
        });
    }

    private class DirectionOp
    extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(
                Void... params) {
            String data = "";
            try {
                URL url = new URL(
                        "https://maps.googleapis.com" +
                        "/maps/api/directions/json?" +
                        "origin=" + origin +
                        "&destination=" + dest);
                InputStream stream =
                        url.openConnection().getInputStream();
                InputStreamReader reader =
                        new InputStreamReader(stream);
                BufferedReader br =
                        new BufferedReader(reader);
                String line = "";
                while((line = br.readLine()) != null){
                    data = data + line + "\n";
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            // Display data on listview
            // Perform JSON Parsing
            // Convert JSON STring into JSON object
            JSONParser parser = new JSONParser();
            try {
                JSONObject dobj =
                        (JSONObject)parser.parse(data);
                String status = dobj.get("status").toString();
                if(status.matches("OK")){
                    Toast.makeText(getApplicationContext(),
                            "Results fetched", Toast.LENGTH_LONG).show();
                    JSONArray routes =
                            (JSONArray)dobj.get("routes");
                    JSONObject robj = (JSONObject)routes.get(0);
                    JSONArray legs = (JSONArray)robj.get("legs");
                    JSONObject lobj = (JSONObject)legs.get(0);
                    JSONObject t_dist =
                            (JSONObject)lobj.get("distance");

                    JSONObject t_dur =
                            (JSONObject)lobj.get("duration");
                    Toast.makeText(getApplicationContext(),
                            "Distance: " + t_dist.get("text").toString() +
                            " and Duration is " +
                                    t_dur.get("text").toString(),
                            Toast.LENGTH_LONG).show();
                    JSONArray steps = (JSONArray)lobj.get("steps");
                    List<Integer> ids = new ArrayList<Integer>();
                    for(int i = 0; i < steps.size(); i++){
                        ids.add(i);
                    }
                    ArrayAdapter adapter =
                            new OurAdapter(getApplicationContext(),
                            steps, ids);
                    lv.setAdapter(adapter);
                }else{
                    Toast.makeText(getApplicationContext(),
                            status, Toast.LENGTH_LONG).show();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private class OurAdapter extends ArrayAdapter {
        JSONArray STEPS;

        public OurAdapter(
                Context context, JSONArray steps,
                List<Integer> ids) {
            super(context, R.layout.activity_main,
                    R.id.textView5, ids);
            STEPS = steps;
        }

        @Override
        public View getView(int position,
                            View convertView,
                            ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater)
                    getSystemService(
                            LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(
                    R.layout.list_design,
                    parent, false);
            TextView tv2 = (TextView)convertView.findViewById(
                    R.id.textView2);
            TextView tv3 = (TextView)convertView.findViewById(
                    R.id.textView3);
            TextView tv4 = (TextView)convertView.findViewById(
                    R.id.textView4);

                JSONObject sobj =
                        (JSONObject)STEPS.get(
                                position);
                tv2.setText(
                        sobj.get("html_instructions")
                                .toString());
                JSONObject dist =
                        (JSONObject)sobj.get("distance");
                tv3.setText(dist.get("text").toString());
                JSONObject dur =
                        (JSONObject)sobj.get("duration");
                tv4.setText(dur.get("text").toString());

            return convertView;
        }
    }
}
