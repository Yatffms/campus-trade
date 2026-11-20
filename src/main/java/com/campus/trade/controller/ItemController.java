package com.campus.trade.controller;



import com.campus.trade.VO.ItemVO;
import com.campus.trade.VO.PageResult;
import com.campus.trade.common.Result;
import com.campus.trade.dto.ItemCreateDto;
import com.campus.trade.dto.ItemStatusDto;
import com.campus.trade.service.ItemService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/item")//规定门牌号 每次前端访问必须先访问这个门牌号

public class ItemController {

    @Resource
    private ItemService itemService;
    @PostMapping
    public Result<Long> publish(@RequestBody @Valid ItemCreateDto dto){
        return Result.ok(itemService.publish(dto));
    }




    @GetMapping("/page")
    public Result<PageResult<ItemVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        return Result.ok(itemService.page(page, size, keyword, categoryId));
    }

    @GetMapping("/{id}")
    public Result<ItemVO> detail(@PathVariable("id") Long id) {
        return Result.ok(itemService.getDetail(id));
    }
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus (@PathVariable Long id,
                                      @RequestBody @Valid ItemStatusDto dto) {
        itemService.updateStatus(id, dto.getStatus());
        return Result.ok(null);
    }

    @PutMapping("/{id}/edit")
    public Result<Void> edit(@PathVariable Long id,
                              @RequestBody @Valid ItemCreateDto dto) {
        itemService.edit(id, dto);
        return Result.ok(null);
    }

}
