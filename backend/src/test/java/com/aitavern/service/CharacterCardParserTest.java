package com.aitavern.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CharacterCardParserTest {

    @Autowired
    private CharacterCardParser parser;

    @Test
    void parseJson_shouldExtractFields() throws Exception {
        String json = """
            {
                "name": "TestBot",
                "description": "A test character",
                "personality": "Friendly",
                "first_mes": "Hello!",
                "scenario": "Testing",
                "system_prompt": "You are helpful",
                "mes_example": "User: Hi\\nBot: Hey!"
            }
            """;
        InputStream in = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        var result = parser.parse(in, "test.json");

        assertEquals("TestBot", result.fields().get("name"));
        assertEquals("A test character", result.fields().get("description"));
        assertEquals("Hello!", result.fields().get("first_message"));
        assertNull(result.avatarBytes());
    }

    @Test
    void parseUnsupportedFormat_shouldThrow() {
        InputStream in = new ByteArrayInputStream("hello".getBytes());
        assertThrows(IllegalArgumentException.class, () -> parser.parse(in, "test.txt"));
    }
}
