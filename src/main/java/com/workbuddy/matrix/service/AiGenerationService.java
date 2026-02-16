package com.workbuddy.matrix.service;

import aj.org.objectweb.asm.commons.Remapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;

public interface AiGenerationService {
    Flux<String> stremResponse(@NotBlank String message, @NotNull Long aLong);
}
