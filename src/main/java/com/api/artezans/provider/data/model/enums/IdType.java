package com.api.artezans.provider.data.model.enums;

import com.api.artezans.exceptions.ArtezanException;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;

public enum IdType {
    VOTER_CARD,

    NATIONAL_ID,

    INTERNATIONAL_PASSPORT;


    private static final Map<String, IdType> MAPPING = Map.of(
            "National ID", NATIONAL_ID,
            "International Passport", INTERNATIONAL_PASSPORT,
            "Voter Card", VOTER_CARD
    );

    public static IdType fromString(String value) {
        if (StringUtils.hasText(value)) {
            return Optional.ofNullable(MAPPING.get(value))
                    .orElseThrow(() -> new ArtezanException("Invalid ID type: " + value));
        }

        throw new ArtezanException("ID type cannot be null or empty");
    }
}
