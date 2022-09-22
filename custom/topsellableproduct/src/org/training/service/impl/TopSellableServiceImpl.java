package org.training.service.impl;

import org.training.dao.TopSellableDao;
import org.training.service.TopSellableService;

import javax.annotation.Resource;
import java.util.List;

public class TopSellableServiceImpl implements TopSellableService {
    @Resource
    private TopSellableDao topSellableDao;

    @Override
    public List<String> getTopSellableProducts() {
       return topSellableDao.getTopSellableProducts();
    }
}
