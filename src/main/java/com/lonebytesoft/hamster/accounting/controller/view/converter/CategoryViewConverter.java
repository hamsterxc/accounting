package com.lonebytesoft.hamster.accounting.controller.view.converter;

import com.lonebytesoft.hamster.accounting.controller.view.input.CategoryInputView;
import com.lonebytesoft.hamster.accounting.controller.view.output.CategoryView;
import com.lonebytesoft.hamster.accounting.model.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryViewConverter implements ModelViewConverter<Category, CategoryInputView, CategoryView> {

    @Override
    public Category populateFromInput(Category base, CategoryInputView input) {
        base.setName(input.getName());
        base.setVisible(input.getVisible());
        return base;
    }

    @Override
    public CategoryView convertToOutput(Category model) {
        return new CategoryView(
                model.getId(),
                model.getName(),
                model.getOrdering(),
                model.getVisible()
        );
    }

}
