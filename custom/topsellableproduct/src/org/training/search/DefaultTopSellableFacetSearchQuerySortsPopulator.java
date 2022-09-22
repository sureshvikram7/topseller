package org.training.search;

import de.hybris.platform.core.PK;
import de.hybris.platform.solrfacetsearch.config.IndexedTypeSort;
import de.hybris.platform.solrfacetsearch.config.IndexedTypeSortField;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.search.FieldNameTranslator;
import de.hybris.platform.solrfacetsearch.search.OrderField;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.impl.SearchQueryConverterData;
import org.apache.commons.collections.CollectionUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import de.hybris.platform.converters.Populator;


public class DefaultTopSellableFacetSearchQuerySortsPopulator implements Populator<SearchQueryConverterData, SolrQuery> {
    protected static final int BUFFER_SIZE = 256;
    protected static final float MAX_PROMOTED_RESULT_SCORE = 10000.0F;
    protected static final String SOLR_SCORE_FIELD = "score";
    protected static final String ISTOPSELLABLE = "isTopSellable";
    protected static final String UNSERSCORE = "_";
    protected static final String BOOLEAN = "boolean";
    private FieldNameTranslator fieldNameTranslator;

    public DefaultTopSellableFacetSearchQuerySortsPopulator() {
    }

    public FieldNameTranslator getFieldNameTranslator() {
        return this.fieldNameTranslator;
    }

    @Required
    public void setFieldNameTranslator(FieldNameTranslator fieldNameTranslator) {
        this.fieldNameTranslator = fieldNameTranslator;
    }

    public void populate(SearchQueryConverterData source, SolrQuery target) {
        SearchQuery searchQuery = source.getSearchQuery();
        List<PK> promotedItems = new ArrayList();
        List<OrderField> sorts = new ArrayList();
        this.buildSorts(source, promotedItems, sorts);
        if (CollectionUtils.isNotEmpty(promotedItems)) {
            String promotedItemsSort = this.buildPromotedItemsSort(searchQuery);
            target.addSort(promotedItemsSort, SolrQuery.ORDER.desc);
        }

        if (CollectionUtils.isNotEmpty(sorts)) {
            Iterator var7 = sorts.iterator();

            while(var7.hasNext()) {
                OrderField sort = (OrderField)var7.next();
                String field = Objects.equals(sort.getField(), "score") ? sort.getField() : this.fieldNameTranslator.translate(searchQuery, sort.getField(), FieldNameProvider.FieldType.SORT);
                if(field.contains(ISTOPSELLABLE)){
                    field=ISTOPSELLABLE+UNSERSCORE+BOOLEAN;
                    target.addSort(field, SolrQuery.ORDER.desc);
                }else {
                target.addSort(field, sort.isAscending() ? SolrQuery.ORDER.asc : SolrQuery.ORDER.desc);
                }

            }
        } else {
            target.addSort("score", SolrQuery.ORDER.desc);
        }

    }

    protected void buildSorts(SearchQueryConverterData searchQueryConverterData, List<PK> promotedItems, List<OrderField> sorts) {
        SearchQuery searchQuery = searchQueryConverterData.getSearchQuery();
        IndexedTypeSort currentSort = searchQueryConverterData.getFacetSearchContext().getNamedSort();
        if (currentSort != null) {
            if (currentSort.isApplyPromotedItems()) {
                promotedItems.addAll(searchQuery.getPromotedItems());
            }

            Iterator var7 = currentSort.getFields().iterator();

            while(var7.hasNext()) {
                IndexedTypeSortField indexedTypeSortField = (IndexedTypeSortField)var7.next();
                sorts.add(new OrderField(indexedTypeSortField.getFieldName(), indexedTypeSortField.isAscending() ? OrderField.SortOrder.ASCENDING : OrderField.SortOrder.DESCENDING));
            }
        } else {
            promotedItems.addAll(searchQuery.getPromotedItems());
            sorts.addAll(searchQuery.getSorts());
        }

    }

    protected String buildPromotedItemsSort(SearchQuery searchQuery) {
        List<PK> promotedItems = searchQuery.getPromotedItems();
        if (CollectionUtils.isEmpty(promotedItems)) {
            return "";
        } else {
            StringBuilder query = new StringBuilder(256);
            query.append("query({!v='");
            float score = 10000.0F;
            int index = 0;

            for(Iterator var7 = promotedItems.iterator(); var7.hasNext(); ++index) {
                PK promotedItem = (PK)var7.next();
                if (index != 0) {
                    query.append(' ');
                }

                query.append("pk");
                query.append(':');
                query.append(promotedItem.getLongValueAsString());
                query.append("^=");
                query.append(score);
                score = Math.nextDown(score);
            }

            query.append("'},1)");
            return query.toString();
        }
    }
}
