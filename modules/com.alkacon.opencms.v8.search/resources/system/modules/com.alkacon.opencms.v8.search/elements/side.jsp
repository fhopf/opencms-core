<%@page session="false" taglibs="c,cms,fmt,fn" %>
<fmt:setLocale value="${cms.locale}" />
<fmt:bundle basename="com.alkacon.opencms.v8.search.frontend">

<cms:formatter var="content" val="value">
	<c:set var="formlink"><cms:link>${content.filename}</cms:link></c:set>
	<div class="box ${cms.element.settings.boxschema}">
	<c:choose>
		<c:when test="${fn:endsWith(formlink, content.filename)}">
			<h4>Search Error</h4>
			<div class="boxbody">No detail page for the search has been defined in the sitemap.</div>
		</c:when>
		<c:otherwise>
			<h4>${value.Title}</h4>
		
			<div class="boxbody">
			<form id="searchFormSide" name="searchForm" action="${formlink}" method="post">
				<input type="hidden" name="searchaction" value="search" />
				<input type="hidden" name="searchPage" value="1" />
				<input type="text" name="query" value="${search.query}" />
				<input type="submit" name="submit" value="<fmt:message key="search.button" />" />
			</form>
			</div>
		</c:otherwise>
	</c:choose>
	</div>
</cms:formatter>
</fmt:bundle>