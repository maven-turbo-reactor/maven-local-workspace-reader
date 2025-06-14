package com.github.seregamorph.maven.extension.local;

import org.apache.maven.execution.MavenSession;

/**
 * @author Sergey Chernov
 */
final class MavenPropertyUtils {

    /*@Nullable*/
    static String getProperty(MavenSession session, String propertyName) {
        String propertyValue = session.getSystemProperties().getProperty(propertyName);
        if (propertyValue == null) {
            propertyValue = session.getUserProperties().getProperty(propertyName);
        }
        return propertyValue;
    }

    static boolean isTrue(String value) {
        return "true".equals(value);
    }

    private MavenPropertyUtils() {
    }
}
