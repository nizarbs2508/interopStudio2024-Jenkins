<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:cml="http://www.xml-cml.org/schema"
	xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
	xmlns="http://www.w3.org/1999/xhtml"
	xmlns:dc="http://purl.org/dc/elements/1.1/"
	xmlns:gvr="http://validationreport.gazelle.ihe.net/" version="2.0">

	<xsl:param name="title" />
	<xsl:template match="/">
		<xsl:apply-templates />
	</xsl:template>


	<xsl:template match="gvr:validationReport ">
		<html>
			<head>
				<title>schematron failed unit tests</title>
				<!-- CSS only -->
				<link
					href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css"
					rel="stylesheet"
					integrity="sha384-EVSTQN3/azprG1Anm3QDgpJLIm9Nao0Yz1ztcQTwFspd3yD65VohhpuuCOmLASjC"
					crossorigin="anonymous" />
				<!-- JavaScript Bundle with Popper -->
				<script
					src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/js/bootstrap.bundle.min.js"
					integrity="sha384-MrcW6ZMFYlzcLA8Nl+NtUVF0sA7MsXsP1UyJoMp4YLEuNSfAP+JcXn/tWtIaxVXM"
					crossorigin="anonymous"></script>
			</head>
			<body style="background-color:#B0C4DE;">
				<div style="margin:50px">
					<h1 class="display-5">
						<xsl:value-of
							select="/gvr:validationReport/gvr:validationOverview/gvr:validationServiceName" />
						-
						<xsl:value-of
							select="/gvr:validationReport/gvr:validationOverview/gvr:validatorID" />
						:
						<span style="color: #ff0000; background-color: #ffff00;">
							<xsl:value-of select="@result" />
						</span>
					</h1>
					<table class="table table-striped table-hover">

						<tbody>
							<xsl:apply-templates />

						</tbody>
					</table>
				</div>
			</body>
		</html>
	</xsl:template>

	<xsl:template match="gvr:subReport">


		<tr>
			<td VALIGN="TOP">
				<hr />
				<xsl:value-of select="@name" />
			</td>

			<td class=".small">

				<hr />
				<br />
				<xsl:value-of select="@subReportResult" />
				<table class="table table-striped table-hover">
					<xsl:apply-templates />
				</table>
				<hr />
			</td>

		</tr>

	</xsl:template>

	<xsl:template match="gvr:constraint[@severity != 'INFO']">


		<tr>
			<td VALIGN="MIDDLE">
				<span style="color: #ff0000; background-color: #ffff00;">
					<br />
					<xsl:value-of select="@severity" />
					<br />
				</span>
			</td>

			<td color="red">
				<hr />
				<B>
					<xsl:value-of select="@constraintID" />
				</B>
				<br />
				<xsl:value-of select="gvr:constraintDescription" />

				<br />
				<xsl:value-of select="gvr:locationInValidatedObject" />
				<hr />
			</td>
		</tr>

	</xsl:template>

	<xsl:template match="*">
		<!-- drop these -->
	</xsl:template>

	<xsl:template name="break">
		<xsl:param name="text" select="string(.)" />
		<xsl:choose>
			<xsl:when test="contains($text, '&#xa;')">
				<xsl:value-of select="substring-before($text, '&#xa;')" />
				<br />
				<xsl:call-template name="break">
					<xsl:with-param name="text"
						select="substring-after($text, '&#xa;')" />
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$text" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>
