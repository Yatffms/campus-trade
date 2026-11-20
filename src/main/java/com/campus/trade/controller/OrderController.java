package com.campus.trade.controller;


import com.campus.trade.VO.OrderVO;
import com.campus.trade.VO.PageResult;
import com.campus.trade.common.Result;
import com.campus.trade.dto.ItemCreateDto;
import com.campus.trade.dto.OrderCreateDto;
import com.campus.trade.service.OrderService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    @Resource
    private OrderService orderService;

    @PostMapping
    public Result<Long> create (@RequestBody @Valid OrderCreateDto orderCreateDto){
        return Result.ok(orderService.create(orderCreateDto.getItemId()));
    }
    @GetMapping("/bought")
    public Result<PageResult<OrderVO>> bought(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(orderService.pageBought(page, size));
    }

    @GetMapping("/sold")
    public Result<PageResult<OrderVO>> sold(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(orderService.pageSold(page, size));
    }
    @PutMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id){
        orderService.confirm(id);
        return Result.ok();
    }
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        orderService.cancel(id);
        return Result.ok(null);
    }
    @PutMapping("/{id}/finish")
    public Result<Void> finish(@PathVariable Long id) {
        orderService.finish(id);
        return Result.ok();
    }

}
