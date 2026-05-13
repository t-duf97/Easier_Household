package be.duft.easier_household.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.ZoneId;

@Converter(autoApply = true)
public class ZoneIdConverter implements AttributeConverter<ZoneId, String> {

    @Override
    public String convertToDatabaseColumn(ZoneId zoneId) {
        return (zoneId == null) ? null : zoneId.getId();
    }

    @Override
    public ZoneId convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return null;
        return ZoneId.of(dbData);
    }
}
