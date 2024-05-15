package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {


    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryList() {
        //先从缓存中查询店铺类型列表
        List<String> shopTypeList = stringRedisTemplate.opsForList().range(RedisConstants.CACHE_SHOP_TYPE_KEY, 0, -1);

        List<ShopType> list = new ArrayList<>();
        //若缓存中不为空，则返回数据,返回的是对象，所以要将结合中的String字符串转换为对象
        if(!shopTypeList.isEmpty()){
            for (String shopType : shopTypeList) {
                ShopType sp = JSONUtil.toBean(shopType, ShopType.class);
                list.add(sp);
            }
            return Result.ok(list);
        }

        //若缓存中为空，则从数据库中查找
        List<ShopType> shopTypeListDB = query().orderByAsc("sort").list();
        if(shopTypeListDB.isEmpty()){
            return Result.fail("分类不存在");
        }
        for (ShopType shopTypeDB : shopTypeListDB) {
            String jsonStr = JSONUtil.toJsonStr(shopTypeDB);
            shopTypeList.add(jsonStr);
        }
        stringRedisTemplate.opsForList().rightPushAll(RedisConstants.CACHE_SHOP_TYPE_KEY,shopTypeList);
        return Result.ok(shopTypeListDB);

    }
}
