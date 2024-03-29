<%@ include file="/include.jsp" %>
<%@ page import="jetbrains.buildServer.buildTriggers.astronomical.AstronomicalTriggerUtil" %>
<%@ page import="jetbrains.buildServer.buildTriggers.astronomical.controller.AstronomicalTriggerPropertiesController" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" type="jetbrains.buildServer.controllers.BasePropertiesBean" scope="request"/>

<tr class="noBorder" >
    <td colspan="2">
        <em>
            Astronomical build trigger will add a build to the queue on a daily basis whenever the selected astronomical
            event occurs for the specified location (defined using latitude and longitude coordinates). This trigger
            leverages a free API offered by <a href="https://sunrise-sunset.org/api" target="_blank">Sunrise-Sunset</a>.
        </em>
    </td>
</tr>
<tr class="groupingTitle">
    <td colspan="2">Location</td>
</tr>
<tr class="noBorder">
    <td class="_label noBorder" style="vertical-align: top;">
        <label for="<%=AstronomicalTriggerUtil.LATITUDE_PARAM%>">Latitude:<l:star/></label>
    </td>
    <td class="noBorder">
        <props:textProperty name="<%=AstronomicalTriggerUtil.LATITUDE_PARAM%>" onchange="BS.AstronomicalTrigger.valuesChanged()" />
        <span class="error" id="error_<%=AstronomicalTriggerUtil.LATITUDE_PARAM%>"></span>
    </td>
</tr>
<tr class="noBorder">
    <td class="_label noBorder" style="vertical-align: top;">
        <label for="<%=AstronomicalTriggerUtil.LONGITUDE_PARAM%>">Longitude:<l:star/></label>
    </td>
    <td class="noBorder">
        <props:textProperty name="<%=AstronomicalTriggerUtil.LONGITUDE_PARAM%>" onchange="BS.AstronomicalTrigger.valuesChanged()" />
        <span class="error" id="error_<%=AstronomicalTriggerUtil.LONGITUDE_PARAM%>"></span>
    </td>
</tr>
<tr class="groupingTitle">
    <td colspan="2">Astronomical Event</td>
</tr>
<tr class="noBorder">
    <td class="_label noBorder">
        <label for="<%=AstronomicalTriggerUtil.EVENT_PARAM%>">Event:<l:star/></label>
    </td>
    <td class="noBorder">
        <props:selectProperty name="<%=AstronomicalTriggerUtil.EVENT_PARAM%>" enableFilter="true" onchange="BS.AstronomicalTrigger.valuesChanged()">
            <props:option value="sunrise">Sunrise</props:option>
            <props:option value="sunset">Sunset</props:option>
            <props:option value="solar_noon">Solar Noon</props:option>
            <props:option value="civil_twilight_begin">Civil Twilight - Begin</props:option>
            <props:option value="civil_twilight_end">Civil Twilight - End</props:option>
            <props:option value="nautical_twilight_begin">Nautical Twilight - Begin</props:option>
            <props:option value="nautical_twilight_end">Nautical Twilight - End</props:option>
            <props:option value="astronomical_twilight_begin">Astronomical Twilight - Begin</props:option>
            <props:option value="astronomical_twilight_end">Astronomical Twilight - End</props:option>
        </props:selectProperty>
    </td>
</tr>
<tr class="noBorder">
    <td class="_label noBorder" style="vertical-align: top;">
        <label for="<%=AstronomicalTriggerUtil.OFFSET_PARAM%>">Offset (minutes):<l:star/></label>
    </td>
    <td class="noBorder">
        <props:textProperty name="<%=AstronomicalTriggerUtil.OFFSET_PARAM%>" onchange="BS.AstronomicalTrigger.valuesChanged()" />
        <span class="error" id="error_<%=AstronomicalTriggerUtil.OFFSET_PARAM%>"></span>
        <span class="smallNote">Defining a positive or negative offset will allow the triggering of the build a specified
            number of minutes before or after the selected astronomical event.</span>
    </td>
</tr>
<tr class="noBorder">
    <td class="_label noBorder"></td>
    <td class="noBorder">
        <forms:button id="calculateTriggerTime" onclick="BS.AstronomicalTrigger.calculateTriggerTime(); return false;">Calculate next trigger time</forms:button>

        <div id="calculateTriggerTimeResult" style="display: none; padding-top: 10px;">
            <span class="title">Upcoming trigger times:</span>
            <br>
            <span class="result"></span>
        </div>
    </td>
</tr>

<style>
    #calculateTriggerTimeResult .title {
        font-weight: bolder;
    }

    #calculateTriggerTimeResult .result {
        font-size: 90%;
    }
</style>

<script>
    BS.AstronomicalTrigger = {
        valuesChanged: function() {
            let resultContainer = $j("#calculateTriggerTimeResult");
            resultContainer.hide();
        },

        calculateTriggerTime: function() {
            let resultContainer = $j("#calculateTriggerTimeResult");
            resultContainer.hide();

            BS.ajaxRequest(window["base_uri"] + "/checkAstronomicalTriggerTime.html", {
                parameters: {
                    latitude: $("<%=AstronomicalTriggerUtil.LATITUDE_PARAM%>").getValue(),
                    longitude: $("<%=AstronomicalTriggerUtil.LONGITUDE_PARAM%>").getValue(),
                    event: $("<%=AstronomicalTriggerUtil.EVENT_PARAM%>").getValue(),
                    offset: $("<%=AstronomicalTriggerUtil.OFFSET_PARAM%>").getValue()
                },
                onSuccess: function(response) {
                    let xmlDoc = $(response.responseXML.documentElement);
                    let timeElements = xmlDoc.querySelectorAll("times *");
                    let resultContainer = $j("#calculateTriggerTimeResult");
                    let resultElement = $j("#calculateTriggerTimeResult span.result");
                    let html = '';

                    if (timeElements.length > 0) {
                        for (let i = 0; i < timeElements.length; i ++) {
                            let date = new Date(timeElements[i].getAttribute("value") + "Z");
                            let value = date.toLocaleString(Intl.DateTimeFormat().resolvedOptions().locale, {
                                timeZone: 'UTC',
                                timeZoneName: 'short',
                                year: 'numeric',
                                month: 'long',
                                day: 'numeric',
                                hour: 'numeric',
                                minute: 'numeric',
                                second: 'numeric',
                                hour12: false
                            });

                            html += `\${value}`;

                            value = date.toLocaleString(Intl.DateTimeFormat().resolvedOptions().locale, {
                                timeZone: Intl.DateTimeFormat().resolvedOptions().timeZone,
                                timeZoneName: 'short',
                                year: 'numeric',
                                month: 'long',
                                day: 'numeric',
                                hour: 'numeric',
                                minute: 'numeric',
                                second: 'numeric',
                                hour12: false
                            });

                            html += ` (\${value})<br/>`;
                        }
                    } else {
                        html += "There are no upcoming events matching the specified criteria.";
                    }

                    resultElement.html(html);
                    resultContainer.show();
                }
            });
        }
    }
</script>

<bs:dialog dialogId="calculateTimeDialog" title="Calculate Astronomical Trigger Time" closeCommand="BS.TestConnectionDialog.close();"
           closeAttrs="showdiscardchangesmessage='false'">
    <div id="testConnectionStatus"></div>
    <div id="testConnectionDetails" class="mono"></div>
</bs:dialog>