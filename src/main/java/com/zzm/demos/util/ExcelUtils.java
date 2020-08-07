package com.zzm.demos.util;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.Cell;

/**
 * <b>Description</b>  ExcelUtils
 *
 * @Author Zhenzhen
 * @Info ExcelUtils
 */
public class ExcelUtils {

    //全部当字符串处理
    public static String getCellValueByCell(Cell cell) throws Exception {
        if (cell == null || "".equals(cell.toString().trim())) {
            return "";
        }
        String value;
        int cellType = cell.getCellType();
        cell.setCellType(HSSFCell.CELL_TYPE_STRING);
        if (HSSFCell.CELL_TYPE_NUMERIC == cellType || HSSFCell.CELL_TYPE_STRING == cellType) {
            value = cell.getStringCellValue();
        } else {
            throw new Exception("Out of Type Scope！");
        }
        return value;
    }


}
