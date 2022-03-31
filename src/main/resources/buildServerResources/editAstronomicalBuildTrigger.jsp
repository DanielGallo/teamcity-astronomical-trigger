<%@ include file="/include.jsp" %>
<%@ page import="jetbrains.buildServer.buildTriggers.astronomical.AstronomicalTriggerUtil" %>
<%@ page import="jetbrains.buildServer.buildTriggers.astronomical.controller.AstronomicalTriggerPropertiesController" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="propertiesBean" type="jetbrains.buildServer.controllers.BasePropertiesBean" scope="request"/>

<tr class="noBorder" >
    <td colspan="2">
        <em>Astronomical build trigger will add a build to the queue on a daily basis whenever the selected astronomical
            event occurs for the specified location (defined using latitude and longitude coordinates).</em>
    </td>
</tr>

<tr class="noBorder" >
    <th><label for="<%=AstronomicalTriggerUtil.LATITUDE_PARAM%>">Latitude: <l:star/></label></th>
    <td>
        <props:textProperty name="<%=AstronomicalTriggerUtil.LATITUDE_PARAM%>"/>
        <span class="smallNote">
          The timings of astronomical events will be based on this latitude
      </span>
        <span class="error" id="error_<%=AstronomicalTriggerUtil.LATITUDE_PARAM%>"></span>
    </td>
</tr>

<tr class="noBorder" >
    <th><label for="<%=AstronomicalTriggerUtil.LONGITUDE_PARAM%>">Longitude: <l:star/></label></th>
    <td>
        <props:textProperty name="<%=AstronomicalTriggerUtil.LONGITUDE_PARAM%>"/>
        <span class="smallNote">
          The timings of astronomical events will be based on this longitude
      </span>
        <span class="error" id="error_<%=AstronomicalTriggerUtil.LONGITUDE_PARAM%>"></span>
    </td>
</tr>
