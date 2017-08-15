<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:XslFormatHelper="com.lonebytesoft.hamster.accounting.util.XslFormatHelper"
                exclude-result-prefixes="XslFormatHelper">

    <xsl:template match="block" mode="summary-item">
        <table class="summary">
            <tr>
                <th colspan="2">
                    <xsl:value-of select="XslFormatHelper:formatDate($format-helper, time, $summary-date-format)"/>
                </th>
            </tr>
            <xsl:apply-templates select="items/item" mode="summary-item-row"/>
        </table>
    </xsl:template>

    <xsl:template match="item" mode="summary-item-row">
        <tr>
            <td><xsl:value-of select="name"/></td>
            <td>
                <xsl:call-template name="amount">
                    <xsl:with-param name="amount" select="amount"/>
                </xsl:call-template>
            </td>
        </tr>
    </xsl:template>

</xsl:stylesheet>
