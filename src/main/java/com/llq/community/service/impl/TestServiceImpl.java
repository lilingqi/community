package com.llq.community.service.impl;

import com.llq.community.dao.TestDao;
import com.llq.community.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author llq
 * @create 2021-08-26  15:54
 */
@Service
public class TestServiceImpl implements TestService {
    @Autowired
    private TestDao testDao;
    @Override
    public String find() {
        return testDao.find();
    }
}
