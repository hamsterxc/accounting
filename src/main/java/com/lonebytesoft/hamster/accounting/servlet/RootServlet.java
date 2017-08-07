package com.lonebytesoft.hamster.accounting.servlet;

import com.lonebytesoft.hamster.accounting.ApplicationContextListener;
import com.lonebytesoft.hamster.accounting.ModelViewMapper;
import com.lonebytesoft.hamster.accounting.dao.AccountDao;
import com.lonebytesoft.hamster.accounting.dao.CategoryDao;
import com.lonebytesoft.hamster.accounting.dao.ConfigDao;
import com.lonebytesoft.hamster.accounting.dao.CurrencyDao;
import com.lonebytesoft.hamster.accounting.dao.OperationDao;
import com.lonebytesoft.hamster.accounting.dao.TransactionDao;
import com.lonebytesoft.hamster.accounting.model.Account;
import com.lonebytesoft.hamster.accounting.model.Category;
import com.lonebytesoft.hamster.accounting.model.Config;
import com.lonebytesoft.hamster.accounting.model.Currency;
import com.lonebytesoft.hamster.accounting.model.Operation;
import com.lonebytesoft.hamster.accounting.model.Transaction;
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
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    private AccountDao accountDao;
    private CategoryDao categoryDao;
    private CurrencyDao currencyDao;
    private OperationDao operationDao;
    private TransactionDao transactionDao;
    private ConfigDao configDao;

    private TransactionService transactionService;

    private ModelViewMapper modelViewMapper;

    private XmlManager xmlManager;

    @Override
    public void init(ServletConfig config) throws ServletException {
        logger.info("Initializing servlet");

        super.init(config);

        final BeanFactory beanFactory = (BeanFactory) config.getServletContext()
                .getAttribute(ApplicationContextListener.ATTRIBUTE_APPLICATION_CONTEXT);

        accountDao = beanFactory.getBean("accountDao", AccountDao.class);
        categoryDao = beanFactory.getBean("categoryDao", CategoryDao.class);
        currencyDao = beanFactory.getBean("currencyDao", CurrencyDao.class);
        operationDao = beanFactory.getBean("operationDao", OperationDao.class);
        transactionDao = beanFactory.getBean("transactionDao", TransactionDao.class);
        configDao = beanFactory.getBean("configDao", ConfigDao.class);

        transactionService = beanFactory.getBean("transactionService", TransactionService.class);

        modelViewMapper = beanFactory.getBean("modelViewMapper", ModelViewMapper.class);

        xmlManager = beanFactory.getBean("xmlManager", XmlManager.class);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("GET {}", Utils.getFullUrl(req));

        final TransactionAction transactionAction = extractParamTransactionAction(req);
        final Long transactionId = extractParamLong(req, PARAM_TRANSACTION_ID);
        if((transactionAction != null) && (transactionId != null)) {
            final Optional<Transaction> transactionOptional = transactionDao.get(transactionId);
            transactionOptional.ifPresent(transaction -> {
                switch(transactionAction) {
                    case MOVE_EARLIER:
                    case MOVE_LATER:
                        transactionService.performTimeAction(transaction, transactionAction);
                        break;

                    case DELETE:
                        transactionService.remove(transaction);
                        break;
                }
            });
            Utils.httpRedirect(resp, req.getContextPath() + "/");
            return;
        }

        final AccountingView accountingView = buildAccountingView();
        final String html = xmlManager.transform(AccountingView.class, accountingView, "xsl/root.xsl");
        resp.getWriter().println(html);
    }

    private AccountingView buildAccountingView() {
        final AccountingView accountingView = new AccountingView();

        final Map<Long, Account> accounts = accountDao.getAll();
        final Map<Long, Category> categories = categoryDao.getAll();
        final Map<Long, Currency> currencies = currencyDao.getAll();
        final Config config = configDao.get();

        final List<AccountView> accountViews = accounts
                .values()
                .stream()
                .map(account -> modelViewMapper.mapAccount(account, currencies))
                .collect(Collectors.toList());
        accountingView.setAccounts(accountViews);

        final List<CategoryView> categoryViews = categories
                .values()
                .stream()
                .map(modelViewMapper::mapCategory)
                .collect(Collectors.toList());
        accountingView.setCategories(categoryViews);

        final long from = calculateMonthStartTime(System.currentTimeMillis());
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(from);
        calendar.add(Calendar.MONTH, 1);
        final long to = calendar.getTimeInMillis();

        final Map<Long, Double> accountsRunningTotal = accounts.keySet().stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        accountId -> 0.0,
                        Utils.obtainNoDuplicatesFunction(),
                        HashMap::new // allowing null key for total
                ));
        accountsRunningTotal.put(null, 0.0);

        final List<TransactionView> transactionViews = new ArrayList<>();
        final Map<Long, Map<Long, Double>> summary = new LinkedHashMap<>();
        accountingView.setAccountsRunningTotalBefore(null);
        accountingView.setAccountsRunningTotalAfter(null);
        transactionDao.getAll()
                .forEach(transaction -> {
                    final Collection<Operation> operations = operationDao.get(transaction.getId());

                    if((transaction.getTime() >= from) && (accountingView.getAccountsRunningTotalBefore() == null)) {
                        final RunningTotalView runningTotalView = generateRunningTotalView(accountsRunningTotal);
                        accountingView.setAccountsRunningTotalBefore(runningTotalView);
                    }

                    final double total = operations
                            .stream()
                            .mapToDouble(operation -> {
                                final Account account = accounts.computeIfAbsent(operation.getAccountId(), Utils.obtainNotFoundFunction("account"));
                                accountsRunningTotal.compute(account.getId(), (id, amount) -> amount + operation.getAmount());
                                return convert(currencies, account.getCurrencyId(), config.getCurrencyIdDefault(), operation.getAmount());
                            })
                            .sum();
                    accountsRunningTotal.compute(null, (id, amount) -> amount + total);

                    if((transaction.getTime() >= from) && (transaction.getTime() <= to)) {
                        final TransactionView transactionView = modelViewMapper.mapTransaction(transaction, operations, accounts, categories);
                        transactionView.setTotal(total);
                        transactionViews.add(transactionView);
                    }

                    if((transaction.getTime() > to) && (accountingView.getAccountsRunningTotalAfter() == null)) {
                        final RunningTotalView runningTotalView = generateRunningTotalView(accountsRunningTotal);
                        accountingView.setAccountsRunningTotalAfter(runningTotalView);
                    }

                    final Map<Long, Double> summaryItems = summary.computeIfAbsent(
                            calculateMonthStartTime(transaction.getTime()),
                            time -> categories
                                    .keySet()
                                    .stream()
                                    .collect(Collectors.toMap(
                                            Function.identity(),
                                            id -> 0.0,
                                            Utils.obtainNoDuplicatesFunction(),
                                            () -> new LinkedHashMap<>()
                                    ))
                    );
                    summaryItems.compute(transaction.getCategoryId(), (id, amount) -> amount + total);
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
                                summaryItemView.setName(categories.get(itemEntry.getKey()).getName());
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

    private double convert(final Map<Long, Currency> currencies, final long from, final long to, final double amount) {
        final Currency currencyFrom = currencies.computeIfAbsent(from, Utils.obtainNotFoundFunction("currency"));
        final Currency currencyTo = currencies.computeIfAbsent(to, Utils.obtainNotFoundFunction("currency"));

        final double amountBase = amount * currencyFrom.getValue();
        return amountBase / currencyTo.getValue();
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

        final Map<Long, Category> categories = categoryDao.getAll();
        if(!categories.containsKey(categoryId)) {
            return;
        }

        final String comment = req.getParameter(PARAM_COMMENT);

        final Map<Long, Double> operations = new HashMap<>();
        accountDao.getAll()
                .keySet()
                .forEach(accountId -> {
                    final Double amount = extractParamAccountValue(req, accountId);
                    if(amount != null) {
                        operations.put(accountId, amount);
                    }
                });

        transactionService.add(time, categoryId, comment, operations);
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
