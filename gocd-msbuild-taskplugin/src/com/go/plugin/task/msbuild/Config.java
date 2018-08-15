/*************************GO-LICENSE-START*********************************
 * Copyright 2014 ThoughtWorks, Inc.
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
 *************************GO-LICENSE-END***********************************/

package com.go.plugin.task.msbuild;

import java.util.Map;

public class Config {    
    private final String customizeMSBuildPath;
    private final String msBuildPath;
    private final String solutionFile;
    private final String properties;
    private final String verbosity;
    private final String specifyTargets;
    private final String targets;
    private final String additionalParameters;
    private final String fileLogger;
    private final String detailedSummary;
    private final String noLogo;
    private final String noAutoResponse;
    private final String workingDirectory;
    
    public Config(Map config) {
    	customizeMSBuildPath = getValue(config, MSBuildTask.CUSTOMIZEMSBUILDPATH);
    	msBuildPath = getValue(config, MSBuildTask.MSBUILDPATH);
    	solutionFile = getValue(config, MSBuildTask.SOLUTIONFILE);
    	properties = getValue(config, MSBuildTask.PROPERTIES);
    	verbosity = getValue(config, MSBuildTask.VERBOSITY);
    	specifyTargets = getValue(config, MSBuildTask.SPECIFYTARGETS);
    	targets = getValue(config, MSBuildTask.TARGETS);
    	additionalParameters = getValue(config, MSBuildTask.ADDITIONALPARAMETERS);
    	fileLogger = getValue(config, MSBuildTask.FILELOGGER);
    	detailedSummary = getValue(config, MSBuildTask.DETAILEDSUMMARY);
    	noLogo = getValue(config, MSBuildTask.NOLOGO);
    	noAutoResponse = getValue(config, MSBuildTask.NOAUTORESPONSE);
    	workingDirectory = getValue(config, MSBuildTask.WORKINGDIRECTORY);
    	
    }

    private String getValue(Map config, String property) {
        return (String) ((Map) config.get(property)).get("value");
    }

    public String getCustomizeMSBuildPath() {
        return customizeMSBuildPath;
    }

    public String getMSBuildPath() {
        return msBuildPath;
    }
    public String getSolutionFile() {
        return solutionFile;
    }
    public String getProperties() {
        return properties;
    }
    public String getVerbosity() {
        return verbosity;
    }
    public String getSpecifyTargets() {
        return specifyTargets;
    }
    public String getTargets() {
        return targets;
    }
    public String getAdditionalParameters() {
        return additionalParameters;
    }
    public String getFileLogger() {
        return fileLogger;
    }
    public String getDetailedSummary() {
        return detailedSummary;
    }
    public String getNoLogo() {
        return noLogo;
    }
    public String getNoAutoResponse() {
        return noAutoResponse;
    }
    public String getWorkingDirectory() {
        return workingDirectory;
    }
    
}