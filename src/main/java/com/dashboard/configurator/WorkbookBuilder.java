package com.dashboard.configurator;

import lombok.Getter;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@Component
public class WorkbookBuilder {

    @Getter Workbook workbook;
    String usedPath;

    WorkbookBuilder(AppProperties appProperties)  throws IOException, InvalidFormatException{
        String path = System.getProperty("user.dir") + "\\TemplateConfiguration.xlsx";
        File f = new File(path);
        if(f.exists()) {
            usedPath = path;
            FileInputStream file = new FileInputStream(path);
            workbook =  WorkbookFactory.create(file);
        } else {
            path = appProperties.getFilepath();
            f = new File(path);
            if(f.exists()) {
                usedPath = path;
                FileInputStream file = new FileInputStream(path);
                workbook =  WorkbookFactory.create(file);
            } else {
                throw new RuntimeException("No file found!!");
            }
        }
    }

    public void saveModifiedWorkbook() throws IOException {
        FileOutputStream fileOut = new FileOutputStream(new File(usedPath));
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
    }


}
