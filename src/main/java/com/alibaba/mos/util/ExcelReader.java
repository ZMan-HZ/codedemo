package com.alibaba.mos.util;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONStreamAware;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import org.springframework.util.ResourceUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


/**
 * <b>Description</b>  ExcelReader
 *
 * @Author Zhenzhen
 * @Since 2020-07-19 周日 12:13
 * @Info ExcelReader
 */
public class ExcelReader {


    public ExcelReader() {
    }

    public void readExcel() {
        try {
            File data = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "data/skus.xls");
            // 设置读文件编码
            WorkbookSettings setEncode = new WorkbookSettings();
            setEncode.setEncoding("GB2312");
            // 从文件流中获取Excel工作区对象（WorkBook）
            Workbook wb = Workbook.getWorkbook(data, setEncode);
            // 从工作区中取得页（Sheet）,默认单独一页，第一页
            Sheet sheet = wb.getSheet(0);
            // 测试：循环打印Excel表中的内容
            JSONObject title = new JSONObject();
            for (int i = 0; i < sheet.getRows(); i++) {
                JSONObject jsonObject = new JSONObject();
                for (int j = 0; j < sheet.getColumns(); j++) {
                    Cell cell = sheet.getCell(j, i);
                    if (i == 0) {
                        title.put(String.valueOf(j), cell.getContents());
                    } else {
                        String key = String.valueOf(title.get(String.valueOf(j)));
                        jsonObject.put(key,cell.getContents());

                    }
                }
                System.out.print(jsonObject);
                System.out.println();
            }
        } catch (BiffException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void convertToCsv() {
        String buffer = "";
        try {
            File data = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "data/skus.xls");
            // 设置读文件编码
            WorkbookSettings setEncode = new WorkbookSettings();
            setEncode.setEncoding("GB2312");
            // 从文件流中获取Excel工作区对象（WorkBook）
            Workbook wb = Workbook.getWorkbook(data, setEncode);
            Sheet sheet = wb.getSheet(0);

            for (int i = 0; i < sheet.getRows(); i++) {
                for (int j = 0; j < 11; j++) {
                    Cell cell = sheet.getCell(j, i);
                    buffer += cell.getContents().replaceAll("\n", " ") + ",";
                }
                buffer = buffer.substring(0, buffer.lastIndexOf(",")).toString();
                buffer += "\n";
            }
        } catch (BiffException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //write the string into the file
        String savePath = "yourPath/datas.csv";
        File saveCSV = new File(savePath);
        try {
            if (!saveCSV.exists())
                saveCSV.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(saveCSV));
            writer.write(buffer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        ExcelReader reader = new ExcelReader();

        reader.readExcel();
//        reader.convertToCsv();
    }


}
