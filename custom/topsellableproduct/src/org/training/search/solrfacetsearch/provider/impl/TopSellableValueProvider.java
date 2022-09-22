package org.training.search.solrfacetsearch.provider.impl;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.provider.FieldValue;
import de.hybris.platform.solrfacetsearch.provider.FieldValueProvider;
import de.hybris.platform.solrfacetsearch.provider.impl.AbstractPropertyFieldValueProvider;
import org.training.util.TopSellableUtil;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TopSellableValueProvider extends AbstractPropertyFieldValueProvider implements FieldValueProvider {

    @Resource
    private TopSellableUtil topSellableUtil;

    private  FieldNameProvider fieldNameProvider;

    @Override
    public Collection<FieldValue> getFieldValues(IndexConfig indexConfig, IndexedProperty indexedProperty, Object product) throws FieldValueProviderException {
        final Collection<FieldValue> fieldValues = new ArrayList<FieldValue>();
        ProductModel productModel = (ProductModel)product;
      List<String> topSellableProductList = topSellableUtil.topSellableProductList;
      if(topSellableProductList==null){
           topSellableUtil.getTopSellableProducts();
          topSellableProductList = topSellableUtil.topSellableProductList;
      }
       if(topSellableProductList.contains(productModel)) {
           addFieldValues(fieldValues, indexedProperty, Boolean.TRUE);
       }else{
           addFieldValues(fieldValues, indexedProperty, Boolean.FALSE);
       }
        return fieldValues;
    }
    private void addFieldValues(final Collection<FieldValue> fieldValues,IndexedProperty indexedProperty, Object value ) {
        Collection<String> fieldNames = fieldNameProvider.getFieldNames(indexedProperty, null);
        for(final String fieldName:fieldNames) {
            fieldValues.add(new FieldValue(fieldName, value));
        }
    }

    public FieldNameProvider getFieldNameProvider() {
        return fieldNameProvider;
    }

    public void setFieldNameProvider(FieldNameProvider fieldNameProvider) {
        this.fieldNameProvider = fieldNameProvider;
    }
}
