package io.ona.kujaku.utils;

import android.content.Context;
import androidx.annotation.NonNull;
import android.text.format.Formatter;

import java.text.DecimalFormat;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 05/12/2017.
 */

public class NumberFormatter {

    /**
     *  Formats a double to a maximum of two decimal places or min of an integer representation eg. 2.45, 3.2, 189
     */
    public static String formatDecimal(double no) {
        java.text.DecimalFormat twoDForm = new DecimalFormat("0.##");
        return twoDForm.format(no);
    }

    /**
     * Converts bytes to a user-friendly size format eg. GB, KB or MB dependencing on the size.
     * It returns a formatted string eg. 16 KB, 242 MB, 1.06 GB
     *
     * @param context
     * @param bytes
     * @return
     */
    public static String getFriendlyFileSize(@NonNull Context context, long bytes) {
        return Formatter.formatFileSize(context, bytes);
    }
}
