<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : wlpfight.xsl
    Created on : den 14 juni 2008, 18:09
    Author     : racy
    Description:
        Purpose of transformation follows.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xml:space="preserve">
    <xsl:output method="html"/>

    <!-- TODO customize transformation rules 
         syntax recommendation http://www.w3.org/TR/xslt 
    -->
    <xsl:template match="/Fight">
        <html>
            <head>
                <title>
                    <xsl:value-of select="@na"/>
                </title>
            </head>
            <body>
                <h1>
                    <xsl:value-of select="@na"/>
                </h1>
                      Merged:
                      <xsl:value-of select="Merg"/>
                <br/>
                        Duration:
                <xsl:value-of select="Du"/>
                <br/>
                        Active Duration:
                <xsl:value-of select="AcDu"/>
                <br/>
                <table bgcolor="#DDDDDD">
                    <tr><th>Name</th><th bgcolor="#EE0000">Damage</th><th bgcolor="#00EE00">Healing</th></tr>
                    <xsl:for-each select="Par">
                        <tr>
                            <td bgcolor="#EEEEEE"><xsl:value-of select="@na"/></td>
                            <td bgcolor="#EE0000"><xsl:value-of select="ToDa/Am"/></td>
                            <td bgcolor="#00EE00"><xsl:value-of select="ToHe/Am"/></td>
                        </tr>
                    </xsl:for-each>
                </table>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>
