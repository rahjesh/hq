/**
 * NOTE: This copyright does *not* cover user programs that use HQ
 * program services by normal system calls through the application
 * program interfaces provided as part of the Hyperic Plug-in Development
 * Kit or the Hyperic Client Development Kit - this is merely considered
 * normal use of the program, and does *not* fall under the heading of
 *  "derived work".
 *
 *  Copyright (C) [2010], VMware, Inc.
 *  This file is part of HQ.
 *
 *  HQ is free software; you can redistribute it and/or modify
 *  it under the terms version 2 of the GNU General Public License as
 *  published by the Free Software Foundation. This program is distributed
 *  in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE. See the GNU General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 *
 */

package org.hyperic.hq.appdef.server.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.NonUniqueObjectException;
import org.hyperic.hq.appdef.Agent;
import org.hyperic.hq.appdef.Ip;
import org.hyperic.hq.appdef.shared.AIPlatformValue;
import org.hyperic.hq.appdef.shared.AppdefDuplicateFQDNException;
import org.hyperic.hq.appdef.shared.AppdefDuplicateNameException;
import org.hyperic.hq.appdef.shared.AppdefEntityID;
import org.hyperic.hq.appdef.shared.AppdefEntityNotFoundException;
import org.hyperic.hq.appdef.shared.IpValue;
import org.hyperic.hq.appdef.shared.PlatformNotFoundException;
import org.hyperic.hq.appdef.shared.PlatformValue;
import org.hyperic.hq.appdef.shared.ServerNotFoundException;
import org.hyperic.hq.appdef.shared.ServiceNotFoundException;
import org.hyperic.hq.appdef.shared.UpdateException;
import org.hyperic.hq.appdef.shared.ValidationException;
import org.hyperic.hq.authz.shared.PermissionException;
import org.hyperic.hq.common.ApplicationException;
import org.hyperic.hq.common.NotFoundException;
import org.hyperic.hq.common.SystemException;
import org.hyperic.hq.common.VetoException;
import org.hyperic.hq.inventory.dao.ResourceDao;
import org.hyperic.hq.test.BaseInfrastructureTest;
import org.hyperic.util.pager.PageControl;
import org.hyperic.util.pager.PageList;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;


/**
 * Integration test of the {@link PlatformManagerImpl}
 * @author iperumal
 * @author jhickey
 * 
 */
@DirtiesContext
public class PlatformManagerTest
    extends BaseInfrastructureTest {

    private Agent testAgent;

    private List<PlatformType> testPlatformTypes;

    private PlatformType testPlatformType;

    private List<Platform> testPlatforms;

    private Platform testPlatform;

    private Server testServer;

    private Service testService;
    
    private ServiceType serviceType;
    
    private String agentToken = "agentToken123";
    
    @Autowired
    private ResourceDao resourceDao;

    private List<Platform> createPlatforms(String agentToken) throws ApplicationException {
        List<Platform> platforms = new ArrayList<Platform>(10);
        for (int i = 1; i < 10; i++) {
            platforms.add(i - 1, createPlatform(agentToken, "pType" + i, "TestPlatform" + i,
                "TestPlatform" + i,2));
        }
        // Create on Linux platform (supported platform)
        platforms.add(9, createPlatform(agentToken, "Linux", "Test Platform Linux",
            "Test Platform Linux",2));
        return platforms;
    }

    private List<PlatformType> createTestPlatformTypes() throws NotFoundException {
        List<PlatformType> pTypes = new ArrayList<PlatformType>(10);
        String platformType;
        for (int i = 1; i < 10; i++) {
            platformType = "pType" + i;
            pTypes.add(i - 1, createPlatformType(platformType));
        }
        pTypes.add(9, createPlatformType("Linux"));
        return pTypes;
    }

    @Before
    public void initializeTestData() throws ApplicationException, NotFoundException {
        testAgent = createAgent("127.0.0.1", 2144, "authToken", agentToken, "4.5");
        flushSession();
        testPlatformTypes = createTestPlatformTypes();
        // Get Linux platform type
        testPlatformType = testPlatformTypes.get(9);
        testPlatforms = createPlatforms(agentToken);
        // Get Linux platform
        testPlatform = testPlatforms.get(9);
        // Create ServerType
        ServerType testServerType = createServerType("Tomcat", "6.0", new String[] { "Linux" });
        // Create test server
        testServer = createServer(testPlatform, testServerType, "My Tomcat");
        // Create ServiceType
        serviceType = createServiceType("Spring JDBC Template", testServerType);
        // Create test service
        testService = createService(testServer.getId(), serviceType, "platformService jdbcTemplate",
            "Spring JDBC Template", "my computer");
        Set<Platform> testPlatforms = new HashSet<Platform>(1);
        testPlatforms.add(testPlatform);
        createPlatformResourceGroup(testPlatforms, "AllPlatformGroup");
    }

    @Test
    public void testFindPlatformType() {
        PlatformType pType = platformManager.findPlatformType(testPlatformTypes.get(0).getId());
        assertEquals("Incorrect PlatformType Found ById", pType, testPlatformTypes.get(0));
    }

    @Test
    public void testFindPlatformTypeByName() throws PlatformNotFoundException {
        PlatformType pType = platformManager.findPlatformTypeByName("pType1");
        assertEquals("Incorrect PlatformType found ByName", pType, testPlatformTypes.get(0));
    }

    @Test(expected = PlatformNotFoundException.class)
    public void testFindPlatformTypeByNameNotFound() throws PlatformNotFoundException {
        platformManager.findPlatformTypeByName("Test");
    }

    public void testGetAllPlatformTypes() {
        fail("Not yet implemented");
    }

    public void testGetViewablePlatformTypes() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetPlatformPluginNameForPlatform() throws AppdefEntityNotFoundException {
        String platformPluginName = platformManager.getPlatformPluginName(testPlatform
            .getEntityId());
        assertEquals("PlatformPluginName incorrect for platform", "Linux", platformPluginName);
    }

    @Test
    public void testGetPlatformPluginNameForServer() throws AppdefEntityNotFoundException {
        String ppNameOfServer = platformManager.getPlatformPluginName(testServer.getEntityId());
        assertEquals("PlatformPluginName incorrect for server", "Tomcat Linux", ppNameOfServer);
    }

    @Test
    public void testGetPlatformPluginNameForService() throws AppdefEntityNotFoundException {
        String ppNameOfService = platformManager.getPlatformPluginName(testService.getEntityId());
        assertEquals("PlatformPluginName incorrect for service", "Spring JDBC Template Linux",
            ppNameOfService);
    }

    @Test
    public void testRemovePlatform() throws PermissionException,
          VetoException, PlatformNotFoundException, ServerNotFoundException, AppdefDuplicateNameException, ValidationException, NotFoundException {
        platformManager.addIp(testPlatform, "127.0.0.1", "255:255:255:0", "12:34:G0:93:58:96");
        //create a Service directly under Platform
        ServiceType serviceType = createServiceType("MyServiceType",testPlatform.getPlatformType());
      
        Service platformService = createService(testPlatform.getId(), serviceType, "Service123",
            "Spring JDBC Template", "my computer");
        platformManager.removePlatform(authzSubjectManager.getOverlordPojo(),
              testPlatform);
        try {
            platformManager.findPlatformById(testPlatform.getId());
            fail("Platform was found after removal");
        }catch(PlatformNotFoundException e) {
            //expected
        }
        //Server and Service should be deleted as well
        try {
            serverManager.findServerById(testServer.getId());
            fail("Server was found after removal");
        }catch(ServerNotFoundException e) {
            //expected
        }
        try {
            serviceManager.findServiceById(testService.getId());
            fail("Service was found after removal");
        }catch(ServiceNotFoundException e) {
            //expected
        }
        try {
            serviceManager.findServiceById(platformService.getId());
            fail("Platform service was found after removal");
        }catch(ServiceNotFoundException e) {
            //expected
        }
        //Ensure IP is removed
        assertTrue(resourceManager.findResourceTypeByName(PlatformManagerImpl.IP_RESOURCE_TYPE_NAME).getResources().isEmpty());
    }
    
    
    @Test
    public void testCreatePlatformByAIPlatformValues() throws ApplicationException {
        String agentToken = "agentToken123";

        Platform platform = createPlatform(agentToken, testPlatformType.getName(),
            "Test Platform CreationByAIValues", "Test PlatformByAIValues",2);
        assertNotNull(platform.getResource());
        assertEquals(platform.getName(), "Test PlatformByAIValues");
        assertEquals(platform.getAgent().getAgentToken(), agentToken);
        assertEquals(platform.getCpuCount(), new Integer(2));
        assertEquals(platform.getPlatformType().getName(), testPlatformType.getName());
        assertEquals(platform.getFqdn(), "Test Platform CreationByAIValues");
    }

    @Test
    public void testCreatePlatformIncorrectPlatformType() throws ApplicationException {
        String agentToken = "agentToken123";

        try {
            // Provide a non-existent platform type
            createPlatform(agentToken, "abcd", "Test Platform Creation", "Test Platform",2);
        } catch (SystemException e) {
            assertEquals(e.getMessage(), "Unable to find PlatformType [abcd]");
            return;
        }
        fail("Expected SystemException is not thrown");
    }

    @Test(expected = NonUniqueObjectException.class)
    public void testCreatePlatformDuplicate() throws ApplicationException {
        String agentToken = "agentToken123";
        AIPlatformValue aiPlatform = new AIPlatformValue();
        aiPlatform.setCpuCount(2);
        aiPlatform.setName("Test Platform");
        aiPlatform.setPlatformTypeName(testPlatformType.getName());
        aiPlatform.setAgentToken(agentToken);
        aiPlatform.setFqdn("Test Platform Creation");
        aiPlatform.setCertdn("CertDN");
        platformManager.createPlatform(authzSubjectManager.getOverlordPojo(), aiPlatform);
        platformManager.createPlatform(authzSubjectManager.getOverlordPojo(), aiPlatform);
    }

    @Test
    public void testCreatePlatformIncorrectAgentToken() throws ApplicationException {
        // Add an invalid agent token
        String agentToken = "agentToken";
        AIPlatformValue aiPlatform = new AIPlatformValue();
        aiPlatform.setCpuCount(2);
        aiPlatform.setPlatformTypeName(testPlatformType.getName());
        aiPlatform.setAgentToken(agentToken);
        aiPlatform.setFqdn("Test Platform Creation");
        aiPlatform.setName("Test Platform Creation");
        try {
            platformManager.createPlatform(authzSubjectManager.getOverlordPojo(), aiPlatform);
        } catch (ApplicationException e) {
            assertEquals(e.getMessage(), "Unable to find agent: " + aiPlatform.getAgentToken());
            return;
        }
        fail("Expected ApplicationException for invalid agent token is not thrown");
    }

    @Test
    public void testCreatePlatformByPlatformType() throws ApplicationException {
       
        Platform platform = createPlatform(testAgent.getAgentToken(), testPlatformType.getName(), "Test Platform CreationByPlatformType", "Test Platform ByPlatformType",2);
       
        assertNotNull(platform.getResource());
        assertEquals(platform.getName(), "Test Platform ByPlatformType");
        assertEquals(platform.getFqdn(), "Test Platform CreationByPlatformType");
        assertEquals(platform.getCpuCount(), new Integer(2));
    }

    @Test(expected = NonUniqueObjectException.class)
    public void testCreatePlatformDuplicateName() throws ApplicationException {
        createPlatform(testAgent.getAgentToken(), testPlatformType.getName(),"Test Platform CreationByPlatformType","Test Platform ByPlatformType",2);
        createPlatform(testAgent.getAgentToken(), testPlatformType.getName(),"Test Platform CreationByPlatformType","Test Platform ByPlatformType",2);
    }

    @Test
    public void testGetAllPlatforms() throws ApplicationException, NotFoundException {
        PageList<PlatformValue> pValues = platformManager.getAllPlatforms(authzSubjectManager
            .getOverlordPojo(), null);
        assertEquals(testPlatforms.size(), pValues.size());
    }

    @Test
    public void testGetRecentPlatforms() throws ApplicationException, NotFoundException {
        long setTime = System.currentTimeMillis();
        int i = 1;
        for (Platform p : testPlatforms) {
            // Set Platforms creation time 20 minutes before the current time.
            p.getResource().setProperty(PlatformFactory.CREATION_TIME,setTime - 20 * 60000l);
            resourceDao.merge(p.getResource());
            i++;
        }
        // Change two of the platform's creation time to recent
        testPlatforms.get(0).getResource().setProperty(PlatformFactory.CREATION_TIME,setTime);
        resourceDao.merge(testPlatforms.get(0).getResource());
        testPlatforms.get(1).getResource().setProperty(PlatformFactory.CREATION_TIME,setTime - 2 * 60000l);
        resourceDao.merge(testPlatforms.get(1).getResource());
        testPlatforms.get(2).getResource().setProperty(PlatformFactory.CREATION_TIME,setTime - 3 * 60000l);
        resourceDao.merge(testPlatforms.get(2).getResource());
        PageList<PlatformValue> pValues = platformManager.getRecentPlatforms(authzSubjectManager
            .getOverlordPojo(), 5 * 60000l, 10);
        assertEquals(3, pValues.size());
    }

    @Test
    public void testGetPlatformById() throws ApplicationException, NotFoundException {
        Platform platform = platformManager.getPlatformById(authzSubjectManager.getOverlordPojo(),
            testPlatform.getId());
        assertEquals("Correct Platform is not fetched", platform, testPlatform);
    }

    @Test(expected = PlatformNotFoundException.class)
    public void testGetPlatformByInvalidId() throws ApplicationException, NotFoundException {
        platformManager.getPlatformById(authzSubjectManager.getOverlordPojo(), -2);
    }

    @Test
    public void testFindPlatformById() throws ApplicationException {
        Platform platform = platformManager.findPlatformById(testPlatform.getId());
        assertEquals("Correct Platform is not found by Id", platform, testPlatform);
    }

    @Test(expected = PlatformNotFoundException.class)
    public void testFindPlatformByInvalidId() throws ApplicationException {
        platformManager.findPlatformById(-2);
    }

    @Test
    public void testGetPlatformByAIPlatformFQDN() throws ApplicationException {
        AIPlatformValue aiPlatform = new AIPlatformValue();
        aiPlatform.setFqdn(testPlatform.getFqdn());
        // Platform platform =
        // platformManager.createPlatform(authzSubjectManager.getOverlordPojo(),
        // aiPlatform);
        // Following method will fetch the platform based on the FQDN
        Platform fetchedPlatform = platformManager.getPlatformByAIPlatform(authzSubjectManager
            .getOverlordPojo(), aiPlatform);
        assertNotNull(fetchedPlatform);
        assertEquals(testPlatform, fetchedPlatform);
    }

    @Test
    public void testFindPlatformByFqdn() throws ApplicationException {
        Platform fetchedPlatform = platformManager.findPlatformByFqdn(authzSubjectManager
            .getOverlordPojo(), testPlatform.getFqdn());
        assertNotNull(fetchedPlatform);
        assertEquals(testPlatform, fetchedPlatform);
    }

    @Test
    public void testFindPlatformByInvalidFqdn() throws ApplicationException {
        try {
            platformManager.findPlatformByFqdn(authzSubjectManager.getOverlordPojo(), "abcd");
        } catch (PlatformNotFoundException e) {
            assertEquals(e.getMessage(), "Platform with fqdn abcd not found");
            return;
        }
        fail("PlatformNotFoundException is not thrown for an invalid FQDN");
    }

  

    @Test
    public void testGetPlatformPksByAgentToken() throws ApplicationException {
        Collection<Integer> platformPKs = platformManager.getPlatformPksByAgentToken(
            authzSubjectManager.getOverlordPojo(), "agentToken123");
        Set<Integer> testPlatformPKs = new HashSet<Integer>();
        for (Platform platform : testPlatforms) {
            testPlatformPKs.add(platform.getId());
        }
        assertEquals(testPlatformPKs, platformPKs);
    }

    @Test
    public void testGetPlatformPksByInvalidAgentToken() throws ApplicationException {
        try {
            platformManager.getPlatformPksByAgentToken(authzSubjectManager.getOverlordPojo(),
                "agentTokenInvalid");
        } catch (PlatformNotFoundException e) {
            assertEquals(e.getMessage(), "Platform with agent token agentTokenInvalid not found");
            return;
        }
        fail("PlatformNotFoundException is not thrown for an invalid agent token");
    }

    @Test
    public void testGetPlatformByService() throws ApplicationException {
        PlatformValue pValue = platformManager.getPlatformByService(authzSubjectManager
            .getOverlordPojo(), testService.getId());
        assertEquals(testPlatform.getPlatformValue(), pValue);
    }

    @Test
    public void testGetPlatformByInvalidService() throws ApplicationException {
        Integer invalidId = testService.getId() + 12345;
        try {
            platformManager.getPlatformByService(authzSubjectManager.getOverlordPojo(), invalidId);
        } catch (PlatformNotFoundException e) {
            assertEquals(e.getMessage(), "platform for service " + invalidId + " not found");
            return;
        }
        fail("PlatformNotFoundException is not thrown for invalid service");
    }

    @Test
    public void testGetPlatformIdByService() throws ApplicationException {
        Integer platformId = platformManager.getPlatformIdByService(testService.getId());
        assertEquals(testPlatform.getId(), platformId);
    }

    @Test
    public void testGetPlatformIdByInvalidService() throws ApplicationException {
        Integer invalidId = testService.getId() + 12345;
        try {
            platformManager.getPlatformIdByService(invalidId);
        } catch (PlatformNotFoundException e) {
            assertEquals(e.getMessage(), "platform for service " + invalidId + " not found");
            return;
        }
        fail("PlatformNotFoundException is not thrown for invalid service");
    }

    @Test
    public void testGetPlatformByServer() throws ApplicationException {
        PlatformValue pValue = platformManager.getPlatformByServer(authzSubjectManager
            .getOverlordPojo(), testServer.getId());
        assertEquals(testPlatform.getPlatformValue(), pValue);
    }

    @Test
    public void testGetPlatformByInvalidServer() throws ApplicationException {
        Integer invalidId = testServer.getId() + 12345;
        try {
            platformManager.getPlatformByServer(authzSubjectManager.getOverlordPojo(), invalidId);
        } catch (PlatformNotFoundException e) {
            assertEquals(e.getMessage(), "platform for server " + invalidId + " not found");
            return;
        }
        fail("PlatformNotFoundException is not thrown for invalid server");
    }

    @Test
    public void testGetPlatformIdByServer() throws ApplicationException {
        Integer platformId = platformManager.getPlatformIdByServer(testServer.getId());
        assertEquals(testPlatform.getId(), platformId);
    }

    @Test
    public void testGetPlatformIdByInvalidServer() throws ApplicationException {
        Integer invalidId = testServer.getId() + 12345;
        try {
            platformManager.getPlatformIdByServer(invalidId);
        } catch (PlatformNotFoundException e) {
            assertEquals(e.getMessage(), "platform for server " + invalidId + " not found");
            return;
        }
        fail("PlatformNotFoundException is not thrown for invalid server");
    }

    @Test
    public void testGetPlatformsByServers() throws ApplicationException {
        List<AppdefEntityID> serverIds = new ArrayList<AppdefEntityID>();
        serverIds.add(testServer.getEntityId());
        PageList<PlatformValue> pValues = platformManager.getPlatformsByServers(authzSubjectManager
            .getOverlordPojo(), serverIds);
        assertEquals(testPlatform.getPlatformValue(), pValues.get(0));
    }

    @Test
    public void testGetPlatformsByApplication() throws ApplicationException, NotFoundException {
        AppdefEntityID serviceId= testService.getEntityId();
        List<AppdefEntityID> services = new ArrayList<AppdefEntityID>();
        services.add(serviceId);
        Application app = createApplication("Test Application", "testing", services);
        flushSession();
        //clear the session to update the bi-directional app to app service relationship
        clearSession();
        PageControl pc = new PageControl();
        PageList<PlatformValue> pValues = platformManager.getPlatformsByApplication(
            authzSubjectManager.getOverlordPojo(), app.getId(), pc);
        assertEquals(testPlatform.getPlatformValue(), pValues.get(0));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetPlatformIdsByType() throws ApplicationException {
        Integer[] platformIds = platformManager.getPlatformIds(authzSubjectManager
            .getOverlordPojo(), testPlatform.getPlatformType().getId());
        
        Set<Integer> expectedIds = new HashSet<Integer>();
        expectedIds.add(testPlatform.getId());
        //platformIds is an Array, but not guaranteed to be ordered
        assertEquals(expectedIds,new HashSet<Integer>(Arrays.asList(platformIds)));
    }

    @Test
    public void testGetPlatformsByType() throws ApplicationException {
        Collection<Platform> platforms = platformManager.getPlatformsByType(authzSubjectManager
            .getOverlordPojo(), testPlatform.getPlatformType().getName());
        Set<Platform> expectedPlats = new HashSet<Platform>();
        expectedPlats.add(testPlatform);
        assertEquals(expectedPlats, platforms);
    } 

    @Test
    public void testCreatePlatformType() throws NotFoundException {
        String platformTypeName = "platformType";
        PlatformType pType = platformManager.createPlatformType(platformTypeName, BaseInfrastructureTest.TEST_PLUGIN_NAME);
        assertEquals(pType.getName(), platformTypeName);
        assertEquals(pType.getPlugin(), BaseInfrastructureTest.TEST_PLUGIN_NAME);
        assertEquals(resourceManager.findResourceTypeById(pType.getId()).getName(), platformTypeName);
    }

    @Test
    public void testUpdateWithAI() throws ApplicationException {
        AIPlatformValue aiPlatform = new AIPlatformValue();
        // Set AIPlatformValue for the testPlatform
        aiPlatform.setPlatformTypeName(testPlatform.getPlatformType().getName());
        aiPlatform.setAgentToken(testPlatform.getAgent().getAgentToken());
        aiPlatform.setFqdn(testPlatform.getFqdn());
        // Now set the name & CPU count of the platform
        aiPlatform.setName("Updated PlatformName");
        aiPlatform.setCertdn("CertDn");
        aiPlatform.setCpuCount(4);
        platformManager.updateWithAI(aiPlatform, authzSubjectManager.getOverlordPojo());
        Platform updatedPlatform = platformManager.findPlatformById(testPlatform.getId());
        assertEquals(updatedPlatform.getName(), "Updated PlatformName");
        assertEquals(updatedPlatform.getCpuCount().intValue(), 4);
    }

    @Test
    public void testUpdatePlatform() throws PlatformNotFoundException, AppdefDuplicateNameException, AppdefDuplicateFQDNException, UpdateException, PermissionException, ApplicationException {
        PlatformValue platform = new PlatformValue();
        platform.setId(testPlatform.getId());
        platform.setCpuCount(4);
        platform.setLocation("Somewhere");
        platform.setName("Updated Name");
        platform.setFqdn("Updated FQDN");
        IpValue newIp = new IpValue();
        newIp.setAddress("1.2.3.4");
        newIp.setMACAddress("10");
        newIp.setNetmask("100");
        platform.addIpValue(newIp);
        platform.setCommentText("A comment");
        platform.setCertdn("Certdn");
        platformManager.updatePlatform(authzSubjectManager.getOverlordPojo(), platform);
        Platform updatedPlatform = platformManager.findPlatformById(testPlatform.getId());
        assertEquals("Updated Name",updatedPlatform.getName());
        assertEquals("Updated FQDN",updatedPlatform.getFqdn());
        assertEquals("Somewhere",updatedPlatform.getLocation());
        assertEquals(new Integer(4),updatedPlatform.getCpuCount());
        assertEquals(1,updatedPlatform.getIps().size());
    }

    @Test
    public void testAddIp() throws PlatformNotFoundException {
        platformManager.addIp(testPlatform, "127.0.0.1", "255:255:255:0", "12:34:G0:93:58:96");
        platformManager.addIp(testPlatform, "192.168.1.2", "255:255:0:0", "91:34:45:93:67:96");
        Collection<Ip> ips = platformManager.findPlatformById(testPlatform.getId()).getIps();
        assertEquals(ips.size(), 2);
        for (Ip ip : ips) {
            if (ip.getAddress().equals("192.168.1.2")) {
                assertEquals(ip.getMacAddress(), "91:34:45:93:67:96");
                assertEquals(ip.getNetmask(), "255:255:0:0");
            } else {
                assertEquals(ip.getAddress(), "127.0.0.1");
                assertEquals(ip.getMacAddress(), "12:34:G0:93:58:96");
                assertEquals(ip.getNetmask(), "255:255:255:0");
            }
        }
    }

    @Test
    public void testUpdateIp() throws PlatformNotFoundException {
        platformManager.addIp(testPlatform, "127.0.0.1", "255:255:255:0", "12:34:G0:93:58:96");
        Collection<Ip> ips = platformManager.findPlatformById(testPlatform.getId()).getIps();
        for (Ip ip : ips) {
            assertEquals(ip.getAddress(), "127.0.0.1");
            assertEquals(ip.getMacAddress(), "12:34:G0:93:58:96");
            assertEquals(ip.getNetmask(), "255:255:255:0");
        }
        platformManager.updateIp(testPlatform, "127.0.0.1", "255:255:0:0", "91:34:45:93:67:96");
        ips = platformManager.findPlatformById(testPlatform.getId()).getIps();
        for (Ip ip : ips) {
            assertEquals(ip.getAddress(), "127.0.0.1");
            assertEquals(ip.getMacAddress(), "91:34:45:93:67:96");
            assertEquals(ip.getNetmask(), "255:255:0:0");
        }
    }

    @Test
    public void testRemoveIp() throws PlatformNotFoundException {
        platformManager.addIp(testPlatform, "127.0.0.1", "255:255:255:0", "12:34:G0:93:58:96");
        platformManager.addIp(testPlatform, "192.168.1.2", "255:255:0:0", "91:34:45:93:67:96");
        platformManager.removeIp(testPlatform, "192.168.1.2", "255:255:0:0", "91:34:45:93:67:96");
        Collection<Ip> ips = platformManager.findPlatformById(testPlatform.getId()).getIps();
        assertEquals(ips.size(), 1);
        for (Ip ip : ips) {
            assertEquals(ip.getAddress(), "127.0.0.1");
            assertEquals(ip.getMacAddress(), "12:34:G0:93:58:96");
            assertEquals(ip.getNetmask(), "255:255:255:0");
        }
    }

    @Test
    public void testGetPlatformTypeCounts() {
        Map<String,Integer> counts = platformManager.getPlatformTypeCounts();
        Map<String,Integer> actuals = new LinkedHashMap<String,Integer>();
        // Add the Linux testPlatformType as the result is sorted
        actuals.put(testPlatformTypes.get(9).getName(),1);
        actuals.put("PluginTestPlatform",0);
        for (int i = 2; i <= 10; i++) {
            // Add platform Type name and count (here count is always 1)
            actuals.put(testPlatformTypes.get(i - 2).getName(),1 );
        }
        assertEquals(actuals,counts);
    }

    @Test
    public void testGetPlatformCount() {
        // we have added 10 test platforms during initial setup
        assertEquals(platformManager.getPlatformCount().intValue(), 10);
    }
}
