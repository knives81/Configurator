package com.dashboard.configurator;

import com.dashboard.commondashboard.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
public class ConfiguratorService {

    @Autowired
    ManualSheetConfigurator manualSheetConfigurator;

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

    @Autowired
    DefectConfBuilder defectConfBuilder;

    @Autowired
    TestsetConfBuilder testsetConfBuilder;

    @Autowired
    WorkbookBuilder workbookBuilder;

    @Autowired
    PianificationSheetConfigurator pianificationSheetConfigurator;

    @Autowired
    PianificationRepository pianificationRepository;

    @Autowired
    UserSheetConfigurator userSheetConfigurator;

    boolean writeOnDbAndXlsInit = false;
    boolean writeOnDbPianification = false;
    boolean writeOnDbManual = false;
    boolean writeOnDbUser = true;

    public String read() throws IOException, InvalidFormatException {
        //private static final String XLSX_FILE_PATH = "C:\\Users\\pinzi\\Desktop\\TemplateConfiguration.xlsx";



        HashMap<String,Target> prjToTarget = targetSheetConfigurator.getTargets();
        List<Testset> testsets = testSetSheetConfigurator.getTestsets();

        TestsetHelper testsetHelper = new TestsetHelper(testsets,prjToTarget);

        aoCyclePrjSheetConfigurator.computeAoCyclePrj(testsetHelper);

        TestsetConf testsetConf = testsetConfBuilder.build(testsetHelper);
        System.out.println(testsetConf);
        DefectConf defectConf = defectConfBuilder.build(testsetHelper);
        System.out.println(defectConf);
        List<ChartItem> chartitems = chartItemBuilder.build(testsetConf, defectConf);

        if(writeOnDbAndXlsInit) {
            testsetConfRepository.save(testsetConf);
            defectConfRepository.save(defectConf);
            chartitems.stream().forEach(item -> chartItemRepository.save(item));
            workbookBuilder.saveModifiedWorkbook();

        }
        if(writeOnDbPianification) {
            List<Pianification> pianifications = pianificationSheetConfigurator.getPianifications();
            pianifications.stream().forEach(item -> pianificationRepository.save(item));
        }
        if(writeOnDbManual) {
            chartitems = manualSheetConfigurator.getChartItems();
            chartitems.stream().forEach(item -> chartItemRepository.save(item));
        }
        if(writeOnDbUser) {
            chartitems = userSheetConfigurator.getChartItemsWithUser();
            //chartitems.stream().forEach(item -> chartItemRepository.save(item));
        }
        return "Done!";
    }
















}
