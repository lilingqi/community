package com.llq.community.dao.impl;

import com.llq.community.dao.TestDao;
import org.springframework.stereotype.Repository;

/**
 * @author llq
 * @create 2021-08-26  15:52
 */
@Repository
public class TestDaoImpl implements TestDao{
    @Override
    public String find() {
        return "hello llq";
    }
}
