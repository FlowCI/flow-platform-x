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

package com.flow.platform.api.domain.sync;

import com.flow.platform.domain.Jsonable;
import com.flow.platform.util.CommandUtil;
import com.flow.platform.util.StringUtil;
import com.google.gson.annotations.Expose;

/**
 * @author yang
 */
public class SyncEvent extends Jsonable {

    /**
     * Output env variable for agent repo list
     */
    public final static String FLOW_SYNC_LIST = "FLOW_SYNC_LIST";

    public final static SyncEvent DELETE_ALL =
        new SyncEvent(null, StringUtil.EMPTY, StringUtil.EMPTY, SyncType.DELETE_ALL);

    public final static SyncEvent LIST =
        new SyncEvent(null, StringUtil.EMPTY, StringUtil.EMPTY, SyncType.LIST);

    /**
     * Git source url
     */
    private String gitUrl;

    /**
     * Repo name and tag
     */
    @Expose
    private SyncRepo repo;

    /**
     * Sync type
     */
    @Expose
    private SyncType syncType;

    public SyncEvent(String gitUrl, String name, String tag, SyncType syncType) {
        this.gitUrl = gitUrl;
        this.repo = new SyncRepo(name, tag);
        this.syncType = syncType;
    }

    public SyncEvent(String gitUrl, SyncRepo repo, SyncType syncType) {
        this.gitUrl = gitUrl;
        this.repo = repo;
        this.syncType = syncType;
    }

    public String getGitUrl() {
        return gitUrl;
    }

    public void setGitUrl(String gitUrl) {
        this.gitUrl = gitUrl;
    }

    public SyncRepo getRepo() {
        return repo;
    }

    public SyncType getSyncType() {
        return syncType;
    }

    public String toScript() {
        if (syncType == SyncType.LIST) {
            return CommandUtil.setVariable(FLOW_SYNC_LIST, "\"$(ls)\"");
        }

        if (syncType == SyncType.DELETE_ALL) {
            return CommandUtil.rmdir();
        }

        // the sync event type DELETE, CREATE, UPDATE needs folder name
        String folder = repo.toString();

        if (syncType == SyncType.DELETE) {
            return CommandUtil.rmdir(folder);
        }

        return "git init " + folder +
            System.lineSeparator() +
            "cd " + folder +
            System.lineSeparator() +
            "git pull " + gitUrl + " --tags" +
            System.lineSeparator() +
            "git checkout " + repo.getTag();
    }

    @Override
    public String toString() {
        return "SyncEvent{" +
            "gitUrl='" + gitUrl + '\'' +
            ", repo='" + repo + '\'' +
            ", syncType=" + syncType +
            "} " + super.toString();
    }
}
