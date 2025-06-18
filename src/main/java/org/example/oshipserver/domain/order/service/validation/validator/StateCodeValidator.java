package org.example.oshipserver.domain.order.service.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;
import org.example.oshipserver.domain.order.dto.request.OrderCreateRequest;
import org.example.oshipserver.domain.order.entity.enums.CountryCode;

public class StateCodeValidator implements ConstraintValidator<org.example.oshipserver.domain.order.service.validation.annotation.ValidStateCode, OrderCreateRequest> {

    private static final Set<CountryCode> REQUIRED_STATE_CODES = Set.of(
        CountryCode.CA, CountryCode.US, CountryCode.IN, CountryCode.MX, CountryCode.AE
    );

    @Override
    public boolean isValid(OrderCreateRequest request, ConstraintValidatorContext context) {
        if (request == null) return true;

        boolean senderValid = !REQUIRED_STATE_CODES.contains(request.senderCountryCode())
            || request.senderStateCode() != null;

        boolean recipientValid = !REQUIRED_STATE_CODES.contains(request.recipientCountryCode())
            || request.recipientStateCode() != null;

        return senderValid && recipientValid;
    }
}