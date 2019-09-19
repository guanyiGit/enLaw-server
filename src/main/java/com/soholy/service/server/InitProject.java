package com.soholy.service.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class InitProject implements ApplicationRunner {
    private static final Logger LOG = LoggerFactory.getLogger(InitProject.class);


    @Autowired
    private EnLawServer enLawServer;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LOG.info("==========init project===========");
        enLawServer.start();
    }
}
