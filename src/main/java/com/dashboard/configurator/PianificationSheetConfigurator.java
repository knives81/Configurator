package com.dashboard.configurator;

import com.dashboard.commondashboard.Pianification;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class PianificationSheetConfigurator {


    private static DataFormatter dataFormatter = new DataFormatter();
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    private Workbook workbook;
    PianificationSheetConfigurator(WorkbookBuilder workbookBuilder) {
        workbook = workbookBuilder.getWorkbook();
    }

    List<Pianification> getPianifications() {


        Sheet pianificationSheet = workbook.getSheet("pianification");

        List<Pianification> pianifications = new ArrayList<>();

        List<Date> daysInPianification = new ArrayList<>();
        HashMap<Integer,List<Integer>> confId2numsOfTest = new HashMap<>();




        for (Row row: pianificationSheet) {
            if(row.getRowNum()==0) {
                for (int columnCounter = 1; columnCounter < 40; columnCounter++) {
                    Cell dateCell = row.getCell(columnCounter);
                    Date dateCellValue = dateCell.getDateCellValue();
                    daysInPianification.add(dateCellValue);
                }
            } else {
                List<Integer> numsOfTest = new ArrayList<>();

                Cell confIdCell = row.getCell(0);
                String confIdCellValue = dataFormatter.formatCellValue(confIdCell);

                for(int columnCounter=1;columnCounter<40;columnCounter++) {
                    Cell numOfTestCell = row.getCell(columnCounter);
                    String numOfTestCellValue = dataFormatter.formatCellValue(numOfTestCell);
                    if(StringUtils.isEmpty(numOfTestCellValue)){
                        break;
                    }
                    numsOfTest.add(Integer.valueOf(numOfTestCellValue));
                }
                confId2numsOfTest.put(Integer.valueOf(confIdCellValue),numsOfTest);
            }
        }

        for (Map.Entry<Integer, List<Integer>> entry : confId2numsOfTest.entrySet()) {
            List<Pianification.TestPerDay> pianificationTestPerDays = new ArrayList<>();
            Integer confId = entry.getKey();
            List<Integer> numsOfTest = entry.getValue();
            for(int numOfTestCounter=0; numOfTestCounter<numsOfTest.size();numOfTestCounter++){
                Integer numOfTest = numsOfTest.get(numOfTestCounter);
                String day = SDF.format(daysInPianification.get(numOfTestCounter));
                Pianification.TestPerDay testPerDay = new Pianification.TestPerDay(day,numOfTest);
                pianificationTestPerDays.add(testPerDay);
            }
            Pianification pianification = new Pianification(confId,pianificationTestPerDays);
            pianifications.add(pianification);
        }
        System.out.println(pianifications);
        return pianifications;
    }
}
