package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.dao.SkuEsMapper;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SkuService;
import entity.Result;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.querydsl.QSort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author Steven
 * @version 1.0
 * @description com.changgou.search.service.impl
 * @date 2019-11-3
 */
@Service
public class SkuServiceImpl implements SkuService {
    @Autowired
    private SkuEsMapper skuEsMapper;
    @Autowired
    private SkuFeign skuFeign;
    @Override
    public void importSku() {
        //远程调用，获取sku列表
        List<Sku> skuList = skuFeign.findByStatus("1").getData();
        //把sku转换成skuinfo
        List<SkuInfo> skuInfos = JSON.parseArray(JSON.toJSONString(skuList), SkuInfo.class);
        //规格嵌套域-.........
        for (SkuInfo skuInfo : skuInfos) {
            Map specMap = JSON.parseObject(skuInfo.getSpec(), Map.class);
            skuInfo.setSpecMap(specMap);
        }
        skuEsMapper.saveAll(skuInfos);
    }

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Override
    public Map search(Map<String, String> searchMap) {
        Map map = new HashMap();
        //构建查询条件
        NativeSearchQueryBuilder builder = builderBasicQuery(searchMap);
        //1、根据条件查询商品列表
        searchList(builder, map);
        /*//2、分组查询商品分类列表
        searchCategoryList(builder,map);
        //3、分组查询品牌列表
        searchBrandList(builder,map);
        //4、分组查询规格列表
        searchSpec(builder,map);*/

        //分组查询分类、品牌与规格列表
        searchGroup(builder,map);

        return map;
    }

    /**
     * 分组查询分类、品牌与规格列表
     * @param builder  查询条件
     * @param map 查询结果集
     */
    private void searchGroup(NativeSearchQueryBuilder builder, Map map){
        //分类分组-聚合
        //1.设置分组域名-termsAggregationBuilder = AggregationBuilders.terms(别名).field(域名);
        TermsAggregationBuilder categoryBuilder = AggregationBuilders.terms("group_category").field("categoryName");
        //2.添加分组查询参数-builder.addAggregation(termsAggregationBuilder)
        builder.addAggregation(categoryBuilder);

        //品牌聚合
        //1.设置分组域名-termsAggregationBuilder = AggregationBuilders.terms(别名).field(域名);
        TermsAggregationBuilder brandBuilder = AggregationBuilders.terms("group_brand").field("brandName");
        //2.添加分组查询参数-builder.addAggregation(termsAggregationBuilder)
        builder.addAggregation(brandBuilder);

        //规格聚合
        //1.设置分组域名-termsAggregationBuilder = AggregationBuilders.terms(别名).field(域名);
        TermsAggregationBuilder specBuilder = AggregationBuilders.terms("group_spec").field("spec.keyword").size(100000);
        //2.添加分组查询参数-builder.addAggregation(termsAggregationBuilder)
        builder.addAggregation(specBuilder);

        //3.执行搜索-esTemplate.queryForPage(builder.build(), SkuInfo.class)
        AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);
        //4.获取所有分组查询结果集-page.getAggregations()
        Aggregations aggregations = page.getAggregations();
        //提取分类数据
        List<String> categoryList = getGroupResult(aggregations, "group_category");
        map.put("categoryList", categoryList);

        //提取分类数据
        List<String> brandList = getGroupResult(aggregations, "group_brand");
        map.put("brandList", brandList);

        //提取分类数据
        List<String> specList = getGroupResult(aggregations, "group_spec");
        //组装规格集合
        Map<String, Set<String>> specMap = new HashMap<>();
        //{"手机屏幕尺寸":"5.5寸","网络":"移动4G","颜色":"绿","测试":"测试","机身内存":"128G","存储":"16G","像素":"300万像素"}
        Map<String,String> tempMap = null;
        for (String json : specList) {
            tempMap = JSON.parseObject(json, Map.class);
            //读取Map
            for (String key : tempMap.keySet()) {
                Set<String> tempSet = specMap.get(key);
                //说明是第一个key
                if(tempSet == null){
                    tempSet = new HashSet<>();
                    //记录新列表
                    specMap.put(key, tempSet);
                }
                //记录规格内容
                tempSet.add(tempMap.get(key));
            }
        }
        //把规格返回
        map.put("specMap", specMap);
    }

    /**
     * 提取聚合结果
     * @param aggregations 查询到聚合对象结果集
     * @param group_name 分组名称
     * @return 聚合组装的List列表
     */
    private List<String> getGroupResult(Aggregations aggregations,String group_name) {
        //5.提取分组结果数据-stringTerms = aggregations.get(填入刚才查询时的别名)
        StringTerms stringTerms = aggregations.get(group_name);
        //6.定义分类名字列表-categoryList = new ArrayList<String>()
        List<String> categoryList = new ArrayList<String>();
        //7.遍历读取分组查询结果-stringTerms.getBuckets().for
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            //7.1获取分类名字，并将分类名字存入到集合中-bucket.getKeyAsString()
            categoryList.add(bucket.getKeyAsString());
        }
        return categoryList;
    }

    /**
     * 分组查询品牌列表
     * @param builder  查询条件
     * @param map 查询结果集
     */
    private void searchSpec(NativeSearchQueryBuilder builder, Map map) {
        //1.设置分组域名-termsAggregationBuilder = AggregationBuilders.terms(别名).field(域名);
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("group_spec").field("spec.keyword").size(100000);
        //2.添加分组查询参数-builder.addAggregation(termsAggregationBuilder)
        builder.addAggregation(termsAggregationBuilder);
        //3.执行搜索-esTemplate.queryForPage(builder.build(), SkuInfo.class)
        AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);
        //4.获取所有分组查询结果集-page.getAggregations()
        Aggregations aggregations = page.getAggregations();
        //5.提取分组结果数据-stringTerms = aggregations.get(填入刚才查询时的别名)
        StringTerms stringTerms = aggregations.get("group_spec");
        //6.定义分类名字列表-categoryList = new ArrayList<String>()
        List<String> list = new ArrayList<String>();
        //7.遍历读取分组查询结果-stringTerms.getBuckets().for
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            //7.1获取分类名字，并将分类名字存入到集合中-bucket.getKeyAsString()
            list.add(bucket.getKeyAsString());
        }

        //组装规格集合
        Map<String, Set<String>> specMap = new HashMap<>();
        //{"手机屏幕尺寸":"5.5寸","网络":"移动4G","颜色":"绿","测试":"测试","机身内存":"128G","存储":"16G","像素":"300万像素"}
        Map<String,String> tempMap = null;
        for (String json : list) {
            tempMap = JSON.parseObject(json, Map.class);
            //读取Map
            for (String key : tempMap.keySet()) {
                Set<String> tempSet = specMap.get(key);
                //说明是第一个key
                if(tempSet == null){
                    tempSet = new HashSet<>();
                    //记录新列表
                    specMap.put(key, tempSet);
                }
                //记录规格内容
                tempSet.add(tempMap.get(key));
            }
        }
        //把规格返回
        map.put("specMap", specMap);
    }

    /**
     * 分组查询品牌列表
     * @param builder  查询条件
     * @param map 查询结果集
     */
    private void searchBrandList(NativeSearchQueryBuilder builder, Map map) {
        //1.设置分组域名-termsAggregationBuilder = AggregationBuilders.terms(别名).field(域名);
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("group_brand").field("brandName");
        //2.添加分组查询参数-builder.addAggregation(termsAggregationBuilder)
        builder.addAggregation(termsAggregationBuilder);
        //3.执行搜索-esTemplate.queryForPage(builder.build(), SkuInfo.class)
        AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);
        //4.获取所有分组查询结果集-page.getAggregations()
        Aggregations aggregations = page.getAggregations();
        //5.提取分组结果数据-stringTerms = aggregations.get(填入刚才查询时的别名)
        StringTerms stringTerms = aggregations.get("group_brand");
        //6.定义分类名字列表-categoryList = new ArrayList<String>()
        List<String> brandList = new ArrayList<String>();
        //7.遍历读取分组查询结果-stringTerms.getBuckets().for
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            //7.1获取分类名字，并将分类名字存入到集合中-bucket.getKeyAsString()
            brandList.add(bucket.getKeyAsString());
        }
        //8.返回分类数据列表-map.put("categoryList", categoryList)
        map.put("brandList", brandList);
    }

    /**
     * 分组查询商品分类列表
     * @param builder  查询条件
     * @param map 查询结果集
     */
    private void searchCategoryList(NativeSearchQueryBuilder builder, Map map) {
        //1.设置分组域名-termsAggregationBuilder = AggregationBuilders.terms(别名).field(域名);
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("group_category").field("categoryName");
        //2.添加分组查询参数-builder.addAggregation(termsAggregationBuilder)
        builder.addAggregation(termsAggregationBuilder);
        //3.执行搜索-esTemplate.queryForPage(builder.build(), SkuInfo.class)
        AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);
        //4.获取所有分组查询结果集-page.getAggregations()
        Aggregations aggregations = page.getAggregations();
        //5.提取分组结果数据-stringTerms = aggregations.get(填入刚才查询时的别名)
        StringTerms stringTerms = aggregations.get("group_category");
        //6.定义分类名字列表-categoryList = new ArrayList<String>()
        List<String> categoryList = new ArrayList<String>();
        //7.遍历读取分组查询结果-stringTerms.getBuckets().for
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            //7.1获取分类名字，并将分类名字存入到集合中-bucket.getKeyAsString()
            categoryList.add(bucket.getKeyAsString());
        }
        //8.返回分类数据列表-map.put("categoryList", categoryList)
        map.put("categoryList", categoryList);
    }

    /**
     * 根据查询条件查询商品列表
     * @param builder 条件
     * @param map 返回对象
     */
    private void searchList(NativeSearchQueryBuilder builder, Map map) {
        //3、获取NativeSearchQuery搜索条件对象-builder.build()
        //4.查询数据-esTemplate.queryForPage(条件对象,搜索结果对象)
        //AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(query, SkuInfo.class);

        //h1.配置高亮查询信息-hField = new HighlightBuilder.Field()
        //h1.1:设置高亮域名-在构造函数中设置
        HighlightBuilder.Field hField = new HighlightBuilder.Field("name");
        //h1.2：设置高亮前缀-hField.preTags
        hField.preTags("<em style='color:red;'>");
        //h1.3：设置高亮后缀-hField.postTags
        hField.postTags("</em>");
        //h1.4：设置碎片大小-hField.fragmentSize
        hField.fragmentSize(100);
        //h1.5：追加高亮查询信息-builder.withHighlightFields()
        builder.withHighlightFields(hField);

        //记得要设置完高亮条件后，再build()
        NativeSearchQuery query = builder.build();

        //h2.高亮数据读取-AggregatedPage<SkuInfo> page = esTemplate.queryForPage(query, SkuInfo.class, new SearchResultMapper(){})
        AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(query, SkuInfo.class, new SearchResultMapper() {
            //h2.1实现mapResults(查询到的结果,数据列表的类型,分页选项)方法
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
                //h2.2 先定义一组查询结果列表-List<T> list = new ArrayList<T>()
                List<T> list = new ArrayList<T>();
                //h2.3 遍历查询到的所有高亮数据-response.getHits().for
                for (SearchHit hit : response.getHits()) {
                    //h2.3.1 先获取当次结果的原始数据(无高亮)-hit.getSourceAsString()
                    String json = hit.getSourceAsString();
                    //h2.3.2 把json串转换为SkuInfo对象-skuInfo = JSON.parseObject()
                    SkuInfo skuInfo = JSON.parseObject(json, SkuInfo.class);
                    //h2.3.3 获取name域的高亮数据-nameHighlight = hit.getHighlightFields().get("name")
                    HighlightField nameHighlight = hit.getHighlightFields().get("name");
                    //h2.3.4 如果高亮数据不为空-读取高亮数据
                    if (nameHighlight != null) {
                        //h2.3.4.1 定义一个StringBuffer用于存储高亮碎片-buffer = new StringBuffer()
                        StringBuffer buffer = new StringBuffer();
                        //h2.3.4.2 循环组装高亮碎片数据- nameHighlight.getFragments().for(追加数据)
                        for (Text fragment : nameHighlight.getFragments()) {
                            buffer.append(fragment);
                        }
                        //h2.3.4.3 将非高亮数据替换成高亮数据-skuInfo.setName()
                        skuInfo.setName(buffer.toString());
                    }

                    //h2.3.5 将替换了高亮数据的对象封装到List中-list.add((T) skuInfo)
                    list.add((T) skuInfo);
                }

                //h2.4 返回当前方法所需要参数-new AggregatedPageImpl<T>(数据列表，分页选项,总记录数)
                //h2.4 参考new AggregatedPageImpl<T>(list,pageable,response.getHits().getTotalHits())
                return new AggregatedPageImpl<T>(list, pageable, response.getHits().getTotalHits());
            }
        });
        //5、包装结果并返回
        map.put("rows", page.getContent());
        map.put("total", page.getTotalElements());
        map.put("totalPages", page.getTotalPages());

        int pageNum = query.getPageable().getPageNumber();  //当前页
        map.put("pageNum", pageNum + 1);
        int pageSize = query.getPageable().getPageSize();//每页查询的条数
        map.put("pageSize", pageSize);
    }

    /**
     * 构建基本查询条件
     * @param searchMap
     * @return
     */
    private NativeSearchQueryBuilder builderBasicQuery(Map<String, String> searchMap) {
        //1、创建查询条件构建器-builder = new NativeSearchQueryBuilder()
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        //2、组装查询条件
        if(searchMap != null){
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

            //2.1关键字搜索-builder.withQuery(QueryBuilders.matchQuery(域名，内容))
            String keyword = searchMap.get("keywords");
            if(StringUtils.isNotBlank(keyword)){
                //builder.withQuery(QueryBuilders.matchQuery("name", keyword));
                boolQueryBuilder.must(QueryBuilders.matchQuery("name", keyword));
            }
            //2.2 分类搜索
            if(StringUtils.isNotBlank(searchMap.get("category"))){
                boolQueryBuilder.must(QueryBuilders.termQuery("categoryName", searchMap.get("category")));
            }
            //2.3 品牌搜索
            if(StringUtils.isNotBlank(searchMap.get("brand"))){
                boolQueryBuilder.must(QueryBuilders.termQuery("brandName", searchMap.get("brand")));
            }
            //2.4规格搜索
            for (String key : searchMap.keySet()) {
                //查找规格的key
                if(key.startsWith("spec_")){
                    String specField = "specMap." + key.substring(5) + ".keyword";
                    //追加条件
                    boolQueryBuilder.must(QueryBuilders.termQuery(specField, searchMap.get(key)));
                }
            }
            //2.5 价格区间搜索-0-500|500-1000|....3000
            if (StringUtils.isNotBlank(searchMap.get("price"))) {
                String[] prices = searchMap.get("price").split("-");

                RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("price");
                //价格>=0
                rangeQuery.gte(prices[0]);
                if(prices.length > 1){
                    //价格 <= 1000
                    rangeQuery.lte(prices[1]);
                }
                boolQueryBuilder.must(rangeQuery);
            }

            //2.7排序查询--第一种
            //排序方式：ASC|DESC
            /*String sortRule = StringUtils.isBlank(searchMap.get("sortRule")) ? "" : searchMap.get("sortRule");
            //排序的域
            String sortField = StringUtils.isBlank(searchMap.get("sortField")) ? "" : searchMap.get("sortField");
            Sort sort = null;
            if(sortField.length() > 0){
                if(sortRule.toLowerCase().equals("asc")){
                    sort = new Sort(Sort.Direction.ASC,sortField);
                }else{
                    sort = new Sort(Sort.Direction.DESC,sortField);
                }
            }
            要在我们PageRequest.of方法中入参
            */

            //2.6分页查询
            Integer pageNum = StringUtils.isBlank(searchMap.get("pageNum")) ? 1 : new Integer(searchMap.get("pageNum"));
            //of(当前页从零开始,每页查询的条件)
            Pageable pageable = PageRequest.of(pageNum - 1, 5);
            builder.withPageable(pageable);


            //2.7排序查询--第2种
            String sortRule = StringUtils.isBlank(searchMap.get("sortRule")) ? "" : searchMap.get("sortRule");
            //排序的域
            String sortField = StringUtils.isBlank(searchMap.get("sortField")) ? "" : searchMap.get("sortField");
            if(sortField.length() > 0){
                builder.withSort(SortBuilders.fieldSort(sortField).order(SortOrder.valueOf(sortRule.toUpperCase())));
            }

            //添加多条件
            builder.withQuery(boolQueryBuilder);
        }
        return builder;
    }
}
