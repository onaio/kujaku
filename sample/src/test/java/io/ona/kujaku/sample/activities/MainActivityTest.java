package io.ona.kujaku.sample.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;

import io.ona.kujaku.sample.BuildConfig;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.sample.TestApplication;
import io.ona.kujaku.utils.Constants;

import static io.ona.kujaku.utils.Constants.MAP_ACTIVITY_REQUEST_CODE;
import static org.junit.Assert.*;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 27/12/2017.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class
        , manifest = Config.NONE
        , qualifiers = "en-port"
        , application = TestApplication.class)
public class MainActivityTest {

    private Context context;
    private MainActivity mainActivity;

    @Before
    public void setupBeforeTest() {
        context = RuntimeEnvironment.application;
        mainActivity = Robolectric.buildActivity(MainActivity.class)
                .get();
    }

    @Test
    public void onActivityResultShouldCreateToastShowingChosenFeature() {
        String geoJSONFeatureString = "{\n" +
                "  \"type\": \"Feature\",\n" +
                "  \"properties\": {\n" +
                "    \"Birth_Weight\": \"2\",\n" +
                "    \"address2\": \"Gordons\",\n" +
                "    \"base_entity_id\": \"55b83f54-78f1-4991-8d12-813236ce39bb\",\n" +
                "    \"epi_card_number\": \"\",\n" +
                "    \"provider_id\": \"\",\n" +
                "    \"last_interacted_with\": \"1511875745328\",\n" +
                "    \"last_name\": \"Karis\",\n" +
                "    \"dod\": \"\",\n" +
                "    \"is_closed\": \"0\",\n" +
                "    \"gender\": \"Male\",\n" +
                "    \"lost_to_follow_up\": \"\",\n" +
                "    \"end\": \"2017-11-28 16:29:05\",\n" +
                "    \"Place_Birth\": \"Home\",\n" +
                "    \"inactive\": \"\",\n" +
                "    \"relational_id\": \"3d6b0d3a-e3ed-4146-8612-d8ac8ff84e8c\",\n" +
                "    \"client_reg_date\": \"2017-11-28T00:00:00.000Z\",\n" +
                "    \"geopoint\": \"0.3508685 37.5844647\",\n" +
                "    \"pmtct_status\": \"MSU\",\n" +
                "    \"address\": \"usual_residence\",\n" +
                "    \"start\": \"2017-11-28 16:27:06\",\n" +
                "    \"First_Health_Facility_Contact\": \"2017-11-28\",\n" +
                "    \"longitude\": \"37.5844647\",\n" +
                "    \"dob\": \"2017-09-28T00:00:00.000Z\",\n" +
                "    \"Home_Facility\": \"42abc582-6658-488b-922e-7be500c070f3\",\n" +
                "    \"date\": \"2017-11-28T00:00:00.000Z\",\n" +
                "    \"zeir_id\": \"1061647\",\n" +
                "    \"deviceid\": \"867104020633980\",\n" +
                "    \"addressType\": \"usual_residence\",\n" +
                "    \"latitude\": \"0.3508685\",\n" +
                "    \"provider_uc\": \"\",\n" +
                "    \"provider_location_id\": \"\",\n" +
                "    \"address3\": \"6c814e69-ed6f-4fcc-ac2c-8406508603f2\",\n" +
                "    \"first_name\": \"Frank\"\n" +
                "  },\n" +
                "  \"geometry\": {\n" +
                "    \"type\": \"Point\",\n" +
                "    \"coordinates\": [\n" +
                "      43.35402,\n" +
                "      4.6205\n" +
                "    ]\n" +
                "  }\n" +
                "}";

        Intent intent = new Intent();
        intent.putExtra(Constants.PARCELABLE_KEY_GEOJSON_FEATURE, geoJSONFeatureString);
        mainActivity.onActivityResult(MAP_ACTIVITY_REQUEST_CODE, Activity.RESULT_OK, intent);

        assertEquals(geoJSONFeatureString, ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void onActivityResultWithInvalidIntentShouldShowErrorToast() {
        Intent intent = new Intent();
        mainActivity.onActivityResult(MAP_ACTIVITY_REQUEST_CODE, Activity.RESULT_OK, intent);

        assertEquals(context.getString(R.string.error_msg_could_not_retrieve_chosen_feature), ShadowToast.getTextOfLatestToast());
    }
}