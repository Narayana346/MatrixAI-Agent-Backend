package com.workbuddy.matrix.service.impl;

import aj.org.objectweb.asm.commons.Remapper;
import com.workbuddy.matrix.service.AiGenerationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
public class AiGenerationServiceImp implements AiGenerationService {

    @Override
    public Flux<String> stremResponse(String message, Long aLong) {
        return null;
    }
}
