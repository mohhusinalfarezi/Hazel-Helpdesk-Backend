package com.chatkeluhan.demo.service;

import com.chatkeluhan.demo.dto.ChatResponseDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class GeminiEscalationTest {

    private static final Pattern ESCALATE_PATTERN = Pattern.compile("<<<ESCALATE:\\s*(\\{.*?\\})\\s*>>>", Pattern.DOTALL);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testEscalationParsingSuccess() throws Exception {
        String aiReply = "Baik, sepertinya kendala monitor Anda memerlukan pemeriksaan langsung oleh teknisi.\n\n" +
                "<<<ESCALATE:{\"needTicket\":true,\"category\":\"Hardware\",\"summary\":\"Monitor kantor mati total dan bau hangus\"}>>>";

        Matcher matcher = ESCALATE_PATTERN.matcher(aiReply);
        assertTrue(matcher.find());

        String jsonPart = matcher.group(1).trim();
        JsonNode node = objectMapper.readTree(jsonPart);

        boolean shouldEscalate = node.path("needTicket").asBoolean(true);
        String category = node.path("category").asText();
        String summary = node.path("summary").asText();

        String cleanReply = matcher.replaceAll("").trim();

        assertEquals("Baik, sepertinya kendala monitor Anda memerlukan pemeriksaan langsung oleh teknisi.", cleanReply);
        assertTrue(shouldEscalate);
        assertEquals("Hardware", category);
        assertEquals("Monitor kantor mati total dan bau hangus", summary);

        ChatResponseDto dto = new ChatResponseDto(cleanReply, shouldEscalate, category, summary);
        assertEquals(cleanReply, dto.getReply());
        assertTrue(dto.isShouldEscalate());
        assertEquals("Hardware", dto.getSuggestedCategory());
        assertEquals("Monitor kantor mati total dan bau hangus", dto.getSuggestedSummary());
    }

    @Test
    public void testNoEscalation() {
        String aiReply = "Silakan coba restart router Anda terlebih dahulu dengan menekan tombol power selama 10 detik.";

        Matcher matcher = ESCALATE_PATTERN.matcher(aiReply);
        assertFalse(matcher.find());

        ChatResponseDto dto = new ChatResponseDto(aiReply, false, null, null);
        assertEquals(aiReply, dto.getReply());
        assertFalse(dto.isShouldEscalate());
        assertNull(dto.getSuggestedCategory());
        assertNull(dto.getSuggestedSummary());
    }
}
