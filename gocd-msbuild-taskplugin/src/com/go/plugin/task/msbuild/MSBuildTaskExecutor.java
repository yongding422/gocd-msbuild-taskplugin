package com.go.plugin.task.msbuild;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.io.FilenameUtils;

import com.go.plugin.task.msbuild.Result;
import com.go.plugin.task.msbuild.Config;
import com.go.plugin.task.msbuild.Context;
import com.thoughtworks.go.plugin.api.task.*;

import java.io.*;
import java.util.*;

public class MSBuildTaskExecutor {

	public Result execute(Config config, Context context, JobConsoleLogger console) {
		
        ProcessBuilder msbuild = createMSBuildCommand(context, config);

        console.printLine("---------------------------------------------------------------------------------------");
        console.printLine("|                         Starting MS Build Task                                      |");
        console.printLine("---------------------------------------------------------------------------------------");
        console.printLine("\nLaunching command: \n\t" + StringUtils.join(msbuild.command(), " "));

        try {
            Process process = msbuild.start();
            
            console.readErrorOf(process.getErrorStream());
            console.readOutputOf(process.getInputStream());

            int exitCode = process.waitFor();
            process.destroy();

            if (exitCode != 0) {
                return new Result(false,"Build Failure");
            }
        }
        catch(Exception e) {
            console.printLine(e.getMessage());
            return new Result(false,"Fail: Exception while running MSBuild task.\n" + e.getMessage());
        }
        
        return new Result(true,"Build Success");
    }

    ProcessBuilder createMSBuildCommand(Context taskContext, Config taskConfig) {

        List<String> command = new ArrayList<String>();

        String msBuildPath = "C:\\Windows\\Microsoft.NET\\Framework\\v4.0.30319\\MSBuild.exe";
        String customizeMSBuildPath = taskConfig.getCustomizeMSBuildPath();
        if(customizeMSBuildPath != null && customizeMSBuildPath.equals("true")) {
        	msBuildPath = taskConfig.getMSBuildPath();
        }
        command.add(msBuildPath);
        
        AddMSBuildArguments(taskConfig, command);
        AddAdditionalParameters(taskConfig, command);
        AddProjectFile(taskConfig, command);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(new File(taskContext.getWorkingDir()));
        
        return processBuilder;
    }

    private void AddProjectFile(Config taskConfig, List<String> command) {
    	//Prepend "working directory" containing solution file. Default value: "."
    	String workingDirectory = taskConfig.getWorkingDirectory();
    	if(workingDirectory == null || workingDirectory.isEmpty()) {
    		workingDirectory = ".";
    	}
    	String solutionFile = taskConfig.getSolutionFile();
    	
        command.add(FilenameUtils.concat(workingDirectory, solutionFile));
    }

    private void AddMSBuildArguments(Config taskConfig, List<String> command) {
        String rawProperties = taskConfig.getProperties();
        if(rawProperties != null && !StringUtils.isEmpty(rawProperties)) {
        	String properties[] = rawProperties.split("[\r\n]+"); //split props by line
        	for(String prop : properties){
        		prop = prop.replaceAll("\\s+", ""); //strip any whitespace, leaving {property}={value}
        		command.add("/property:"+prop);
        	}
        }
        
        String verbosity = taskConfig.getVerbosity();
        if(verbosity != null && !StringUtils.isEmpty(verbosity)) {
        	command.add("/verbosity:" + verbosity);
        } else {
        	command.add("/verbosity:normal");
        }
        
        String specifyTargets = taskConfig.getSpecifyTargets();
        if(specifyTargets != null && specifyTargets.equals("true")) {
        	String targets = taskConfig.getTargets();
        	if(targets != null &&  !StringUtils.isEmpty(targets)) {
        		targets = targets.replaceAll("\\s+", "");
        		command.add("/targets:"+targets);
        	}
        }
        
        String fileLogger = taskConfig.getFileLogger();
        if (fileLogger != null && fileLogger.equals("true")) {
            command.add("/fileLogger");
        }
        
        String detailedSummary = taskConfig.getDetailedSummary();
        if (detailedSummary != null && detailedSummary.equals("true")) {
            command.add("/detailedsummary");
        }
        
        String noLogo = taskConfig.getNoLogo();
        if (noLogo != null && noLogo.equals("true")) {
            command.add("/nologo");
        }
        
        String noAutoResponse = taskConfig.getNoAutoResponse();
        if (noAutoResponse != null && noAutoResponse.equals("true")) {
            command.add("/noautoResponse");
        }
    }
    
    private void AddAdditionalParameters(Config taskConfig, List<String> command) {
    	String additionalParams = taskConfig.getAdditionalParameters();
    	if(additionalParams == null || additionalParams.isEmpty()) {
    		return;
    	}
    	String splitParams[] = additionalParams.split("[\r\n]+"); 
    	for(String param : splitParams){
    		//strip any whitespace from parameter leaving only 'propertyName'='value'
    		param = param.replaceAll("\\s+", ""); 
    		command.add(param);
    	}
    }
}