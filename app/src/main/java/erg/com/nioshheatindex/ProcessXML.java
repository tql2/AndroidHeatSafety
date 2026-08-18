package erg.com.nioshheatindex;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

class ProcessXML extends AsyncTask<String[], Integer, Long>
{
    public static String TAG = ProcessXML.class.getSimpleName();
    private ParsedDataSetWeather myParsedExampleDataSet;
    private ProgressDialog pDialog;
    private final HeatIndexActivity activity;
    private final String url;

    public ProcessXML(HeatIndexActivity activity, String url)
    {
        this.activity = activity;
        this.url = url;
    }

    protected Long doInBackground(String[]... params)
    {
        long totalSize = 0;
        try
        {
            URL url = new URL(this.url);
            /* Get a SAXParser from the SAXPArserFactory. */
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            /* Get the XMLReader of the SAXParser we created. */
            XMLReader xr = sp.getXMLReader();
            /* Create a new ContentHandler and apply it to the XML-Reader*/
            HandlerWeather myExampleHandler = new HandlerWeather();
            xr.setContentHandler(myExampleHandler);
            /* Parse the xml-data from our URL. */
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            String UA = "OSHA NIOSH Heat Safety Tool" + System.getProperty("http.agent");
            urlConnection.setRequestProperty("User-Agent",UA);
            xr.parse(new InputSource(urlConnection.getInputStream()));
            //WCS 2024
            //xr.parse(new InputSource(activity.getResources().openRawResource(R.raw.failuredata)));
            ////
            myParsedExampleDataSet = myExampleHandler.getParsedData();
            totalSize = 1;
            urlConnection.disconnect();
            return totalSize;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return totalSize;
        }
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
        pDialog = new ProgressDialog(activity);
        pDialog.setTitle("Get Weather Information");
        pDialog.setMessage("Loading...");
        pDialog.show();
    }

    protected void onProgressUpdate(Integer... progress)
    {
    }

    @Override
    protected void onPostExecute(Long totalSize)
    {
        //call back data to main thread
        if (null != pDialog)
        {
            pDialog.dismiss();
        }
        activity.callBackData(myParsedExampleDataSet);
    }

}
