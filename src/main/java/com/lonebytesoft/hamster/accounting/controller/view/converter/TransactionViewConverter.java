package com.lonebytesoft.hamster.accounting.controller.view.converter;

import com.lonebytesoft.hamster.accounting.controller.exception.BadRequestException;
import com.lonebytesoft.hamster.accounting.controller.exception.CategoryNotFoundException;
import com.lonebytesoft.hamster.accounting.controller.view.input.OperationInputView;
import com.lonebytesoft.hamster.accounting.controller.view.input.TransactionInputView;
import com.lonebytesoft.hamster.accounting.controller.view.output.OperationView;
import com.lonebytesoft.hamster.accounting.controller.view.output.TransactionView;
import com.lonebytesoft.hamster.accounting.model.Category;
import com.lonebytesoft.hamster.accounting.model.Operation;
import com.lonebytesoft.hamster.accounting.model.Transaction;
import com.lonebytesoft.hamster.accounting.repository.CategoryRepository;
import com.lonebytesoft.hamster.accounting.service.date.DateService;
import com.lonebytesoft.hamster.accounting.service.transaction.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class TransactionViewConverter implements ModelViewConverter<Transaction, TransactionInputView, TransactionView> {

    private final CategoryRepository categoryRepository;

    private final TransactionService transactionService;
    private final DateService dateService;

    private final ModelViewConverter<Operation, OperationInputView, OperationView> operationConverter;

    @Autowired
    public TransactionViewConverter(
            final CategoryRepository categoryRepository,
            final DateService dateService,
            final TransactionService transactionService,
            final ModelViewConverter<Operation, OperationInputView, OperationView> operationConverter
    ) {
        this.categoryRepository = categoryRepository;

        this.transactionService = transactionService;
        this.dateService = dateService;

        this.operationConverter = operationConverter;
    }

    @Override
    public Transaction populateFromInput(Transaction base, TransactionInputView input) {
        final Long time = dateService.parse(input.getDate());
        if(time == null) {
            throw new BadRequestException("Could not parse date: '" + input.getDate() + "'");
        }
        base.setTime(time);

        final Category category = categoryRepository.findById(input.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(input.getCategoryId()));
        base.setCategory(category);

        base.setComment(input.getComment());

        base.setVisible(true);

        if(input.getOperations() != null) {
            final Collection<Operation> operations = input.getOperations()
                    .stream()
                    .filter(operationInputView -> !isBlank(operationInputView.getAmount()))
                    .map(operationInputView -> {
                        final Operation operation = operationConverter.populateFromInput(new Operation(), operationInputView);
                        operation.setTransaction(base);
                        return operation;
                    })
                    .collect(Collectors.toList());

            // preserving collection possibly tracked by Hibernate
            base.getOperations().clear();
            base.getOperations().addAll(operations);
        }

        return base;
    }

    @Override
    public TransactionView convertToOutput(Transaction model) {
        return new TransactionView(
                model.getId(),
                model.getTime(),
                model.getCategory().getId(),
                model.getComment(),
                model.getVisible(),
                model.getOperations()
                        .stream()
                        .map(operationConverter::convertToOutput)
                        .collect(Collectors.toList()),
                transactionService.calculateTotal(model)
        );
    }

    private boolean isBlank(final String s) {
        return (s == null) || (s.trim().equals(""));
    }

}
