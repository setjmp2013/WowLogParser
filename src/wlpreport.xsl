<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : wlpreport.xsl
    Created on : den 14 juni 2008, 17:31
    Author     : racy
    Description:
        Purpose of transformation follows.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html"/>

    <!-- TODO customize transformation rules 
         syntax recommendation http://www.w3.org/TR/xslt 
    -->
    <xsl:template match="/">
        <html>
            <head>
                <title>Fights</title>
            </head>
            <body>
                <xsl:for-each select="FightGroup">
                    <h1><xsl:value-of select="@name"/></h1>
                    <xsl:for-each select="FightReference">
                        <a>
                            <xsl:attribute name="href">
                                <xsl:value-of select="@filename"/>
                            </xsl:attribute>
                            <xsl:value-of select="@name"/><br/>
                        </a>
                    </xsl:for-each>
                </xsl:for-each>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>
