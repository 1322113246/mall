package com.lb.gulimail.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.lb.gulimail.product.service.CategoryBrandRelationService;
import com.lb.gulimail.product.vo.Catalog2Vo;
import com.lb.gulimail.product.dao.CategoryDao;
import com.lb.gulimail.product.entity.CategoryEntity;
import com.lb.gulimail.product.service.CategoryService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lb.common.utils.PageUtils;
import com.lb.common.utils.Query;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    private final static String LOCK_NAME = "catalogLock";
    private final static String UNLOCK_LUA = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end  ";
    //    @Autowired
//    CategoryDao categoryDao;
    @Autowired
    RedissonClient redisson;
    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1、查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        //2、组装成父子的树形结构

        //2.1）、找到所有的一级分类
        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity ->
                categoryEntity.getParentCid() == 0
        ).map((menu) -> {
            menu.setChildren(getChildrens(menu, entities));
            return menu;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());


        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO  1、检查当前删除的菜单，是否被别的地方引用

        //逻辑删除
        baseMapper.deleteBatchIds(asList);
    }

    //[2,25,225]
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);

        Collections.reverse(parentPath);


        return parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * 级联更新所有关联的数据
     *
     * @param category
     */
    //失效模式 删除缓存再更新数据库

//    @Caching(evict = {
//            @CacheEvict(value = "cache",key = "'getCatalogJson'"),
//            @CacheEvict(value = "cache",key = "'getLevel1Categorys'")
//    })
    @CacheEvict(value = {"category"},allEntries = true)//指定删除category分区的缓存
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    @Cacheable(value = {"category"},key = "#root.method.name")
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        System.out.println("getLevel1Categorys");
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntities;
    }

    /**
     * 使用springcache注释来缓存处理
     * @return
     */
    @Cacheable(value = {"category"},key = "#root.methodName")
    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        System.out.println("数据库中获取到的数据");
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        //1.查出一级分类
        List<CategoryEntity> level1Categorys = getParent_Cid(categoryEntities, 0l);
        //2.封装一级分类
        Map<String, List<Catalog2Vo>> catalogMap = level1Categorys.stream().collect(Collectors.toMap(key -> key.getCatId().toString(), value -> {
            //查出每个一级分类的二级子分类
            List<CategoryEntity> level2Categorys = getParent_Cid(categoryEntities, value.getCatId());
            //封装二级子分类
            List<Catalog2Vo> catalog2VoList = null;
            if (level2Categorys != null) {
                catalog2VoList = level2Categorys.stream().map(l2 -> {
                    //查出每个二级分类的三级子分类
                    List<CategoryEntity> level3Categorys = getParent_Cid(categoryEntities, l2.getCatId());
                    //封装三级子分类
                    List<Catalog2Vo.Catalog3Vo> catalog3VoList = null;
                    if (level3Categorys != null) {
                        catalog3VoList = level3Categorys.stream().map(cat3 -> {
                            Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(l2.getCatId().toString(), cat3.getCatId().toString(), cat3.getName());
                            return catalog3Vo;
                        }).collect(Collectors.toList());
                    }
                    //将数据封装到二级子分类
                    Catalog2Vo catalog2Vo = new Catalog2Vo(value.getCatId().toString(), catalog3VoList, l2.getCatId().toString(), l2.getName());
                    return catalog2Vo;
                }).collect(Collectors.toList());
            }
            return catalog2VoList;
        }));
        return catalogMap;
    }
    /**
     * TODO 产生堆外内存溢出：OutOfDirectMemoryError
     * 1)springboot2.0默认使用lettuce作为redis的客户端，它使用netty进行网络通信。
     * 2）lettuce的bug导致netty堆外内存溢出。
     * 解决方案 tips：不能使用-Dio.netty.maxDirectMemory只去调大堆外内存
     * 1) 升级lettuce客户端 2）切换使用jedis
     *
     * @return
     */
    public Map<String, List<Catalog2Vo>> getCatalogJson2() {
        /**
         *先从缓存中查询
         * 1.空结果缓存：解决缓存击穿
         * 2.设置随机过期时间：解决缓存雪崩
         * 3.加锁：缓存穿透
         */
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String catalogJson = ops.get("catalog");
        if (StringUtils.isEmpty(catalogJson)) {
            //缓存中没有，则从数据库中查询出来，并将数据放入缓存
            Map<String, List<Catalog2Vo>> catalogJsonFromDB = getCatalogJsonFromDBWithRedissonLock();

            return catalogJsonFromDB;
        }
        //将字符串转化为返回类型
        TypeReference<Map<String, List<Catalog2Vo>>> typeReference = new TypeReference<Map<String, List<Catalog2Vo>>>() {
        };
        Map<String, List<Catalog2Vo>> result = JSON.parseObject(catalogJson, typeReference);
        return result;
    }

    /**
     * 使用redisson分布式锁
     */
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDBWithRedissonLock() {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        RLock lock = redisson.getLock(LOCK_NAME);
        lock.lock(60,TimeUnit.SECONDS);
        Map<String, List<Catalog2Vo>> catalogJsonFromDB = null;
        try {
            //抢占到锁，从数据库查询到数据
            catalogJsonFromDB = getCatalogJsonFromDB();
        } finally {
            //释放锁
            lock.unlock();
            return catalogJsonFromDB;
        }
    }

    /**
     * 使用redis分布式锁结合lua脚本进行加锁
     */
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDBWithRedisLock() {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String uuid = UUID.randomUUID().toString();
        //redis占坑并设置过期时间，分布式锁
        Boolean lock = ops.setIfAbsent(LOCK_NAME, uuid, 300, TimeUnit.SECONDS);
        if (lock) {
            Map<String, List<Catalog2Vo>> catalogJsonFromDB = null;
            try {
                //抢占到锁，从数据库查询到数据
                catalogJsonFromDB = getCatalogJsonFromDB();
            } finally {
                //lua脚本删锁
                Long execute = redisTemplate.execute(new DefaultRedisScript<Long>(UNLOCK_LUA, Long.class), Arrays.asList(LOCK_NAME), uuid);
                return catalogJsonFromDB;
            }
        } else {
            //当未抢占到锁继续进行竞争
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                return getCatalogJsonFromDBWithRedisLock();
            }
        }

    }

    /**
     * 从数据库获取分类数据并封装成指定类型属性
     *
     * @return
     */

    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDB() {
        //查出所有商品分类,先从缓存中查询
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String catalogJson = ops.get("catalog");
        if (!StringUtils.isEmpty(catalogJson)) {
            //查询到缓存中存在数据，直接返回数据
            TypeReference<Map<String, List<Catalog2Vo>>> typeReference = new TypeReference<Map<String, List<Catalog2Vo>>>() {
            };
            Map<String, List<Catalog2Vo>> result = JSON.parseObject(catalogJson, typeReference);
            System.out.println("缓存中获取到的数据");
            return result;
        }
        System.out.println("数据库中获取到的数据");
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        //1.查出一级分类
        List<CategoryEntity> level1Categorys = getParent_Cid(categoryEntities, 0l);
        //2.封装一级分类
        Map<String, List<Catalog2Vo>> catalogMap = level1Categorys.stream().collect(Collectors.toMap(key -> key.getCatId().toString(), value -> {
            //查出每个一级分类的二级子分类
            List<CategoryEntity> level2Categorys = getParent_Cid(categoryEntities, value.getCatId());
            //封装二级子分类
            List<Catalog2Vo> catalog2VoList = null;
            if (level2Categorys != null) {
                catalog2VoList = level2Categorys.stream().map(l2 -> {
                    //查出每个二级分类的三级子分类
                    List<CategoryEntity> level3Categorys = getParent_Cid(categoryEntities, l2.getCatId());
                    //封装三级子分类
                    List<Catalog2Vo.Catalog3Vo> catalog3VoList = null;
                    if (level3Categorys != null) {
                        catalog3VoList = level3Categorys.stream().map(cat3 -> {
                            Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(l2.getCatId().toString(), cat3.getCatId().toString(), cat3.getName());
                            return catalog3Vo;
                        }).collect(Collectors.toList());
                    }
                    //将数据封装到二级子分类
                    Catalog2Vo catalog2Vo = new Catalog2Vo(value.getCatId().toString(), catalog3VoList, l2.getCatId().toString(), l2.getName());
                    return catalog2Vo;
                }).collect(Collectors.toList());
            }
            return catalog2VoList;
        }));
        String jsonString = JSON.toJSONString(catalogMap);
        ops.set("catalog", jsonString, 1, TimeUnit.DAYS);
        return catalogMap;
    }

    /**
     * 过滤分类数据得到指定数据
     */
    public List<CategoryEntity> getParent_Cid(List<CategoryEntity> categoryEntities, Long parentCid) {
        List<CategoryEntity> collect = categoryEntities.stream().filter(item -> {
            return item.getParentCid() == parentCid;
        }).collect(Collectors.toList());
        return collect;
    }

    //225,25,2
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        //1、收集当前节点id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;

    }


    //递归查找所有菜单的子菜单
    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {

        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == root.getCatId();
        }).map(categoryEntity -> {
            //1、找到子菜单
            categoryEntity.setChildren(getChildrens(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1, menu2) -> {
            //2、菜单的排序
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());

        return children;
    }


}