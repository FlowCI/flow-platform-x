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
     */
    void report(String cmdId, CmdStatus status, CmdResult result);

    /**
     * Record full zipped log to store
     *
     * @param cmdId
     * @param file
     */
    void saveFullLog(String cmdId, MultipartFile file);

    /**
     * Get full log path
     * TODO: should replace local file system
     *
     * @param cmdId
     * @return
     */
    Path getFullLog(String cmdId);

    /**
     * Check timeout cmd by created date for all busy agent
     */
    void checkTimeoutTask();
}
