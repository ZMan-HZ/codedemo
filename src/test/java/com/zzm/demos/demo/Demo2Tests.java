package com.zzm.demos.demo;

import com.alibaba.fastjson.JSON;
import com.zzm.demos.api.ProviderConsumer;
import com.zzm.demos.api.SkuReadService;
import com.zzm.demos.data.ChannelInventoryDO;
import com.zzm.demos.data.ItemDO;
import com.zzm.demos.data.SkuDO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;
import java.util.function.Function;

/**
 * 注意： 假设sku数据很多, 无法将sku列表完全加载到内存中
 */
@SpringBootTest
@Slf4j
class Demo2Tests {

    @Autowired
    SkuReadService skuReadService;

    @Autowired
    ProviderConsumer<List<ItemDO>> providerConsumer;

    /**
     * 1：
     * 在com.zzm.demos.service.SkuReadServiceImpl中实现com.zzm.demos.api.SkuReadService#loadSkus(com.zzm.demos.api.SkuReadService.SkuHandler)
     * 从/resources/data/data.xls读取数据并逐条打印数据
     */
    @Test
    void readDataFromExcelWithHandlerTest() {
        AtomicInteger count = new AtomicInteger();
        skuReadService.loadSkus(skuDO -> {
            log.info("读取SKU信息={}", JSON.toJSONString(skuDO));
            count.incrementAndGet();
            return skuDO;
        });
        Assert.isTrue(count.get() == 10, "未能读取商品列表");
    }

    /**
     * 2：
     * 计算以下统计值:
     * 1、获取价格在最中间的任意一个skuId，假设所有sku的价格都是精确到1元且一定小于1万元
     * 2、每个渠道库存量为前五的skuId列表 例如( miao:[1,2,3,4,5],tmall:[3,4,5,6,7],intime:[7,8,4,3,1]
     * 3、所有sku的总价值
     */
    @Test
    void statisticsDataTest() {
        List<SkuDO> skuDOList = new ArrayList<>();
        skuReadService.loadSkus(skuDO -> {
            skuDOList.add(skuDO);
            return skuDO;
        });
        //1。获取价格在最中间的任意一个skuId
        log.info("1.获取价格在最中间的任意一个skuId");
        findMiddlestSkuPrice(skuDOList);
        //2.每个渠道库存量为前五的skuId列表
        log.info("2.每个渠道库存量为前五的skuId列表");
        getEachChannelInventory(skuDOList);
        //3.所有sku的总价值
        log.info("3.所有sku的总价值");
        calcPositionOfAllSkus(skuDOList);

    }

    private String findMiddlestSkuPrice(List<SkuDO> skuDOList) {

        //所有sku价格的平均值，作为中间值
        Function<SkuDO,BigDecimal> function = skuDO -> skuDO.getPrice();
        BinaryOperator<BigDecimal> binaryOperator = (bigDecimal, bigDecimal2) -> bigDecimal.add(bigDecimal2);
        BigDecimal average = skuDOList.stream().map(function).reduce(BigDecimal.ZERO, binaryOperator).divide(BigDecimal.valueOf(skuDOList.size())).setScale(2, BigDecimal.ROUND_HALF_UP);

        //对Sku按价格升序排序
        //下面一行代码经过两次lambda转换就是下下面注释掉的Comparator.comparing 写法
        skuDOList.sort(new Comparator<SkuDO>() {
            @Override
            public int compare(SkuDO o1, SkuDO o2) {
                return o1.getPrice().compareTo(o2.getPrice());
            }
        });
//        skuDOList.sort(Comparator.comparing(SkuDO::getPrice));
        int index = skuDOList.size() / 2;
        int jndex = skuDOList.size() / 2 + 1;
        //遍历所有sku的价格，key=skuId， value= abs(price-avg)
        Map<String, BigDecimal> idMap = new HashMap<>();
        while (index >= 0 && jndex < skuDOList.size()) {
            BigDecimal abs1 = average.subtract(skuDOList.get(index).getPrice()).abs();
            BigDecimal abs2 = average.subtract(skuDOList.get(jndex).getPrice()).abs();
            idMap.put(String.valueOf(index), abs1);
            index--;
            idMap.put(String.valueOf(jndex), abs2);
            jndex++;
        }
        //对遍历map, value最小的对应key最接近中间值
        List<Map.Entry<String, BigDecimal>> mapList = new ArrayList<>(idMap.entrySet());
        mapList.sort(Comparator.comparing(Map.Entry::getValue));
        String id = mapList.get(0).getKey();
        log.info("最靠近中间价格的Sku ID【 " + id + " 】");
        return id;
    }


    private  Map<String, List<String>> getEachChannelInventory(List<SkuDO> skuDOList) {
        //key=channel code， value=<key=skuId, value=inventory>
        Map<String, Map<String, BigDecimal>> allChannelMap = new HashMap<>();
        for (SkuDO sku : skuDOList) {
            List<ChannelInventoryDO> inventoryList = sku.getInventoryList();
            String skuId = sku.getId();
            for (ChannelInventoryDO channel : inventoryList) {
                Map<String, BigDecimal> skuInventoryMap = new TreeMap<>();
                String channelCode = channel.getChannelCode();
                BigDecimal inventory = channel.getInventory();
                if (allChannelMap.containsKey(channelCode)) {
                    allChannelMap.get(channelCode).put(skuId, inventory);
                } else {
                    skuInventoryMap.put(skuId, inventory);
                    allChannelMap.put(channelCode, skuInventoryMap);
                }
            }
        }
        //最终结果：
        // key = channel code， value=list of skuIds
        Map<String, List<String>> result = new HashMap<>();
        for (Map.Entry<String, Map<String, BigDecimal>> entry : allChannelMap.entrySet()) {
            Map<String, BigDecimal> decimalMap = entry.getValue();
            List<Map.Entry<String, BigDecimal>> list = new ArrayList<>(decimalMap.entrySet());
            list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
            List<String> ids = new ArrayList<>();
            for (Map.Entry<String, BigDecimal> sortedMap : list) {
                ids.add(sortedMap.getKey());
            }
            result.put(entry.getKey(), ids);
        }
        //打印每个channel的前5名
        for (Map.Entry<String, List<String>> entry : result.entrySet()) {
            String channel = entry.getKey();
            List<String> idsList = entry.getValue();
            log.info(channel + idsList.subList(0, 5));
        }
        return result;
    }


    private BigDecimal calcPositionOfAllSkus(List<SkuDO> skuDOList) {
        //所有sku的总价值 = 每个sku的总价值之和 = 每个sku的价格 X 每个sku的总库存
        BigDecimal skuPosition = new BigDecimal("0.00");
        for (SkuDO sku : skuDOList) {
            //每个sku的库存
            BigDecimal inventoryOfEachSku = new BigDecimal("0.00");
            //每个sku的价格
            BigDecimal price = sku.getPrice().setScale(2, BigDecimal.ROUND_HALF_UP);
            List<ChannelInventoryDO> channelInventoryList = sku.getInventoryList();
            //每个sku的总库存
            inventoryOfEachSku = inventoryOfEachSku.add(channelInventoryList.stream().map(ChannelInventoryDO::getInventory).reduce(BigDecimal.ZERO, BigDecimal::add));
            //每个sku的总价值
            BigDecimal eachSkuPosition = price.multiply(inventoryOfEachSku).setScale(2, BigDecimal.ROUND_HALF_UP);
            skuPosition = skuPosition.add(eachSkuPosition).setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        log.info("所有sku的总价值: " + skuPosition);
        return skuPosition;
    }

    /**
     * 3:
     * 基于1, 在com.zzm.demos.service.ItemAggregationProviderConsumer中实现一个生产者消费者, 将sku列表聚合为商品, 并通过回调函数返回,
     * 聚合规则为：
     * 对于sku type为原始商品(ORIGIN)的, 按货号(artNo)聚合成ITEM
     * 对于sku type为数字化商品(DIGITAL)的, 按spuId聚合成ITEM
     * 聚合结果需要包含: item的最大价格、最小价格、sku列表及总库存
     */
    @Test
    void aggregationSkusWithConsumerProviderTest() {
        AtomicInteger count = new AtomicInteger();
        providerConsumer.execute(list -> {
            list.forEach(item -> {
                log.info("聚合后ITEM信息={}", JSON.toJSONString(item));
                count.incrementAndGet();
            });
            return list;
        });
        Assert.isTrue(count.get() == 7, "未能聚合商品列表");
    }
}
