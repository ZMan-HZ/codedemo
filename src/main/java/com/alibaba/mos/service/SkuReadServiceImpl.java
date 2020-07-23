/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.alibaba.mos.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.mos.api.SkuReadService;
import com.alibaba.mos.data.ChannelInventoryDO;
import com.alibaba.mos.data.SkuDO;
import com.alibaba.mos.util.ExcelUtils;
import jxl.WorkbookSettings;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * TODO: 实现
 *
 * @author superchao
 * @version $Id: SkuReadServiceImpl.java, v 0.1 2019年10月28日 10:49 AM superchao Exp $
 */
@Service
public class SkuReadServiceImpl implements SkuReadService {


    /**
     * 这里假设excel数据量很大无法一次性加载到内存中
     *
     * @param handler
     */
    @Override
    public void loadSkus(SkuHandler handler) {
        HSSFWorkbook workbook = null;
        HSSFSheet sheet;
        BufferedInputStream in = null;
//        SXSSFSheet sxssfSheet;
//        SXSSFWorkbook sxssfWorkbook = null;
//        XSSFWorkbook xssfWorkbook = null;  //parser xlsx
//        XSSFSheet xssfSheet;

        try {
            File data = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "data/skus.xls");
            in = new BufferedInputStream(new FileInputStream(data), 1024);
//            workbook = (HSSFWorkbook) WorkbookFactory.create(data);
            workbook = new HSSFWorkbook(in);
            sheet = workbook.getSheetAt(0);
            HSSFRow titleRow = sheet.getRow(0);
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                JSONObject jsonObject = new JSONObject();
                HSSFRow row = sheet.getRow(rowIndex);
                for (int cellIndex = 0; cellIndex < row.getLastCellNum(); cellIndex++) {
                    HSSFCell cell = row.getCell(cellIndex);
                    jsonObject.put(ExcelUtils.getCellValueByCell(titleRow.getCell(cellIndex)), ExcelUtils.getCellValueByCell(cell));
                }
                SkuDO skuDO = JSONObject.toJavaObject(jsonObject, SkuDO.class);
                String jsonArray = String.valueOf(jsonObject.get("inventorys"));
                List<ChannelInventoryDO> list = JSONArray.parseArray(jsonArray, ChannelInventoryDO.class);
                skuDO.setInventoryList(list);
                handler.handleSku(skuDO);
//                workbook.write(new FileOutputStream("/Users/Zhenzhen/Code/data.xls"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (workbook != null) {
                    workbook.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
