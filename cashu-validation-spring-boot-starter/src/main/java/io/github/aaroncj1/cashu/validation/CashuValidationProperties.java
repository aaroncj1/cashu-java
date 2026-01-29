package io.github.aaroncj1.cashu.validation;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cashu.validation")
public class CashuValidationProperties {
    private boolean enabled = true;
    private String tokenHeader = "X-Cashu-Token";
    private long defaultMinAmount = 0;
    private boolean requireAnnotation = true;
    private String checkEndpointPath = "/v1/check";
    private boolean rejectPending = true;
    private boolean allowMultipleMints = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTokenHeader() {
        return tokenHeader;
    }

    public void setTokenHeader(String tokenHeader) {
        this.tokenHeader = tokenHeader;
    }

    public long getDefaultMinAmount() {
        return defaultMinAmount;
    }

    public void setDefaultMinAmount(long defaultMinAmount) {
        this.defaultMinAmount = defaultMinAmount;
    }

    public boolean isRequireAnnotation() {
        return requireAnnotation;
    }

    public void setRequireAnnotation(boolean requireAnnotation) {
        this.requireAnnotation = requireAnnotation;
    }

    public String getCheckEndpointPath() {
        return checkEndpointPath;
    }

    public void setCheckEndpointPath(String checkEndpointPath) {
        this.checkEndpointPath = checkEndpointPath;
    }

    public boolean isRejectPending() {
        return rejectPending;
    }

    public void setRejectPending(boolean rejectPending) {
        this.rejectPending = rejectPending;
    }

    public boolean isAllowMultipleMints() {
        return allowMultipleMints;
    }

    public void setAllowMultipleMints(boolean allowMultipleMints) {
        this.allowMultipleMints = allowMultipleMints;
    }
}
