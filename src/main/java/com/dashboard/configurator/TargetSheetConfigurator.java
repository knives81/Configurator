package com.dashboard.configurator;

import com.dashboard.commondashboard.Target;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class TargetSheetConfigurator {



    private static DataFormatter dataFormatter = new DataFormatter();

    HashMap<String, Target> getTargets(Workbook workbook) {
        HashMap<String, Target> prjToTarget = new HashMap<>();

        Sheet targetSheet = workbook.getSheet("target");
        for(Row row : targetSheet){
            if(row.getRowNum()>0) {
                Cell prjCell = row.getCell(0);
                String prjCellValue = dataFormatter.formatCellValue(prjCell);
                Cell targetDomainCell = row.getCell(1);
                String targetDomainCellValue = dataFormatter.formatCellValue(targetDomainCell);
                Cell targetPrjCell = row.getCell(2);
                String targetPrjCellValue = dataFormatter.formatCellValue(targetPrjCell);

                prjToTarget.put(prjCellValue,new Target(targetDomainCellValue,targetPrjCellValue));
            }
        }
        System.out.println(prjToTarget);
        return prjToTarget;
}
}

