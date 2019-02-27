package io.ona.kujaku.comparisons;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegexComparison implements Comparison {
    private final String TAG = this.getClass().getSimpleName();
    public static final String COMPARISON_NAME = "regex";

    @Override
    public boolean compare(String a, String type, String b) {
        if (a != null && b != null) {
            try {
                Pattern pattern = Pattern.compile(b);
                Matcher matcher = pattern.matcher(a);
                return matcher.matches();
            } catch (PatternSyntaxException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        } else {
            return false;
        }

        return false;
    }

    @Override
    public String getFunctionName() {
        return COMPARISON_NAME;
    }
}
