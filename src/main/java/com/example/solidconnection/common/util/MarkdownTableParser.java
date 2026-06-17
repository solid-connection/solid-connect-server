package com.example.solidconnection.common.util;

import static com.example.solidconnection.common.exception.ErrorCode.INVALID_MARKDOWN_FORMAT;

import com.example.solidconnection.common.exception.CustomException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class MarkdownTableParser {

    public List<Map<String, String>> parse(String markdown) {
        String[] lines = markdown.trim().split("\n");
        validate(lines);
        List<String> headers = parseRow(lines[0]);
        return Arrays.stream(lines)
                .skip(2)
                .filter(line -> !line.isBlank())
                .map(line -> buildRowMap(headers, parseRow(line)))
                .filter(row -> !row.isEmpty())
                .collect(Collectors.toList());
    }

    private void validate(String[] lines) {
        if (lines.length < 3 || !lines[1].contains("---")) {
            throw new CustomException(INVALID_MARKDOWN_FORMAT);
        }
    }

    private List<String> parseRow(String line) {
        String stripped = line.trim();
        if (stripped.startsWith("|")) stripped = stripped.substring(1);
        if (stripped.endsWith("|")) stripped = stripped.substring(0, stripped.length() - 1);
        return Arrays.stream(stripped.split("(?<!\\\\)\\|"))
                .map(cell -> cell.replace("\\|", "|").trim())
                .collect(Collectors.toList());
    }

    private Map<String, String> buildRowMap(List<String> headers, List<String> cells) {
        Map<String, String> row = new LinkedHashMap<>();
        for (int i = 0; i < headers.size() && i < cells.size(); i++) {
            if (!cells.get(i).isBlank()) {
                row.put(headers.get(i), cells.get(i));
            }
        }
        return row;
    }
}
