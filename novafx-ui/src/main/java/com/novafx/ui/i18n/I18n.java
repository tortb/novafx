package com.novafx.ui.i18n;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Application-wide internationalization (i18n) support.
 * <p>
 * Provides localized strings from resource bundles. Default locale is
 * Simplified Chinese ({@code zh_CN}). The locale can be changed at
 * runtime; the preference is persisted to the config directory.
 */
public final class I18n {

    private static final Logger log = LoggerFactory.getLogger(I18n.class);
    private static final String BUNDLE_NAME = "i18n/messages";
    private static final String LOCALE_FILE = "locale.conf";

    private static Locale currentLocale = Locale.SIMPLIFIED_CHINESE;
    private static ResourceBundle bundle = loadBundle(currentLocale);

    private I18n() {
    }

    /**
     * Returns a localized string for the given key.
     *
     * @param key the message key
     * @return the localized string, or the key itself if not found
     */
    public static String get(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            log.warn("Missing i18n key: {}", key);
            return key;
        }
    }

    /**
     * Returns a formatted localized string (supports {@link MessageFormat}
     * placeholders like {@code {0}}, {@code {1}}).
     *
     * @param key  the message key
     * @param args arguments for formatting
     * @return the formatted localized string, or the key itself if not found
     */
    public static String format(String key, Object... args) {
        try {
            String pattern = bundle.getString(key);
            return MessageFormat.format(pattern, args);
        } catch (MissingResourceException e) {
            log.warn("Missing i18n key: {}", key);
            return key;
        }
    }

    /**
     * Returns the current locale.
     *
     * @return the active locale
     */
    public static Locale getLocale() {
        return currentLocale;
    }

    /**
     * Changes the active locale and persists the preference.
     *
     * @param locale the new locale
     */
    public static void setLocale(Locale locale) {
        currentLocale = locale;
        bundle = loadBundle(locale);
        log.info("Locale changed to: {}", locale);
    }

    /**
     * Loads the saved locale preference from the config directory.
     *
     * @param configDir the platform config directory
     */
    public static void loadPreference(Path configDir) {
        Path file = configDir.resolve(LOCALE_FILE);
        if (Files.exists(file)) {
            try {
                String tag = Files.readString(file).trim();
                String[] parts = tag.split("_");
                if (parts.length == 2) {
                    setLocale(new Locale(parts[0], parts[1]));
                } else {
                    setLocale(Locale.forLanguageTag(tag));
                }
            } catch (IOException e) {
                log.warn("Failed to load locale preference", e);
            }
        }
    }

    /**
     * Saves the current locale preference.
     *
     * @param configDir the platform config directory
     */
    public static void savePreference(Path configDir) {
        try {
            Files.createDirectories(configDir);
            Files.writeString(configDir.resolve(LOCALE_FILE), currentLocale.toString());
        } catch (IOException e) {
            log.warn("Failed to save locale preference", e);
        }
    }

    private static ResourceBundle loadBundle(Locale locale) {
        try {
            return ResourceBundle.getBundle(BUNDLE_NAME, locale);
        } catch (MissingResourceException e) {
            log.warn("No resource bundle for {}, falling back to zh_CN", locale);
            try {
                return ResourceBundle.getBundle(BUNDLE_NAME, Locale.SIMPLIFIED_CHINESE);
            } catch (MissingResourceException e2) {
                log.error("No resource bundle found at all, using empty fallback", e2);
                try {
                    return new PropertyResourceBundle(
                            new java.io.ByteArrayInputStream("".getBytes()));
                } catch (IOException e3) {
                    throw new RuntimeException("Failed to create empty resource bundle", e3);
                }
            }
        }
    }
}
