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
package com.flow.platform.api.domain;

/**
 * @author yh@fir.im
 */
public enum JobStatus {

    PENDING("PENDING", 0),

    RUNNING("RUNNING", 1),

    SUCCESS("SUCCESS", 2),

    FAIL("FAIL", 3),

    TIMEOUT("TIMEOUT", 4);

    private String name;

    public Integer getLevel() {
        return level;
    }

    private Integer level;

    JobStatus(String name, Integer level) {
        this.name = name;
        this.level = level;
    }

    public String getName() {
        return name;
    }
}

