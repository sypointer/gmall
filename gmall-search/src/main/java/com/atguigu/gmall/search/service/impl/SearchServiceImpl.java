package com.atguigu.gmall.search.service.impl;

import com.atguigu.gmall.search.service.SearchService;
import com.atguigu.gmall.search.vo.SearchParamVO;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    private JestClient jestClient;

    @Override
    public void search(SearchParamVO searchParamVO) {
        try {
            String queryDSL = buildDSL(searchParamVO);
            System.out.println(queryDSL);
            Search search = new Search.Builder(queryDSL).addIndex("goods").addType("info").build();
            SearchResult result = this.jestClient.execute(search);
            System.out.println(result);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 构建查询dsl语句
     *
     * @param searchParamVO
     * @return
     */
    private String buildDSL(SearchParamVO searchParamVO) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // 1. 构建查询和过滤条件
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 构建查询条件
        String keyword = searchParamVO.getKeyword();
        boolQuery.must(QueryBuilders.matchQuery("name", keyword).operator(Operator.AND));
        // 构建过滤条件
        // 品牌
        String[] brands = searchParamVO.getBrand();
        if (ArrayUtils.isNotEmpty(brands)) boolQuery.filter(QueryBuilders.termsQuery("brandId", brands));
        // 分类
        String[] catelog3 = searchParamVO.getCatelog3();
        if (ArrayUtils.isNotEmpty(catelog3)) boolQuery.filter(QueryBuilders.termsQuery("productCategoryId", catelog3));
        // 搜索的规格属性过滤
        String[] props = searchParamVO.getProps();
        if (ArrayUtils.isNotEmpty(props)) {
            for (String prop : props) {
                String[] attr = StringUtils.split(prop, ":");
                if (attr != null && attr.length == 2) {
                    BoolQueryBuilder propBoolQuery = QueryBuilders.boolQuery();
                    propBoolQuery.must(QueryBuilders.termQuery("attrValueList.productAttributeId", attr[0]));
                    String[] attrValues = StringUtils.split(attr[1], "-");
                    propBoolQuery.must(QueryBuilders.termsQuery("attrValueList.value", attrValues));
                    boolQuery.filter(QueryBuilders.nestedQuery("attrValueList", propBoolQuery, ScoreMode.None));
                }
            }
        }
        sourceBuilder.query(boolQuery);
        // 2. 完成分页的构建
        Integer pageNum = searchParamVO.getPageNum();
        Integer pageSize = searchParamVO.getPageSize();
        sourceBuilder.from((pageNum - 1) * pageSize);
        sourceBuilder.size(pageSize);
        // 3. 完成排序的构建
        String[] order = StringUtils.split(searchParamVO.getOrder(), ":");
        if (ArrayUtils.isNotEmpty(order)) {
            SortOrder sortOrder = StringUtils.equals("asc", order[1]) ? SortOrder.ASC : SortOrder.DESC;
            switch (order[0]) {
                case "0":
                    sourceBuilder.sort("_score", sortOrder);
                    break;
                case "1":
                    sourceBuilder.sort("sale", sortOrder);
                    break;
                case "2":
                    sourceBuilder.sort("price", sortOrder);
                    break;
                default:
                    break;
            }
        }

        // 4. 完成高亮的构建
        HighlightBuilder highlighter = new HighlightBuilder();
        highlighter.field("name");
        highlighter.preTags("<font color='red'>");
        highlighter.postTags("</font>");
        sourceBuilder.highlighter(highlighter);
        // 5. 完成聚合条件的构建
        // 品牌
        sourceBuilder.aggregation(
                AggregationBuilders.terms("brandAgg").field("brandId")
                        .subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName")));

        // 分类
        sourceBuilder.aggregation(
                AggregationBuilders.terms("categoryAgg").field("productCategoryId")
                        .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("productCategoryName")));

        // 搜索属性
        sourceBuilder.aggregation(
                AggregationBuilders.nested("attrAgg", "attrValueList")
                        .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrValueList.productAttributeId")
                                .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrValueList.name"))
                                .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrValueList.value")))
        );

        return sourceBuilder.toString();
    }

}
