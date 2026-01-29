package io.github.aaroncj1.cashu.wallet.persisence.converters;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.aaroncj1.cashu.core.model.api.mintInfo.v1.response.MintInfoResponse;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.List;

@Converter
public class ContactListConverter implements AttributeConverter<List<MintInfoResponse.Contact>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<MintInfoResponse.Contact> attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Contact list to JSON", e);
        }
    }

    @Override
    public List<MintInfoResponse.Contact> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(dbData, new TypeReference<List<MintInfoResponse.Contact>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error converting JSON to Contact list", e);
        }
    }
}
