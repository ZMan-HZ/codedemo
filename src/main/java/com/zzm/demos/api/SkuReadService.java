/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.zzm.demos.api;

import com.zzm.demos.data.SkuDO;

/**
 * @version $Id: SkuReadService.java, v 0.1 2019年10月28日 10:45 AM superchao Exp $
 */
public interface SkuReadService {

    /**
     * 通过处理器加载sku并处理
     *
     * @param handler
     */
    void loadSkus(SkuHandler handler);

    interface SkuHandler {

        /**
         * 处理读取的sku信息
         *
         * @param skuDO
         * @return
         */
        SkuDO handleSku(SkuDO skuDO);
    }
}
