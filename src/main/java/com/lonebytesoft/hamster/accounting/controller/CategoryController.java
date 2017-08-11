package com.lonebytesoft.hamster.accounting.controller;

import com.lonebytesoft.hamster.accounting.controller.view.CategoryView;
import com.lonebytesoft.hamster.accounting.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryController(final CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/category")
    public List<CategoryView> getCategories() {
        return StreamSupport.stream(categoryRepository.findAll().spliterator(), false)
                .map(category -> {
                    final CategoryView categoryView = new CategoryView();
                    categoryView.setId(category.getId());
                    categoryView.setName(category.getName());
                    return categoryView;
                })
                .collect(Collectors.toList());
    }

}
