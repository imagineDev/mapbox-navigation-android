package com.mapbox.services.android.navigation.ui.v5.map;

import android.graphics.PointF;

import com.mapbox.geojson.Feature;

import java.util.List;

public interface FeatureInteractor {

  List<Feature> queryRenderedFeatures(PointF point, String[] layerIds);
}
