package com.workmate.ai.service;

import com.workmate.ai.vo.PromptKnowledgeItem;

import java.util.List;

public interface PromptBuilder {

    String buildKnowledgeAnswerPrompt(String question, List<PromptKnowledgeItem> knowledgeItems);
}