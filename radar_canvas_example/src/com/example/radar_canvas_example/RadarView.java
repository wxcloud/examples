package com.example.radar_canvas_example;

import java.util.List;

import org.json.JSONArray;

import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class RadarView extends View {

	JSONArray polys;
	Paint fillPaint;
	Path p;

	public RadarView(Context context, AttributeSet attrs) {
        super(context, attrs);
		fillPaint = new Paint();
	    fillPaint.setStyle(Paint.Style.FILL);
	    fillPaint.setStrokeWidth(0);
	    p = new Path();
    }

	public void setPolys(JSONArray polys) {
		this.polys = polys;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (this.polys == null) {
			return;
		}
		JSONArray polys = this.polys;
		double zoom = 4.0;
		double scale = Math.pow(2, zoom);
		//canvas.scale((float)scale,(float)scale);
	    try {
			for (int i = 0; i < polys.length(); i++) {
				JSONArray color_lines = polys.getJSONArray(i);
				String hex_color = color_lines.getString(0);
				int colorInt = Color.parseColor(hex_color);
				fillPaint.setColor(colorInt);
				System.out.println(color_lines.length());
				for (int j = 1; j < color_lines.length(); j++) {
					String lineData = color_lines.getString(j);
					List<LatLng> pts = MainActivity.decode(lineData);
					p.reset();
					LatLng start = pts.get(0);
					//System.out.println(start.latitude + ", " + start.longitude);
					p.moveTo((float)start.latitude,(float)start.longitude);
					for (int k = 1; k < pts.size(); k++) {
						LatLng pt = pts.get(k);
						p.lineTo((float)pt.latitude,(float)pt.longitude);
					}
					p.close();
					canvas.drawPath(p,fillPaint);
				}
			}
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	}
}
