package com.dashboard.configurator;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class TestSetSheetConfigurator {

    private static List<String> cycles = Arrays.asList("CA","CFI","CFI2");

    private static DataFormatter dataFormatter = new DataFormatter();


    List<Testset> getTestsets(Workbook workbook) {
        Sheet testsetSheet = workbook.getSheet("testset");


        List<Testset> testsets  = new ArrayList<>();
        for (Row row: testsetSheet) {
            if(row.getRowNum()>0) {
                Cell testsetIdCell = row.getCell(0);
                String testsetIdCellValue = dataFormatter.formatCellValue(testsetIdCell);
                Integer testsetId = Integer.valueOf(testsetIdCellValue);

                Cell testsetNameCell = row.getCell(1);
                String testsetNameCellValue = dataFormatter.formatCellValue(testsetNameCell).toUpperCase();

                Cell prjCell = row.getCell(2);
                String prjCellValue = dataFormatter.formatCellValue(prjCell);

                String AO = "";
                String usedCycle = "";
                for(String cycle : cycles) {
                    int indexCycle = testsetNameCellValue.indexOf("_" + cycle + "_");
                    if(indexCycle>-1) {
                        AO = testsetNameCellValue.substring(0,indexCycle);
                        usedCycle = cycle;
                        break;
                    }
                }
                if(StringUtils.isNotEmpty(AO) && StringUtils.isNotEmpty(usedCycle)) {
                    testsets.add(new Testset(testsetId,AO,usedCycle,prjCellValue));
                } else {
                    System.out.println(testsetNameCellValue);
                }

            }
        }
        return testsets;
    }
}
