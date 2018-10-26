package com.dashboard.configurator;

import com.dashboard.commondashboard.DefectConf;
import com.dashboard.commondashboard.DefectNode;
import com.dashboard.commondashboard.Filter;
import com.dashboard.commondashboard.Target;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DefectConfBuilder extends ConfBuilderAbstract{

    private Workbook workbook;
    DefectConfBuilder(WorkbookBuilder workbookBuilder) {
        workbook = workbookBuilder.getWorkbook();
        numSheet = workbook.getSheet("numdefect");
    }

    private List<DefectNode> buildDefectNodes(TestsetHelper testsetHelper) {

        List<String> AOs = testsetHelper.getAOs();
        HashMap<String,Integer> keyToConfId = getKeyToConfId();
        Integer rowIndexCounter = getRowIndexCounter();
        Integer confIdCounter = getConfIdCounter();

        List<DefectNode> defectNodes = new ArrayList<>();
        for(String AO : AOs) {
            Target target = testsetHelper.getTargetPerAOFromTestset(AO);
            String key = AO;
            int indexDefectNode;

            if(keyToConfId.keySet().contains(key)) {
                indexDefectNode = keyToConfId.get(key);
            } else {
                modifyRow(rowIndexCounter, confIdCounter, key);
                indexDefectNode = confIdCounter;
                confIdCounter++;
                rowIndexCounter++;
            }

            defectNodes.add(new DefectNode(indexDefectNode,"\\\"user-template-02='"+AO+"'\\\"",Arrays.asList(AO),target));
        }
        return defectNodes;
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

    public DefectConf build(TestsetHelper testsetHelper){
        List<DefectNode> defectNodes = buildDefectNodes(testsetHelper);
        return buildDefectConf(defectNodes);
    }


}



