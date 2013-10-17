package com.example.radar_canvas_example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;

public class MainActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
//		SupportMapFragment frag = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
//		GoogleMap map = frag.getMap();
//				
//		int w = frag.getView().getMeasuredWidth();
//		int h = frag.getView().getMeasuredHeight();
//		Bitmap bmp = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
//		Canvas canvas = new Canvas(bmp);
		String json = null;
		try {
			Log.e("read","radar.json");
			InputStream example_radar_data = getAssets().open("radar.json");
			BufferedReader in = new BufferedReader(new InputStreamReader(example_radar_data));
			StringBuffer buff = new StringBuffer();
			int numLines = 0;
			while (true) {
				String line = in.readLine();
				if (line == null) {
					break;
				}
				numLines ++;
				buff.append(line);
			}
			json = buff.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (json == null) {
			return;
		}
		try {
			RadarView radarCanvas = (RadarView)findViewById(R.id.canvas);
			Log.e("decode","json");
			JSONArray seq = new JSONArray(json);
			Log.e("decode",seq.length()+"");
			radarCanvas.setPolys(seq.getJSONArray(0));
			radarCanvas.invalidate();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void drawPolys(JSONArray polys, GoogleMap gmap) throws JSONException {
		LatLng lastPt = null;
		Log.e("polys","num polys " + polys.length());
		for (int i = 0; i < polys.length(); i++) {
			JSONArray color_lines = polys.getJSONArray(i);
			String hex_color = color_lines.getString(0);
			int colorInt = Color.parseColor(hex_color);
			for (int j = 1; j < color_lines.length(); j++) {
				String lineData = color_lines.getString(j);
				List<LatLng> pts = decode(lineData);
				gmap.addPolygon(new PolygonOptions()
					.addAll(pts)
					.fillColor(colorInt)
					.strokeWidth(0));
				lastPt = pts.get(0);
			}
		}
		System.out.println(lastPt.latitude + ", " + lastPt.longitude);
		CameraUpdate center = CameraUpdateFactory.newLatLng(lastPt);
		CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
		gmap.moveCamera(center);
		gmap.animateCamera(zoom);
	}
	public static List<LatLng> decode(String encoded) {
		List<LatLng> track = new ArrayList<LatLng>();
		int index = 0;
		int lat = 0, lng = 0;

		while (index < encoded.length()) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			LatLng p = new LatLng( (double)lat/1E5, (double)lng/1E5 );
			//p = getLatLng( (double)lng/1E5, (double)lat/1E5 );
			track.add(p);
		}
		return track;
	}

	public LatLng getLatLng(double px, double py) {
		double originShift = 2 * Math.PI * 6378137 / 2.0;
		double initialResolution = 2 * Math.PI * 6378137 / 256;
		double mx = px * initialResolution - originShift;
        double my = py * initialResolution - originShift;
        double lon = (mx / originShift) * 180.0;
        double lat = (my / originShift) * 180.0;
        lat = 180 / Math.PI * (2 * Math.atan( Math.exp( lat * Math.PI / 180.0)) - Math.PI / 2.0);
        lat = Math.abs(lat);
        return new LatLng(lat,lon);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
