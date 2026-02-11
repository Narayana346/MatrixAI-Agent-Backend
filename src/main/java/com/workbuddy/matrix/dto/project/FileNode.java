package com.workbuddy.matrix.dto.project;

public record FileNode(
        String path,
        String modifiedAt,
        Long size,
        String type
) {
}
