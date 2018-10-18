package com.dashboard.configurator;

import com.dashboard.commondashboard.DefectConf;
import com.dashboard.commondashboard.DefectNode;
import com.dashboard.commondashboard.Filter;
import com.dashboard.commondashboard.Target;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DefectConfBuilder {

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


}



