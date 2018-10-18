package com.dashboard.configurator;

import com.dashboard.commondashboard.*;
import com.dashboard.commondashboard.Chart;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class ConfiguratorController {

    @Autowired
    DefectConfRepository defectConfRepository;

    @Autowired
    TestsetConfRepository testsetConfRepository;

    @Autowired
    ChartItemRepository chartItemRepository;

    @Autowired
    AoCyclePrjSheetConfigurator aoCyclePrjSheetConfigurator;

    @Autowired
    TestSetSheetConfigurator testSetSheetConfigurator;

    @Autowired
    TargetSheetConfigurator targetSheetConfigurator;

    @Autowired
    ChartItemBuilder chartItemBuilder;




    private static List<String> cycles = Arrays.asList("CA","CFI","CFI2");

    private static DataFormatter dataFormatter = new DataFormatter();


    public String read(String path) throws IOException, InvalidFormatException {
        //private static final String XLSX_FILE_PATH = "C:\\Users\\pinzi\\Desktop\\TemplateConfiguration.xlsx";
        Workbook workbook = getWorkbook(path);

        HashMap<String,Target> prjToTarget = targetSheetConfigurator.getTargets(workbook);
        List<Testset> testsets = testSetSheetConfigurator.getTestsets(workbook);
        System.out.println(testsets);

        aoCyclePrjSheetConfigurator.computeAoCyclePrj(workbook, testsets);

        List<String> AOs = testsets.stream()
                .filter(distinctByKey(Testset::getTag1))
                .map(Testset::getTag1)
                .map(ao -> ao.toUpperCase())
                .collect(Collectors.toList());
        System.out.println(AOs);




        List<TestsetNode> testsetNodes = buildTestsetNodes(workbook, prjToTarget, testsets, AOs);
        TestsetConf testsetConf = buildeTestsetConf(testsetNodes);
        System.out.println(testsetConf);

        List<DefectNode> defectNodes = buildDefectNodes(workbook, prjToTarget, testsets, AOs);
        DefectConf defectConf = buildDefectConf(defectNodes);
        System.out.println(defectConf);

        List<ChartItem> chartitems = chartItemBuilder.buildChartItem(workbook, testsetConf, defectConf);

        //testsetConfRepository.save(testsetConf);
        //defectConfRepository.save(defectConf);
        //chartitems.stream().forEach(item -> chartItemRepository.save(item));
        //saveModifiedWorkbook(workbook,path);

        return "Done!";
    }



    private Workbook getWorkbook(String path) throws IOException, InvalidFormatException {
        File f = new File(path);
        if(!f.exists()) {
            throw new RuntimeException("File "+path+" not found");
        }
        FileInputStream file = new FileInputStream(path);
        return WorkbookFactory.create(file);
    }




    private int modifyRow(Sheet numSheet, int rowIndexCounter, Integer indexCounter, String key) {
        int indexTestNode;Row row = row = numSheet.createRow(rowIndexCounter);
        Cell tagsCell = row.createCell(0);
        Cell indexCell = row.createCell(1);

        tagsCell.setCellType(CellType.STRING);
        tagsCell.setCellValue(key);
        indexCell.setCellType(CellType.STRING);
        indexCell.setCellValue(Integer.valueOf(indexCounter).toString());

        indexTestNode = indexCounter;
        return indexTestNode;
    }




    private void saveModifiedWorkbook(Workbook workbook,String path) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(new File(path));
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
    }



    private Target getTargetPerAOFromTestset(HashMap<String, Target> prjToTarget, List<Testset> testsets, String AO) {
        String prj = testsets.stream()
                .filter(t -> t.getTag1().equals(AO))
                .map(Testset::getPrj)
                .findFirst().orElseThrow(() -> new RuntimeException("No Prj Found"));

        Target target = prjToTarget.get(prj);
        if(target==null) {
            throw new RuntimeException("No project found in target sheet");
        }
        return target;
    }




    private HashMap<String, Integer> getTagsToConfId(Sheet numSheet) {
        HashMap<String,Integer> tagsToConfId = new HashMap<>();
        for (Row row: numSheet) {
            Cell tagsCell = row.getCell(0);
            String tagsCellValue = dataFormatter.formatCellValue(tagsCell);
            if(StringUtils.isEmpty(tagsCellValue)) {
                break;
            }
            Cell confIdCell = row.getCell(1);
            String confIdCellValue = dataFormatter.formatCellValue(confIdCell);
            tagsToConfId.put(tagsCellValue, Integer.valueOf(confIdCellValue));
        }
        return tagsToConfId;
    }


    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}
