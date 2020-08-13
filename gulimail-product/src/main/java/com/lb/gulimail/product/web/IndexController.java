package com.lb.gulimail.product.web;

        import com.lb.gulimail.product.entity.CategoryEntity;
        import com.lb.gulimail.product.service.CategoryService;
        import com.lb.gulimail.product.vo.Catalog2Vo;
        import org.redisson.api.RLock;
        import org.redisson.api.RReadWriteLock;
        import org.redisson.api.RSemaphore;
        import org.redisson.api.RedissonClient;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.stereotype.Controller;
        import org.springframework.ui.Model;
        import org.springframework.web.bind.annotation.GetMapping;
        import org.springframework.web.bind.annotation.ResponseBody;

        import java.util.List;
        import java.util.Map;

@Controller
public class IndexController {
    @Autowired
    CategoryService categoryService;
    @Autowired
    RedissonClient redisson;

    @GetMapping({"/", "/index.html", "/index"})
    public String indexPage(Model model) {
        //查出所有1级分类
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categorys();
        model.addAttribute("categorys", categoryEntities);
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        Map<String, List<Catalog2Vo>> catalogJson = categoryService.getCatalogJson();
        return catalogJson;
    }

    /**
     * 测试Redisson的可重入锁
     * @return
     */
    @ResponseBody
    @GetMapping("/lock")
    public String testRedissonLock() {
        RLock lock = redisson.getLock("my_lock");
        lock.lock();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return "testRedissonLock";
    }

    /**
     * 测试redisson读写锁
     * @return
     */
    @GetMapping("/read")
    @ResponseBody
    public String read(){
        RReadWriteLock rwLock = redisson.getReadWriteLock("rw_lock");
        RLock rLock = rwLock.readLock();
        rLock.lock();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            rLock.unlock();
        }
        return "read";
    }
    @GetMapping("/write")
    @ResponseBody
    public String write(){
        RReadWriteLock rwLock = redisson.getReadWriteLock("rw_lock");
        RLock wLock = rwLock.writeLock();
        wLock.lock();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            wLock.unlock();
        }
        return "write";
    }
    @GetMapping("/park")
    @ResponseBody
    public String park() throws InterruptedException {
        RSemaphore semaphore = redisson.getSemaphore("park");

        semaphore.acquire();

        return "ok";
    }
    @GetMapping("/go")
    @ResponseBody
    public String go() throws InterruptedException{
        RSemaphore semaphore = redisson.getSemaphore("park");
        semaphore.release();
        return "ok";
    }
}
