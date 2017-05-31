package com.flow.platform.cc.test.controller;

import com.flow.platform.cc.config.AppConfig;
import com.flow.platform.cc.service.AgentService;
import com.flow.platform.cc.service.CmdService;
import com.flow.platform.cc.service.ZoneService;
import com.flow.platform.cc.test.TestBase;
import com.flow.platform.domain.*;
import com.flow.platform.util.zk.ZkNodeHelper;
import com.flow.platform.util.zk.ZkPathBuilder;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Queue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by gy@fir.im on 25/05/2017.
 * Copyright fir.im
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CmdControllerTest extends TestBase {

    @Autowired
    private CmdService cmdService;

    @Autowired
    private AgentService agentService;

    @Autowired
    private ZoneService zoneService;

    @Autowired
    private Queue<Path> cmdLoggingQueue;

    @Before
    public void before() {
        cmdLoggingQueue.clear();
    }

    @Test
    public void should_update_cmd_status() throws Throwable {
        // given:
        AgentPath path = new AgentPath("test-zone-00", "test-001");
        agentService.reportOnline("test-zone-00", Lists.newArrayList(path));

        CmdBase base = new CmdBase("test-zone-00", "test-001", CmdBase.Type.STOP, null);
        Cmd cmd = cmdService.create(base);

        // when:
        CmdReport postData = new CmdReport(cmd.getId(), Cmd.Status.EXECUTED, new CmdResult());

        MockHttpServletRequestBuilder content = post("/cmd/report")
                .contentType(MediaType.APPLICATION_JSON)
                .content(postData.toJson());

        this.mockMvc.perform(content)
                .andDo(print())
                .andExpect(status().isOk());

        // then:
        Cmd loaded = cmdService.find(cmd.getId());
        Assert.assertNotNull(loaded);
        Assert.assertTrue(loaded.getStatus().contains(Cmd.Status.EXECUTED));
    }

    @Test
    public void should_send_cmd_to_agent() throws Throwable {
        // given:
        String zoneName = "test-zone-02";
        zoneService.createZone(zoneName);
        Thread.sleep(1000);

        String agentName = "act-002";
        ZkPathBuilder builder = zkHelper.buildZkPath(zoneName, agentName);
        ZkNodeHelper.createEphemeralNode(zkClient, builder.path(), "");
        Thread.sleep(1000);

        // when: send post request
        CmdBase cmd = new CmdBase(zoneName, agentName, Cmd.Type.RUN_SHELL, "~/hello.sh");
        gsonConfig.toJson(cmd);

        MockHttpServletRequestBuilder content = post("/cmd/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(gsonConfig.toJson(cmd));

        // then: check response data
        MvcResult result = this.mockMvc.perform(content)
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        Cmd cmdInfo = gsonConfig.fromJson(result.getResponse().getContentAsString(), Cmd.class);
        Assert.assertNotNull(cmdInfo);
        Assert.assertTrue(cmdInfo.getStatus().contains(Cmd.Status.PENDING));
        Assert.assertEquals(zoneName, cmdInfo.getZone());
        Assert.assertEquals(agentName, cmdInfo.getAgent());

        // then: check node data
        byte[] raw = ZkNodeHelper.getNodeData(zkClient, builder.path(), null);
        Assert.assertNotNull(raw);

        Cmd received = Jsonable.parse(raw, Cmd.class);
        Assert.assertNotNull(received);
        Assert.assertEquals(cmdInfo, received);
    }

    @Test
    public void should_upload_and_download_zipped_log() throws Throwable {
        // given:
        ClassLoader classLoader = CmdControllerTest.class.getClassLoader();
        URL resource = classLoader.getResource("test-cmd-id.out.zip");
        Path path = Paths.get(resource.getFile());
        byte[] data = Files.readAllBytes(path);

        CmdBase cmdBase = new CmdBase("test-zone-1", "test-agent-1", Cmd.Type.RUN_SHELL, "~/hello.sh");
        Cmd cmd = cmdService.create(cmdBase);

        String originalFilename = cmd.getId() + ".out.zip";

        MockMultipartFile zippedCmdLogPart = new MockMultipartFile("file", originalFilename, "application/zip", data);
        MockMultipartFile cmdIdPart = new MockMultipartFile("cmdId", "", "text/plain", cmd.getId().getBytes());

        // when: upload zipped cmd log
        MockMultipartHttpServletRequestBuilder content = fileUpload("/cmd/log/upload")
                .file(zippedCmdLogPart)
                .file(cmdIdPart);

        this.mockMvc.perform(content)
                .andDo(print())
                .andExpect(status().isOk());

        // then: check upload file path and logging queue
        Path zippedLogPath = Paths.get(AppConfig.CMD_LOG_DIR.toString(), originalFilename);
        Assert.assertTrue(Files.exists(zippedLogPath));

        Assert.assertEquals(1, cmdLoggingQueue.size());
        Assert.assertEquals(zippedLogPath, cmdLoggingQueue.peek());
        Assert.assertEquals(data.length, Files.size(zippedLogPath));

        // when: download uploaded zipped cmd log
        MvcResult result = this.mockMvc.perform(get("/cmd/log/download").param("cmdId", cmd.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // then:
        MockHttpServletResponse response = result.getResponse();
        Assert.assertEquals("application/zip", response.getContentType());
        Assert.assertEquals(data.length, response.getContentLength());
        Assert.assertTrue(response.getHeader("Content-Disposition").contains(originalFilename));
    }
}
