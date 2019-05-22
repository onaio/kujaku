package io.ona.kujaku.plugin.switcher;

import android.support.annotation.NonNull;

import com.mapbox.mapboxsdk.style.expressions.Expression;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-05-21
 */

public class ExpressionArrayLiteral extends Expression.ExpressionLiteral {

    private Object[] object;

    /**
     * Create an expression literal.
     *
     * @param object the object to be treated as literal
     */
    public ExpressionArrayLiteral(@NonNull Object[] object) {
        super(object);
        this.object = object;
    }

    @Override
    public String toString() {
        String literal =  "[";

        for (int i = 0; i < object.length; i++) {
            if (i > 0) {
                literal += ", ";
            }

            literal += object[i];
        }

        return literal + "]";
    }
}
