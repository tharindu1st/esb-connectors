/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.connector.integration.test.gplus;

import org.apache.axis2.context.ConfigurationContext;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.proxy.admin.ProxyServiceAdminClient;
import org.wso2.carbon.automation.api.clients.utils.AuthenticateStub;
import org.wso2.carbon.automation.utils.axis2client.ConfigurationContextProvider;
import org.wso2.carbon.connector.integration.test.common.ConnectorIntegrationUtil;
import org.wso2.carbon.esb.ESBIntegrationTest;
import org.wso2.carbon.mediation.library.stub.MediationLibraryAdminServiceStub;
import org.wso2.carbon.mediation.library.stub.upload.MediationLibraryUploaderStub;

import javax.activation.DataHandler;
import java.net.URL;
import java.util.Properties;

public class GooglePlusActivitiesTest extends ESBIntegrationTest {

    private ProxyServiceAdminClient proxyAdmin;

    private String pathToProxiesDirectory = null;

    private String pathToRequestsDirectory = null;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {

        super.init();

        ConfigurationContextProvider configurationContextProvider =
                ConfigurationContextProvider.getInstance();
        ConfigurationContext cc = configurationContextProvider.getConfigurationContext();
        MediationLibraryUploaderStub mediationLibUploadStub = new MediationLibraryUploaderStub(cc,
                esbServer
                        .getBackEndUrl() +
                        "MediationLibraryUploader");
        AuthenticateStub.authenticateStub("admin", "admin", mediationLibUploadStub);

        MediationLibraryAdminServiceStub adminServiceStub = new MediationLibraryAdminServiceStub(cc,
                esbServer
                        .getBackEndUrl() +
                        "MediationLibraryAdminService");

        AuthenticateStub.authenticateStub("admin", "admin", adminServiceStub);

        String repoLocation;
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            repoLocation = System.getProperty("connector_repo").replace("/", "\\");
        } else {
            repoLocation = System.getProperty("connector_repo").replace("/", "/");
        }
        proxyAdmin = new ProxyServiceAdminClient(esbServer.getBackEndUrl(),
                esbServer.getSessionCookie());

        String CONNECTOR_NAME = "googleplus-connector-1.0.0";
        String googlePlusConnectorFileName = CONNECTOR_NAME + ".zip";
        ConnectorIntegrationUtil
                .uploadConnector(repoLocation, mediationLibUploadStub, googlePlusConnectorFileName);
        log.info("Sleeping for " + 60000 / 1000 + " seconds while waiting for synapse import");
        Thread.sleep(60000);
        adminServiceStub
                .updateStatus("{org.wso2.carbon.connector}" + "googleplus", "googleplus",
                        "org.wso2.carbon.connector", "enabled");

        Properties googlePlusConnectorProperties =
                ConnectorIntegrationUtil.getConnectorConfigProperties(CONNECTOR_NAME);

        pathToProxiesDirectory = repoLocation + googlePlusConnectorProperties
                .getProperty("proxyDirectoryRelativePath");
        pathToRequestsDirectory = repoLocation + googlePlusConnectorProperties
                .getProperty("requestDirectoryRelativePath");
    }

    @Override
    protected void cleanup() {
        axis2Client.destroy();
    }

    /**
     * Mandatory parameter test case for getActivity method.
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {getActivity} integration test with mandatory parameters.")
    public void testGetActivityWithMandatoryParams() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getActivities.txt";
        String methodName = "getActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {

            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requestJsonString);
            Assert.assertEquals("plus#activity", responseJson.getString("kind"));

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for getActivity method.
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {getActivity} integration test with mandatory and optional parameters.")
    public void testGetActivityWithOptionalParams() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getActivitiesOptionalParams.txt";
        String methodName = "getActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {

            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requestJsonString);
            Assert.assertEquals("plus#activity", responseJson.getString("kind"));

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Negative test case for getActivity method.
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {getActivity} integration test with Negative parameters.")
    public void testGetActivityWithNegativeParams() throws Exception {

        String jsonRequestFilePath = pathToRequestsDirectory + "getActivitiesUnhappy.txt";
        String methodName = "getActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            int statusCode = ConnectorIntegrationUtil
                    .sendRequestToRetriveHeaders(getProxyServiceURL(methodName), requestJsonString);

            Assert.assertTrue(statusCode == 404 || statusCode == 403);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Mandatory parameter test case for listActivity method.
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {listActivity} integration test with mandatory parameters.")
    public void testListActivityWithMandatoryParams() throws Exception {

        String jsonRequestFilePath = pathToRequestsDirectory + "listActivities.txt";
        String methodName = "listActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requestJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for listActivity method.
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {listActivity} integration test with mandatory and optional parameters.")
    public void testListActivityWithOptionalParams() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "listActivitiesOptionalParams.txt";
        String methodName = "listActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requestJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for listActivity method with maxResults Optional Parameter.
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {listActivity} integration test with mandatory and maxResults optional parameter.")
    public void testListActivityWithOneOptionalParam1() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "listActivitiesOptionalParams.txt";
        String methodName = "listActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String[] unneededOptionalParameters = {"pageToken", "fields"};
            String requiredJsonString =
                    ConnectorIntegrationUtil.getRequiredJsonString(requestJsonString, unneededOptionalParameters);
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requiredJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for listActivity method with pageToken Optional Parameter.
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {listActivity} integration test with mandatory and pageToken optional parameter.")
    public void testListActivityWithOneOptionalParam2() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "listActivitiesOptionalParams.txt";
        String methodName = "listActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String[] unneededOptionalParameters = {"maxResults", "fields"};
            String requiredJsonString =
                    ConnectorIntegrationUtil.getRequiredJsonString(requestJsonString, unneededOptionalParameters);
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requiredJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for listActivity method with fields Optional Parameter.
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {listActivity} integration test with mandatory and fields optional parameter.")
    public void testListActivityWithOneOptionalParam3() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "listActivitiesOptionalParams.txt";
        String methodName = "listActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String[] unneededOptionalParameters = {"maxResults", "pageToken"};
            String requiredJsonString =
                    ConnectorIntegrationUtil.getRequiredJsonString(requestJsonString, unneededOptionalParameters);
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requiredJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for listActivity method with maxResults,pageToken Optional Parameters.
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {listActivity} integration test with mandatory and maxResults,pageToken optional parameters.")
    public void testListActivityWithTwoOptionalParam1() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "listActivitiesOptionalParams.txt";
        String methodName = "listActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String[] unneededOptionalParameters = {"fields"};
            String requiredJsonString =
                    ConnectorIntegrationUtil.getRequiredJsonString(requestJsonString, unneededOptionalParameters);
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requiredJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for listActivity method with pageToken,fields Optional Parameters.
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {listActivity} integration test with mandatory and pageToken,,fields optional parameters.")
    public void testListActivityWithTwoOptionalParam2() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "listActivitiesOptionalParams.txt";
        String methodName = "listActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String[] unneededOptionalParameters = {"maxResults"};
            String requiredJsonString =
                    ConnectorIntegrationUtil.getRequiredJsonString(requestJsonString, unneededOptionalParameters);
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requiredJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for listActivity method with maxResults,fields Optional Parameters.
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {listActivity} integration test with mandatory and maxResults,fields optional parameters.")
    public void testListActivityWithTwoOptionalParam3() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "listActivitiesOptionalParams.txt";
        String methodName = "listActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String[] unneededOptionalParameters = {"pageToken"};
            String requiredJsonString =
                    ConnectorIntegrationUtil.getRequiredJsonString(requestJsonString, unneededOptionalParameters);
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requiredJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Negative test case for listActivity method.
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {listActivity} integration test with Negative parameters.")
    public void testListActivityWithNegativeParams() throws Exception {

        String jsonRequestFilePath = pathToRequestsDirectory + "listActivitiesUnhappy.txt";
        String methodName = "listActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            int statusCode = ConnectorIntegrationUtil
                    .sendRequestToRetriveHeaders(getProxyServiceURL(methodName), requestJsonString);
            Assert.assertTrue(statusCode == 404 || statusCode == 403);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Mandatory parameter test case for searchActivities method.
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {searchActivities} integration test with mandatory parameters.")
    public void testSearchActivityWithMandatoryParams() throws Exception {

        String jsonRequestFilePath = pathToRequestsDirectory + "searchActivities.txt";
        String methodName = "searchActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";


        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            log.info("Sleep for 30 seconds");
            Thread.sleep(3000);

            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requestJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for searchActivities method.
     */

    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {searchActivities} integration test with mandatory and optional parameters.")
    public void testSearchActivityWithOptionalParams() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "searchActivitiesOptionalParams.txt";
        String methodName = "searchActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requestJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for searchActivities method with maxResults optional parameter.
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {searchActivities} integration test with mandatory and maxResults optional parameters.")
    public void testSearchActivityWithOneOptionalParams1() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "searchActivitiesOptionalParams.txt";
        String methodName = "searchActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String[] unneededOptionalParameters = {"language", "orderBy", "pageToken", "fields"};
            String requiredJsonString =
                    ConnectorIntegrationUtil.getRequiredJsonString(requestJsonString, unneededOptionalParameters);
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requiredJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for searchActivities method with orderBy optional parameter.
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {searchActivities} integration test with mandatory and orderBy optional parameters.")
    public void testSearchActivityWithOneOptionalParams2() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "searchActivitiesOptionalParams.txt";
        String methodName = "searchActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String[] unneededOptionalParameters =
                    {"language", "maxResults", "pageToken", "fields"};
            String requiredJsonString =
                    ConnectorIntegrationUtil.getRequiredJsonString(requestJsonString, unneededOptionalParameters);
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requiredJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for searchActivities method with pageToken optional parameter.
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {searchActivities} integration test with mandatory and pageToken optional parameters.")
    public void testSearchActivityWithOneOptionalParams3() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "searchActivitiesOptionalParams.txt";
        String methodName = "searchActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String[] unneededOptionalParameters = {"language", "maxResults", "orderBy", "fields"};
            String requiredJsonString =
                    ConnectorIntegrationUtil.getRequiredJsonString(requestJsonString, unneededOptionalParameters);
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requiredJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for searchActivities method with fields.
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {searchActivities} integration test with mandatory and fields optional parameters.")
    public void testSearchActivityWithOneOptionalParams4() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "searchActivitiesOptionalParams.txt";
        String methodName = "searchActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String[] unneededOptionalParameters =
                    {"language", "maxResults", "orderBy", "pageToken"};
            String requiredJsonString =
                    ConnectorIntegrationUtil.getRequiredJsonString(requestJsonString, unneededOptionalParameters);
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requiredJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for searchActivities method with language optional parameter
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {searchActivities} integration test with mandatory and language optional parameters.")
    public void testSearchActivityWithOneOptionalParams5() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "searchActivitiesOptionalParams.txt";
        String methodName = "searchActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String[] unneededOptionalParameters =
                    {"language", "maxResults", "orderBy", "pageToken"};
            String requiredJsonString =
                    ConnectorIntegrationUtil.getRequiredJsonString(requestJsonString, unneededOptionalParameters);
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requiredJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for searchActivities method with language,maxResults,orderBy,pageToken .
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {searchActivities} integration test with mandatory and language,maxResults,orderBy,pageToken optional parameters.")
    public void testSearchActivityWithFourOptionalParams1() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "searchActivitiesOptionalParams.txt";
        String methodName = "searchActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String[] unneededOptionalParameters = {"fields"};
            String requiredJsonString =
                    ConnectorIntegrationUtil.getRequiredJsonString(requestJsonString, unneededOptionalParameters);
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requiredJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for searchActivities method with language,maxResults,orderBy,fields .
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {searchActivities} integration test with mandatory and language,maxResults,orderBy,fields optional parameters.")
    public void testSearchActivityWithFourOptionalParams2() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "searchActivitiesOptionalParams.txt";
        String methodName = "searchActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String[] unneededOptionalParameters = {"pageToken"};
            String requiredJsonString =
                    ConnectorIntegrationUtil.getRequiredJsonString(requestJsonString, unneededOptionalParameters);
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requiredJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for searchActivities method with language,maxResults,pageToken,fields .
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {searchActivities} integration test with mandatory and language,maxResults,pageToken,fields optional parameters.")
    public void testSearchActivityWithFourOptionalParams3() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "searchActivitiesOptionalParams.txt";
        String methodName = "searchActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String[] unneededOptionalParameters = {"orderBy"};
            String requiredJsonString =
                    ConnectorIntegrationUtil.getRequiredJsonString(requestJsonString, unneededOptionalParameters);
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requiredJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for searchActivities method with language,orderBy,pageToken,fields .
     */

    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {searchActivities} integration test with mandatory and language,orderBy,pageToken,fields optional parameters.")
    public void testSearchActivityWithFourOptionalParams4() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "searchActivitiesOptionalParams.txt";
        String methodName = "searchActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String[] unneededOptionalParameters = {"maxResults"};
            String requiredJsonString =
                    ConnectorIntegrationUtil.getRequiredJsonString(requestJsonString, unneededOptionalParameters);
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requiredJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for searchActivities method with maxResults,orderBy,pageToken,fields.
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {searchActivities} integration test with mandatory and maxResults,orderBy,pageToken,fields optional parameters.")
    public void testSearchActivityWithFourOptionalParams5() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "searchActivitiesOptionalParams.txt";
        String methodName = "searchActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String[] unneededOptionalParameters = {"language"};
            String requiredJsonString =
                    ConnectorIntegrationUtil.getRequiredJsonString(requestJsonString, unneededOptionalParameters);
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requiredJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for searchActivities method with language,maxResults,fields optional parameter.
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {searchActivities} integration test with mandatory and language,maxResults,fields optional parameters.")
    public void testSearchActivityWithThreeOptionalParams1() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "searchActivitiesOptionalParams.txt";
        String methodName = "searchActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String[] unneededOptionalParameters = {"orderBy", "pageToken"};
            String requiredJsonString =
                    ConnectorIntegrationUtil.getRequiredJsonString(requestJsonString, unneededOptionalParameters);
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requiredJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for searchActivities method with language,orderBy,pageToken optional parameter.
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {searchActivities} integration test with mandatory and language,orderBy,pageToken optional parameters.")
    public void testSearchActivityWithThreeOptionalParams2() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "searchActivitiesOptionalParams.txt";
        String methodName = "searchActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String[] unneededOptionalParameters = {"maxResults", "fields"};
            String requiredJsonString =
                    ConnectorIntegrationUtil.getRequiredJsonString(requestJsonString, unneededOptionalParameters);
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requiredJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for searchActivities method with language,orderBy,fields optional parameter.
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {searchActivities} integration test with mandatory and language,orderBy,fields optional parameters.")
    public void testSearchActivityWithThreeOptionalParams3() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "searchActivitiesOptionalParams.txt";
        String methodName = "searchActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String[] unneededOptionalParameters = {"maxResults", "pageToken"};
            String requiredJsonString =
                    ConnectorIntegrationUtil.getRequiredJsonString(requestJsonString, unneededOptionalParameters);
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requiredJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for searchActivities method with language,pageToken,fields.
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {searchActivities} integration test with mandatory and language,pageToken,fields optional parameters.")
    public void testSearchActivityWithThreeOptionalParams4() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "searchActivitiesOptionalParams.txt";
        String methodName = "searchActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String[] unneededOptionalParameters = {"maxResults", "orderBy"};
            String requiredJsonString =
                    ConnectorIntegrationUtil.getRequiredJsonString(requestJsonString, unneededOptionalParameters);
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requiredJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for searchActivities method with maxResults,orderBy,pageToken optional parameter
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {searchActivities} integration test with mandatory and maxResults,orderBy,pageToken optional parameters.")
    public void testSearchActivityWithThreeOptionalParams5() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "searchActivitiesOptionalParams.txt";
        String methodName = "searchActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String[] unneededOptionalParameters = {"language", "fields"};
            String requiredJsonString =
                    ConnectorIntegrationUtil.getRequiredJsonString(requestJsonString, unneededOptionalParameters);
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requiredJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for searchActivities method with maxResults,orderBy,fields optional parameter.
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {searchActivities} integration test with mandatory and maxResults,orderBy,fields optional parameters.")
    public void testSearchActivityWithThreeOptionalParams6() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "searchActivitiesOptionalParams.txt";
        String methodName = "searchActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String[] unneededOptionalParameters = {"language", "pageToken"};
            String requiredJsonString =
                    ConnectorIntegrationUtil.getRequiredJsonString(requestJsonString, unneededOptionalParameters);
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requiredJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for searchActivities method with maxResults,pageToken,fields optional parameter.
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {searchActivities} integration test with mandatory and maxResults,pageToken,fields optional parameters.")
    public void testSearchActivityWithThreeOptionalParams7() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "searchActivitiesOptionalParams.txt";
        String methodName = "searchActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String[] unneededOptionalParameters = {"language", "orderBy"};
            String requiredJsonString =
                    ConnectorIntegrationUtil.getRequiredJsonString(requestJsonString, unneededOptionalParameters);
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requiredJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for searchActivities method with orderBy,pageToken,fields optional parameter.
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {searchActivities} integration test with mandatory and orderBy,pageToken,fields optional parameters.")
    public void testSearchActivityWithThreeOptionalParams8() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "searchActivitiesOptionalParams.txt";
        String methodName = "searchActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String[] unneededOptionalParameters = {"language", "fields"};
            String requiredJsonString =
                    ConnectorIntegrationUtil.getRequiredJsonString(requestJsonString, unneededOptionalParameters);
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requiredJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for searchActivities method with language,maxResults,orderBy.
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {searchActivities} integration test with mandatory and language,maxResults,orderBy optional parameters.")
    public void testSearchActivityWithThreeOptionalParams9() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "searchActivitiesOptionalParams.txt";
        String methodName = "searchActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String[] unneededOptionalParameters = {"pageToken", "fields"};
            String requiredJsonString =
                    ConnectorIntegrationUtil.getRequiredJsonString(requestJsonString, unneededOptionalParameters);
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requiredJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for searchActivities method with language,maxResults,pageToken optional parameter
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {searchActivities} integration test with mandatory and language,maxResults,pageToken optional parameters.")
    public void testSearchActivityWithThreeOptionalParams10() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "searchActivitiesOptionalParams.txt";
        String methodName = "searchActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String[] unneededOptionalParameters = {"orderBy", "fields"};
            String requiredJsonString =
                    ConnectorIntegrationUtil.getRequiredJsonString(requestJsonString, unneededOptionalParameters);
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requiredJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for searchActivities method with orderBy,pageToken  optional parameter.
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {searchActivities} integration test with mandatory and orderBy,pageToken optional parameters.")
    public void testSearchActivityWithTwoOptionalParams1() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "searchActivitiesOptionalParams.txt";
        String methodName = "searchActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String[] unneededOptionalParameters = {"language", "maxResults", "fields"};
            String requiredJsonString =
                    ConnectorIntegrationUtil.getRequiredJsonString(requestJsonString, unneededOptionalParameters);
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requiredJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for searchActivities method with maxResults,fields optional parameter.
     */
    @Test(groups = {"wso2.esb"},
            description = "googleplus {searchActivities} integration test with mandatory and maxResults,fields optional parameters.")
    public void testSearchActivityWithTwoOptionalParams2() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "searchActivitiesOptionalParams.txt";
        String methodName = "searchActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String[] unneededOptionalParameters = {"language", "orderBy", "pageToken"};
            String requiredJsonString =
                    ConnectorIntegrationUtil.getRequiredJsonString(requestJsonString, unneededOptionalParameters);
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requiredJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for searchActivities method with maxResults,pageToken optional parameter.
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {searchActivities} integration test with mandatory and maxResults,pageToken optional parameters.")
    public void testSearchActivityWithTwoOptionalParams3() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "searchActivitiesOptionalParams.txt";
        String methodName = "searchActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String[] unneededOptionalParameters = {"language", "orderBy", "fields"};
            String requiredJsonString =
                    ConnectorIntegrationUtil.getRequiredJsonString(requestJsonString, unneededOptionalParameters);
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requiredJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for searchActivities method with maxResults,orderBy.
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {searchActivities} integration test with mandatory and maxResults,orderBy optional parameters.")
    public void testSearchActivityWithTwoOptionalParams4() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "searchActivitiesOptionalParams.txt";
        String methodName = "searchActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String[] unneededOptionalParameters = {"language", "pageToken", "fields"};
            String requiredJsonString =
                    ConnectorIntegrationUtil.getRequiredJsonString(requestJsonString, unneededOptionalParameters);
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requiredJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for searchActivities method with language,fields optional parameter
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {searchActivities} integration test with mandatory and language,fields optional parameters.")
    public void testSearchActivityWithTwoOptionalParams5() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "searchActivitiesOptionalParams.txt";
        String methodName = "searchActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String[] unneededOptionalParameters = {"maxResults", "orderBy", "pageToken"};
            String requiredJsonString =
                    ConnectorIntegrationUtil.getRequiredJsonString(requestJsonString, unneededOptionalParameters);
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requiredJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for searchActivities method with language,pageToken optional parameter.
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {searchActivities} integration test with mandatory and language,pageToken optional parameters.")
    public void testSearchActivityWithTwoOptionalParams6() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "searchActivitiesOptionalParams.txt";
        String methodName = "searchActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String[] unneededOptionalParameters = {"maxResults", "orderBy", "fields"};
            String requiredJsonString =
                    ConnectorIntegrationUtil.getRequiredJsonString(requestJsonString, unneededOptionalParameters);
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requiredJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for searchActivities method with language,orderBy optional parameter.
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {searchActivities} integration test with mandatory and language,orderBy optional parameters.")
    public void testSearchActivityWithTwoOptionalParams7() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "searchActivitiesOptionalParams.txt";
        String methodName = "searchActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String[] unneededOptionalParameters = {"maxResults", "pageToken", "fields"};
            String requiredJsonString =
                    ConnectorIntegrationUtil.getRequiredJsonString(requestJsonString, unneededOptionalParameters);
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requiredJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for searchActivities method with language,maxResults optional parameter.
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {searchActivities} integration test with mandatory and language,maxResults optional parameters.")
    public void testSearchActivityWithTwoOptionalParams8() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "searchActivitiesOptionalParams.txt";
        String methodName = "searchActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String[] unneededOptionalParameters = {"orderBy", "pageToken", "fields"};
            String requiredJsonString =
                    ConnectorIntegrationUtil.getRequiredJsonString(requestJsonString, unneededOptionalParameters);
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requiredJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for searchActivities method with pageToken,fields.
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {searchActivities} integration test with mandatory and pageToken,fields optional parameters.")
    public void testSearchActivityWithTwoOptionalParams9() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "searchActivitiesOptionalParams.txt";
        String methodName = "searchActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String[] unneededOptionalParameters = {"language", "maxResults", "orderBy"};
            String requiredJsonString =
                    ConnectorIntegrationUtil.getRequiredJsonString(requestJsonString, unneededOptionalParameters);
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requiredJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Optional parameter test case for searchActivities method with orderBy,fields optional parameter
     */
    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {searchActivities} integration test with mandatory and orderBy,fields optional parameters.")
    public void testSearchActivityWithTwoOptionalParams10() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "searchActivitiesOptionalParams.txt";
        String methodName = "searchActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String[] unneededOptionalParameters = {"language", "maxResults", "pageToken"};
            String requiredJsonString =
                    ConnectorIntegrationUtil.getRequiredJsonString(requestJsonString, unneededOptionalParameters);
            JSONObject responseJson = ConnectorIntegrationUtil
                    .sendRequest(getProxyServiceURL(methodName), requiredJsonString);
            Assert.assertEquals("plus#activityFeed", responseJson.getString("kind"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Negative parameter test case for searchActivities method.
     */

    @Test(groups = {"wso2.esb"},
            description = "GooglePlus {searchActivities} integration test with Negative parameters.")
    public void testSearchActivityWithNegativeParams() throws Exception {

        String jsonRequestFilePath = pathToRequestsDirectory + "searchActivitiesUnhappy.txt";
        String methodName = "searchActivities";
        final String requestJsonString =
                ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            int statusCode = ConnectorIntegrationUtil
                    .sendRequestToRetriveHeaders(getProxyServiceURL(methodName), requestJsonString);
            Assert.assertEquals(statusCode, 400);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

}
