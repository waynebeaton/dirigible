/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company and Eclipse Dirigible contributors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-FileCopyrightText: 2021 SAP SE or an SAP affiliate company and Eclipse Dirigible contributors
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.dirigible.engine.odata2.sql;

import com.google.gson.Gson;
import org.apache.cxf.helpers.IOUtils;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.core.ep.feed.ODataDeltaFeedImpl;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static org.apache.olingo.odata2.api.commons.ODataHttpMethod.*;
import static org.junit.Assert.*;

public class ODataSQLProcessorTest extends AbstractSQLPropcessorTest {

    @Test
    public void testSQLProcessor() throws Exception {

        Response response = OData2RequestBuilder.createRequest(sf) //
                .segments("Cars") //
                .param("$top", "10") //
                .executeRequest(GET);
        String content = IOUtils.toString((InputStream) response.getEntity());
        assertEquals(200, response.getStatus());
        assertTrue(content.contains("BMW"));
        assertTrue(content.contains("Moskvitch"));
        assertTrue(content.contains("Lada"));
    }

    @Test
    public void testNotFoundXmlResponse() throws Exception {
        String nonExistingUUID = "77777777-7777-7777-7777-777777777777";
        Response response = OData2RequestBuilder.createRequest(sf) //
                .segments("Cars('" + nonExistingUUID + "')") //
                .accept("application/atom+xml").executeRequest(GET);
        assertNotNull(response);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void testNotFoundJsonResponse() throws Exception {
        String nonExistingUUID = "77777777-7777-7777-7777-777777777777";
        Response response = OData2RequestBuilder.createRequest(sf) //
                .segments("Cars('" + nonExistingUUID + "')") //
                .accept("application/atom+xml").executeRequest(GET);
        assertNotNull(response);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void testRequestEntity() throws Exception {
        Response response = OData2RequestBuilder.createRequest(sf) //
                .segments("Cars('639cac17-4cfd-4d94-b5d0-111fd5488423')") //
                .accept("application/atom+xml").executeRequest(GET);
        assertEquals(200, response.getStatus());
        ODataEntry resultEntry = retrieveODataEntry(response, "Cars");
        Map<String, Object> properties = resultEntry.getProperties();
        assertEquals("BMW", properties.get("Make"));
        assertEquals("530e", properties.get("Model"));
        assertEquals(2021, properties.get("Year"));
        assertEquals(67000.0d, properties.get("Price"));

        Calendar timestamp = (Calendar) properties.get("Updated");
        assertEquals(2021, timestamp.get(Calendar.YEAR));
        assertEquals(5, timestamp.get(Calendar.MONTH));
        assertEquals(7, timestamp.get(Calendar.DAY_OF_MONTH));

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testTop() throws Exception {

        Response response = OData2RequestBuilder.createRequest(sf) //
                .segments("Drivers")//
                .param("$top", "2") //
                .accept("application/json")//
                .executeRequest(GET);

        assertEquals(200, response.getStatus());

        String content = IOUtils.toString((InputStream) response.getEntity());
        Map jobj = new Gson().fromJson(content, Map.class);
        List elements = (List) ((Map) jobj.get("d")).get("results");
        assertEquals(2, elements.size());
    }

    @Test
    public void testCount() throws Exception {
        Response response = OData2RequestBuilder.createRequest(sf) //
                .segments("Cars", "$count") //
                .accept("application/json")//
                .executeRequest(GET);

        assertEquals(200, response.getStatus());

        ByteArrayInputStream bais = (ByteArrayInputStream) response.getEntity();
        assertEquals("7", IOUtils.toString(bais));
    }

    @Test
    public void testCreateEntity() throws Exception {

        String content = "{" //
                + "  \"d\": {" //
                + "    \"__metadata\": {" //
                + "      \"type\": \"org.eclipse.dirigible.engine.odata2.sql.entities.Car\"" //
                + "    }," //
                + "    \"Id\": \"3ab18d92-a574-45bb-a5e5-bcce38b7afb8\"," //
                + "    \"Make\": \"BMW\"," //
                + "    \"Model\": \"320i\"," //
                + "    \"Year\": 2021," //
                + "    \"Price\": 30000.0" //
                + " }" + "}";

        Response response = modifyingRequestBuilder(sf, content)//
                .segments("Cars") //
                .accept("application/json")//
                .content(content).param("content-type", "application/json")//
                .contentSize(content.length()).executeRequest(POST);
        assertEquals(201, response.getStatus());

        String res = IOUtils.toString((InputStream) response.getEntity());
        assertEquals("{" // 
                        + "\"d\":{" //
                        + "\"__metadata\":{" //
                        + "\"id\":\"http://localhost:8080/api/v1/Cars('3ab18d92-a574-45bb-a5e5-bcce38b7afb8')\"," //
                        + "\"uri\":\"http://localhost:8080/api/v1/Cars('3ab18d92-a574-45bb-a5e5-bcce38b7afb8')\"," //
                        + "\"type\":\"org.eclipse.dirigible.engine.odata2.sql.entities.Car\"" //
                        + "}," //
                        + "\"Id\":\"3ab18d92-a574-45bb-a5e5-bcce38b7afb8\"," //
                        + "\"Make\":\"BMW\"," //
                        + "\"Model\":\"320i\"," //
                        + "\"Year\":2021," //
                        + "\"Price\":\"30000.0\"," //
                        + "\"Updated\":null," //
                        + "\"Drivers\":{\"__deferred\":{\"uri\":\"http://localhost:8080/api/v1/Cars('3ab18d92-a574-45bb-a5e5-bcce38b7afb8')/Drivers\"}}," //
                        + "\"Owners\":{\"__deferred\":{\"uri\":\"http://localhost:8080/api/v1/Cars('3ab18d92-a574-45bb-a5e5-bcce38b7afb8')/Owners\"}}" //
                        + "}" //
                        + "}", //
                res);

    }

    @Test
    public void testReadEntity() throws Exception {

        String content = "{" //
                + "  \"d\": {" //
                + "    \"__metadata\": {" //
                + "      \"type\": \"org.eclipse.dirigible.engine.odata2.sql.entities.Car\"" //
                + "    }," //
                + "    \"Id\": \"3ab18d92-a574-45bb-a5e5-bcce38b7af11\"," //
                + "    \"Make\": \"BMW\"," //
                + "    \"Model\": \"320i\"," //
                + "    \"Year\": 2021," //
                + "    \"Price\": 30000.0" //
                + " }" + "}";

        modifyingRequestBuilder(sf, content)//
                .segments("Cars") //
//                .accept("application/json")//
                .content(content).param("content-type", "application/json")//
                .contentSize(content.length()).executeRequest(POST);

        Response response = OData2RequestBuilder.createRequest(sf) //
                .segments("Cars('3ab18d92-a574-45bb-a5e5-bcce38b7af11')") //
                .accept("application/atom+xml").executeRequest(GET);
        assertEquals(200, response.getStatus());

        String res = IOUtils.toString((InputStream) response.getEntity());
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(res.getBytes()));
        String idValue = null;
        NodeList nList = document.getElementsByTagName("d:Id");
        for (int i = 0; i < nList.getLength(); i++) {
            Node node = nList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;
                idValue = eElement.getTextContent();
            }
        }
        assertEquals("3ab18d92-a574-45bb-a5e5-bcce38b7af11", idValue);
    }

    @Test
    public void testCreateEntityWithoutId() throws Exception {

        String content = "{" //
                + "  \"d\": {" //
                + "    \"__metadata\": {" //
                + "      \"type\": \"org.eclipse.dirigible.engine.odata2.sql.entities.Car\"" //
                + "    }," //
                + "    \"Make\": \"BMW\"," //
                + "    \"Model\": \"320i\"," //
                + "    \"Price\": 30000.0" //
                + " }" + "}";

        Response response = modifyingRequestBuilder(sf, content)//
                .segments("Cars") //
                .accept("application/json")//
                .content(content).param("content-type", "application/json")//
                .contentSize(content.length()).executeRequest(POST);
        assertEquals(500, response.getStatus());
    }

    @Test
    public void testCreateEntityWithReferenceID() throws IOException, ODataException {
        String existingCarId = "639cac17-4cfd-4d94-b5d0-111fd5488423";
        String newDriverId = "11111111-1111-1111-1111-111111111111";
        String content = "{" //
                + "  \"d\": {" //
                + "    \"__metadata\": {" //
                + "      \"type\": \"org.eclipse.dirigible.engine.odata2.sql.entities.Driver\"" //
                + "    }," //
                + "    \"Id\": \"" + newDriverId + "\"," //
                + "    \"FirstName\": \"John\"," //
                + "    \"LastName\": \"Smith\"," //
                + "    \"Car\": " + "         { \"Id\": \"" + existingCarId + "\"}" // 
                + " }" //
                + "}";

        System.out.println(content);
        Response createResponse = modifyingRequestBuilder(sf, content)//
                .segments("Drivers") //
                .accept("application/json")//
                .param("content-type", "application/json") //
                .content(content)//
                .contentSize(content.length()).executeRequest(POST);

        assertEquals(201, createResponse.getStatus());
        String createResponseStr = IOUtils.toString((InputStream) createResponse.getEntity());
        System.out.println(createResponseStr);

        Response getResponseCreatedEntity = OData2RequestBuilder.createRequest(sf) //
                .segments("Drivers('" + newDriverId + "')") //
                .segments("Car").accept("application/json").executeRequest(GET);
        assertEquals(200, getResponseCreatedEntity.getStatus());
        String referencedCarResponse = IOUtils.toString((InputStream) getResponseCreatedEntity.getEntity());
        System.out.println(referencedCarResponse);
        assertTrue(referencedCarResponse.contains(existingCarId));

    }

    @Test
    public void testMergeEntity() throws Exception {
        Response existingCar = OData2RequestBuilder.createRequest(sf) //
                .segments("Cars('639cac17-4cfd-4d94-b5d0-111fd5488423')") //
                .accept("application/json").executeRequest(GET);
        assertEquals(200, existingCar.getStatus());

        String content = "{" //
                + "  \"d\": {" //
                + "    \"__metadata\": {" //
                + "      \"type\": \"org.eclipse.dirigible.engine.odata2.sql.entities.Car\"" //
                + "    }," //
                + "    \"Id\": \"639cac17-4cfd-4d94-b5d0-111fd5488423\"," //
                + "    \"Price\": 50000.0" //
                + " }" + "}";

        Response response = modifyingRequestBuilder(sf, content)//
                .segments("Cars('639cac17-4cfd-4d94-b5d0-111fd5488423')") //
                .accept("application/json")//
                .content(content)//
                .param("content-type", "application/json")//
                .contentSize(content.length()).executeRequest(MERGE);
        assertEquals(204, response.getStatus());

        existingCar = OData2RequestBuilder.createRequest(sf) //
                .segments("Cars('639cac17-4cfd-4d94-b5d0-111fd5488423')") //
                .accept("application/json").executeRequest(GET);
        assertEquals(200, existingCar.getStatus());
        ODataEntry resultEntry = retrieveODataEntry(existingCar, "Cars");
        Map<String, Object> properties = resultEntry.getProperties();
        assertEquals("BMW", properties.get("Make"));
        assertEquals("530e", properties.get("Model"));
        assertEquals(2021, properties.get("Year"));
        assertEquals(50000.0d, properties.get("Price"));
    }


    @Test
    public void testDeleteEntity() throws Exception {
        Response getCar = OData2RequestBuilder.createRequest(sf) //
                .segments("Drivers('695796c4-09a1-11ec-9a03-0242ac130006')") //
                .accept("application/json").executeRequest(GET);
        assertEquals(200, getCar.getStatus());

        Response deleteCar = OData2RequestBuilder.createRequest(sf) //
                .segments("Drivers('695796c4-09a1-11ec-9a03-0242ac130006')") //
                .accept("application/json").executeRequest(DELETE);
        assertEquals(204, deleteCar.getStatus());

        Response getDeletedCar = OData2RequestBuilder.createRequest(sf) //
                .segments("Drivers('695796c4-09a1-11ec-9a03-0242ac130006')") //
                .accept("application/json").executeRequest(GET);
        assertEquals(404, getDeletedCar.getStatus());
    }

    @Test
    public void testChangeReferenceId() throws Exception {
        String existingCarId = "639cac17-4cfd-4d94-b5d0-111fd5488423";

        String content = "{" //
                + "  \"d\": {" //
                + "    \"__metadata\": {" //
                + "      \"type\": \"org.eclipse.dirigible.engine.odata2.sql.entities.Driver\"" //
                + "    }," //
                + "    \"Id\": \"695796c4-09a1-11ec-9a03-0242ac130005\"," //
                + "    \"Car\": " + "         { \"Id\": \"" + existingCarId + "\"}" // 
                + " }" + "}";

        Response response = modifyingRequestBuilder(sf, content)//
                .segments("Drivers('695796c4-09a1-11ec-9a03-0242ac130005')") //
                .accept("application/json")//
                .content(content)//
                .param("content-type", "application/json")//
                .contentSize(content.length()).executeRequest(MERGE);
        assertEquals(204, response.getStatus());
    }

    @Test
    public void testPutEntity() throws Exception {
        Response existingCar = OData2RequestBuilder.createRequest(sf) //
                .segments("Cars('639cac17-4cfd-4d94-b5d0-111fd5488423')") //
                .accept("application/json").executeRequest(GET);
        assertEquals(200, existingCar.getStatus());

        String content = "{" //
                + "  \"d\": {" //
                + "    \"__metadata\": {" //
                + "      \"type\": \"org.eclipse.dirigible.engine.odata2.sql.entities.Car\"" //
                + "    }," //
                + "    \"Id\": \"639cac17-4cfd-4d94-b5d0-111fd5488423\"," //
                + "    \"Price\": 50000.0" //
                + " }" + "}";

        Response response = modifyingRequestBuilder(sf, content)//
                .segments("Cars('639cac17-4cfd-4d94-b5d0-111fd5488423')") //
                .accept("application/json")//
                .content(content)//
                .param("content-type", "application/json")//
                .contentSize(content.length()).executeRequest(PUT);
        assertEquals(204, response.getStatus());

        existingCar = OData2RequestBuilder.createRequest(sf) //
                .segments("Cars('639cac17-4cfd-4d94-b5d0-111fd5488423')") //
                .accept("application/json").executeRequest(GET);
        assertEquals(200, existingCar.getStatus());
        ODataEntry resultEntry = retrieveODataEntry(existingCar, "Cars");
        Map<String, Object> properties = resultEntry.getProperties();
        assertEquals("BMW", properties.get("Make"));
        assertEquals("530e", properties.get("Model"));
        assertEquals(2021, properties.get("Year"));
        assertEquals(50000.0d, properties.get("Price"));
    }

    @Test
    public void testPatchEntity() throws Exception {
        Response existingCar = OData2RequestBuilder.createRequest(sf) //
                .segments("Cars('639cac17-4cfd-4d94-b5d0-111fd5488423')") //
                .accept("application/json").executeRequest(GET);
        assertEquals(200, existingCar.getStatus());

        String content = "{" //
                + "  \"d\": {" //
                + "    \"__metadata\": {" //
                + "      \"type\": \"org.eclipse.dirigible.engine.odata2.sql.entities.Car\"" //
                + "    }," //
                + "    \"Price\": 50001.0" //
                + " }" + "}";

        Response response = modifyingRequestBuilder(sf, content)//
                .segments("Cars('639cac17-4cfd-4d94-b5d0-111fd5488423')") //
                .accept("application/json")//
                .content(content)//
                .param("content-type", "application/json")//
                .contentSize(content.length()).executeRequest(PATCH);
        assertEquals(204, response.getStatus());

        existingCar = OData2RequestBuilder.createRequest(sf) //
                .segments("Cars('639cac17-4cfd-4d94-b5d0-111fd5488423')") //
                .accept("application/json").executeRequest(GET);
        assertEquals(200, existingCar.getStatus());
        ODataEntry resultEntry = retrieveODataEntry(existingCar, "Cars");
        Map<String, Object> properties = resultEntry.getProperties();
        assertEquals("BMW", properties.get("Make"));
        assertEquals("530e", properties.get("Model"));
        assertEquals(2021, properties.get("Year"));
        assertEquals(50001.0d, properties.get("Price"));
    }

    @Test
    public void testPatchEntityNonExistingId() throws Exception {
        String content = "{" //
                + "  \"d\": {" //
                + "    \"__metadata\": {" //
                + "      \"type\": \"org.eclipse.dirigible.engine.odata2.sql.entities.Car\"" //
                + "    }," //
                + "    \"Price\": 50001.0" //
                + " }" + "}";
        Response response = modifyingRequestBuilder(sf, content)//
                .segments("Cars('88888888-8888-8888-8888-888888888888')") //
                .accept("application/json")//
                .content(content)//
                .param("content-type", "application/json")//
                .contentSize(content.length()).executeRequest(PATCH);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void testPutEntityWithFilterNotAllowed() throws Exception {
        String content = "{" //
                + "  \"d\": {" //
                + "    \"__metadata\": {" //
                + "      \"type\": \"org.eclipse.dirigible.engine.odata2.sql.entities.Car\"" //
                + "    }," //
                + "    \"Id\": \"639cac17-4cfd-4d94-b5d0-111fd5488423\"," //
                + "    \"Price\": 50000.0" //
                + " }" + "}";

        Response response = modifyingRequestBuilder(sf, content)//
                .segments("Cars('639cac17-4cfd-4d94-b5d0-111fd5488423')") //

                .accept("application/json")//
                .content(content)//
                .param("$filter", "Year eq 1982 or endswith(Make,'W')") //
                .param("content-type", "application/json")//
                .contentSize(content.length()).executeRequest(PUT);
        assertEquals(500, response.getStatus());
    }

    @Test
    public void testNavigation() throws IOException, ODataException {

        Response response = OData2RequestBuilder.createRequest(sf) //
                .segments("Cars('7990d49f-cfaf-48ab-8c6f-adbe7aaa069e')", "Drivers") //
                .accept("application/json").executeRequest(GET);

        assertEquals(200, response.getStatus());
        InputStream res = (InputStream) response.getEntity();
        String responseStr = IOUtils.toString(res);
        assertTrue(responseStr.contains("\"uri\":\"http://localhost:8080/api/v1/Drivers('695796c4-09a1-11ec-9a03-0242ac130003')/Car"));
        assertTrue(responseStr.contains("\"uri\":\"http://localhost:8080/api/v1/Drivers('695796c4-09a1-11ec-9a03-0242ac130006')/Car"));
    }

    @Test
    public void testTop2() throws Exception {
        Response response = OData2RequestBuilder.createRequest(sf) //
                .segments("Drivers") //
                .param("$top", "3") //
                .accept("application/atom+xml").executeRequest(GET);
        assertEquals(200, response.getStatus());
        ODataFeed resultFeed = retrieveODataFeed(response, "Drivers");
        assertEquals(3, resultFeed.getEntries().size());
    }

    @Test
    public void testSkip1() throws Exception {
        Response response = OData2RequestBuilder.createRequest(sf) //
                .segments("Cars") //
                .param("$skip", "3") //
                .accept("application/atom+xml").executeRequest(GET);
        assertEquals(200, response.getStatus());
        ODataFeed resultFeed = retrieveODataFeed(response, "Cars");
        assertEquals(4, resultFeed.getEntries().size());

    }

    @Test
    public void testSkip2() throws Exception {
        Response response = OData2RequestBuilder.createRequest(sf) //
                .segments("Drivers") //
                .param("$skip", "3") //
                .accept("application/atom+xml").executeRequest(GET);
        assertEquals(200, response.getStatus());
        ODataFeed resultFeed = retrieveODataFeed(response, "Drivers");
        assertEquals(3, resultFeed.getEntries().size());

    }

    @Test
    public void testSelect() throws Exception {
        Response response = OData2RequestBuilder.createRequest(sf) //
                .segments("Cars") //
                .param("$select", "Make,Model") //
                .accept("application/atom+xml").executeRequest(GET);
        ODataFeed resultFeed = retrieveODataFeed(response, "Cars");
        Map<String, Object> properties = resultFeed.getEntries().get(0).getProperties();
        assertEquals(2, properties.size());
        assertNotNull(properties.get("Make"));
        assertNotNull(properties.get("Model"));
    }

    @Test
    public void testOrderByAsc() throws Exception {
        Response response = OData2RequestBuilder.createRequest(sf) //
                .segments("Cars") //
                .param("$orderby", "Price asc") //
                .accept("application/atom+xml").executeRequest(GET);
        assertEquals(200, response.getStatus());

        ODataFeed resultFeed = retrieveODataFeed(response, "Cars");
        List<ODataEntry> entries = resultFeed.getEntries();
        assertEquals(7, entries.size());
        Map<String, Object> firstCarProperties = entries.get(0).getProperties();
        assertEquals(3000.0, firstCarProperties.get("Price"));
    }

    @Test
    public void testOrderByDesc() throws Exception {
        Response response = OData2RequestBuilder.createRequest(sf) //
                .segments("Cars") //
                .param("$orderby", "Price desc") //
                .accept("application/atom+xml").executeRequest(GET);
        assertEquals(200, response.getStatus());

        ODataFeed resultFeed = retrieveODataFeed(response, "Cars");
        List<ODataEntry> entries = resultFeed.getEntries();
        assertEquals(7, entries.size());
        Map<String, Object> firstCarProperties = entries.get(0).getProperties();
        assertEquals(67000.0, firstCarProperties.get("Price"));
    }

    @Test
    public void tesFilter() throws Exception {
        Response response = OData2RequestBuilder.createRequest(sf) //
                .segments("Cars") //
                .param("$orderby", "Price desc") //
                .param("$filter", "Year eq 1982 or endswith(Make,'W')") //
                .accept("application/atom+xml").executeRequest(GET);
        assertEquals(200, response.getStatus());

        ODataFeed resultFeed = retrieveODataFeed(response, "Cars");
        List<ODataEntry> entries = resultFeed.getEntries();
        assertEquals(2, entries.size());
        assertEquals(67000.0, entries.get(0).getProperties().get("Price"));
        assertEquals(4000.0, entries.get(1).getProperties().get("Price"));

    }

    @Test
    public void tesFilterTwoConditionsAnd() throws Exception {
        Response response = OData2RequestBuilder.createRequest(sf) //
                .segments("Cars") //
                .param("$orderby", "Price desc") //
                .param("$filter", "Year eq 1982 and startswith(Make,'Mos')") //
                .accept("application/atom+xml").executeRequest(GET);
        assertEquals(200, response.getStatus());

        ODataFeed resultFeed = retrieveODataFeed(response, "Cars");
        List<ODataEntry> entries = resultFeed.getEntries();
        assertEquals(1, entries.size());
        assertEquals(4000.0, entries.get(0).getProperties().get("Price"));
        assertEquals("Moskvitch", entries.get(0).getProperties().get("Make"));
    }

    @Test
    public void testFilter2() throws Exception {
        Response response = OData2RequestBuilder.createRequest(sf) //
                .segments("Cars") //
                .param("$orderby", "Price desc") //
                .param("$filter", "Make eq 'Moskvitch' ") //
                .accept("application/atom+xml").executeRequest(GET);
        assertEquals(200, response.getStatus());

        ODataFeed resultFeed = retrieveODataFeed(response, "Cars");
        List<ODataEntry> entries = resultFeed.getEntries();
        assertEquals(1, entries.size());
        Map<String, Object> properties = entries.get(0).getProperties();
        assertEquals("Moskvitch", properties.get("Make"));
    }

    @Test
    public void testIncompatibleValue() throws Exception {
        Response response = OData2RequestBuilder.createRequest(sf) //
                .segments("Cars") //
                .param("$filter", "Price eq 'hugo' ") //<--has to be convertible in a Double
                .accept("application/atom+xml").executeRequest(GET);
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testSQLProcessorFilterLeTime() throws Exception {
        Response response = OData2RequestBuilder.createRequest(sf) //
                .segments("Cars") //
                .param("$filter", "Updated le datetime'2016-11-07T10:12:39'") //
                .accept("application/atom+xml").executeRequest(GET);
        assertEquals(200, response.getStatus());

        ODataFeed resultFeed = retrieveODataFeed(response, "Cars");
        List<ODataEntry> entries = resultFeed.getEntries();
        assertEquals(1, entries.size());
    }

    @Test
    public void testFilterGeTime() throws Exception {
        Response response = OData2RequestBuilder.createRequest(sf) //
                .segments("Drivers") //
                .param("$filter", "Updated ge datetime'2011-11-07T10:12:39'") //
                .accept("application/atom+xml").executeRequest(GET);
        assertEquals(200, response.getStatus());

        ODataFeed resultFeed = retrieveODataFeed(response, "Drivers");
        List<ODataEntry> entries = resultFeed.getEntries();
        assertEquals(6, entries.size());
    }

    @Test
    public void testExpand() throws Exception {
        Response response = OData2RequestBuilder.createRequest(sf) //
                .segments("Cars") //
                .param("$top", "3") //
                .param("$expand", "Drivers") //
                .accept("application/atom+xml").executeRequest(GET);
        assertEquals(200, response.getStatus());

        ODataFeed resultFeed = retrieveODataFeed(response, "Cars");
        assertEquals(3, resultFeed.getEntries().size());
        assertEquals(7, resultFeed.getEntries().get(0).getProperties().size());

        List<ODataEntry> entries = resultFeed.getEntries();
        assertEquals("There shall be exactly 3 entries", 3, entries.size());
        Map<String, Object> firstEntryProperties = entries.get(0).getProperties();
        assertEquals(1982, firstEntryProperties.get("Year"));
        assertEquals("Moskvitch", firstEntryProperties.get("Make"));
        assertEquals("412", firstEntryProperties.get("Model"));

        List<ODataEntry> drivers = ((ODataDeltaFeedImpl) (firstEntryProperties.get("Drivers"))).getEntries();
        assertEquals(2, drivers.size());
        Map<String, Object> firstDriverProperties = drivers.get(0).getProperties();
        assertEquals("Johnny", firstDriverProperties.get("FirstName"));
        Map<String, Object> secondDriverProperties = drivers.get(1).getProperties();
        assertEquals("Natalie", secondDriverProperties.get("FirstName"));
    }

    @Test
    public void testGetEntityExpandTwoEntities() throws Exception {
        Response response = OData2RequestBuilder.createRequest(sf) //
                .segments("Cars('b4dc3e22-bacb-44ed-aa02-70273525fb73')") //
                .param("$expand", "Drivers,Owners") //
                .accept("application/atom+xml").executeRequest(GET);

        ODataEntry resultEntry = retrieveODataEntry(response, "Cars");

        List<ODataEntry> drivers = ((ODataDeltaFeedImpl) (resultEntry.getProperties().get("Drivers"))).getEntries();
        assertEquals(2, drivers.size());
        Map<String, Object> firstDriverProperties = drivers.get(0).getProperties();
        assertEquals("Johnny", firstDriverProperties.get("FirstName"));
        Map<String, Object> secondDriverProperties = drivers.get(1).getProperties();
        assertEquals("Natalie", secondDriverProperties.get("FirstName"));

        List<ODataEntry> owners = ((ODataDeltaFeedImpl) (resultEntry.getProperties().get("Owners"))).getEntries();
        assertEquals(4, owners.size());
        Map<String, Object> firstOwnerProperties = owners.get(0).getProperties();
        assertEquals("Johnny", firstOwnerProperties.get("FirstName"));
        Map<String, Object> secondOwnerProperties = owners.get(1).getProperties();
        assertEquals("Mihail", secondOwnerProperties.get("FirstName"));
        Map<String, Object> thirdOwnerProperties = owners.get(2).getProperties();
        assertEquals("Morgan", thirdOwnerProperties.get("FirstName"));
        Map<String, Object> fourthOwnerProperties = owners.get(3).getProperties();
        assertEquals("Grigor", fourthOwnerProperties.get("FirstName"));
    }

    @Test
    public void testGetEntitySetExpandTwoEntities() throws Exception {
        Response response = OData2RequestBuilder.createRequest(sf) //
                .segments("Cars") //
                .param("$filter", "Id eq 'b4dc3e22-bacb-44ed-aa02-70273525fb73'") //
                .param("$expand", "Drivers,Owners") //
                .accept("application/atom+xml").executeRequest(GET);

        ODataFeed resultFeed = retrieveODataFeed(response, "Cars");
        assertEquals(1, resultFeed.getEntries().size());
        ODataEntry resultEntry = resultFeed.getEntries().get(0);
        List<ODataEntry> drivers = ((ODataDeltaFeedImpl) (resultEntry.getProperties().get("Drivers"))).getEntries();
        assertEquals(2, drivers.size());
        Map<String, Object> firstDriverProperties = drivers.get(0).getProperties();
        assertEquals("Johnny", firstDriverProperties.get("FirstName"));
        Map<String, Object> secondDriverProperties = drivers.get(1).getProperties();
        assertEquals("Natalie", secondDriverProperties.get("FirstName"));

        List<ODataEntry> owners = ((ODataDeltaFeedImpl) (resultEntry.getProperties().get("Owners"))).getEntries();
        assertEquals(4, owners.size());
        Map<String, Object> firstOwnerProperties = owners.get(0).getProperties();
        assertEquals("Johnny", firstOwnerProperties.get("FirstName"));
        Map<String, Object> secondOwnerProperties = owners.get(1).getProperties();
        assertEquals("Mihail", secondOwnerProperties.get("FirstName"));
        Map<String, Object> thirdOwnerProperties = owners.get(2).getProperties();
        assertEquals("Morgan", thirdOwnerProperties.get("FirstName"));
        Map<String, Object> fourthOwnerProperties = owners.get(3).getProperties();
        assertEquals("Grigor", fourthOwnerProperties.get("FirstName"));
    }

    @Test
    @Ignore
    public void testGetEntitySetExpandSubEntity() throws Exception {
        Response response = OData2RequestBuilder.createRequest(sf) //
                .segments("Cars") //
                .param("$filter", "Id eq 'b4dc3e22-bacb-44ed-aa02-70273525fb73'") //
                .param("$expand", "Drivers,Owners/Addresses") //
                .accept("application/atom+xml").executeRequest(GET);
        //TODO implmeent me
        String content = IOUtils.toString((InputStream) response.getEntity());
        System.out.println(content);

        ODataFeed resultFeed = retrieveODataFeed(response, "Cars");
        assertEquals(1, resultFeed.getEntries().size());
        ODataEntry resultEntry = resultFeed.getEntries().get(0);
        List<ODataEntry> drivers = ((ODataDeltaFeedImpl) (resultEntry.getProperties().get("Drivers"))).getEntries();
        assertEquals(2, drivers.size());

    }

    @Test
    public void testOrderByExpandedEntityWithoutExpand() throws Exception {
        Response response = OData2RequestBuilder.createRequest(sf) //
                .segments("Cars") //
                .param("$top", "3") //
                .param("$orderby", "Drivers/FirstName desc") //
                //expand missing
                .accept("application/atom+xml").executeRequest(GET);
        assertEquals(500, response.getStatus()); //TODO refine the error codes, here it should be a bad request
    }

    @Test
    public void testOrderByExpandedEntityProperty() throws Exception {
        Response response = OData2RequestBuilder.createRequest(sf) //
                .segments("Cars") //
                .param("$top", "3") //
                .param("$orderby", "Drivers/FirstName desc") //
                .param("$expand", "Drivers") //
                .accept("application/atom+xml").executeRequest(GET);
        assertEquals(200, response.getStatus());

        ODataFeed resultFeed = retrieveODataFeed(response, "Cars");
        List<ODataEntry> entries = resultFeed.getEntries();
        assertEquals("The limit must work with expand", 3, entries.size());
        Map<String, Object> firstEntryProperties = entries.get(0).getProperties();
        assertEquals(1982, firstEntryProperties.get("Year"));
        assertEquals("Moskvitch", firstEntryProperties.get("Make"));
        assertEquals("412", firstEntryProperties.get("Model"));

        List<ODataEntry> drivers = ((ODataDeltaFeedImpl) (firstEntryProperties.get("Drivers"))).getEntries();
        assertEquals(2, drivers.size());
        Map<String, Object> firstDriverProperties = drivers.get(0).getProperties();
        assertEquals("Natalie", firstDriverProperties.get("FirstName"));
        Map<String, Object> secondDriverProperties = drivers.get(1).getProperties();
        assertEquals("Johnny", secondDriverProperties.get("FirstName"));


        Response responseAsc = OData2RequestBuilder.createRequest(sf) //
                .segments("Cars") //
                .param("$top", "3") //
                .param("$orderby", "Drivers/FirstName asc") //
                .param("$expand", "Drivers") //
                .accept("application/atom+xml").executeRequest(GET);
        assertEquals(200, responseAsc.getStatus());

        ODataFeed resultFeedAsc = retrieveODataFeed(responseAsc, "Cars");
        List<ODataEntry> entriesAsc = resultFeedAsc.getEntries();
        assertEquals("The limit must work with expand", 3, entriesAsc.size());
        Map<String, Object> firstEntryPropertiesAsc = entriesAsc.get(0).getProperties();
        assertEquals(2015, firstEntryPropertiesAsc.get("Year"));
        assertEquals("Ford", firstEntryPropertiesAsc.get("Make"));
        assertEquals("S-Max", firstEntryPropertiesAsc.get("Model"));

        List<ODataEntry> driversAsc = ((ODataDeltaFeedImpl) (firstEntryPropertiesAsc.get("Drivers"))).getEntries();
        assertEquals(0, driversAsc.size());
    }

    @Test
    public void testOrderByExpandedEntityProperties() throws Exception {
        Response response = OData2RequestBuilder.createRequest(sf) //
                .segments("Cars") //
                .param("$top", "3") //
                .param("$orderby", "Drivers/FirstName desc,Drivers/LastName asc") //
                .param("$expand", "Drivers") //
                .accept("application/atom+xml").executeRequest(GET);
        assertEquals(200, response.getStatus());
        ODataFeed resultFeed = retrieveODataFeed(response, "Cars");
        List<ODataEntry> entries = resultFeed.getEntries();
        assertEquals("The limit must work with expand", 3, entries.size());
        Map<String, Object> firstEntryProperties = entries.get(0).getProperties();
        assertEquals(1982, firstEntryProperties.get("Year"));
        assertEquals("Moskvitch", firstEntryProperties.get("Make"));
        assertEquals("412", firstEntryProperties.get("Model"));

        List<ODataEntry> drivers = ((ODataDeltaFeedImpl) (firstEntryProperties.get("Drivers"))).getEntries();
        assertEquals(2, drivers.size());
        Map<String, Object> firstDriverProperties = drivers.get(0).getProperties();
        assertEquals("Natalie", firstDriverProperties.get("FirstName"));
        Map<String, Object> secondDriverProperties = drivers.get(1).getProperties();
        assertEquals("Johnny", secondDriverProperties.get("FirstName"));

    }
}