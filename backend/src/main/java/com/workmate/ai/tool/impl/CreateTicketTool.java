package com.workmate.ai.tool.impl;

import com.workmate.ai.common.ErrorCode;
import com.workmate.ai.dto.TicketCreateDTO;
import com.workmate.ai.exception.BusinessException;
import com.workmate.ai.service.TicketService;
import com.workmate.ai.tool.AgentTool;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CreateTicketTool implements AgentTool {

    public static final String NAME = "createTicket";

    private final TicketService ticketService;

    public CreateTicketTool(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Object execute(Map<String, Object> parameters) {
        Boolean confirmed = getRequiredBoolean(parameters, "confirmed");
        if (!Boolean.TRUE.equals(confirmed)) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT);
        }

        Long userId = getRequiredLong(parameters, "userId");

        TicketCreateDTO request = new TicketCreateDTO();
        request.setSessionId(getRequiredLong(parameters, "sessionId"));
        request.setQuestionMessageId(getRequiredLong(parameters, "questionMessageId"));
        request.setTitle(getRequiredString(parameters, "title"));
        request.setDescription(getRequiredString(parameters, "description"));

        return ticketService.createTicket(userId, request);
    }

    private Long getRequiredLong(Map<String, Object> parameters, String key) {
        if (parameters == null || !(parameters.get(key) instanceof Number value)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        return value.longValue();
    }

    private String getRequiredString(Map<String, Object> parameters, String key) {
        if (parameters == null || !(parameters.get(key) instanceof String value) || value.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        return value.trim();
    }

    private Boolean getRequiredBoolean(Map<String, Object> parameters, String key) {
        if (parameters == null || !(parameters.get(key) instanceof Boolean value)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        return value;
    }
}