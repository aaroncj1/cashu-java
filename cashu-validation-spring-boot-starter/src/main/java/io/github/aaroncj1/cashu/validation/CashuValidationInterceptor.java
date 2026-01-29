package io.github.aaroncj1.cashu.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CashuValidationInterceptor implements HandlerInterceptor {
    private final CashuTokenValidator validator;
    private final CashuValidationProperties properties;
    private final ObjectMapper objectMapper;

    public CashuValidationInterceptor(CashuTokenValidator validator,
                                      CashuValidationProperties properties,
                                      ObjectMapper objectMapper) {
        this.validator = validator;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!properties.isEnabled()) {
            return true;
        }
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        CashuRequired required = resolveRequirement(handlerMethod);
        boolean shouldValidate = required != null || !properties.isRequireAnnotation();
        if (!shouldValidate) {
            return true;
        }

        long minAmount = properties.getDefaultMinAmount();
        if (required != null && required.minAmount() >= 0) {
            minAmount = required.minAmount();
        }

        String token = request.getHeader(properties.getTokenHeader());
        if (token == null || token.isBlank()) {
            return writeError(response, HttpStatus.UNAUTHORIZED, "Missing Cashu token");
        }

        CashuValidationResult result = validator.validate(token, minAmount);
        if (!result.valid()) {
            return writeError(response, HttpStatus.PAYMENT_REQUIRED,
                    result.message() == null ? "Cashu token validation failed" : result.message());
        }

        request.setAttribute(CashuValidationResult.REQUEST_ATTRIBUTE, result);
        return true;
    }

    private CashuRequired resolveRequirement(HandlerMethod handlerMethod) {
        CashuRequired methodAnnotation = handlerMethod.getMethodAnnotation(CashuRequired.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }
        return handlerMethod.getBeanType().getAnnotation(CashuRequired.class);
    }

    private boolean writeError(HttpServletResponse response, HttpStatus status, String message) {
        if (response.isCommitted()) {
            return false;
        }
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        try {
            objectMapper.writeValue(response.getOutputStream(),
                    new CashuValidationError("cashu_validation_failed", message));
        } catch (IOException ex) {
            // If response body fails, at least return the status.
        }
        return false;
    }
}
