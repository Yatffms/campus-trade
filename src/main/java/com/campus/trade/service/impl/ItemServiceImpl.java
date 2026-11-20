package com.campus.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trade.cache.ItemCache;
import com.campus.trade.VO.ItemVO;
import com.campus.trade.VO.PageResult;
import com.campus.trade.common.UserContext;
import com.campus.trade.dto.ItemCreateDto;
import com.campus.trade.entity.Item;
import com.campus.trade.entity.User;
import com.campus.trade.mapper.ItemMapper;
import com.campus.trade.mapper.UserMapper;
import com.campus.trade.service.ItemService;
import jakarta.annotation.Resource;
import com.campus.trade.common.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
public class ItemServiceImpl implements ItemService {
    @Resource
    private ItemMapper itemMapper;

    @Resource
    private UserMapper userMapper;  //后面补的


    @Resource
    private ItemCache itemCacheService;

    @Override
    public Long publish(ItemCreateDto dto) {

        Item item = new Item();
        item.setSellerId(UserContext.getUserId());//从token里面取
        item.setCategoryId(dto.getCategoryId());
        item.setTitle(dto.getTitle());
        item.setDescription(dto.getDescription());
        item.setPrice(new BigDecimal(dto.getPrice()));

        if (dto.getOriginalPrice() != null){
            item.setOriginalPrice((dto.getOriginalPrice()));
        }

        item.setCoverImage(dto.getCoverImage());
        item.setSchool(dto.getSchool());
        item.setStatus(1);//默认在售

        itemMapper.insert(item);

        return item.getId();
    }

    @Override
    public PageResult<ItemVO> page(int page, int size, String keyword, Long categoryId) {

        size=Math.min(size,50);//防暴击
        Page<Item> pageparam = new Page<>(page, size);
        LambdaQueryWrapper<Item> qw = new LambdaQueryWrapper<>();
              qw.eq(Item::getStatus,1)//只查找在售
                .eq(categoryId!=null,Item::getCategoryId,categoryId)//分类可选
                .like(StringUtils.hasText(keyword),Item::getTitle,keyword)//关键词可选
                .orderByDesc(Item::getCreateTime);//新的在前面

        Page<Item> itemPage = itemMapper.selectPage(pageparam, qw);
        List<Item> items = itemPage.getRecords();

        //先把这一页所有卖家ID都接受起来 然后去重‘
        List<Long> sellerIds=items.stream()
                .map(Item::getSellerId)
                .distinct()
                .collect(Collectors.toList());
        //一次SQL把所以卖家都查出来 做成ID到user的map  字典
        Map<Long, User>sellerMap=sellerIds.isEmpty()
                ? Map.of()
                :userMapper.selectBatchIds(sellerIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        //组装VO时候按照SellerId从map取

        PageResult  <ItemVO> result=new PageResult<>();
        result.setRecords(items.stream()
                .map(item -> toVO(item,sellerMap.get(item.getSellerId())))
                .collect(Collectors.toList()));
        result.setTotal(itemPage.getTotal());
        result.setSize(itemPage.getSize());
        result.setPage(itemPage.getCurrent());
        result.setPages(itemPage.getPages());
        return result;

       // return itemMapper.selectPage(pageparam,qw);
    }
   /* @Override
    public Item getDetail(Long id) {
        Item item = itemMapper.selectById(id);
        if (item==null){
            throw new BusinessException( 404,"商品不存在");
        }
        item.setViewCount(item. getViewCount()+1);//增加浏览量
        itemMapper.updateById(item);
        return item;
    }*/
//    @Override
//    public ItemVO getDetail(Long id) {
//        Item item = itemMapper.selectById(id);
//        if (item==null){
//            throw new BusinessException( 404,"商品不存在");
//        }
//
//        item.setViewCount(item. getViewCount()+1);//增加浏览量
//        itemMapper.updateById(item);
//        User seller = userMapper.selectById(item.getSellerId());
//
//        return toVO(item, seller);
//
//        }

    @Override
    public ItemVO getDetail(Long id) {
        ItemCache.CacheResult<ItemVO> cached = itemCacheService.get(id);
        ItemVO vo;
        if (cached.hit()) {
            vo = cached.value();
            if (vo == null) {
                throw new BusinessException(404, "商品不存在");
            }
        }else {
            Item item = itemMapper.selectById(id);
            if (item == null) {
                itemCacheService.putNull(id);
                throw new BusinessException(404, "商品不存在");
            }
            User seller = userMapper.selectById(item.getSellerId());
            vo = toVO(item, seller);
            itemCacheService.put( vo);
        }
        //至于最终的浏览量如何计算  直接交给SQL原子操作就行了 不写入缓存
        itemMapper.update(null, new LambdaUpdateWrapper<Item>()
                .eq(Item::getId, id)
                .setSql("view_count = view_count + 1"));
        Item latest = itemMapper.selectById(id);
        vo.setViewCount(latest == null ? vo.getViewCount() : latest.getViewCount());
        return vo;





    }
    private ItemVO toVO(Item item, User seller) {
        ItemVO vo = new ItemVO();

        //seller一定要要填写，前端靠他判断“是不是我发布的”
        vo.setSellerId(item.getSellerId()); // Seller ID
        //如果卖家被删除 那就只保留SellerId 不填写昵称头像
        if (seller != null) {
            vo.setSellerNickname(seller.getNickname());
            vo.setSellerAvatar(seller.getAvatar());
        }
        vo.setId(item.getId());
        vo.setTitle(item.getTitle());
        vo.setDescription(item.getDescription());
        vo.setPrice(item.getPrice());
        vo.setOriginalPrice(item.getOriginalPrice());
        vo.setCoverImage(item.getCoverImage());
        vo.setSchool(item.getSchool());
        vo.setCategoryId(item.getCategoryId());
        vo.setStatus(item.getStatus());
        vo.setViewCount(item.getViewCount());
        vo.setCreateTime(item.getCreateTime());
        return vo;
    }

    @Override
    public void updateStatus(Long itemId, Integer status) {
        Item item = itemMapper.selectById(itemId);
        if (item == null)
            throw new BusinessException(404, "商品不存在");
        if (!Objects.equals(item.getSellerId(), UserContext.getUserId()))
            throw new BusinessException(403, "只能修改自己的商品");
        if (Integer.valueOf(2).equals(item.getStatus()))
            throw new BusinessException(400, "交易中的商品不能下架");
        if (status == null || !Integer.valueOf(0).equals(status) && !Integer.valueOf(1).equals(status))
            throw new BusinessException(400, "状态只能是0或1");//其实这句话不用写属于过度防御了因为你在Dto的数据流层面已经判断过这个status只有 0 1状态了

        item.setStatus(status);
        itemMapper.updateById(item);
        itemCacheService.evict(itemId);
    }
    @Override
    public void edit(Long itemId, ItemCreateDto dto) {
        Item item = itemMapper.selectById(itemId);
        if (item == null)
            throw new BusinessException(404, "商品不存在");
        if (!Objects.equals(item.getSellerId(), UserContext.getUserId()))
            throw new BusinessException(403, "只能修改自己发布的商品");
        if (Integer.valueOf(2).equals(item.getStatus()))
            throw new BusinessException(400, "交易中的商品不能修改");
        item.setTitle(dto.getTitle());
        item.setDescription(dto.getDescription());
        item.setPrice(new BigDecimal(dto.getPrice()));
        if (dto.getOriginalPrice() != null)
            item.setOriginalPrice((dto.getOriginalPrice()));



        item.setCoverImage(dto.getCoverImage());
        item.setSchool(dto.getSchool());
        item.setCategoryId(dto.getCategoryId());

        itemMapper.updateById(item);
        itemCacheService.evict(itemId);
    }

}
