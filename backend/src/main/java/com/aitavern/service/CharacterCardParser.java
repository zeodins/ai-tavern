package com.aitavern.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class CharacterCardParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public record ParsedCard(Map<String, String> fields, byte[] avatarBytes) {}

    public ParsedCard parse(InputStream inputStream, String filename) throws IOException {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) {
            return parsePng(inputStream);
        } else if (lower.endsWith(".json")) {
            return parseJson(inputStream);
        }
        throw new IllegalArgumentException("Unsupported file format: " + filename);
    }

    private ParsedCard parseJson(InputStream inputStream) throws IOException {
        String jsonContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        JsonNode root = objectMapper.readTree(jsonContent);
        Map<String, String> fields = extractFields(root);
        return new ParsedCard(fields, null);
    }

    private ParsedCard parsePng(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        inputStream.transferTo(baos);
        byte[] fullData = baos.toByteArray();

        ByteArrayInputStream pngStream = new ByteArrayInputStream(fullData);
        BufferedImage image = ImageIO.read(pngStream);

        String jsonContent = extractTextChunk(fullData);
        if (jsonContent == null || jsonContent.isEmpty()) {
            throw new IllegalArgumentException("PNG does not contain embedded character card data");
        }

        JsonNode root = objectMapper.readTree(jsonContent);
        Map<String, String> fields = extractFields(root);

        byte[] avatarBytes = null;
        if (image != null) {
            ByteArrayOutputStream avatarBaos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", avatarBaos);
            avatarBytes = avatarBaos.toByteArray();
        }

        return new ParsedCard(fields, avatarBytes);
    }

    private Map<String, String> extractFields(JsonNode root) {
        Map<String, String> fields = new HashMap<>();
        fields.put("name", safeText(root, "name"));
        fields.put("description", safeText(root, "description"));
        fields.put("personality", safeText(root, "personality"));
        fields.put("first_message", safeText(root, "first_mes"));
        fields.put("scenario", safeText(root, "scenario"));
        fields.put("system_prompt", safeText(root, "system_prompt"));
        fields.put("mes_example", safeText(root, "mes_example"));
        return fields;
    }

    private String safeText(JsonNode root, String fieldName) {
        JsonNode node = root.get(fieldName);
        return node != null && !node.isNull() ? node.asText() : "";
    }

    private String extractTextChunk(byte[] pngData) {
        int offset = 8;
        while (offset < pngData.length - 12) {
            int length = readInt(pngData, offset);
            String type = new String(pngData, offset + 4, 4, StandardCharsets.US_ASCII);
            if ("tEXt".equals(type)) {
                byte[] data = new byte[length];
                System.arraycopy(pngData, offset + 8, data, 0, length);
                String kv = new String(data, StandardCharsets.ISO_8859_1);
                int nullIdx = kv.indexOf('\0');
                if (nullIdx >= 0) {
                    String key = kv.substring(0, nullIdx);
                    if ("ccv3".equals(key) || "chara".equals(key)) {
                        String rawValue = kv.substring(nullIdx + 1);
                        if ("chara".equals(key)) {
                            return new String(Base64.getDecoder().decode(rawValue), StandardCharsets.UTF_8);
                        }
                        return rawValue;
                    }
                }
            }
            offset += 12 + length;
        }
        return null;
    }

    private int readInt(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 24)
             | ((data[offset + 1] & 0xFF) << 16)
             | ((data[offset + 2] & 0xFF) << 8)
             | (data[offset + 3] & 0xFF);
    }
}
