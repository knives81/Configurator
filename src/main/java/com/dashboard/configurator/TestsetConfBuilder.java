package com.dashboard.configurator;

import com.dashboard.commondashboard.Target;
import com.dashboard.commondashboard.TestsetConf;
import com.dashboard.commondashboard.TestsetNode;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class TestsetConfBuilder {

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

    private TestsetConf buildeTestsetConf(List<TestsetNode> testsetNodes) {
        TestsetConf testsetConf = new TestsetConf();
        testsetConf.setTagNames(Arrays.asList("AO","Cycle"));
        testsetConf.setNodes(testsetNodes);
        return testsetConf;
    }
}
