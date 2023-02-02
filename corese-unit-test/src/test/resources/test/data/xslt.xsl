<?xml version="1.0"?>


<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">


    <xsl:template match="*|@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="*|@*|text()" />
        </xsl:copy>
    </xsl:template>
    

</xsl:stylesheet>
