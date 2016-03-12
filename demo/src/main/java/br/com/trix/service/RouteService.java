package br.com.trix.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.google.maps.DirectionsApi;
import com.google.maps.model.DirectionsResult;
import com.google.maps.GeoApiContext;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.LatLng;

import br.com.trix.model.Route;
import br.com.trix.model.Waypoint;

@Component
public class RouteService {
	
	public RouteService(){}
	
	@Value("${apikey}")
	private String apiKey;
	
	private static final Logger LOGGER = Logger.getLogger(Route.class);
	
	public Route getApiResult(List<Waypoint> routePoints) throws Exception{
		Route route = new Route();
		DirectionsResult dirResult = getDirectionsResult(routePoints);
		List<String> listStops = getListStops(dirResult);
		List<LatLng> routePath = getRoutePath(dirResult);
		
		route.setName(routePoints.get(0).getName());
		route.setDate(Calendar.getInstance().getTime());
		route.setVehicleId("001");
		route.setStop(listStops);
		route.setPath(routePath);
		return route;
	}

	public DirectionsResult  getDirectionsResult(List<Waypoint> routePoints) throws Exception {
		GeoApiContext context = new GeoApiContext().setApiKey("AIzaSyCGUhLM8pidet05dKWxJ5U9oV0v_mPq9gA");
		DirectionsResult result = null;
		String startPoint;
		String[] wayPoints;
		
		startPoint = routePoints.get(0).toString();
		int waypointSize = routePoints.size()-1;
		wayPoints = new String[waypointSize];
		for (int i = 0; i < waypointSize; i++) {
			wayPoints[i] = routePoints.get(i+1).toString();
		}
		
		try {
			result = DirectionsApi.newRequest(context)
										 .origin(startPoint)
										 .destination(startPoint)
										 .optimizeWaypoints(true)
										 .waypoints(wayPoints)
										 .await();
			LOGGER.info("Route result ok!");
			return result;
		} catch (Exception e) {
			LOGGER.error("Error requesting DirectionsApi", e);
			throw e;
		} finally{
			DirectionsApi.newRequest(context).cancel(); 
		}
	}
	
	private List<String> getListStops(DirectionsResult dirResult) {
		List<String> stops = new ArrayList<String>();
		for (int i = 0; i < dirResult.routes[0].legs.length; i++) {
			stops.add(dirResult.routes[0].legs[i].startAddress);
		}
		return stops;
	}

	public List<LatLng> getRoutePath(DirectionsResult dirResult){
		String EncodedpolyLines = dirResult.routes[0].overviewPolyline.getEncodedPath().toString();	
		List<LatLng> polyLines = PolylineEncoding.decode(EncodedpolyLines);
		return polyLines;
	}

}
