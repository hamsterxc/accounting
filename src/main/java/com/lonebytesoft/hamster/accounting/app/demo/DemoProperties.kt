package com.lonebytesoft.hamster.accounting.app.demo

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties("accounting.demo")
@Component
class DemoProperties {

    var count: DemoPropertiesCount = DemoPropertiesCount()
    var words: DemoPropertiesWords = DemoPropertiesWords()
    var probability: DemoPropertiesProbability = DemoPropertiesProbability()
    var transaction: DemoPropertiesTransaction = DemoPropertiesTransaction()

    class DemoPropertiesCount {
        var category: Int = 1
        var currency: Int = 1
        var account: Int = 1
        var transaction: Int = 0
    }

    class DemoPropertiesWords {
        var categoryNameMin: Int = 1
        var categoryNameMax: Int = 3
        var accountNameMin: Int = 1
        var accountNameMax: Int = 3
        var transactionCommentMin: Int = 1
        var transactionCommentMax: Int = 6
    }

    class DemoPropertiesProbability {
        var categoryVisible: Double = 0.8
        var accountVisible: Double = 0.8
        var transactionVisible: Double = 0.95
        var operationDifferentCurrency: Double = 0.4
        var operationActive: Double = 0.6
    }

    class DemoPropertiesTransaction {
        var dateRangeBack: String = "1y"
        var dateRangeForward: String = "0d"
        var operationCountMin: Int = 1
        var operationCountMax: Int = 1
        var operationAmountMin: Double = -1.0
        var operationAmountMax: Double = 1.0
    }

}
