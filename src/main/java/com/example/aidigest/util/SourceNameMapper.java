package com.example.aidigest.util;

import org.springframework.stereotype.Component;

@Component("sourceNameMapper")
public class SourceNameMapper {

    public String display(String raw) {
        if (raw == null || raw.isBlank()) return "";
        String lower = raw.toLowerCase();
        if (lower.startsWith("dev community")) return "dev.to";
        if (lower.contains("hacker news")) return "Hacker News";
        if (lower.contains("anthropic")) return "Anthropic";
        if (lower.contains("openai")) return "OpenAI";
        if (lower.contains("karpathy")) return "Andrej Karpathy";
        if (lower.contains("simon willison") || lower.contains("simonwillison")) return "Simon Willison";
        if (lower.contains("addy osmani") || lower.contains("addyosmani")) return "Addy Osmani";
        if (lower.contains("mitchell") || lower.contains("mitchellh")) return "Mitchell Hashimoto";
        if (lower.contains("hey world") || lower.contains("dhh")) return "DHH";
        return raw;
    }
}
