/*
 * Copyright 2017 flow.ci
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flow.platform.api.test.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.flow.platform.api.domain.Flow;
import com.flow.platform.api.domain.Job;
import com.flow.platform.api.domain.JobFlow;
import com.flow.platform.api.domain.JobStep;
import com.flow.platform.api.domain.NodeStatus;
import com.flow.platform.api.domain.Step;
import com.flow.platform.api.service.JobNodeService;
import com.flow.platform.api.service.JobService;
import com.flow.platform.api.service.JobServiceImpl;
import com.flow.platform.api.service.NodeService;
import com.flow.platform.api.test.TestBase;
import com.flow.platform.api.util.UrlUtil;
import com.flow.platform.domain.Cmd;
import com.flow.platform.domain.CmdBase;
import com.flow.platform.domain.CmdResult;
import com.flow.platform.domain.CmdStatus;
import com.flow.platform.domain.CmdType;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedList;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * @author yh@firim
 */
public class WebhookControllerTest extends TestBase {

    @Autowired
    NodeService nodeService;

    @Autowired
    JobNodeService jobNodeService;

    @Autowired
    JobService jobService;

    @Test
    public void should_callback_session_success() throws Exception {

        Flow flow = new Flow();
        flow.setPath("/flow");
        flow.setName("flow");
        Step step1 = new Step();
        step1.setPath("/flow/step1");
        step1.setName("step1");
        Step step2 = new Step();
        step2.setPath("/flow/step2");
        step2.setName("step2");

        flow.getChildren().add(step1);
        flow.getChildren().add(step2);

        step1.setParent(flow);
        step2.setParent(flow);

        nodeService.create(flow);

        Job job = jobService.createJob(flow.getPath());

        // create session
        Cmd cmd = new Cmd("default", null, CmdType.CREATE_SESSION, null);
        cmd.setStatus(CmdStatus.SENT);
        String sessionId = "1111111";
        cmd.setSessionId(sessionId);

        CmdBase cmdBase = cmd;
        MockHttpServletRequestBuilder content = post("/hooks/" + UrlUtil.urlEncoder(job.getId()))
            .contentType(MediaType.APPLICATION_JSON)
            .content(cmdBase.toJson());
        this.mockMvc.perform(content)
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();
        Assert.assertNotNull(job.getSessionId());
        Assert.assertEquals(sessionId, job.getSessionId());
        Assert.assertEquals(job.getStatus(), NodeStatus.PENDING);

        // run first step running
        cmd = new Cmd("default", null, CmdType.RUN_SHELL, step1.getScript());
        cmd.setStatus(CmdStatus.RUNNING);

        cmdBase = cmd;
        content = post("/hooks/" + UrlUtil.urlEncoder(step1.getPath()))
            .contentType(MediaType.APPLICATION_JSON)
            .content(cmdBase.toJson());
        this.mockMvc.perform(content)
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();

        JobStep jobStep1 = (JobStep) jobNodeService.find(step1.getPath());
        JobFlow jobFlow = (JobFlow) jobNodeService.find(flow.getPath());
        Assert.assertEquals(job.getStatus(), NodeStatus.RUNNING);
        Assert.assertEquals(jobStep1.getStatus(), NodeStatus.RUNNING);
        Assert.assertEquals(jobFlow.getStatus(), NodeStatus.RUNNING);

        // run first step finish
        cmd = new Cmd("default", null, CmdType.RUN_SHELL, step1.getScript());
        cmd.setStatus(CmdStatus.LOGGED);
        CmdResult cmdResult = new CmdResult();
        cmdResult.setExitValue(1);
        cmdResult.setDuration(100L);
        cmd.setCmdResult(cmdResult);

        cmdBase = cmd;
        content = post("/hooks/" + UrlUtil.urlEncoder(step1.getPath()))
            .contentType(MediaType.APPLICATION_JSON)
            .content(cmd.toJson());
        this.mockMvc.perform(content)
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();

        jobStep1 = (JobStep) jobNodeService.find(step1.getPath());
        jobFlow = (JobFlow) jobNodeService.find(flow.getPath());
        Assert.assertEquals(jobStep1.getStatus(), NodeStatus.SUCCESS);
        Assert.assertEquals((Integer) 1, jobStep1.getExitCode());
        Assert.assertEquals(job.getStatus(), NodeStatus.RUNNING);
        Assert.assertEquals(jobFlow.getStatus(), NodeStatus.RUNNING);

        // run first step finish
        cmd = new Cmd("default", null, CmdType.RUN_SHELL, step1.getScript());
        cmd.setStatus(CmdStatus.LOGGED);
        cmdResult = new CmdResult();
        cmdResult.setExitValue(1);
        cmdResult.setDuration(100L);
        cmd.setCmdResult(cmdResult);

        cmdBase = cmd;
        content = post("/hooks/" + UrlUtil.urlEncoder(step2.getPath()))
            .contentType(MediaType.APPLICATION_JSON)
            .content(cmd.toJson());
        this.mockMvc.perform(content)
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();

        JobStep jobStep2 = (JobStep) jobNodeService.find(step2.getPath());
        jobFlow = (JobFlow) jobNodeService.find(flow.getPath());
        Assert.assertEquals(jobStep2.getStatus(), NodeStatus.SUCCESS);
        Assert.assertEquals((Integer) 1, jobStep2.getExitCode());
        Assert.assertEquals(job.getStatus(), NodeStatus.SUCCESS);
        Assert.assertEquals(jobFlow.getStatus(), NodeStatus.SUCCESS);
    }

    @Test
    public void should_callback_failure() throws Exception {
        Flow flow = new Flow();
        flow.setPath("/flow");
        flow.setName("flow");
        Step step1 = new Step();
        step1.setPath("/flow/step1");
        step1.setName("step1");
        Step step2 = new Step();
        step2.setPath("/flow/step2");
        step2.setName("step2");

        flow.getChildren().add(step1);
        flow.getChildren().add(step2);

        step1.setParent(flow);
        step2.setParent(flow);

        nodeService.create(flow);

        Job job = jobService.createJob(flow.getPath());

        // create session
        Cmd cmd = new Cmd("default", null, CmdType.CREATE_SESSION, null);
        cmd.setStatus(CmdStatus.SENT);
        String sessionId = "1111111";
        cmd.setSessionId(sessionId);

        CmdBase cmdBase = cmd;
        MockHttpServletRequestBuilder content = post("/hooks/" + UrlUtil.urlEncoder(job.getId()))
            .contentType(MediaType.APPLICATION_JSON)
            .content(cmdBase.toJson());
        this.mockMvc.perform(content)
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();
        Assert.assertNotNull(job.getSessionId());
        Assert.assertEquals(sessionId, job.getSessionId());
        Assert.assertEquals(job.getStatus(), NodeStatus.PENDING);

        // run first step timeout
        cmd = new Cmd("default", null, CmdType.RUN_SHELL, step1.getScript());
        cmd.setStatus(CmdStatus.TIMEOUT_KILL);

        cmdBase = cmd;
        content = post("/hooks/" + UrlUtil.urlEncoder(step1.getPath()))
            .contentType(MediaType.APPLICATION_JSON)
            .content(cmdBase.toJson());
        this.mockMvc.perform(content)
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();

        JobStep jobStep1 = (JobStep) jobNodeService.find(step1.getPath());
        JobFlow jobFlow = (JobFlow) jobNodeService.find(flow.getPath());
//        Assert.assertEquals(job.getStatus(), NodeStatus.FAIL);
        Assert.assertEquals(jobStep1.getStatus(), NodeStatus.TIMEOUT);
//        Assert.assertEquals(jobFlow.getStatus(), NodeStatus.FAIL);
    }
}