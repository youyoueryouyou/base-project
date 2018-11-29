package com.you.boot.cloud.config.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author shicz
 */
public class ServerListener implements ServletContextListener {
    Logger logger = LoggerFactory.getLogger(ServerListener.class);
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.debug("contextInitialized...");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
