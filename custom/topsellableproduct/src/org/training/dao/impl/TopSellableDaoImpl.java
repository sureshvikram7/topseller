package org.training.dao.impl;

import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

import org.training.dao.TopSellableDao;

import javax.annotation.Resource;
import java.util.List;

public class TopSellableDaoImpl implements TopSellableDao {

    @Resource
    private FlexibleSearchService flexibleSearchService;

    private static final String QUERY_FIND_TOP100_SELLABLE_PRODUCTS="select TOP 10 {"+OrderEntryModel.PRODUCT+"} from {"+OrderEntryModel._TYPECODE+"} group by {"+OrderEntryModel.PRODUCT+"} order by sum({"+OrderEntryModel.PRODUCT+"}) desc";

    public List<String> getTopSellableProducts() {
        final FlexibleSearchQuery query = new FlexibleSearchQuery(QUERY_FIND_TOP100_SELLABLE_PRODUCTS);
        final SearchResult<String> result = flexibleSearchService.search(query);
        return result.getResult();
    }
}
