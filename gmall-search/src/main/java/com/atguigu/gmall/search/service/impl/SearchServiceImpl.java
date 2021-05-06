package com.atguigu.gmall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.search.service.SearchService;
import com.atguigu.gmall.search.vo.GoodsVO;
import com.atguigu.gmall.search.vo.SearchParamVO;
import com.atguigu.gmall.search.vo.SearchResponse;
import com.atguigu.gmall.search.vo.SearchResponseAttrVO;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.ChildrenAggregation;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
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
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    private JestClient jestClient;

    @Override
    public SearchResponse search(SearchParamVO searchParamVO) {
        try {
            String queryDSL = buildDSL(searchParamVO);
            System.out.println(queryDSL);
            Search search = new Search.Builder(queryDSL).addIndex("goods").addType("info").build();
            SearchResult result = this.jestClient.execute(search);
            // 解析搜索结果
            SearchResponse searchResponse = analyzeResult(result);
            // 设置分页参数
            searchResponse.setPageNum(searchParamVO.getPageNum());
            searchResponse.setPageSize(searchParamVO.getPageSize());
            searchResponse.setTotal(result.getTotal());
            return searchResponse;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解析搜索结果 方法
     *
     * @param result
     * @return
     */
    private SearchResponse analyzeResult(SearchResult result) {
        SearchResponse searchResponse = new SearchResponse();
        // 获取所有聚合
        MetricAggregation aggregations = result.getAggregations();
        // 解析品牌的聚合结果集 格式：“品牌”(写死)------------------>[{"id":"品牌id","name":"品牌名称"},{"id":"品牌id","name":"品牌名称"},{"id":"品牌id","name":"品牌名称"}...]
        // 获取品牌聚合
        TermsAggregation brandAgg = aggregations.getTermsAggregation("brandAgg");
        // 获取品牌聚合中的所有桶
        List<TermsAggregation.Entry> brandAggBuckets = brandAgg.getBuckets();
        // 判断品牌聚合是否为空
        if (!CollectionUtils.isEmpty(brandAggBuckets)) {
            // 初始化品牌vo对象
            SearchResponseAttrVO brand = new SearchResponseAttrVO();
            // 写死品牌聚合名称
            brand.setName("品牌");
            List<String> values = brandAggBuckets.stream().map(brandAggBucket -> {
                Map<String, String> map = new HashMap<>();
                map.put("id", brandAggBucket.getKeyAsString());
                // 获取品牌id桶中子聚合（品牌的名称）
                TermsAggregation brandNameAgg = brandAggBucket.getTermsAggregation("brandNameAgg");
                map.put("name", brandNameAgg.getBuckets().get(0).getKeyAsString());
                return JSON.toJSONString(map);
            }).collect(Collectors.toList());
            // 设置品牌的所有聚合值
            brand.setValue(values);
            searchResponse.setBrand(brand);

        }
        // 解析分类的聚合结果集
        TermsAggregation categoryAgg = aggregations.getTermsAggregation("categoryAgg");
        List<TermsAggregation.Entry> categoryAggBuckets = categoryAgg.getBuckets();
        // 判断分类聚合是否为空
        if (!CollectionUtils.isEmpty(categoryAggBuckets)) {
            // 初始化分类vo对象
            SearchResponseAttrVO category = new SearchResponseAttrVO();
            // 写死分类聚合名称
            category.setName("分类");
            List<String> values = categoryAggBuckets.stream().map(categoryAggBucket -> {
                Map<String, String> map = new HashMap<>();
                map.put("id", categoryAggBucket.getKeyAsString());
                // 获取分类id桶中子聚合（品牌的名称）
                TermsAggregation categoryNameAgg = categoryAggBucket.getTermsAggregation("categoryNameAgg");
                map.put("name", categoryNameAgg.getBuckets().get(0).getKeyAsString());
                return JSON.toJSONString(map);
            }).collect(Collectors.toList());
            // 设置品牌的所有聚合值
            category.setValue(values);
            searchResponse.setCatelog(category);

        }
        // 解析搜索属性的聚合结果集 格式：属性名+属性id------------------>属性值1，属性值2，属性值3，属性值4，属性值5
        ChildrenAggregation attrAgg = aggregations.getChildrenAggregation("attrAgg");
        TermsAggregation attrIdAgg = attrAgg.getTermsAggregation("attrIdAgg");
        List<TermsAggregation.Entry> attrIdAggBuckets = attrIdAgg.getBuckets();
        List<SearchResponseAttrVO> attrs = attrIdAggBuckets.stream().map(attrIdAggBucket -> {
            SearchResponseAttrVO attr = new SearchResponseAttrVO();
            //属性的id
            attr.setProductAttributeId(Long.parseLong(attrIdAggBucket.getKeyAsString()));
            TermsAggregation attrNameAgg = attrIdAggBucket.getTermsAggregation("attrNameAgg");
            // 属性的名字
            attr.setName(attrNameAgg.getBuckets().get(0).getKeyAsString());
            TermsAggregation attrValueAgg = attrIdAggBucket.getTermsAggregation("attrValueAgg");
            List<TermsAggregation.Entry> attrValueAggBuckets = attrValueAgg.getBuckets();
            List<String> values = attrValueAggBuckets.stream().map(TermsAggregation.Entry::getKeyAsString).collect(Collectors.toList());
            attr.setValue(values);
            return attr;
        }).collect(Collectors.toList());
        searchResponse.setAttrs(attrs);

        // 解析商品列表的结果集
        searchResponse.setProducts(result.getSourceAsObjectList(GoodsVO.class, false));

        return searchResponse;
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
        if (StringUtils.isNotBlank(keyword))
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
