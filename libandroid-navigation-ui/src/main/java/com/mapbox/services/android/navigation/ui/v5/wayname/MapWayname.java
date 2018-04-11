package com.mapbox.services.android.navigation.ui.v5.wayname;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;

import com.mapbox.geojson.Feature;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.services.android.navigation.ui.v5.map.FeatureInteractor;
import com.mapbox.services.android.navigation.ui.v5.map.LayerInteractor;
import com.mapbox.services.android.navigation.ui.v5.utils.ViewUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.mapbox.mapboxsdk.style.expressions.Expression.exponential;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.expressions.Expression.zoom;
import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ANCHOR_TOP;
import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ROTATION_ALIGNMENT_VIEWPORT;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconRotationAlignment;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;
import static com.mapbox.services.android.navigation.v5.navigation.NavigationConstants.MAPBOX_LOCATION_SOURCE;
import static com.mapbox.services.android.navigation.v5.navigation.NavigationConstants.MAPBOX_WAYNAME_ICON;
import static com.mapbox.services.android.navigation.v5.navigation.NavigationConstants.MAPBOX_WAYNAME_LAYER;
import static com.mapbox.services.android.navigation.v5.navigation.NavigationConstants.WAYNAME_OFFSET;

public class MapWayname {

  private static final Set<String> ROAD_LABEL_LAYER_ID = new HashSet<String>() {
    {
      add("road-label-small");
      add("road-label-medium");
      add("road-label-large");
    }
  };
  public static final String NAME_PROPERTY = "name";
  public static final int FIRST_ROAD_FEATURE = 0;
  private LayerInteractor layerInteractor;
  private FeatureInteractor featureInteractor;
  private WaynameView waynameView;
  private String wayname = "";

  public MapWayname(Context context, LayerInteractor layerInteractor, FeatureInteractor featureInteractor) {
    this.waynameView = new WaynameView(context);
    this.layerInteractor = layerInteractor;
    this.featureInteractor = featureInteractor;
    if (containsStreetsStyle()) {
      initWaynameLayer();
    }
  }

  public void updateWaynameWithPoint(PointF point) {
    List<Feature> roads = findRoadLabelFeatures(point);
    String currentWayname = roads.get(FIRST_ROAD_FEATURE).getStringProperty(NAME_PROPERTY);
    boolean newWayname = !wayname.contentEquals(currentWayname);
    if (newWayname) {
      updateWaynameLayer(currentWayname);
    }
  }

  public void updateWaynameLayer(String wayname) {
    Layer waynameLayer = layerInteractor.getLayer(MAPBOX_WAYNAME_LAYER);
    if (waynameLayer != null) {
      createWaynameIcon(wayname, waynameLayer);
    }
  }

  public void updateWaynameVisibility(boolean isVisible) {
    Layer waynameLayer = layerInteractor.getLayer(MAPBOX_WAYNAME_LAYER);
    if (waynameLayer != null) {
      waynameLayer.setProperties(visibility(isVisible ? Property.VISIBLE : Property.NONE));
    }
  }

  private void initWaynameLayer() {
    SymbolLayer waynameLayer = new SymbolLayer(MAPBOX_WAYNAME_LAYER, MAPBOX_LOCATION_SOURCE)
      .withProperties(
        iconAllowOverlap(true),
        iconIgnorePlacement(true),
        iconSize(
          interpolate(exponential(1f), zoom(),
            stop(0f, 0.6f),
            stop(18f, 1.2f)
          )
        ),
        iconAnchor(ICON_ANCHOR_TOP),
        iconOffset(WAYNAME_OFFSET),
        iconRotationAlignment(ICON_ROTATION_ALIGNMENT_VIEWPORT)
      );
    layerInteractor.addLayer(waynameLayer);
  }

  private boolean containsStreetsStyle() {
    for (Layer layer : layerInteractor.getLayers()) {
      if (ROAD_LABEL_LAYER_ID.contains(layer.getId())) {
        return true;
      }
    }
    return false;
  }

  private List<Feature> findRoadLabelFeatures(PointF point) {
    String[] layerIds = ROAD_LABEL_LAYER_ID.toArray(new String[ROAD_LABEL_LAYER_ID.size()]);
    return featureInteractor.queryRenderedFeatures(point, layerIds);
  }

  private void createWaynameIcon(String wayname, Layer waynameLayer) {
    boolean isVisible = waynameLayer.getVisibility().getValue().contentEquals(Property.VISIBLE);
    if (isVisible) {
      waynameView.setWaynameText(wayname);
      Bitmap waynameBitMap = ViewUtils.loadBitmapFromView(waynameView);
      if (waynameBitMap != null) {
        layerInteractor.addLayerImage(MAPBOX_WAYNAME_ICON, waynameBitMap);
        waynameLayer.setProperties(iconImage(MAPBOX_WAYNAME_ICON));
      }
    }
  }
}
