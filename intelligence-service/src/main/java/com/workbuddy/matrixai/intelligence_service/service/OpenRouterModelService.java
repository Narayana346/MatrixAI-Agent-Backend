package com.workbuddy.matrixai.intelligence_service.service;

import com.workbuddy.matrixai.intelligence_service.dto.model.OpenRouterModelInfo;

import java.util.List;

public interface OpenRouterModelService {
    List<OpenRouterModelInfo> listModels();
}
