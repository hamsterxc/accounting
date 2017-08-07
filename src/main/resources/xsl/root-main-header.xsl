<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template name="main-header">
        <tr>
            <th/>
            <th>Date</th>
            <xsl:apply-templates select="accounts/account" mode="main-header-cell"/>
            <th>Total</th>
            <th>Category</th>
            <th>Comment</th>
        </tr>
    </xsl:template>

    <xsl:template match="account" mode="main-header-cell">
        <th>
            <xsl:value-of select="name"/>
            <xsl:text>, </xsl:text>
            <xsl:value-of select="currencySymbol"/>
        </th>
    </xsl:template>

</xsl:stylesheet>
