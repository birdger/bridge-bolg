package com.example.blog.service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.function.Predicate;

public final class SlugUtil {

    private SlugUtil() {
    }

    public static String uniqueSlug(Predicate<String> exists, String source) {
        String base = slugify(source);
        if (base.trim().isEmpty()) {
            base = "post";
        }
        String candidate = base;
        int index = 2;
        while (exists.test(candidate)) {
            candidate = base + "-" + index++;
        }
        return candidate;
    }

    public static String slugify(String text) {
        if (text == null) {
            return "";
        }
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}]+", "-")
                .replaceAll("^-+|-+$", "");
    }
}
