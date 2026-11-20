package com.campus.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trade.VO.ItemVO;
import com.campus.trade.VO.PageResult;
import com.campus.trade.common.BusinessException;
import com.campus.trade.common.UserContext;
import com.campus.trade.entity.Favorite;
import com.campus.trade.entity.Item;
import com.campus.trade.entity.User;
import com.campus.trade.mapper.FavoriteMapper;
import com.campus.trade.mapper.ItemMapper;
import com.campus.trade.mapper.UserMapper;
import com.campus.trade.service.FavoriteService;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
public class FavoriteServiceImpl implements FavoriteService {
    @Resource
    private FavoriteMapper favoriteMapper;

    @Resource
    private ItemMapper itemMapper;
    @Resource
    private UserMapper userMapper;


    @Override
    public void add(Long itemId) {
        Long userId = UserContext.getUserId();
        //商品必须存在
        if (itemMapper.selectById(itemId) == null)
            throw new BusinessException(404, "商品不存在");
        //已经收藏就直接返回
        Long count = favoriteMapper.selectCount(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getItemId, itemId));
        if (count > 0)
            return;


        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setItemId(itemId);

        try {
            favoriteMapper.insert(favorite);
        } catch (DuplicateKeyException e) {
            //并发下两次请求同时插入，唯一索引栏下视为已收藏
        }
    }

    @Override
    public void remove(Long itemId) {
        Long userId = UserContext.getUserId();
        favoriteMapper.delete(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getItemId, itemId));
    }

    @Override
    public PageResult<ItemVO> page(int page, int size) {
        Long userId = UserContext.getUserId();
        if (size > 50) {
            size = 50;
        }

        //首先是 分页查 我收藏了哪些ITemID
        Page<Favorite> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Favorite> qw = new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreateTime);
        Page<Favorite> favoritePage = favoriteMapper.selectPage(pageParam, qw);
        List<Long> itemIds = favoritePage
                .getRecords()
                .stream().map(Favorite::getItemId)
                .toList();

        //然后是 一次查询所有ITemID 不是循环查
        List<Item> items = itemIds.isEmpty()
                ? List.of()
                : itemMapper.selectBatchIds(itemIds);
        //下一步是id 到ITem 方便按照收藏顺序还原
        Map<Long, Item> itemMap = items.stream()
                .collect(Collectors.toMap(Item::getId, item -> item));

        List<Long> sellerIds = items.stream().map(Item::getSellerId).distinct().toList();
        Map<Long, User> userMap = sellerIds.isEmpty()
                ? Map.of()
                : userMapper.selectBatchIds(sellerIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));


        List<ItemVO> records = itemIds.stream()
                .map(itemMap::get)
                .filter(Objects::nonNull)     //商品被删除时跳过
                .map(i -> toVO(i, userMap.get(i.getSellerId())))
                .toList();
        //最后组装分页结果
        PageResult<ItemVO> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(favoritePage.getTotal());
        result.setSize(favoritePage.getSize());
        result.setPage(favoritePage.getCurrent());
        result.setPages(favoritePage.getPages());
        return result;
    }

    private ItemVO toVO(Item item , User seller) {
        ItemVO vo = new ItemVO();
        vo.setId(item.getId());
        vo.setTitle(item.getTitle());
        vo.setPrice(item.getPrice());
        vo.setOriginalPrice(item.getOriginalPrice());
        vo.setDescription(item.getDescription());
        vo.setCoverImage(item.getCoverImage());
        vo.setCategoryId(item.getCategoryId());
        vo.setStatus(item.getStatus());
        vo.setSchool(item.getSchool());
        vo.setViewCount(item.getViewCount());
        vo.setCreateTime(item.getCreateTime());

        if (seller != null) {
            vo.setSellerId(seller.getId());
            vo.setSellerNickname(seller.getNickname());
            vo.setSellerAvatar(seller.getAvatar());
        }

        return vo;
    }
}
