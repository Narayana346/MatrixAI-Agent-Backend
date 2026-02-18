package com.workbuddy.matrix.llm.tool;

import com.workbuddy.matrix.service.ProjectFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;

import java.util.ArrayList;
import java.util.List;


@RequiredArgsConstructor
public class CodeGenerationTool {

    private final ProjectFileService projectFileService;
    private final Long projectId;

    @Tool(
            name = "Read Files",
            description = """
                    Reads the content of the specified files and returns the content as a list of strings.
                    In that result file content format is as follows:
                    --- START OF FILE <file_path> ---
                    <file_content>
                    --- END OF FILE <file_path> ---
                    Only input the file names present inside the file tree. DO NOT input any file names that are not present in the file tree.
                    """
    )
    public List<String> readFiles(List<String> path){
        List<String> result = new ArrayList<>();
        for(String filePath : path){
            String cleanPath = filePath.startsWith("/") ? filePath.substring(1) : filePath;
            String content = projectFileService.getFileContent(projectId,cleanPath).content();
            result.add(String.format("--- START OF FILE %s ---\n%s\n--- END OF FILE %s ---\n",cleanPath,content,cleanPath));
        }
        return result;
    }
}
