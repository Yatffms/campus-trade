package com.campus.trade.controller;
import com.campus.trade.VO.MessageVO;
import com.campus.trade.VO.PageResult;
import com.campus.trade.common.Result;
import com.campus.trade.dto.MessageCreateDto;
import com.campus.trade.service.MessageService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/message")

public class MessageController {
    @Resource
    private MessageService messageService;

    @PostMapping
    public Result<Void> send(@RequestBody @Valid MessageCreateDto dto){
        messageService.send(dto);
        return Result.ok();
    }
    @GetMapping("/item/{itemId}")
    public Result<PageResult<MessageVO>> pageByItem(
            @PathVariable Long itemId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return Result.ok(messageService.pageByItem(itemId, page, size));
    }
    @GetMapping("/received")
    public Result<PageResult<MessageVO>> pageReceived(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return Result.ok(messageService.pageReceived(page, size));
    }
    @GetMapping("/unread-count")
    public Result<Long> unReadCount(){
        return Result.ok(messageService.unReadCount());
    }
    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id){
        messageService.markRead(id);
        return Result.ok();
    }
    @PutMapping("/read-all")
    public Result<Void> markAllRead(){
        messageService.markAllRead();
        return Result.ok();
    }
}
