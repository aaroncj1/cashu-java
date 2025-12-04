package io.github.aaroncj1.cashu.core.model.api.mintInfo.v1.response;

import lombok.Data;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Data
public class MintInfoResponse {
    private String name;
    private String pubkey;
    private String version;
    private String description;
    private String description_long;
    private List<Contact> contact;
    private String icon_url;
    private List<String> urls;
    private Timestamp time;
    private String tos_url;
    private Map<Integer, Nut> nuts;

    public record Contact(String method, String info) {
    }

    public record Nut(List<NutMethod> methods, boolean disabled, Object supported) {
    }

    public record NutMethod(String method, String unit, boolean description, List<String> commands, long min_amount, long max_amount) {
    }
}
