package ch.stageconcept.dtraff.util;

import java.util.function.Supplier;

/**
 * Helper enum class to :
 * - If first argument is TRUE, run second one!
 *
 * @author Olivier Durand
 */
public enum Is1st {

    ;

    /**
     * If first argument is TRUE, run second one!
     *
     * @param first
     * @param second
     */
    public static void do2nd(Supplier<Boolean> first, Runnable second) {
        if (first.get()) second.run();
    }

}
