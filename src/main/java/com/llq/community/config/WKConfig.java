package com.llq.community.config;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.logging.Logger;

/**
 * @author llq
 * @create 2021-09-20  14:20
 */
@Configuration
public class WKConfig {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(WKConfig.class);
    @Value("d:/java_development_tool/wkhtmltopdf/data/wk-image")
    private String wkImageStorage;
     //项目启动之后这个就会执行
    @PostConstruct
    public void init() {

        File file = new File(wkImageStorage);

        if (!file.exists()) {
            file.mkdir();
            logger.info("创建wk图片目录" + wkImageStorage);
        }
    }

}
