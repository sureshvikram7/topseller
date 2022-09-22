package org.training.util;

import org.training.service.TopSellableService;

import javax.annotation.Resource;
import java.util.List;

public class TopSellableUtil {
@Resource
 private   TopSellableService topSellableService;

public  static List<String> topSellableProductList;

public void getTopSellableProducts(){
    topSellableProductList=  topSellableService.getTopSellableProducts();
}

}
