package io.ona.kujaku.comparisons;

public abstract class Comparison {
    public static final String TYPE_STRING = "string";

    public abstract boolean compare(String a, String type, String b);

    public abstract String getFunctionName();
}