package org.training.search;

import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commercefacades.search.converters.populator.SearchResultVariantProductPopulator;
import de.hybris.platform.commerceservices.search.resultdata.SearchResultValueData;

public class TopSellableSearchResultVariantProductPopulator extends SearchResultVariantProductPopulator {
    public static final String ISTOPSALEPRODUCT = "isTopSellable_boolean";

    @Override
    public void populate(final SearchResultValueData source, final ProductData target)
    {
        super.populate(source, target);
        if (source.getValues() != null)
        {
            target.setIsTopSellable((Boolean) source.getValues().get(ISTOPSALEPRODUCT));

        }
    }
}
