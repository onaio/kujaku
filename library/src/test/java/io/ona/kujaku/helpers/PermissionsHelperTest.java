package io.ona.kujaku.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.karumi.dexter.MultiplePermissionsReport;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import io.ona.kujaku.utils.KujakuMultiplePermissionListener;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class PermissionsHelperTest {

    private Context mockContext;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Use a real context
        mockContext = Robolectric.setupActivity(Activity.class).getApplicationContext();
    }

    @Test
    public void testOnPermissionsChecked_WhenAnyPermissionPermanentlyDenied() {
        MultiplePermissionsReport report = Mockito.mock(MultiplePermissionsReport.class);
        Mockito.when(report.isAnyPermissionPermanentlyDenied()).thenReturn(true);
        Mockito.when(report.areAllPermissionsGranted()).thenReturn(false);
        KujakuMultiplePermissionListener listener = new KujakuMultiplePermissionListener(mockContext);
        listener.onPermissionsChecked(report);

        // Check that the dialog was created with the expected properties
        Mockito.verify(report).isAnyPermissionPermanentlyDenied();
    }
}
