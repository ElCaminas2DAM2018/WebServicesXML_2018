package org.ieselcaminas.pmdm.webservicesxml_2018;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    EditText editText;

    private class AccessWebServicesTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls) {
            InputStream in;
            String strResult = "";
            try {
                in = openHttpConnection(urls[0]);
                XmlPullParser parser = Xml.newPullParser();
                try {
                    parser.setInput(in, null);
                    int eventType = parser.getEventType();
                    String text = "";
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        String  tagName = parser.getName();
                        switch (eventType) {
                            case XmlPullParser.START_DOCUMENT:
                                break;
                            case XmlPullParser.TEXT:
                                text = parser.getText();
                                break;
                            case XmlPullParser.START_TAG:
                                // name = parser.getName();
                                break;
                            case XmlPullParser.END_TAG:
                                if (tagName.equalsIgnoreCase("city")) {
                                    strResult += "<b>"+text+"</b><br>";
                                }
                                if (tagName.equalsIgnoreCase("country_name")) {
                                    strResult += text+"<br>";
                                }
                                break;
                        }
                        eventType = parser.next();
                    } // end while
                } catch (XmlPullParserException ex) {
                    ex.printStackTrace();
                }
                in.close();
            } catch (IOException e1) {
                Log.d("NetworkingActivity", e1.getLocalizedMessage());
            }
            return strResult;
        }

        protected void onPostExecute(String result) {
            textView.setText(Html.fromHtml(result));
        }

        private InputStream openHttpConnection(String urlString) throws IOException {
            InputStream in = null;
            int response;
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            if (!(conn instanceof HttpURLConnection))
                throw new IOException("Not an HTTP connection");
            try{
                HttpURLConnection httpConn = (HttpURLConnection) conn;
                httpConn.setInstanceFollowRedirects(true);
                httpConn.setRequestMethod("GET");
                httpConn.connect();
                response = httpConn.getResponseCode();
                if (response == HttpURLConnection.HTTP_OK) {
                    in = httpConn.getInputStream();
                }
            }
            catch (Exception ex) {
                Log.d("Networking", ex.getLocalizedMessage());
                throw new IOException("Error connecting");
            }
            return in;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);
        editText = (EditText) findViewById(R.id.editText);
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // This 2 lines are to hide the keyboard
                InputMethodManager inputManager = (InputMethodManager)
                        getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);

                //Detect connection to the Internet
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    // fetch data
                    AccessWebServicesTask downloadTextTask = new AccessWebServicesTask();
                    String ipStr = "";
                    if (editText.getText() != null && ! editText.getText().toString().isEmpty()) {
                        ipStr = editText.getText().toString()+"/";
                    }
                    downloadTextTask.execute("https://ipapi.co/" + ipStr + "xml");
                } else {
                    // display error
                    Toast.makeText(getApplicationContext(), "No internet connection available.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}