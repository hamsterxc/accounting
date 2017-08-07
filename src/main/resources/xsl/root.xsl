<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:XslFormatHelper="com.lonebytesoft.hamster.accounting.util.XslFormatHelper"
                exclude-result-prefixes="XslFormatHelper">

    <xsl:output omit-xml-declaration="yes" method="html"/>

    <xsl:import href="root-main-header.xsl"/>
    <xsl:import href="root-main-row.xsl"/>
    <xsl:import href="root-main-total.xsl"/>
    <xsl:import href="root-main-add.xsl"/>
    <xsl:import href="root-summary.xsl"/>

    <xsl:variable name="transaction-date-format" select="'dd.MM.yyyy'"/>
    <xsl:variable name="summary-date-format" select="'MMMM yyyy'"/>
    <xsl:variable name="format-helper" select="XslFormatHelper:getInstance()"/>

    <xsl:template match="/accounting">
        <html>
            <head>
                <style>
                    <![CDATA[
                    table, tr, th, td {
                        border: 1px solid black;
                        border-collapse: collapse;
                    }
                    table {
                        margin: 10px 10px;
                    }
                    td, th {
                        padding: 5px;
                    }

                    table.summary {
                        display: inline-table;
                    }

                    span.warn {
                        color: red;
                    }

                    a.action {
                        text-decoration: none;
                    }
                    ]]>
                </style>
                <title>Accounting</title>
            </head>
            <body>
                <table>
                    <xsl:call-template name="main-header"/>
                    <xsl:call-template name="main-row-total">
                        <xsl:with-param name="total" select="accountsRunningTotalBefore"/>
                    </xsl:call-template>
                    <xsl:apply-templates select="transactions/transaction" mode="main-row-transaction"/>
                    <xsl:call-template name="main-row-total">
                        <xsl:with-param name="total" select="accountsRunningTotalAfter"/>
                    </xsl:call-template>
                    <xsl:call-template name="main-row-add"/>
                </table>
                <xsl:apply-templates select="summary/block" mode="summary-item"/>
            </body>
        </html>
    </xsl:template>

    <xsl:template name="amount">
        <xsl:param name="amount"/>
        <xsl:variable name="amountFormatted" select="format-number($amount, '#.##')"/>
        <xsl:choose>
            <xsl:when test="$amount &lt; 0">
                <span class="warn"><xsl:value-of select="$amountFormatted"/></span>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$amountFormatted"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
