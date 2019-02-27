package io.ona.kujaku.comparisons;

public interface Comparison {
    String TYPE_STRING = "string";

    boolean compare(String a, String type, String b);

    String getFunctionName();
}