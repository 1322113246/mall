package com.lb.gulimail.search.service;

import com.lb.gulimail.search.vo.SearchParam;
import com.lb.gulimail.search.vo.SearchResult;

public interface MallSearchService {
    SearchResult search(SearchParam param);
}
