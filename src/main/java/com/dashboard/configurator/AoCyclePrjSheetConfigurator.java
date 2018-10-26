package com.dashboard.configurator;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class AoCyclePrjSheetConfigurator {

    private static DataFormatter dataFormatter = new DataFormatter();

    private Workbook workbook;
    AoCyclePrjSheetConfigurator(WorkbookBuilder workbookBuilder) {
        workbook = workbookBuilder.getWorkbook();
    }

    void computeAoCyclePrj(TestsetHelper testsetHelper) {
        Set<Testset> uniqueTestsetsNoId = testsetHelper.getUniqueTestsetsNoId();
        System.out.println(uniqueTestsetsNoId);
        Sheet aocycleprjSheet = workbook.getSheet("aocycleprj");
        int aocycleprjSheetCounter=0;
        for(Testset testset: uniqueTestsetsNoId) {
            Row row = row = aocycleprjSheet.createRow(aocycleprjSheetCounter);
            Cell aoCell = row.createCell(0);
            aoCell.setCellType(CellType.STRING);
            aoCell.setCellValue(testset.getTag1());

            Cell cycleCell = row.createCell(1);
            cycleCell.setCellType(CellType.STRING);
            cycleCell.setCellValue(testset.getTag2());

            Cell prjCell = row.createCell(2);
            prjCell.setCellType(CellType.STRING);
            prjCell.setCellValue(testset.getPrj());

            aocycleprjSheetCounter++;
        }
    }
    public HashMap<String, String> getAo2Project() {
        Sheet aocycleprjSheet = workbook.getSheet("aocycleprj");
        HashMap<String,String> ao2project = new HashMap<>();
        for (Row row: aocycleprjSheet) {
            if (row.getRowNum() > 0) {

                Cell AOCell = row.getCell(0);
                String AOCellValue = dataFormatter.formatCellValue(AOCell);

                Cell projectCell = row.getCell(2);
                String projectCellValue = dataFormatter.formatCellValue(projectCell);

                if(projectCellValue!=null && projectCellValue!="") {
                    ao2project.put(AOCellValue,projectCellValue);
                }
            }
        }
        System.out.println("ao2project"+ao2project);
        return ao2project;
    }
}
