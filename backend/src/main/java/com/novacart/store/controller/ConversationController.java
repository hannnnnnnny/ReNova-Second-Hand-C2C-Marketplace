package com.novacart.store.controller;

import com.novacart.store.dto.ApiResponse;
import com.novacart.store.dto.MessageDtos;
import com.novacart.store.service.MessagingService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final MessagingService messagingService;

    public ConversationController(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MessageDtos.ConversationSummary>>> list() {
        return ResponseEntity.ok(ApiResponse.success("Conversations.", messagingService.listMine()));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> unread() {
        return ResponseEntity.ok(ApiResponse.success("Unread.", Map.of("count", messagingService.unreadCount())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MessageDtos.ConversationDetail>> start(@Valid @RequestBody MessageDtos.StartConversationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Message sent.", messagingService.startOrSendInitial(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MessageDtos.ConversationDetail>> open(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Conversation.", messagingService.openConversation(id)));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<MessageDtos.MessageResponse>> send(
            @PathVariable Long id,
            @Valid @RequestBody MessageDtos.SendMessageRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Message sent.", messagingService.sendMessage(id, request)));
    }
}
