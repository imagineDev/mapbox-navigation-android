package com.mapbox.services.android.navigation.ui.v5.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PointF;
import android.location.Location;
import android.support.annotation.NonNull;

import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;
import com.mapbox.services.android.navigation.ui.v5.R;
import com.mapbox.services.android.navigation.ui.v5.ThemeSwitcher;
import com.mapbox.services.android.navigation.ui.v5.camera.NavigationCamera;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.ui.v5.wayname.MapWayname;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;

import java.util.ArrayList;
import java.util.List;

public class NavigationMapboxMap {

  private MapboxMap mapboxMap;
  private NavigationMapRoute mapRoute;
  private NavigationCamera mapCamera;
  private LocationLayerPlugin locationLayer;
  private MapWayname mapWayname;
  private List<Marker> mapMarkers = new ArrayList<>();

  public NavigationMapboxMap(MapView mapView, MapboxMap mapboxMap, MapboxNavigation mapboxNavigation) {
    this.mapboxMap = mapboxMap;
    initRoute(mapView, mapboxMap);
    initCamera(mapboxMap, mapboxNavigation);
    initLocationLayer(mapView, mapboxMap);
    initWayname(mapView, mapboxMap);
  }

  public void drawRoute(DirectionsRoute directionsRoute) {
    mapRoute.addRoute(directionsRoute);
  }

  public void addMarker(Context context, Point position) {
    Marker marker = createMarkerFromIcon(context, position);
    mapMarkers.add(marker);
  }

  public void clearMarkers() {
    removeAllMarkers();
  }

  public void updateLocation(Location location) {
    locationLayer.forceLocationUpdate(location);
    updateMapWaynameWithLocation(location);
  }

  public void updateCameraTrackingEnabled(boolean isEnabled) {
    mapCamera.updateCameraTrackingLocation(isEnabled);
  }

  public void startCamera(DirectionsRoute directionsRoute) {
    mapCamera.start(directionsRoute);
  }

  public void resumeCamera(Location location) {
    mapCamera.resume(location);
  }

  public void resetCameraPosition() {
    mapCamera.resetCameraPosition();
  }

  @SuppressLint("MissingPermission")
  public void onStart() {
    locationLayer.onStart();
  }

  public void onStop() {
    locationLayer.onStop();
  }

  public void onDestroy() {
    mapCamera.onDestroy();
  }

  private void initRoute(MapView mapView, MapboxMap map) {
    Context context = mapView.getContext();
    int routeStyleRes = ThemeSwitcher.retrieveNavigationViewStyle(context, R.attr.navigationViewRouteStyle);
    mapRoute = new NavigationMapRoute(null, mapView, map, routeStyleRes);
  }

  private void initLocationLayer(MapView mapView, MapboxMap map) {
    Context context = mapView.getContext();
    int locationLayerStyleRes = ThemeSwitcher.retrieveNavigationViewStyle(context,
      R.attr.navigationViewLocationLayerStyle);
    locationLayer = new LocationLayerPlugin(mapView, map, null, locationLayerStyleRes);
    locationLayer.setRenderMode(RenderMode.GPS);
  }

  private void initCamera(MapboxMap map, MapboxNavigation mapboxNavigation) {
    mapCamera = new NavigationCamera(map, mapboxNavigation);
  }

  private void initWayname(MapView mapView, MapboxMap mapboxMap) {
    Context context = mapView.getContext();
    LayerInteractor layerInteractor = new MapLayerInteractor(mapboxMap);
    FeatureInteractor featureInteractor = new MapFeatureInteractor(mapboxMap);
    mapWayname = new MapWayname(context, layerInteractor, featureInteractor);
  }

  @NonNull
  private Marker createMarkerFromIcon(Context context, Point position) {
    LatLng markerPosition = new LatLng(position.latitude(),
      position.longitude());
    Icon markerIcon = ThemeSwitcher.retrieveMapMarker(context);
    return mapboxMap.addMarker(new MarkerOptions()
      .position(markerPosition)
      .icon(markerIcon));
  }

  private void removeAllMarkers() {
    for (Marker marker : mapMarkers) {
      mapboxMap.removeMarker(marker);
    }
  }

  private void updateMapWaynameWithLocation(Location location) {
    LatLng latLng = new LatLng(location);
    PointF mapPoint = mapboxMap.getProjection().toScreenLocation(latLng);
    mapWayname.updateWaynameWithPoint(mapPoint);
  }
}
