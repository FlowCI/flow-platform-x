package com.flow.platform.cc.service;

import com.flow.platform.cc.exception.AgentErr;
import com.flow.platform.domain.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;

/**
 * Created by gy@fir.im on 25/05/2017.
 * Copyright fir.im
 */
public interface CmdService {

    // default cmd execution timeout in seconds
    long CMD_TIMEOUT_SECONDS = 300;

    /**
     * Create command from CmdBase
     *
     * @param cmd
     * @return Cmd objc with id
     */
    Cmd create(CmdBase cmd);

    /**
     * Find cmd obj by id
     *
     * @param cmdId
     * @return Cmd object
     */
    Cmd find(String cmdId);

    /**
     * List cmd by agent path
     *
     * @param agentPath
     * @return
     */
    List<Cmd> listByAgentPath(AgentPath agentPath);

    /**
     * Send CmdBase with AgentPath which to identify where is cmd destination
     *  - AgentPath,
     *   - 'zone' field is required
     *   - 'name' field is optional
     *      - which mean system will automatic select idle agent to send
     *        throw AgentErr.NotAvailableException if no idle agent
     *
     * @param cmd
     * @return command objc with id
     * @exception AgentErr.NotAvailableException if agent busy
     */
    Cmd send(CmdBase cmd);

    /**
     * Check cmd is timeout
     *
     * @param cmd
     * @return timeout or not
     */
    boolean isTimeout(Cmd cmd);

    /**
     * Update cmd status and result
     *
     * @param cmdId
     * @param status
     * @param result
     * @param updateAgentStatus should update agent status according to cmd status
     */
    void updateStatus(String cmdId, CmdStatus status, CmdResult result, boolean updateAgentStatus);

    /**
     * Record full zipped log to store
     *
     * @param cmdId
     * @param file
     */
    void saveLog(String cmdId, MultipartFile file);

    /**
     * Check timeout cmd by created date for all busy agent
     */
    void checkTimeoutTask();
}
