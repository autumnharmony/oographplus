<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template match="/graph">
        <channel>
            <xsl:attribute name="id">
                <xsl:value-of select="current()/@id"></xsl:value-of>
            </xsl:attribute>
            <xsl:apply-templates/>
        </channel>
    </xsl:template>

    <!--<xsl2:template match="link">-->
        <!--<xsl2:copy></xsl2:copy>-->
        <!--<xsl2:apply-templates/>-->
    <!--</xsl2:template>-->

    <xsl:template match="//node">

        <xsl:element name="state">
            <xsl:attribute name="id">
                <xsl:value-of select="current()/@id"></xsl:value-of>
            </xsl:attribute>
            <xsl:attribute name="type">
                <xsl:choose>
                    <xsl:when test="current()/@type = 'Server'">srv</xsl:when>
                    <xsl:when test="current()/@type = 'Client'">cli</xsl:when>
                </xsl:choose>

            </xsl:attribute>
            <xsl:if test="//link[@from = string(current()/@id)]">
                <xsl:element name="message">

                    <xsl:attribute name="state">
                        <xsl:value-of select="string(//link[@from = string(current()/@id)]/@to)"></xsl:value-of>
                    </xsl:attribute>
                    <xsl:attribute name="id">
                        <xsl:value-of select="string(//link[@from = string(current()/@id)]/@id)"></xsl:value-of>
                    </xsl:attribute>

                </xsl:element>
            </xsl:if>
            <xsl:apply-templates></xsl:apply-templates>
        </xsl:element>


    </xsl:template>

</xsl:stylesheet>