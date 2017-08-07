<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template name="main-row-total">
        <xsl:param name="total"/>
        <tr>
            <td/>
            <td/>
            <xsl:apply-templates select="/accounting/accounts/account" mode="main-cell-total">
                <xsl:with-param name="items" select="$total/items"/>
            </xsl:apply-templates>
            <th>
                <xsl:call-template name="amount">
                    <xsl:with-param name="amount" select="$total/total"/>
                </xsl:call-template>
            </th>
            <td/>
            <td/>
        </tr>
    </xsl:template>

    <xsl:template match="account" mode="main-cell-total">
        <xsl:param name="items"/>
        <xsl:variable name="item" select="$items/item[id=current()/id]"/>
        <th>
            <xsl:if test="$item">
                <xsl:call-template name="amount">
                    <xsl:with-param name="amount" select="$item/amount"/>
                </xsl:call-template>
            </xsl:if>
        </th>
    </xsl:template>

</xsl:stylesheet>
