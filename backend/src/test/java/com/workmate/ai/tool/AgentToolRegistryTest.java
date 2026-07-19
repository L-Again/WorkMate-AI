package com.workmate.ai.tool;

import com.workmate.ai.common.ErrorCode;
import com.workmate.ai.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgentToolRegistryTest {

    @Test
    void shouldRegisterToolsByName() {
        AgentTool searchTool = new FakeAgentTool("searchKnowledge");
        AgentToolRegistry registry = new AgentToolRegistry(List.of(searchTool));

        assertThat(registry.getTools()).containsEntry("searchKnowledge", searchTool);
        assertThat(registry.getTool("searchKnowledge")).isSameAs(searchTool);
    }

    @Test
    void shouldRejectUnknownToolName() {
        AgentToolRegistry registry = new AgentToolRegistry(List.of(new FakeAgentTool("searchKnowledge")));

        assertThatThrownBy(() -> registry.getTool("createTicket"))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DATA_NOT_FOUND));
    }

    @Test
    void shouldRejectDuplicateToolName() {
        AgentTool first = new FakeAgentTool("searchKnowledge");
        AgentTool second = new FakeAgentTool("searchKnowledge");

        assertThatThrownBy(() -> new AgentToolRegistry(List.of(first, second)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate agent tool name");
    }

    private static class FakeAgentTool implements AgentTool {

        private final String name;

        private FakeAgentTool(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Object execute(Map<String, Object> parameters) {
            return null;
        }
    }
}