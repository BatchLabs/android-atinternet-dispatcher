package com.batch.android.dispatcher.atinternet;

import androidx.annotation.Nullable;

import java.util.ArrayList;

class Xtor {

    private static final String[] XTOR_PREFIX = {
            "AD", // Advertisement
            "AL", // Affiliation
            "SEC", // Sponsored link
            "EREC", // Email marketing - Acquisition
            "EPR", // Email marketing - Retention
            "ES", // Email marketing - Promotion
            "CS", // Custom marketing campaigns
            "PUB", // On-site ads
            "INT" // Self-promotion
    };

    private String[] parts;

    private Xtor(String[] parts) {
        this.parts = parts;
    }

    static Xtor parse(String xtor) {
        ArrayList<String> parts = new ArrayList<>();
        boolean isEscaped = false;
        int partStart = 0;
        for(int i = 0, n = xtor.length() ; i < n ; i++) {
            char c = xtor.charAt(i);

            switch (c) {
                case '[':
                    isEscaped = true;
                    break;
                case ']':
                    isEscaped = false;
                    break;
                case '-':
                    if (!isEscaped) {
                        parts.add(xtor.substring(partStart, i));
                        partStart = i + 1;
                    }
                    break;
            }
        }

        if (!isEscaped && partStart < xtor.length()) {
            parts.add(xtor.substring(partStart));
        }
        return new Xtor(parts.toArray(new String[0]));
    }

    boolean isValidXtor() {
        if (parts.length >= 2) {
            String prefix = parts[0];
            for (String valid_prefix : XTOR_PREFIX) {
                if (prefix.startsWith(valid_prefix)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable
    String getPart(int index) {
        if (index >= 0 && index < parts.length) {
            return parts[index];
        }
        return null;
    }

    String[] getParts() {
        return parts;
    }
}
