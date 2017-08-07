<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template name="main-row-add">
        <form method="post">
            <input type="hidden" name="action" value="add"/>
            <tr>
                <td>
                    <input type="submit" value="Add"/>
                </td>
                <td>
                    <input type="text" name="date" placeholder="31.12[.[20]12]" size="10"/>
                </td>
                <xsl:apply-templates select="/accounting/accounts/account" mode="main-cell-add-account"/>
                <td/>
                <td>
                    <select name="category">
                        <xsl:apply-templates select="/accounting/categories/category" mode="main-cell-add-category"/>
                    </select>
                </td>
                <td>
                    <input type="text" name="comment" placeholder="Comment" size="30"/>
                </td>
            </tr>
        </form>
    </xsl:template>

    <xsl:template match="account" mode="main-cell-add-account">
        <td>
            <input type="text" placeholder="999.99" size="10">
                <xsl:attribute name="name">
                    <xsl:text>account</xsl:text>
                    <xsl:value-of select="id"/>
                </xsl:attribute>
            </input>
        </td>
    </xsl:template>

    <xsl:template match="category" mode="main-cell-add-category">
        <option>
            <xsl:attribute name="value">
                <xsl:value-of select="id"/>
            </xsl:attribute>
            <xsl:value-of select="name"/>
        </option>
    </xsl:template>

</xsl:stylesheet>
