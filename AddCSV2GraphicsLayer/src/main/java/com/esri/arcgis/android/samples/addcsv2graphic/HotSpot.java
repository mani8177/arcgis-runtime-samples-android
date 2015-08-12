/*
package com.esri.arcgis.android.samples.addcsv2graphic;

*/
/**
 * Created by mani8177 on 7/31/15.
 *//*

*/
/* Copyright 2012 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the sample code usage restrictions document for further information.
 *
 *//*


        import java.util.ArrayList;
        import java.util.Timer;

        import org.codehaus.jackson.JsonFactory;
        import org.codehaus.jackson.JsonParser;
        import org.codehaus.jackson.JsonToken;

        import android.app.Activity;
        import android.app.ProgressDialog;
        import android.graphics.Color;
        import android.os.Bundle;
        import android.os.Handler;
        import android.os.Handler.Callback;
        import android.os.Message;
        import android.os.StrictMode;
        import android.util.Log;
        import android.view.View;
        import android.view.View.OnClickListener;
        import android.widget.ImageButton;
        import android.widget.Toast;

        import com.esri.android.map.GraphicsLayer;
        import com.esri.android.map.MapView;
        import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
        import com.esri.core.geometry.Point;
        import com.esri.core.io.UserCredentials;
        import com.esri.core.map.FeatureSet;
        import com.esri.core.map.Graphic;
        import com.esri.core.symbol.SimpleMarkerSymbol;
        import com.esri.core.tasks.ags.geoprocessing.GPJobResource;
        import com.esri.core.tasks.ags.geoprocessing.GPMessage;
        import com.esri.core.tasks.ags.geoprocessing.GPParameter;
        import com.esri.core.tasks.ags.geoprocessing.GPString;
        import com.esri.core.tasks.ags.geoprocessing.Geoprocessor;
        import com.esri.core.tasks.ags.geoprocessing.GPJobResource.JobStatus;

*/
/**
 * This sample application illustrates the usage of the Hotspot Geoprocessing
 * analysis services from ArcGIS Online.
 *
 * @version $Revision: 1.0 $
 *//*


public class Hotspot extends Activity {

    protected static final int CLOSE_LOADING_WINDOW = 0;
    protected static final int CANCEL_LOADING_WINDOW = 1;
    MapView map = null;

    Geoprocessor gp;
    GraphicsLayer gLayer;
    ProgressDialog dialog = null;
    Timer cancelViewShed = new Timer();
    private ImageButton delete, go;
    Point mappoint;

    Handler handler;

    Handler uiHandler = new Handler(new Callback() {
        @Override
        public boolean handleMessage(final Message msg) {
            switch (msg.what) {
                case CLOSE_LOADING_WINDOW:
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    cancelViewShed.cancel();
                    break;
            }
            return false;
        }

    });

    */
/**
     * Called when the activity is first created.
     *
     * @param savedInstanceState
     *            Bundle
     *//*

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        map = (MapView) findViewById(R.id.map);
        // Add Tiled layer to MapView
        ArcGISTiledMapServiceLayer baselayer = new ArcGISTiledMapServiceLayer(
                "http://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer");
        map.addLayer(baselayer);
        map.enableWrapAround(true);

        // Create a graphics layer for Hotspot drawing
        gLayer = new GraphicsLayer();
        map.addLayer(gLayer);
        // Map Buttons
        delete = (ImageButton) findViewById(R.id.deletebutton);
        go = (ImageButton) findViewById(R.id.gobutton);

        Toast.makeText(this, "Click run to see a hotspot map", Toast.LENGTH_SHORT).show();

        */
/**
         * Clear all graphics from the graphics layer. The method is called when
         * the trash can button is clicked by the user.
         *//*

        delete.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v.isEnabled()) {
                    gLayer.removeAll();
                }
            }
        });

        */
/**
         * In response to the clicking of the arrow button
         *//*

        go.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v.isEnabled()) {
                    buildAnalysisParamsAndStart();
                } else {
                    Log.d("TEST", "View not enabled");
                }
            }
        });

        // Retrieve the non-configuration instance data that was previously
        // returned.
        Object init = getLastNonConfigurationInstance();
        if (init != null) {
            map.restoreState((String) init);
        }

    }

    public void buildAnalysisParamsAndStart() {

        Log.d("TEST", "buildAnalysisParamsAndStart() start");

        UserCredentials credentials = getCred();

        // create geoprocessor
        gp = new Geoprocessor("http://analysis1.arcgis.com/arcgis/rest/services/tasks/GPServer/FindHotSpots",
                credentials);

        // set param for analysis layer as json string
        GPString analysisLayer = new GPString("AnalysisLayer");
        analysisLayer
                .setValue("{\"url\": \"http://services1.arcgis.com/UwIZajkvy9eyvgek/arcgis/rest/services/cities/FeatureServer/0\"}");

        // set Analysis field
        GPString analysisField = new GPString("AnalysisField");
        analysisField.setValue("POPULATION");

        GPString returnFeatureCollection = new GPString("returnFeatureCollection");
        returnFeatureCollection.setValue("true");

        //GPString context = new GPString("Context");
        //context.setValue("{\"outSR\": {\"wkid\":" + map.getSpatialReference().getLatestID() + "}}");

        // build final GPParams
        final ArrayList<GPParameter> paramlist = new ArrayList<GPParameter>();
        paramlist.add(analysisLayer);
        paramlist.add(analysisField);
        paramlist.add(returnFeatureCollection);
        //paramlist.add(context);

        try {
            dialog = ProgressDialog.show(Hotspot.this, "", "Loading. Please wait...", true, true);

            handler = new Handler();

            submitJobandPolling(gp, paramlist);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.unpause();
    }

    private void submitJobandPolling(final Geoprocessor gp, ArrayList<GPParameter> params) {
        try {

            Log.d("TEST", "submitJobandPolling() start");

            //submit job
            GPJobResource jr = gp.submitJob(params);
            JobStatus jobstatus = jr.getJobStatus();
            final String jobid = jr.getJobID();

            if (jobstatus != JobStatus.SUCCEEDED) {

                handler.postDelayed(new Runnable() {

                    public void run() {
                        try {
                            GPJobResource jr = gp.checkJobStatus(jobid);
                            GPMessage[] messages = jr.getMessages();
                            if (messages != null && messages.length > 0) {
                                for (int i = 0; i < messages.length; i++) {
                                    Log.d("TEST", "Message: " + messages[i].getDescription());
                                }
                            }

                            JobStatus status = jr.getJobStatus();
                            boolean jobcomplete = false;

                            if (status == JobStatus.CANCELLED || status == JobStatus.DELETED
                                    || status == JobStatus.FAILED || status == JobStatus.SUCCEEDED
                                    || status == JobStatus.TIMED_OUT) {
                                jobcomplete = true;

                            }
                            if (jobcomplete) {
                                if (status == JobStatus.SUCCEEDED) {

                                    //get output params
                                    GPParameter output = gp.getResultData(jobid, "HotSpotsResultLayer");
                                    if (output != null) {

                                        GPString outputJson = (GPString) output;
                                        outputJson.getValue();

                                        Log.d("TEST", "outputJson value = " + outputJson.getValue());

                                        //parse output
                                        parsingGPValue(outputJson.getValue());

                                    } else {
                                        Log.d("TEST", "output is null");
                                    }

                                    uiHandler.sendEmptyMessage(CLOSE_LOADING_WINDOW);

                                } else {
                                    Log.d("TEST", "GP failed");
                                }

                            } else {
                                handler.postDelayed(this, 5000);
                            }
                        } catch (Exception e) {

                            Log.d("TEST", "Exception in postDeloyed " + e.getLocalizedMessage());

                            e.printStackTrace();
                        }

                    }
                }, 4000);

            }
        } catch (Exception e) {

            Log.d("TEST", "Exception in submitJobandPolling " + e.getLocalizedMessage());

            e.printStackTrace();
        }
    }

    private void parsingGPValue(String values) throws Exception {
        if (values != null) {
            JsonFactory f = new JsonFactory();
            generateGraphicsLayer(f.createJsonParser(values));

        }
    }

    private  void  generateGraphicsLayer(JsonParser parser) throws Exception {
        Log.d("TEST", "generateGraphicsLayer");
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.getCurrentName();
            if ("featureSet".equals(fieldName)) {
                if (parser.nextToken() == JsonToken.START_OBJECT) {
                    FeatureSet fs = FeatureSet.fromJson(parser);
                    for (Graphic feature : fs.getGraphics()) {

                        Integer popValue= (Integer) feature.getAttributes().get("POPULATION");

                        int color = Color.YELLOW;
                        int size = 1;
                        if (popValue > 500*10000) {
                            color = Color.RED;
                            size = 40;
                        } else if(popValue > 100*10000) {
                            color =  Color.rgb(150, 0, 0);
                            size = 20;
                        }  else if(popValue > 50*10000) {
                            color = Color.rgb(50, 0, 0);
                            size = 15;
                        }  else if(popValue > 10*10000) {
                            color = Color.rgb(250, 250, 0);
                            size = 10;
                        }  else if(popValue > 5*10000) {
                            color = Color.rgb(150, 150, 0);
                            size = 5;
                        }
                        Graphic g = new Graphic(feature.getGeometry(), new SimpleMarkerSymbol(color, size, SimpleMarkerSymbol.STYLE.CIRCLE));
                        gLayer.addGraphic(g);
                    }

                }
            }
        }
    }
















    private static UserCredentials getCred() {
        // UserCredentials credentials = new UserCredentials();
        // credentials.setUserAccount("wcrick_esri", "");
        // credentials.setTokenServiceUrl("https://devext.arcgis.com/sharing/generateToken");

        UserCredentials credentials = new UserCredentials();
        // credentials.setUserAccount("willcrick_esri", "");
        credentials.setUserAccount("mani8177","iamtheking31");
                //credentials.setTokenServiceUrl("https://www.arcgis.com/sharing/generateToken");

        return credentials;
    }

}*/
