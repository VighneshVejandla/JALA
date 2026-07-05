package com.jala.backend.feedentry.converter;

import com.jala.backend.feedentry.enums.FeedSize;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class FeedSizeConverter
        implements AttributeConverter<FeedSize, String> {

    @Override
    public String convertToDatabaseColumn(FeedSize attribute) {

        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public FeedSize convertToEntityAttribute(String dbData) {

        if (dbData == null) {
            return null;
        }

        for (FeedSize size : FeedSize.values()) {

            if (size.getCode().equals(dbData)) {
                return size;
            }
        }

        throw new IllegalArgumentException(
                "Unknown feed size: " + dbData);
    }
}