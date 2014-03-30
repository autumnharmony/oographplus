<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/graph">
        <process>
            <xsl:apply-templates/>
        </process>
    </xsl:template>

    <xsl:template match="//node">

        <xsl:choose>

            <xsl:when test="current()/@type = 'Procedure'">
                <method>
                    <xsl:attribute name="id">
                        <xsl:value-of select="string(current()/@id)"></xsl:value-of>

                    </xsl:attribute>

                </method>
            </xsl:when>

            <xsl:when test="current()/@type = 'Process'">
                <method>
                    <xsl:attribute name="id">
                        <xsl:value-of select="string(current()/@id)"></xsl:value-of>

                    </xsl:attribute>

                    <xsl:choose>
                        <xsl:when test="(//link[@from = string(current()/@id)])">


                            <xsl:for-each select="//link[@from = string(current()/@id)]">
                                <xsl:element name="send">


                                    <xsl:attribute name="id">
                                        <xsl:value-of
                                                select="string(@id)"></xsl:value-of>
                                    </xsl:attribute>
                                    <xsl:attribute name="port">
                                        <xsl:value-of
                                                select="substring-before(string(current()/@to),':')"></xsl:value-of>
                                    </xsl:attribute>


                                </xsl:element>
                            </xsl:for-each>

                        </xsl:when>

                        <xsl:otherwise>
                            <xsl:if test="(//link[@to = string(current()/@id)])">

                                <xsl:for-each select="//link[@to = string(current()/@id)]">
                                    <xsl:element name="send">


                                        <xsl:attribute name="id">
                                            <xsl:value-of
                                                    select="string(current()/@id)"></xsl:value-of>
                                        </xsl:attribute>
                                        <xsl:attribute name="port">
                                            <xsl:value-of
                                                    select="substring-before(string(current()/@from),':')"></xsl:value-of>
                                        </xsl:attribute>


                                    </xsl:element>
                                </xsl:for-each>


                            </xsl:if>
                        </xsl:otherwise>
                    </xsl:choose>
                </method>
            </xsl:when>

            <xsl:when test="current()/@type = 'Client' or current()/@type = 'Server'">
                <xsl:element name="port">
                    <xsl:attribute name="id">
                        <xsl:value-of select="substring-before(current()/@id,':')"></xsl:value-of>
                    </xsl:attribute>

                    <xsl:attribute name="channel">
                        <xsl:value-of select="substring-after(current()/@id,':')"></xsl:value-of>
                    </xsl:attribute>

                    <xsl:attribute name="type">
                        <xsl:choose>
                            <xsl:when test="current()/@type = 'Server'">srv</xsl:when>
                            <xsl:when test="current()/@type = 'Client'">cli</xsl:when>
                        </xsl:choose>
                    </xsl:attribute>

                    <xsl:for-each select="//link[@from=current()/@id]">
                        <xsl:element name="receive">
                            <xsl:attribute name="id">
                                <xsl:value-of select="current()/@id"/>
                            </xsl:attribute>
                            <xsl:attribute name="method">
                                <xsl:value-of select="current()/@to"/>
                            </xsl:attribute>

                        </xsl:element>
                    </xsl:for-each>
                </xsl:element>

            </xsl:when>

            <xsl:otherwise>
                <bad/>
                <xsl:apply-templates/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>