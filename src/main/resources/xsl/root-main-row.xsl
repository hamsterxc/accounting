<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:XslFormatHelper="com.lonebytesoft.hamster.accounting.util.XslFormatHelper"
                exclude-result-prefixes="XslFormatHelper">

    <xsl:template match="transaction" mode="main-row-transaction">
        <tr>
            <td>
                <a class="action">
                    <xsl:attribute name="href">
                        <xsl:text>?transactionid=</xsl:text>
                        <xsl:value-of select="id"/>
                        <xsl:text>&amp;action=moveup</xsl:text>
                    </xsl:attribute>
                    <xsl:text>&#x2191;</xsl:text>
                </a>
                <xsl:text>&#160;</xsl:text>
                <a class="action">
                    <xsl:attribute name="href">
                        <xsl:text>?transactionid=</xsl:text>
                        <xsl:value-of select="id"/>
                        <xsl:text>&amp;action=movedown</xsl:text>
                    </xsl:attribute>
                    <xsl:text>&#x2193;</xsl:text>
                </a>
                <xsl:text>&#160;</xsl:text>
                <a class="action">
                    <xsl:attribute name="href">
                        <xsl:text>?transactionid=</xsl:text>
                        <xsl:value-of select="id"/>
                        <xsl:text>&amp;action=delete</xsl:text>
                    </xsl:attribute>
                    <span class="warn">X</span>
                </a>
            </td>
            <td>
                <xsl:value-of select="XslFormatHelper:formatDate($format-helper, time, $transaction-date-format)"/>
            </td>
            <xsl:apply-templates select="/accounting/accounts/account" mode="main-cell-operation">
                <xsl:with-param name="items" select="operations"/>
            </xsl:apply-templates>
            <td>
                <xsl:call-template name="amount">
                    <xsl:with-param name="amount" select="total"/>
                </xsl:call-template>
            </td>
            <td>
                <xsl:value-of select="category"/>
            </td>
            <td>
                <xsl:value-of select="comment"/>
            </td>
        </tr>
    </xsl:template>

    <xsl:template match="account" mode="main-cell-operation">
        <xsl:param name="items"/>
        <xsl:variable name="item" select="$items/operation[id=current()/id]"/>
        <td>
            <xsl:if test="$item">
                <xsl:call-template name="amount">
                    <xsl:with-param name="amount" select="$item/amount"/>
                </xsl:call-template>
            </xsl:if>
        </td>
    </xsl:template>

</xsl:stylesheet>
