package org.example.oshipserver.domain.order.entity.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StateCodeConverter implements AttributeConverter<StateCode, String> {

    /**
     * 저장 시: CountryCode:StateCode 형식 (예: "US:CA")
     */
    @Override
    public String convertToDatabaseColumn(StateCode attribute) {
        if (attribute == null) return null;
        return attribute.getCountryCode().name() + ":" + attribute.getCode();
    }

    /**
     * 읽을 시: "US:CA" → CountryCode.US + "CA" 로 정확히 매핑
     */
    @Override
    public StateCode convertToEntityAttribute(String dbData) {
        if (dbData == null || !dbData.contains(":")) return null;

        String[] parts = dbData.split(":");
        if (parts.length != 2) return null;

        try {
            CountryCode country = CountryCode.valueOf(parts[0]);
            String code = parts[1];
            return StateCode.from(country, code).orElse(null);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
