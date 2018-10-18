package com.dashboard.configurator;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class AoCyclePrjSheetConfigurator {


    void computeAoCyclePrj(Workbook workbook, List<Testset> testsets) {
        Set<Testset> uniqueTestsetsNoId = new LinkedHashSet<>(testsets);
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
}
