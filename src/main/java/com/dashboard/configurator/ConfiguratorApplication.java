package com.dashboard.configurator;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.io.IOException;

@SpringBootApplication
@EnableMongoRepositories(basePackages="com.dashboard")
public class ConfiguratorApplication implements CommandLineRunner {

    @Autowired
    ConfiguratorService configuratorService;



    private static Logger LOG = LoggerFactory.getLogger(ConfiguratorApplication.class);

	public static void main(String[] args) {
        LOG.info("STARTING THE APPLICATION");
	    SpringApplication.run(ConfiguratorApplication.class, args);
	}

    @Override
    public void run(String... args) throws IOException, InvalidFormatException {
        LOG.info("EXECUTING : command line runner");

        String result = configuratorService.read();
        //String result = configuratorController.readManual(path);
        LOG.info(result);
    }
}
