package com.lonebytesoft.hamster.accounting.servlet;

import com.lonebytesoft.hamster.accounting.ApplicationContextListener;
import com.lonebytesoft.hamster.accounting.ModelViewMapper;
import com.lonebytesoft.hamster.accounting.model.Account;
import com.lonebytesoft.hamster.accounting.model.Category;
import com.lonebytesoft.hamster.accounting.model.Config;
import com.lonebytesoft.hamster.accounting.model.Currency;
import com.lonebytesoft.hamster.accounting.model.Transaction;
import com.lonebytesoft.hamster.accounting.repository.AccountRepository;
import com.lonebytesoft.hamster.accounting.repository.CategoryRepository;
import com.lonebytesoft.hamster.accounting.repository.TransactionRepository;
import com.lonebytesoft.hamster.accounting.service.ConfigService;
import com.lonebytesoft.hamster.accounting.service.TransactionAction;
import com.lonebytesoft.hamster.accounting.service.TransactionService;
import com.lonebytesoft.hamster.accounting.util.DateParser;
import com.lonebytesoft.hamster.accounting.util.Utils;
import com.lonebytesoft.hamster.accounting.util.XmlManager;
import com.lonebytesoft.hamster.accounting.view.AccountView;
import com.lonebytesoft.hamster.accounting.view.AccountingView;
import com.lonebytesoft.hamster.accounting.view.CategoryView;
import com.lonebytesoft.hamster.accounting.view.OperationView;
import com.lonebytesoft.hamster.accounting.view.RunningTotalView;
import com.lonebytesoft.hamster.accounting.view.SummaryItemView;
import com.lonebytesoft.hamster.accounting.view.SummaryView;
import com.lonebytesoft.hamster.accounting.view.TransactionView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Configuration
@EnableAutoConfiguration
@EntityScan("com.lonebytesoft.hamster.accounting.model")
@EnableJpaRepositories("com.lonebytesoft.hamster.accounting.repository")
public class RootServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(RootServlet.class);

    private static final String PARAM_TRANSACTION_ID = "transactionid";
    private static final String PARAM_ACTION = "action";
    private static final String PARAM_DATE = "date";
    private static final String PARAM_ACCOUNT_FORMAT = "account%d";
    private static final String PARAM_CATEGORY = "category";
    private static final String PARAM_COMMENT = "comment";

    private static final List<DateParser> PARAM_DATE_FORMATS = Arrays.asList(
            new DateParser("dd.MM.yyyy", "%s." + Calendar.getInstance().get(Calendar.YEAR)), // dd.MM
            new DateParser("dd.MM.yy"),
            new DateParser("dd.MM.yyyy")
    );

    private AccountRepository accountRepository;
    private CategoryRepository categoryRepository;
    private TransactionRepository transactionRepository;

    private ConfigService configService;
    private TransactionService transactionService;

    private ModelViewMapper modelViewMapper;

    private XmlManager xmlManager;

    @Override
    public void init(ServletConfig config) throws ServletException {
        logger.info("Initializing servlet");

        super.init(config);

        final BeanFactory beanFactory = (BeanFactory) config.getServletContext()
                .getAttribute(ApplicationContextListener.ATTRIBUTE_APPLICATION_CONTEXT);

        accountRepository = beanFactory.getBean(AccountRepository.class);
        categoryRepository = beanFactory.getBean(CategoryRepository.class);
        transactionRepository = beanFactory.getBean(TransactionRepository.class);

        configService = beanFactory.getBean(ConfigService.class);
        transactionService = beanFactory.getBean(TransactionService.class);

        modelViewMapper = beanFactory.getBean(ModelViewMapper.class);

        xmlManager = beanFactory.getBean(XmlManager.class);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("GET {}", Utils.getFullUrl(req));

        final TransactionAction transactionAction = extractParamTransactionAction(req);
        final Long transactionId = extractParamLong(req, PARAM_TRANSACTION_ID);
        if((transactionAction != null) && (transactionId != null)) {
            final Transaction transaction = transactionRepository.findOne(transactionId);
            if(transaction != null) {
                switch(transactionAction) {
                    case MOVE_EARLIER:
                    case MOVE_LATER:
                        transactionService.performTimeAction(transaction, transactionAction);
                        break;

                    case DELETE:
                        transactionService.remove(transaction);
                        break;
                }
            }
            Utils.httpRedirect(resp, req.getContextPath() + "/");
            return;
        }

        final AccountingView accountingView = buildAccountingView();
        final String html = xmlManager.transform(AccountingView.class, accountingView, "xsl/root.xsl");
        resp.getWriter().println(html);
    }

    private AccountingView buildAccountingView() {
        final AccountingView accountingView = new AccountingView();

        final Config config = configService.get();

        final Iterable<Account> accounts = accountRepository.findAll();
        final List<AccountView> accountViews = StreamSupport.stream(accounts.spliterator(), false)
                .map(modelViewMapper::mapAccount)
                .collect(Collectors.toList());
        accountingView.setAccounts(accountViews);

        final Iterable<Category> categories = categoryRepository.findAll();
        final List<CategoryView> categoryViews = StreamSupport.stream(categories.spliterator(), false)
                .map(modelViewMapper::mapCategory)
                .collect(Collectors.toList());
        accountingView.setCategories(categoryViews);

        final long from = calculateMonthStartTime(System.currentTimeMillis());
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(from);
        calendar.add(Calendar.MONTH, 1);
        final long to = calendar.getTimeInMillis();

        final Map<Long, Double> accountsRunningTotal = StreamSupport.stream(accounts.spliterator(), false)
                .collect(Collectors.toMap(
                        Account::getId,
                        account -> 0.0,
                        Utils.obtainNoDuplicatesFunction(),
                        HashMap::new // allowing null key for total
                ));
        accountsRunningTotal.put(null, 0.0);

        final List<TransactionView> transactionViews = new ArrayList<>();
        final Map<Long, Map<Long, Double>> summary = new LinkedHashMap<>();
        accountingView.setAccountsRunningTotalBefore(null);
        accountingView.setAccountsRunningTotalAfter(null);
        transactionRepository.findAll()
                .forEach(transaction -> {
                    if((transaction.getTime() >= from) && (accountingView.getAccountsRunningTotalBefore() == null)) {
                        final RunningTotalView runningTotalView = generateRunningTotalView(accountsRunningTotal);
                        accountingView.setAccountsRunningTotalBefore(runningTotalView);
                    }

                    final double total = transaction.getOperations()
                            .stream()
                            .mapToDouble(operation -> {
                                final Account account = operation.getAccount();
                                accountsRunningTotal.compute(account.getId(), (id, amount) -> amount + operation.getAmount());
                                return convert(account.getCurrency(), config.getCurrencyDefault(), operation.getAmount());
                            })
                            .sum();
                    accountsRunningTotal.compute(null, (id, amount) -> amount + total);

                    if((transaction.getTime() >= from) && (transaction.getTime() <= to)) {
                        final TransactionView transactionView = modelViewMapper.mapTransaction(transaction);
                        transactionView.setTotal(total);
                        transactionViews.add(transactionView);
                    }

                    if((transaction.getTime() > to) && (accountingView.getAccountsRunningTotalAfter() == null)) {
                        final RunningTotalView runningTotalView = generateRunningTotalView(accountsRunningTotal);
                        accountingView.setAccountsRunningTotalAfter(runningTotalView);
                    }

                    final Map<Long, Double> summaryItems = summary.computeIfAbsent(
                            calculateMonthStartTime(transaction.getTime()),
                            time -> StreamSupport.stream(categories.spliterator(), false)
                                    .collect(Collectors.toMap(
                                            Category::getId,
                                            category -> 0.0,
                                            Utils.obtainNoDuplicatesFunction(),
                                            () -> new LinkedHashMap<>()
                                    ))
                    );
                    summaryItems.compute(transaction.getCategory().getId(), (id, amount) -> amount + total);
                });
        if(accountingView.getAccountsRunningTotalAfter() == null) {
            final RunningTotalView runningTotalView = generateRunningTotalView(accountsRunningTotal);
            accountingView.setAccountsRunningTotalAfter(runningTotalView);
        }
        accountingView.setTransactions(transactionViews);

        final List<SummaryView> summaryViews = summary
                .entrySet()
                .stream()
                .map(entry -> {
                    final SummaryView summaryView = new SummaryView();
                    summaryView.setTime(entry.getKey());

                    final List<SummaryItemView> items = entry.getValue()
                            .entrySet()
                            .stream()
                            .map(itemEntry -> {
                                final SummaryItemView summaryItemView = new SummaryItemView();
                                summaryItemView.setName(categoryRepository.findOne(itemEntry.getKey()).getName());
                                summaryItemView.setAmount(itemEntry.getValue());
                                return summaryItemView;
                            })
                            .collect(Collectors.toList());
                    summaryView.setItems(items);

                    return summaryView;
                })
                .collect(Collectors.toList());
        accountingView.setSummary(summaryViews);

        return accountingView;
    }

    private long calculateMonthStartTime(final long time) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);

        Utils.setCalendarDayStart(calendar);
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        return calendar.getTimeInMillis();
    }

    private double convert(final Currency from, final Currency to, final double amount) {
        final double amountBase = amount * from.getValue();
        return amountBase / to.getValue();
    }

    private RunningTotalView generateRunningTotalView(final Map<Long, Double> totals) {
        final RunningTotalView runningTotalView = new RunningTotalView();

        final Collection<OperationView> operationViews = totals.entrySet().stream()
                .filter(entry -> entry.getKey() != null)
                .map(entry -> {
                    final OperationView operationView = new OperationView();
                    operationView.setId(entry.getKey());
                    operationView.setAmount(entry.getValue());
                    return operationView;
                })
                .collect(Collectors.toList());
        runningTotalView.setItems(operationViews);

        runningTotalView.setTotal(totals.get(null));

        return runningTotalView;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("POST {}", Utils.getFullUrl(req));

        final TransactionAction transactionAction = extractParamTransactionAction(req);
        if(transactionAction == TransactionAction.ADD) {
            doPostTransactionAdd(req);
        }

        Utils.httpRedirect(resp, req.getContextPath() + "/");
    }

    private void doPostTransactionAdd(final HttpServletRequest req) {
        final Long time = extractParamDate(req);
        if(time == null) {
            return;
        }

        final Long categoryId = extractParamLong(req, PARAM_CATEGORY);
        if(categoryId == null) {
            return;
        }

        final Category category = categoryRepository.findOne(categoryId);
        if(category == null) {
            return;
        }

        final String comment = req.getParameter(PARAM_COMMENT);

        final Map<Long, Double> operations = new HashMap<>();
        accountRepository.findAll()
                .forEach(account -> {
                    final Double amount = extractParamAccountValue(req, account.getId());
                    if(amount != null) {
                        operations.put(account.getId(), amount);
                    }
                });

        transactionService.add(time, category, comment, operations);
    }

    private TransactionAction extractParamTransactionAction(final HttpServletRequest req) {
        return TransactionAction.parse(req.getParameter(PARAM_ACTION));
    }

    private Long extractParamLong(final HttpServletRequest req, final String paramName) {
        final String param = req.getParameter(paramName);

        if(param != null) {
            try {
                return Long.decode(param);
            } catch (NumberFormatException ignored) {
            }
        }

        return null;
    }

    private Long extractParamDate(final HttpServletRequest req) {
        final String param = req.getParameter(PARAM_DATE);
        return PARAM_DATE_FORMATS
                .stream()
                .map(dateParser -> dateParser.parse(param))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private Double extractParamAccountValue(final HttpServletRequest req, final long acountId) {
        final String paramName = String.format(PARAM_ACCOUNT_FORMAT, acountId);
        final String param = req.getParameter(paramName);

        if(param != null) {
            try {
                return Double.valueOf(param);
            } catch (NumberFormatException ignored) {
            }
        }

        return null;
    }

}
