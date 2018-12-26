package com.lonebytesoft.hamster.accounting.controller;

import com.lonebytesoft.hamster.accounting.controller.view.ActionResultView;
import com.lonebytesoft.hamster.accounting.controller.view.CategoryInputView;
import com.lonebytesoft.hamster.accounting.controller.view.CategoryView;
import com.lonebytesoft.hamster.accounting.controller.view.converter.ModelViewConverter;
import com.lonebytesoft.hamster.accounting.model.Category;
import com.lonebytesoft.hamster.accounting.model.OrderedUtils;
import com.lonebytesoft.hamster.accounting.model.Transaction;
import com.lonebytesoft.hamster.accounting.repository.CategoryRepository;
import com.lonebytesoft.hamster.accounting.repository.TransactionRepository;
import com.lonebytesoft.hamster.accounting.service.EntityAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/category")
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final ModelViewConverter<Category, CategoryInputView, CategoryView> viewConverter;

    @Autowired
    public CategoryController(
            final CategoryRepository categoryRepository,
            final TransactionRepository transactionRepository,
            final ModelViewConverter<Category, CategoryInputView, CategoryView> viewConverter
    ) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.viewConverter = viewConverter;
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<CategoryView> getCategories() {
        return StreamSupport.stream(categoryRepository.findAll().spliterator(), false)
                .map(viewConverter::convertToOutput)
                .collect(Collectors.toList());
    }

    @RequestMapping(method = RequestMethod.POST, consumes = "application/json")
    public CategoryView addCategory(
            @RequestBody final CategoryInputView categoryInputView
    ) {
        Category category = viewConverter.populateFromInput(new Category(), categoryInputView);
        category.setOrdering(OrderedUtils.getMaxOrder(categoryRepository.findAll()) + 1);
        
        category = categoryRepository.save(category);
        return viewConverter.convertToOutput(category);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/{id}", consumes = "application/json")
    public CategoryView modifyCategory(
            @PathVariable final long id,
            @RequestBody final CategoryInputView categoryInputView
    ) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Could not find category, id=" + id));

        category = viewConverter.populateFromInput(category, categoryInputView);
        category = categoryRepository.save(category);
        return viewConverter.convertToOutput(category);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/{id}/{action}")
    public ActionResultView performCategoryAction(
            @PathVariable final long id,
            @PathVariable final EntityAction action
    ) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Could not find category, id=" + id));

        switch(action) {
            case MOVE_UP:
                final Category firstBefore = categoryRepository.findFirstBefore(category.getOrdering());
                OrderedUtils.swapOrder(category, firstBefore);
                categoryRepository.saveAll(
                        Stream.of(category, firstBefore)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList())
                );
                break;

            case MOVE_DOWN:
                final Category firstAfter = categoryRepository.findFirstAfter(category.getOrdering());
                OrderedUtils.swapOrder(category, firstAfter);
                categoryRepository.saveAll(
                        Stream.of(category, firstAfter)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList())
                );
                break;

            case DELETE:
                deleteCategory(category);
                break;

            default:
                throw new UnsupportedOperationException(action.getParamValue());
        }

        final ActionResultView actionResultView = new ActionResultView();
        actionResultView.setStatus(ActionResultView.Status.SUCCESS);
        return actionResultView;
    }

    private void deleteCategory(final Category category) {
        final Collection<Transaction> transactions = transactionRepository.findByCategory(category);
        if(!transactions.isEmpty()) {
            throw new IllegalArgumentException(
                    "Category contains the following transactions: " + Transaction.toUserString(transactions)
            );
        }

        categoryRepository.delete(category);
    }

}
