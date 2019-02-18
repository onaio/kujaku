package io.ona.kujaku.comparisons;


public class EqualToComparison implements Comparison {
    public static final String COMPARISON_NAME = "equalTo";

    @Override
    public boolean compare(String a, String type, String b) {
        return a != null && a.compareTo(b) == 0;
    }

    @Override
    public String getFunctionName() {
        return COMPARISON_NAME;
    }
}
