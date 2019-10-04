package com.ackincolor.mygps;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;

public class CustomMapEventReciever implements MapEventsReceiver {
    private MapView mv;
    private Context c;
    private Polyline last = null;
    public CustomMapEventReciever(MapView mv, Context c){
        this.mv = mv;
        this.c =c;
    }
    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        DownloadRoute dr = new DownloadRoute(mv, c,this.last,Color.GREEN);
        dr.setGeoPoint(p);
        dr.execute("");
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        DownloadRoute dr = new DownloadRoute(mv, c,this.last,Color.RED);
        dr.setGeoPoint(p);
        dr.execute("");
        return false;
    }
    private class DownloadRoute extends AsyncTask<String, Void, Polyline> {
        MapView mapview;
        Context context;
        GeoPoint destination;
        int color;
        public DownloadRoute(MapView mapview, Context context,Polyline lastOverlay,int color) {
            this.mapview = mapview;
            this.context = context;
            this.mapview.getOverlays().remove(lastOverlay);
            this.color = color;
        }
        public void setGeoPoint(GeoPoint g){
            this.destination = g;
        }

        protected Polyline doInBackground(String... urls) {
            if(this.destination!=null) {
                String urltext = "http://172.31.249.161:5000/route/v1/driving/48.518085,2.693475;48.518509,2.691289?steps=true";

                OSRMRoadManager roadManager = new OSRMRoadManager(this.context);
                roadManager.setService("http://172.31.249.161:5000/route/v1/driving/");

                ArrayList<GeoPoint> wayPoints = new ArrayList<GeoPoint>();
                GeoPoint startPoint = new GeoPoint(48.518085, 2.693475);
                wayPoints.add(startPoint);
                GeoPoint endPoint = this.destination;
                wayPoints.add(endPoint);

                Road road = roadManager.getRoad(wayPoints);

                if (road.mStatus != Road.STATUS_OK) {
                    Log.d("DEBUG", "erreur lors de la creation de la route");
                }

                Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
                roadOverlay.setColor(this.color);
                Log.d("DEBUG","ajout de l'overlay");
                this.mapview.getOverlays().add(0,roadOverlay);

                this.mapview.invalidate();
                return roadOverlay;
            }else{
                return null;
            }
        }

        protected void onPostExecute(Polyline result) {
            Log.d("DEBUG",mapview.getMapCenter().toString());
            setLast(result);
        }
    }
    public void setLast(Polyline last) {
        this.last = last;
    }
}
