package com.campus.trade.controller;


import com.campus.trade.VO.ItemVO;
import com.campus.trade.VO.PageResult;
import com.campus.trade.common.Result;
import com.campus.trade.service.FavoriteService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController  {
    @Resource
    private FavoriteService favoriteService;

    @PostMapping("/{itemId}")
    public Result<Void> add(@PathVariable("itemId") Long itemId){
        favoriteService.add(itemId);
        return Result.ok();
    }
    @DeleteMapping("/{itemId}")
    public Result<Void> remove(@PathVariable("itemId") Long itemId){
        favoriteService.remove(itemId);
        return Result.ok();
    }
    @GetMapping("/page")
    public Result<PageResult<ItemVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size){
        return Result.ok(favoriteService.page(page,size));
    }



}
