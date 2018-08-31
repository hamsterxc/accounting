package com.lonebytesoft.hamster.accounting.model;

import java.util.stream.StreamSupport;

public final class OrderedUtils {

    public static long getMaxOrder(final Iterable<? extends Ordered> stream) {
        return StreamSupport.stream(stream.spliterator(), false)
                .mapToLong(Ordered::getOrdering)
                .max()
                .orElse(0);
    }

    public static void swapOrder(final Ordered first, final Ordered second) {
        if((first != null) && (second != null)) {
            final long ordering = first.getOrdering();
            first.setOrdering(second.getOrdering());
            second.setOrdering(ordering);
        }
    }

}
