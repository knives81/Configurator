package com.dashboard.configurator;

import com.dashboard.commondashboard.ChartItem;
import com.dashboard.commondashboard.ChartItemRepository;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Component
public class UserSheetConfigurator {

    @Autowired
    ChartItemRepository chartItemRepository;

    @Autowired
    AoCyclePrjSheetConfigurator aoCyclePrjSheetConfigurator;

    private static DataFormatter dataFormatter = new DataFormatter();

    private Workbook workbook;
    UserSheetConfigurator(WorkbookBuilder workbookBuilder) {
        workbook = workbookBuilder.getWorkbook();
    }

    public List<ChartItem> getChartItemsWithUser()  {
        List<ChartItem> chartItemsWithUser = new ArrayList<>();

        HashMap<String, String> ao2project = aoCyclePrjSheetConfigurator.getAo2Project();
        HashMap<String, HashSet<String>> projectOrAo2users = getProjectOrAo2Users();

        List<ChartItem> chartItemsFromDb = chartItemRepository.findAll();
        for(ChartItem chartItem : chartItemsFromDb) {
            String aoFromDb = chartItem.getTags().get(0);
            String projectFromXls = ao2project.get(aoFromDb);

            if(projectFromXls!=null && projectFromXls!="") {
                HashSet<String> usersBasedOnProject = projectOrAo2users.get(projectFromXls);
                System.out.println("usersBasedOnProject="+projectFromXls+":"+usersBasedOnProject);
                HashSet<String> usersBasedOnAo = projectOrAo2users.get(aoFromDb);
                System.out.println("usersBasedOnAo="+aoFromDb+":"+usersBasedOnAo);
                HashSet<String> usersInDb = new HashSet<>(chartItem.getUsernames());
                System.out.println("usersInDb:"+usersInDb);
                if(usersBasedOnProject!=null) {
                    usersInDb.addAll(usersBasedOnProject);
                }
                if(usersBasedOnAo!=null) {
                    usersInDb.addAll(usersBasedOnAo);
                }

                ArrayList<String> useranameForChartItem = new ArrayList<>(usersInDb);
                System.out.println("useranameForChartItem:"+useranameForChartItem);
                chartItem.setUsernames(useranameForChartItem);

                chartItemsWithUser.add(chartItem);
            }
        }

        return chartItemsWithUser;
    }



    private HashMap<String, HashSet<String>> getProjectOrAo2Users() {
        Sheet userSheet = workbook.getSheet("user");
        HashMap<String,HashSet<String>> projectOrAo2Users = new HashMap<>();
        for (Row row: userSheet) {
            if (row.getRowNum() > 0) {

                Cell usernameCell = row.getCell(1);
                String usernameCellValue = dataFormatter.formatCellValue(usernameCell);

                Cell projectOrAoCell = row.getCell(2);
                String projectOrAoCellValue = dataFormatter.formatCellValue(projectOrAoCell);

                if(projectOrAoCellValue!=null && projectOrAoCellValue!="") {
                    HashSet<String> usernames = projectOrAo2Users.get(projectOrAoCellValue);
                    if(usernames==null) {
                        usernames = new HashSet<>();
                    }
                    usernames.add(usernameCellValue);
                    projectOrAo2Users.put(projectOrAoCellValue,usernames);
                }
            }
        }
        System.out.println("projectOrAo2Users"+projectOrAo2Users);
        return projectOrAo2Users;
    }
}
