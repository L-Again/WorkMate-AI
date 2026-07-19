package com.workmate.ai.controller;

import com.workmate.ai.common.CommonResult;
import com.workmate.ai.common.PageResult;
import com.workmate.ai.dto.TicketCreateDTO;
import com.workmate.ai.dto.TicketStatusUpdateDTO;
import com.workmate.ai.service.TicketService;
import com.workmate.ai.vo.TicketListVO;
import com.workmate.ai.vo.TicketVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/api/tickets")
    public CommonResult<TicketVO> createTicket(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody TicketCreateDTO request) {
        return CommonResult.success(ticketService.createTicket(userId, request));
    }

    @GetMapping("/api/tickets/my")
    public CommonResult<PageResult<TicketListVO>> listMyTickets(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String status) {
        return CommonResult.success(ticketService.listMyTickets(userId, pageNum, pageSize, status));
    }

    @GetMapping("/api/tickets/{ticketId}")
    public CommonResult<TicketVO> getTicketDetail(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long ticketId) {
        return CommonResult.success(ticketService.getTicketDetail(userId, ticketId));
    }

    @GetMapping("/api/admin/tickets")
    public CommonResult<PageResult<TicketListVO>> listAdminTickets(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return CommonResult.success(ticketService.listAdminTickets(userId, pageNum, pageSize, status, keyword));
    }

    @PatchMapping("/api/admin/tickets/{ticketId}/status")
    public CommonResult<TicketVO> updateTicketStatus(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long ticketId,
            @Valid @RequestBody TicketStatusUpdateDTO request) {
        return CommonResult.success(ticketService.updateTicketStatus(userId, ticketId, request));
    }
}