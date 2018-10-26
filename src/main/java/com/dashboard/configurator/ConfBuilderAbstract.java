package com.dashboard.configurator;

import com.dashboard.commondashboard.Target;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;

import java.util.*;

public abstract class ConfBuilderAbstract {


    protected static List<String> cycles = Arrays.asList("CA","CFI","CFI2");
    protected  Sheet numSheet;

    protected static DataFormatter dataFormatter = new DataFormatter();


    void modifyRow(int rowIndexCounter, Integer confIdCounter, String key) {
        Row row = row = numSheet.createRow(rowIndexCounter);
        Cell tagsCell = row.createCell(0);
        Cell indexCell = row.createCell(1);

        tagsCell.setCellType(CellType.STRING);
        tagsCell.setCellValue(key);
        indexCell.setCellType(CellType.STRING);
        indexCell.setCellValue(Integer.valueOf(confIdCounter).toString());
    }


    Integer getRowIndexCounter(){
        return getKeyToConfId().keySet().size();
    }

    Integer getConfIdCounter(){
        Integer confIdCounter = 0;
        if(!getKeyToConfId().values().isEmpty()) {
            confIdCounter = Collections.max(getKeyToConfId().values()) + 1;
        }
        return confIdCounter;
    }

    HashMap<String, Integer> getKeyToConfId() {
        HashMap<String,Integer> keyToConfId = new HashMap<>();
        for (Row row: numSheet) {
            Cell keyCell = row.getCell(0);
            String keyCellValue = dataFormatter.formatCellValue(keyCell);
            if(StringUtils.isEmpty(keyCellValue)) {
                break;
            }
            Cell confIdCell = row.getCell(1);
            String confIdCellValue = dataFormatter.formatCellValue(confIdCell);
            keyToConfId.put(keyCellValue, Integer.valueOf(confIdCellValue));
        }
        return keyToConfId;
    }




}
