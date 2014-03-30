<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template match="/graph">
        <xsl:variable name="channel">
            <xsl:value-of select="substring-after(string(//link/@id), string(':'))"></xsl:value-of>

        </xsl:variable>
        <assemble>

            <xsl:attribute name="id">
                <xsl:value-of select="current()/@id"></xsl:value-of>
            </xsl:attribute>
            <xsl:apply-templates></xsl:apply-templates>
            <xsl:element name="channel">
                <xsl:attribute name="id">
                    <xsl:value-of select="$channel"></xsl:value-of>
                </xsl:attribute>
            </xsl:element>
        </assemble>
    </xsl:template>
    <xsl:template match="node">
        <process>
            <xsl:attribute name="id">
                <xsl:value-of select="current()/@id"></xsl:value-of>
            </xsl:attribute>
            <xsl:apply-templates></xsl:apply-templates>
        </process>
    </xsl:template>


</xsl:stylesheet>