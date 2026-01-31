package com.example.solidconnection.common.constant;

import java.util.List;
import java.util.stream.Stream;

public final class FileConstants {
    private FileConstants() {}

    public static final List<String> IMAGE_EXTENSIONS = List.of(
            "jpg", "jpeg", "png", "webp", "avif", "heic", "heif", "tiff"
    );

    public static final List<String> DOCUMENT_EXTENSIONS = List.of(
            "doc", "docx", "xls", "xlsx", "ppt", "pptx", "hwp", "hwpx", "pdf", "txt"
    );

    public static final List<String> ARCHIVE_EXTENSIONS = List.of(
            "zip", "7z", "rar"
    );

    public static final List<String> ALL_ALLOWED_EXTENSIONS = Stream.of(
                    IMAGE_EXTENSIONS, DOCUMENT_EXTENSIONS, ARCHIVE_EXTENSIONS)
            .flatMap(List::stream)
            .toList();
}
