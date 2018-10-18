package com.dashboard.configurator;

import com.dashboard.commondashboard.*;
import com.dashboard.commondashboard.Chart;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ChartItemBuilder {

    private static DataFormatter dataFormatter = new DataFormatter();

    List<ChartItem> buildChartItem(Workbook workbook, TestsetConf testsetConf, DefectConf defectConf) {
        Sheet chartItemSheet = workbook.getSheet("chartitem");
        int indexForChartItem = 20;
        List<ChartItem> chartItems = new ArrayList<>();
        for (Row row: chartItemSheet) {
            if (row.getRowNum() > 0) {

                Cell AOCell = row.getCell(0);
                String AOCellValue = dataFormatter.formatCellValue(AOCell);

                Cell AODescriptionCell = row.getCell(1);
                String AODescriptionCellValue = dataFormatter.formatCellValue(AODescriptionCell);


                for(TestsetNode testSetNode : testsetConf.getNodes()) {
                    String AOFromTags = testSetNode.getTags().get(0);

                    if(AOCellValue.equals(AOFromTags)) {
                        ChartItem piechartItem = buildTestsetPiechartItem(indexForChartItem, AODescriptionCellValue, testSetNode);
                        System.out.println(piechartItem);
                        indexForChartItem++;
                        chartItems.add(piechartItem);

                        ChartItem linechartItem = buildTestsetLinechartItem(indexForChartItem, AODescriptionCellValue, testSetNode);
                        System.out.println(linechartItem);
                        indexForChartItem++;
                        chartItems.add(linechartItem);

                    }
                }
                for(DefectNode defectNode : defectConf.getNodes()) {
                    String AOFromTags = defectNode.getTags().get(0);

                    if(AOCellValue.equals(AOFromTags)) {
                        ChartItem piechartItem = buildDefectChartItem(indexForChartItem, AODescriptionCellValue, defectNode, Chart.ChartType.PIECHART);
                        System.out.println(piechartItem);
                        indexForChartItem++;
                        chartItems.add(piechartItem);

                    }
                }
            }
        }
        return chartItems;
    }
    private ChartItem buildDefectChartItem(Integer indexForChartItem, String AODescriptionCellValue, DefectNode defectNode,Chart.ChartType chartType) {
        ChartItem chartItem = new ChartItem();
        chartItem.setDesc(AODescriptionCellValue + " defect");
        chartItem.setIds(Arrays.asList(defectNode.getIndex()));
        chartItem.setIsVisible(true);
        chartItem.setTags(defectNode.getTags());
        chartItem.setChartType(chartType.value().toUpperCase());
        chartItem.setEntityType(Entity.EntityType.DEFECT.value().toUpperCase());
        chartItem.setConfId(indexForChartItem);
        chartItem.setUsernames(new ArrayList<>());
        return chartItem;
    }

    private ChartItem buildTestsetPiechartItem(Integer indexForChartItem, String AODescriptionCellValue, TestsetNode testSetNode) {
        return buildTestsetChartItem(indexForChartItem, AODescriptionCellValue,testSetNode,Chart.ChartType.PIECHART);
    }
    private ChartItem buildTestsetLinechartItem(Integer indexForChartItem, String AODescriptionCellValue, TestsetNode testSetNode) {
        return buildTestsetChartItem(indexForChartItem, AODescriptionCellValue,testSetNode,Chart.ChartType.LINECHART);
    }
    private ChartItem buildTestsetChartItem(Integer indexForChartItem, String AODescriptionCellValue, TestsetNode testSetNode,Chart.ChartType chartType) {
        String cycleFromTags = testSetNode.getTags().get(1);
        ChartItem chartItem = new ChartItem();
        chartItem.setDesc(AODescriptionCellValue + " " + cycleFromTags);
        chartItem.setIds(Arrays.asList(testSetNode.getIndex()));
        chartItem.setIsVisible(true);
        chartItem.setTags(testSetNode.getTags());
        chartItem.setChartType(chartType.value().toUpperCase());
        chartItem.setEntityType(Entity.EntityType.TESTSET.value().toUpperCase());
        chartItem.setConfId(indexForChartItem);
        chartItem.setUsernames(new ArrayList<>());
        return chartItem;
    }

}
