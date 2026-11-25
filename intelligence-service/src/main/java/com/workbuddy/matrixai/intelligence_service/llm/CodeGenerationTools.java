package com.workbuddy.matrixai.intelligence_service.llm;

import com.workbuddy.matrixai.common_lib.security.AuthUtil;
import com.workbuddy.matrixai.intelligence_service.client.WorkspaceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class CodeGenerationTools {

    private final Long projectId;
    private final WorkspaceClient workspaceClient;
    private final AuthUtil authUtil;
    private final String jwtToken;

    @Tool(name = "read_files",
            description = "Read the content of files. Only input the file names present inside the FILE_TREE. DO NOT input any path which is not present under the FILE_TREE.")
    public List<String> readFiles(
            @ToolParam(description = "List of relative paths (e.g., [\"src/App.tsx\"])")
            List<String> paths
    ) {

        List<String> result = new ArrayList<>();

        for(String path: normalizePaths(paths)) {
            String cleanPath = path.startsWith("/") ? path.substring(1) : path;

            log.info("Requested file: {}", cleanPath);

            String content = authUtil.runAs(jwtToken, () -> workspaceClient.getFileContent(projectId, cleanPath));

            result.add(String.format(
                    "--- START OF FILE: %s ---\n%s\n--- END OF FILE ---",
                    cleanPath, content
            ));

        }

        return result;
    }

    // Some local models (e.g. Llama-3.1 via llama.cpp) serialize a list argument as a
    // single Python-repr string (e.g. "['a.tsx', 'b.tsx']") instead of a real JSON array,
    // regardless of how the parameter is described. Jackson then binds it as a one-element
    // list containing that literal string rather than throwing, so unpack it defensively.
    private List<String> normalizePaths(List<String> paths) {
        if (paths.size() != 1) {
            return paths;
        }
        String single = paths.get(0).trim();
        if (!single.startsWith("[") || !single.endsWith("]")) {
            return paths;
        }
        List<String> unpacked = new ArrayList<>();
        for (String part : single.substring(1, single.length() - 1).split(",")) {
            String cleaned = part.trim().replaceAll("^['\"]|['\"]$", "");
            if (!cleaned.isEmpty()) {
                unpacked.add(cleaned);
            }
        }
        return unpacked.isEmpty() ? paths : unpacked;
    }
}
