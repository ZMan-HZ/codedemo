/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.zzm.demos.service;

import com.zzm.demos.api.ProviderConsumer;
import com.zzm.demos.api.SkuReadService;
import com.zzm.demos.data.ChannelInventoryDO;
import com.zzm.demos.data.ItemDO;
import com.zzm.demos.data.SkuDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @version $Id: ItemAggregationProviderConsumerImpl.java, v 0.1 2019年11月20日 3:06 PM superchao Exp $
 */
@Service
public class ItemAggregationProviderConsumer implements ProviderConsumer<List<ItemDO>> {
    @Autowired
    SkuReadService skuReadService;

    @Override
    public void execute(ResultHandler<List<ItemDO>> handler) {
        List<SkuDO> skuDOList = new ArrayList<>();
        skuReadService.loadSkus(skuDO -> {
            skuDOList.add(skuDO);
            return skuDO;
        });
        Map<String, List<SkuDO>> collections = skuDOList.stream().collect(Collectors.groupingBy(SkuDO::getSkuType));
        List<ItemDO> items = new ArrayList<>();
        for (Map.Entry<String, List<SkuDO>> entry : collections.entrySet()) {
//            if ("ORIGIN".equals(entry.getKey())) {
//                buildItemDo(entry.getKey(), items, entry);
//            } else {
                buildItemDo(entry.getKey(), items, entry);
//            }
        }
        handler.handleResult(items);
    }

    private static void buildItemDo(String groupBy, List<ItemDO> items, Map.Entry<String, List<SkuDO>> entry) {
        boolean isDigital = false;
        Map<String, List<SkuDO>> collectMap = new HashMap<>();
        switch (groupBy) {
            case "ORIGIN":
                collectMap = entry.getValue().stream().collect(Collectors.groupingBy(SkuDO::getArtNo));
                break;
            case "DIGITAL":
                isDigital = true;
                collectMap = entry.getValue().stream().collect(Collectors.groupingBy(SkuDO::getSpuId));
                break;
            default:
                break;
        }
        for (Map.Entry<String, List<SkuDO>> key : collectMap.entrySet()) {
            ItemDO itemDO = new ItemDO();
            String name = entry.getKey() + "-" + key.getKey();
            List<SkuDO> skuList = key.getValue();
            BigDecimal maxPrice = skuList.stream().max(Comparator.comparing(SkuDO::getPrice)).get().getPrice().setScale(2,BigDecimal.ROUND_HALF_UP);
            BigDecimal minPrice = skuList.stream().min(Comparator.comparing(SkuDO::getPrice)).get().getPrice().setScale(2,BigDecimal.ROUND_HALF_UP);
            BigDecimal inventory = new BigDecimal("0.00");
            List<String> skuIds = new ArrayList<>();
            for (SkuDO skuDO : skuList) {
                skuIds.add(skuDO.getId());
                List<ChannelInventoryDO> channelInventoryDOList = skuDO.getInventoryList();
                inventory = inventory.add(channelInventoryDOList.stream().map(ChannelInventoryDO::getInventory).reduce(BigDecimal.ZERO, BigDecimal::add));
            }
            //给ItemDO赋值
            if(isDigital){
                itemDO.setSpuId(key.getKey());
            }else{
                itemDO.setArtNo(key.getKey());
            }
            itemDO.setName(name);
            itemDO.setInventory(inventory);
            itemDO.setMaxPrice(maxPrice);
            itemDO.setMinPrice(minPrice);
            itemDO.setSkuIds(skuIds);
            items.add(itemDO);
        }
    }

}
