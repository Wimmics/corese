<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 version="1.0"
 xmlns:sparql="http://www.w3.org/2005/sparql-results#"

xmlns:xalan="http://xml.apache.org/xalan"
xmlns:server="xalan://fr.inria.corese.kgtool.print.XSLTQuery"

extension-element-prefixes="server"
>

<xsl:output method="xml" omit-xml-declaration="yes" indent='yes' />

<xsl:param name='engine' />


<xsl:variable name='query1'>
select * where {
?x c:FirstName 'John'
}
</xsl:variable> 

<xsl:variable name='query2'>
construct {
?x c:name 'John'
} 
where {
?x c:FirstName 'John'
}
</xsl:variable> 


<xsl:template match="/">
	<xsl:apply-templates />
</xsl:template>



<xsl:template match="body">

<xsl:variable name='res1'  select='server:sparql($engine, $query1)' />
<xsl:variable name='res2'  select='server:sparql($engine, $query2)' />

<xsl:copy>
<xsl:apply-templates select ='$res1'/>
<xsl:apply-templates select ='$res2'/>

<xsl:apply-templates select = "@* | node()" />
</xsl:copy>

</xsl:template>



<xsl:template match="node() | @*">
<xsl:copy>
	<xsl:apply-templates select = "@* | node()" />
</xsl:copy>
</xsl:template>




</xsl:stylesheet>