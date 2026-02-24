package com.workbuddy.matrix.llm.tool;

import com.workbuddy.matrix.service.ProjectFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class CodeGenerationTool {

    private final ProjectFileService projectFileService;
    private final Long projectId;

    @Tool(name = "read_files",
            description = "Read the content of files. Only input the file names present inside the FILE_TREE. DO NOT input any path which is not present under the FILE_TREE.")
    public List<String> readFiles(
            @ToolParam(description = "List of relative paths (e.g., ['src/App.tsx'])")
            List<String> paths
    ){
        List<String> result = new ArrayList<>();
        log.info("Requested file: {}", paths);
        for(String filePath : paths){
            String cleanPath = filePath.startsWith("/") ? filePath.substring(1) : filePath;
            String content = projectFileService.getFileContent(projectId,cleanPath).content();
            result.add(String.format(
                    "--- START OF FILE: %s ---\n%s\n--- END OF FILE ---",
                    cleanPath, content
            ));
        }
        return result;
    }
}
