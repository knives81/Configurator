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
public class TestsetConfBuilder extends ConfBuilderAbstract {

    private Workbook workbook;


    TestsetConfBuilder(WorkbookBuilder workbookBuilder) {
        workbook = workbookBuilder.getWorkbook();
        numSheet = workbook.getSheet("numtestset");
    }

    private List<TestsetNode> buildTestsetNodes(TestsetHelper testsetHelper) {
        List<String> AOs = testsetHelper.getAOs();
        HashMap<String,Integer> keyToConfId = getKeyToConfId();
        Integer rowIndexCounter = getRowIndexCounter();
        Integer confIdCounter = getConfIdCounter();

        List<TestsetNode> testsetNodes = new ArrayList<>();
        for(String AO : AOs) {
            Target target = testsetHelper.getTargetPerAOFromTestset(AO);
            for(String cycle : cycles) {
                String key = AO+":"+cycle;

                List<Integer> ids = testsetHelper.getIds(AO,cycle);
                if(!ids.isEmpty()) {
                    int indexTestNode;
                    if(keyToConfId.keySet().contains(key)) {
                        indexTestNode = keyToConfId.get(key);
                    } else {
                        modifyRow(rowIndexCounter, confIdCounter, key);
                        indexTestNode = confIdCounter;
                        confIdCounter++;
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

    protected TestsetConf build(TestsetHelper testsetHelper) {
        List<TestsetNode> testsetNodes = buildTestsetNodes(testsetHelper);
        return buildeTestsetConf(testsetNodes);
    }
}
