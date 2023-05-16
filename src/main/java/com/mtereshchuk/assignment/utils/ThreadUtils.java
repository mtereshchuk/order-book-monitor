package com.mtereshchuk.assignment.utils;

import java.util.concurrent.ThreadFactory;

/**
 * @author mtereshchuk
 */
public final class ThreadUtils {
    private ThreadUtils() {
    }

    public static ThreadFactory namedThread(String name) {
        return runnable -> new Thread(runnable, name);
    }
}
