package com.workmate.ai.service;

import com.workmate.ai.service.impl.PromptBuilderImpl;
import com.workmate.ai.vo.PromptKnowledgeItem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PromptBuilderTest {

    private final PromptBuilder promptBuilder = new PromptBuilderImpl();

    @Test
    void shouldBuildPromptWithQuestionAndKnowledge() {
        String prompt = promptBuilder.buildKnowledgeAnswerPrompt(
                "  Git   分支应该怎么命名？ ",
                List.of(new PromptKnowledgeItem(
                        3L,
                        "Git 分支命名规范",
                        "研发规范",
                        "功能分支统一使用 feature/功能名称。"
                ))
        );

        assertThat(prompt).contains("你是 WorkMate AI 企业知识库问答助手。");
        assertThat(prompt).contains("用户问题：");
        assertThat(prompt).contains("Git 分支应该怎么命名？");
        assertThat(prompt).contains("分类：研发规范");
        assertThat(prompt).contains("标题：Git 分支命名规范");
        assertThat(prompt).contains("内容：功能分支统一使用 feature/功能名称。");
        assertThat(prompt).contains("只依据上面的企业知识回答");
        assertThat(prompt).doesNotContain("  Git   分支");
    }

    @Test
    void shouldBuildPromptWhenKnowledgeIsEmpty() {
        String prompt = promptBuilder.buildKnowledgeAnswerPrompt("未知问题", List.of());

        assertThat(prompt).contains("用户问题：");
        assertThat(prompt).contains("未知问题");
        assertThat(prompt).contains("企业知识：");
        assertThat(prompt).contains("无可用知识。");
    }
}