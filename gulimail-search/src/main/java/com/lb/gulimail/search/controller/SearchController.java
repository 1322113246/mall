package com.lb.gulimail.search.controller;

import com.lb.gulimail.search.service.MallSearchService;
import com.lb.gulimail.search.vo.SearchParam;
import com.lb.gulimail.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SearchController {
    @Autowired
    MallSearchService mallSearchService;

    @RequestMapping("/list.html")
    public String listPage(SearchParam param, Model model){
        SearchResult result = mallSearchService.search(param);
        model.addAttribute("result",result);
        return "list";
    }

}
