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



    private static List<String> cycles = Arrays.asList("CA","CFI","CFI2");

    private static DataFormatter dataFormatter = new DataFormatter();

    public String read(String path) throws IOException, InvalidFormatException {
        //private static final String XLSX_FILE_PATH = "C:\\Users\\pinzi\\Desktop\\TemplateConfiguration.xlsx";
        File f = new File(path);
        if(!f.exists()) {
            throw new RuntimeException("File "+path+" not found");
        }


        FileInputStream file = new FileInputStream(path);


        Workbook workbook = WorkbookFactory.create(file);

        HashMap<String,Target> prjToTarget = getTargets(workbook);
        List<Testset> testsets = getTestsets(workbook);
        System.out.println(testsets);

        List<String> AOs = testsets.stream()
                .filter(distinctByKey(Testset::getTag1))
                .map(Testset::getTag1)
                .collect(Collectors.toList());
        System.out.println(AOs);

        List<TestsetNode> testsetNodes = buildTestsetNodes(workbook, prjToTarget, testsets, AOs);
        TestsetConf testsetConf = buildeTestsetConf(testsetNodes);
        testsetConfRepository.save(testsetConf);
        System.out.println(testsetConf);

        List<DefectNode> defectNodes = buildDefectNodes(workbook, prjToTarget, testsets, AOs);
        DefectConf defectConf = buildDefectConf(defectNodes);
        defectConfRepository.save(defectConf);
        System.out.println(defectConf);

        List<ChartItem> chartitems = buildChartItem(workbook, testsetConf, defectConf);
        chartitems.stream().forEach(item -> chartItemRepository.save(item));

        saveModifiedWorkbook(workbook,path);

        return "Done!";
    }

    private List<TestsetNode> buildTestsetNodes(Workbook workbook, HashMap<String, Target> prjToTarget, List<Testset> testsets, List<String> AOs) {
        Sheet numTestsetSheet = workbook.getSheet("numtestset");
        HashMap<String, Integer> tagsToConfId = getTagsToConfId(numTestsetSheet);
        int rowIndexCounter = tagsToConfId.keySet().size();

        Integer indexCounter = 0;
        if(!tagsToConfId.values().isEmpty()) {
            indexCounter = Collections.max(tagsToConfId.values())+1;
        }

        List<TestsetNode> testsetNodes = new ArrayList<>();
        for(String AO : AOs) {
            Target target = getTargetPerAOFromTestset(prjToTarget, testsets, AO);
            for(String cycle : cycles) {
                String key = AO+":"+cycle;

                List<Integer> ids = testsets.stream()
                        .filter(t -> t.getTag1().equals(AO))
                        .filter(t -> t.getTag2().equals(cycle))
                        .map(Testset::getId)
                        .collect(Collectors.toList());
                if(!ids.isEmpty()) {
                    int indexTestNode;

                    if(tagsToConfId.get(key)!=null) {
                        indexTestNode = tagsToConfId.get(key);

                    } else {
                        indexTestNode = modifyRow(numTestsetSheet, rowIndexCounter, indexCounter, key);
                        indexCounter++;
                        rowIndexCounter++;
                    }
                    testsetNodes.add(new TestsetNode(indexTestNode,ids,Arrays.asList(AO,cycle),target));
                }
            }
        }
        return testsetNodes;
    }

    private List<DefectNode> buildDefectNodes(Workbook workbook, HashMap<String, Target> prjToTarget, List<Testset> testsets, List<String> AOs) {
        Sheet numDefectSheet = workbook.getSheet("numdefect");
        HashMap<String, Integer> tagsToConfId = getTagsToConfId(numDefectSheet);
        int rowIndexCounter = tagsToConfId.keySet().size();

        Integer indexCounter = 0;
        if(!tagsToConfId.values().isEmpty()) {
            indexCounter = Collections.max(tagsToConfId.values())+1;
        }

        List<DefectNode> defectNodes = new ArrayList<>();
        for(String AO : AOs) {
            Target target = getTargetPerAOFromTestset(prjToTarget, testsets, AO);
            String key = AO;
            int indexDefectNode;
            if(tagsToConfId.get(key)!=null) {
                indexDefectNode = tagsToConfId.get(key);
            } else {
                indexDefectNode = modifyRow(numDefectSheet, rowIndexCounter, indexCounter, key);
                indexCounter++;
                rowIndexCounter++;
            }
            defectNodes.add(new DefectNode(indexDefectNode,"\\\"user-template-02='"+AO+"'\\\"",Arrays.asList(AO),target));
        }
        return defectNodes;
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

    private TestsetConf buildeTestsetConf(List<TestsetNode> testsetNodes) {
        TestsetConf testsetConf = new TestsetConf();
        testsetConf.setTagNames(Arrays.asList("AO","Cycle"));
        testsetConf.setNodes(testsetNodes);
        return testsetConf;
    }
    private DefectConf buildDefectConf(List<DefectNode> defectNodes) {
        DefectConf defectConf = new DefectConf();
        defectConf.setTagNames(Arrays.asList("AO"));
        Filter filter = new Filter();
        filter.setField("user-01");
        filter.setValues(Arrays.asList("ANOMALIA DI COLLAUDO","REGRESSIONE DI COLLAUDO"));
        defectConf.setFilters(Arrays.asList(filter));
        defectConf.setNodes(defectNodes);
        return defectConf;
    }

    private void saveModifiedWorkbook(Workbook workbook,String path) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(new File(path));
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
    }

    private List<ChartItem> buildChartItem(Workbook workbook, TestsetConf testsetConf, DefectConf defectConf) {
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


    private ChartItem buildTestsetChartItem(Integer indexForChartItem, String AODescriptionCellValue, TestsetNode testSetNode,Chart.ChartType chartType) {
        String cycleFromTags = testSetNode.getTags().get(1);
        ChartItem chartItem = new ChartItem();
        chartItem.setDesc(AODescriptionCellValue + " " + cycleFromTags);
        chartItem.setIds(Arrays.asList(testSetNode.getIndex()));
        chartItem.setIsVisible(true);
        chartItem.setTags(testSetNode.getTags());
        chartItem.setChartType(chartType.value());
        chartItem.setEntityType(Entity.EntityType.TESTSET.value());
        chartItem.setConfId(indexForChartItem);
        chartItem.setUsernames(new ArrayList<>());
        return chartItem;
    }
    private ChartItem buildDefectChartItem(Integer indexForChartItem, String AODescriptionCellValue, DefectNode defectNode,Chart.ChartType chartType) {
        ChartItem chartItem = new ChartItem();
        chartItem.setDesc(AODescriptionCellValue + " defect");
        chartItem.setIds(Arrays.asList(defectNode.getIndex()));
        chartItem.setIsVisible(true);
        chartItem.setTags(defectNode.getTags());
        chartItem.setChartType(chartType.value());
        chartItem.setEntityType(Entity.EntityType.DEFECT.value());
        chartItem.setConfId(indexForChartItem);
        chartItem.setUsernames(new ArrayList<>());
        return chartItem;
    }

    private ChartItem buildTestsetPiechartItem(Integer indexForChartItem, String AODescriptionCellValue, TestsetNode testSetNode) {
        return buildTestsetChartItem(indexForChartItem, AODescriptionCellValue,testSetNode,Chart.ChartType.PIECHART);
    }
    private ChartItem buildTestsetLinechartItem(Integer indexForChartItem, String AODescriptionCellValue, TestsetNode testSetNode) {
        return buildTestsetChartItem(indexForChartItem, AODescriptionCellValue,testSetNode,Chart.ChartType.LINECHART);    }

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

    private List<Testset> getTestsets(Workbook workbook) {
        Sheet testsetSheet = workbook.getSheet("testset");


        List<Testset> testsets  = new ArrayList<>();
        for (Row row: testsetSheet) {
            if(row.getRowNum()>0) {
                Cell testsetIdCell = row.getCell(0);
                String testsetIdCellValue = dataFormatter.formatCellValue(testsetIdCell);
                Integer testsetId = Integer.valueOf(testsetIdCellValue);

                Cell testsetNameCell = row.getCell(1);
                String testsetNameCellValue = dataFormatter.formatCellValue(testsetNameCell);

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

    private HashMap<String, Target> getTargets(Workbook workbook) {
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

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}
