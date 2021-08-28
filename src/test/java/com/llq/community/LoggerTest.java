package com.llq.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author llq
 * @create 2021-08-28  15:46
 */
@RunWith(SpringRunner.class) //如果爆红可能是因为么有引入junit
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class LoggerTest {
    //获取日志对象是通过相应的工厂来获取的，传入的类可以就算该日志对象的名字，可以用来加以区分日志是为哪个类服务的
    private static final Logger logger = LoggerFactory.getLogger(LoggerTest.class);

    @Test
    public void testLogger(){
        System.out.println(logger);
        logger.debug("debug logger");
        logger.info("info logger");
        logger.warn("warn log");
        logger.error("error log");
    }
}
