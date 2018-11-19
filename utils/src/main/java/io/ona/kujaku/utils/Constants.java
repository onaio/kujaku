package io.ona.kujaku.utils;

import android.app.AlarmManager;

/**
 * Created by Jason Rogena - jrogena@ona.io on 11/7/17.
 */

public class Constants {
    public static final String INTENT_ACTION_SHOW_MAP = "io.ona.kujaku.map.SHOW";
    public static final String INTENT_ACTION_MAP_DOWNLOAD_SERVICE_STATUS_UPDATES = "io.ona.kujaku.service.map.downloader.updates";
    public static final String PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN = "mapbox_access_token";
    public static final String PARCELABLE_KEY_MAPBOX_STYLES = "mapbox_styles";
    public static final String PARCELABLE_KEY_STYLE_URL = "offline_map_mapbox_style_url";;
    public static final String PARCELABLE_KEY_MIN_ZOOM = "offline_map_min_zoom";;
    public static final String PARCELABLE_KEY_MAX_ZOOM = "offline_map_max_zoom";
    public static final String PARCELABLE_KEY_MAP_UNIQUE_NAME = "offline_map_unique_name";
    public static final String PARCELABLE_KEY_TOP_LEFT_BOUND = "offline_map_top_left_bound";
    public static final String PARCELABLE_KEY_BOTTOM_RIGHT_BOUND = "offline_map_bottom_right_bound";
    public static final String PARCELABLE_KEY_CAMERA_MAX_ZOOM = "mapbox_camera_max_zoom";
    public static final String PARCELABLE_KEY_CAMERA_MIN_ZOOM = "mapbox_camera_min_zoom";
    public static final String PARCELABLE_KEY_GEOJSON_FEATURE = "geojson_feature";
    public static final String PARCELABLE_POINTS_LIST = "points_list";
    public static final String ENABLE_DROP_POINT_BUTTON = "enable_drop_point_btn";
    public static final String NEW_FEATURE_POINTS_JSON = "new_feature_points_json";


    public static final int MAP_ACTIVITY_RESULT_CODE = 0;

    public static final String PARCELABLE_KEY_SERVICE_ACTION = "map_downloader_service";
    public static final String PARCELABLE_KEY_NETWORK_STATE = "active_network_state";

    public static final String PARCELABLE_KEY_DELETE_TASK_TYPE = "offline_service_delete_action_task_type";

    public static int MAP_DOWNLOAD_SERVICE_ALARM_REQUEST_CODE = 8687;
    public static long MAP_DOWNLOAD_SERVICE_ALARM_INTERVAL = AlarmManager.INTERVAL_FIFTEEN_MINUTES;

    public static final String MAP_BOX_URL_FORMAT = "mapbox://styles/[A-Za-z0-9]+/[A-Za-z0-9]+";

    public static final String INSERT_OR_REPLACE = "INSERT OR REPLACE INTO %s VALUES ";
    public static final String DATABASE_NAME = "kujaku.db";
}
