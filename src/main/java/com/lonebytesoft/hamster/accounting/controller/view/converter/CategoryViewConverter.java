package com.lonebytesoft.hamster.accounting.controller.view.converter;

import com.lonebytesoft.hamster.accounting.controller.view.CategoryInputView;
import com.lonebytesoft.hamster.accounting.controller.view.CategoryView;
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
        final CategoryView output = new CategoryView();
        output.setId(model.getId());
        output.setName(model.getName());
        output.setVisible(model.getVisible());
        return output;
    }

}
