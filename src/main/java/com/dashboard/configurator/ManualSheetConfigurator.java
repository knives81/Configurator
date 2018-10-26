package com.dashboard.configurator;


import com.dashboard.commondashboard.ChartItem;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ManualSheetConfigurator {

    private static DataFormatter dataFormatter = new DataFormatter();

    private Workbook workbook;
    ManualSheetConfigurator(WorkbookBuilder workbookBuilder) {
        workbook = workbookBuilder.getWorkbook();
    }

    List<ChartItem> getChartItems() {
        List<ChartItem> chartItems = new ArrayList<>();
        Sheet manualSheet = workbook.getSheet("manual");
        for (Row row : manualSheet) {
            if (row.getRowNum() > 0) {

                ChartItem chartItem = new ChartItem();

                String confIdCellValue = dataFormatter.formatCellValue(row.getCell(0));
                chartItem.setConfId(Integer.valueOf(confIdCellValue));

                String idsCellCellValue = dataFormatter.formatCellValue(row.getCell(1));
                chartItem.setIds(Arrays.asList(idsCellCellValue.split(",")).stream().map(Integer::valueOf).collect(Collectors.toList()));

                String desc = dataFormatter.formatCellValue(row.getCell(2));
                chartItem.setDesc(desc);

                String tagsCellValue = dataFormatter.formatCellValue(row.getCell(3));
                chartItem.setTags(Arrays.asList(tagsCellValue.split(",")));

                String chartType = dataFormatter.formatCellValue(row.getCell(4));
                chartItem.setChartType(chartType);

                String entityType = dataFormatter.formatCellValue(row.getCell(5));
                chartItem.setEntityType(entityType);

                String isVisible = dataFormatter.formatCellValue(row.getCell(6));
                chartItem.setIsVisible(Boolean.valueOf(isVisible));

                String useranamesCellValue = dataFormatter.formatCellValue(row.getCell(7));
                chartItem.setUsernames(Arrays.asList(useranamesCellValue.split(",")));

                chartItems.add(chartItem);


            }

        }
        return chartItems;
    }
}





