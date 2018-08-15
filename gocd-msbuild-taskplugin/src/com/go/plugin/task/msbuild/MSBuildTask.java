package com.go.plugin.task.msbuild;

import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.annotation.Load;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.info.PluginContext;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoApiResponse;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.plugin.api.task.*;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.*;

import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

@Extension
public class MSBuildTask implements GoPlugin {
	public static final String CUSTOMIZEMSBUILDPATH = "CustomizeMSBuildPath"; 
    public static final String MSBUILDPATH = "MSBuildPath";
    public static final String SOLUTIONFILE = "SolutionFile";
    public static final String PROPERTIES = "Properties";
    public static final String VERBOSITY = "Verbosity";
    public static final String SPECIFYTARGETS = "SpecifyTargets";
    public static final String TARGETS = "Targets";
    public static final String ADDITIONALPARAMETERS = "AdditionalParameters";
    public static final String DETAILEDSUMMARY = "DetailedSummary";
    public static final String NOLOGO = "NoLogo";
    public static final String NOAUTORESPONSE = "NoAutoResponse";
    public static final String FILELOGGER = "FileLogger";
    public static final String WORKINGDIRECTORY = "WorkingDirectory";
    
    public final static String CUSTOMIZEMSBUILDPATH_DEFAULTVALUE = "false";
    public final static String MSBUILDPATH_DEFAULTVALUE = "\"\"%PROGRAMFILES(X86)%\\MSBuild\\14.0\\Bin\\MsBuild.exe\"\"";

    public static final String PLUGIN_SETTINGS_GET_CONFIGURATION = "go.plugin-settings.get-configuration";
    
    private static Logger logger = Logger.getLoggerFor(MSBuildTask.class);
    
    protected boolean propertiesValid(String properties){
    	String props[] = properties.split("\\r?\\n"); //split properties on newline
    	Pattern regex = Pattern.compile("\\w+=\\w+"); //properties specified like 'propname=propvalue'
    	for(String prop : props) {
    		prop = prop.replaceAll("\\s", ""); //strip all whitespace from property
    		Matcher matcher = regex.matcher(prop);
    		
    		if(!matcher.matches()) 
    			return false;    	
    	}
    	return true;
    }

    private GoApplicationAccessor goApplicatiojnAccessor;
    
    @Load
    public void onLoad(PluginContext ctx)
    {
    	logger.info("Loading plugin MSBuild");
    }
    
	@Override
	public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {
		this.goApplicatiojnAccessor = goApplicationAccessor;
	}

	@Override
	public GoPluginApiResponse handle(GoPluginApiRequest goPluginApiRequest) {
		if ("configuration".equals(goPluginApiRequest.requestName())) {
            return handleGetConfigRequest();
        } else if ("validate".equals(goPluginApiRequest.requestName())) {
            return handleValidation(goPluginApiRequest);
        } else if ("execute".equals(goPluginApiRequest.requestName())) {
            return handleTaskExecution(goPluginApiRequest);
        } else if ("view".equals(goPluginApiRequest.requestName())) {
            return handleTaskView();
        }
        return createResponse(404,null);
	}

	@Override
	public GoPluginIdentifier pluginIdentifier() {
		return new GoPluginIdentifier("cendyn.msbuildplugin", Arrays.asList("1"));
	}
	
	private GoPluginApiResponse handleGetConfigRequest() {
        HashMap<String,Object> config = new HashMap<String,Object>();

        HashMap<String,Object> customizemsbuildpath = new HashMap<String,Object>();
        customizemsbuildpath.put("default-value", false);
        customizemsbuildpath.put("display-order", "1");
        customizemsbuildpath.put("display-name", "Enter Customer Path to MSBuild.exe");
        customizemsbuildpath.put("required", false);
        config.put(CUSTOMIZEMSBUILDPATH, customizemsbuildpath);
        
        HashMap<String,Object> msbuildpath = new HashMap<String,Object>();
        msbuildpath.put("default-value", MSBUILDPATH_DEFAULTVALUE);
        msbuildpath.put("display-order", "2");
        msbuildpath.put("display-name", "Path to MSBuild.exe on Agent");
        msbuildpath.put("required", false);
        config.put(MSBUILDPATH, msbuildpath);
        
        HashMap<String,Object> solutionfile = new HashMap<String,Object>();
        solutionfile.put("default-value", "");
        solutionfile.put("display-order", "3");
        solutionfile.put("display-name", "Solution File");
        solutionfile.put("required", true);
        config.put(SOLUTIONFILE, solutionfile);
        
        HashMap<String,Object> workingdirectory = new HashMap<String,Object>();
        workingdirectory.put("default-value", "");
        workingdirectory.put("display-order", "4");
        workingdirectory.put("display-name", "Working Directory");
        workingdirectory.put("required", true);
        config.put(WORKINGDIRECTORY, workingdirectory);
        
        HashMap<String,Object> properties = new HashMap<String,Object>();
        workingdirectory.put("default-value", "");
        workingdirectory.put("display-order", "5");
        workingdirectory.put("display-name", "Properties");
        workingdirectory.put("required", false);
        config.put(PROPERTIES, properties);
        
        HashMap<String,Object> verbosity = new HashMap<String,Object>();
        verbosity.put("default-value", "");
        verbosity.put("display-order", "6");
        verbosity.put("display-name", "Verbosity");
        verbosity.put("required", false);
        config.put(VERBOSITY, verbosity);

        HashMap<String,Object> specifytargets = new HashMap<String,Object>();
        specifytargets.put("default-value", false);
        specifytargets.put("display-order", "7");
        specifytargets.put("display-name", "Specify Target(s)?");
        specifytargets.put("required", false);
        config.put(SPECIFYTARGETS, specifytargets);
        
        HashMap<String,Object> targets = new HashMap<String,Object>();
        targets.put("default-value", "");
        targets.put("display-order", "8");
        targets.put("display-name", "Target(s)");
        targets.put("required", false);
        config.put(TARGETS, targets);

        HashMap<String,Object> additionalparameters = new HashMap<String,Object>();
        additionalparameters.put("default-value", "");
        additionalparameters.put("display-order", "9");
        additionalparameters.put("display-name", "Additional Parameters");
        additionalparameters.put("required", false);
        config.put(ADDITIONALPARAMETERS, additionalparameters);
        
        HashMap<String,Object> filelogger = new HashMap<String,Object>();
        filelogger.put("default-value", false);
        filelogger.put("display-order", "10");
        filelogger.put("display-name", "Enable File Logger(logs to MSBuild.log in current directory");
        filelogger.put("required", false);
        config.put(FILELOGGER, filelogger);
        
        HashMap<String,Object> detailedsummary = new HashMap<String,Object>();
        detailedsummary.put("default-value", false);
        detailedsummary.put("display-order", "11");
        detailedsummary.put("display-name", "Show detailed summay");
        detailedsummary.put("required", false);
        config.put(DETAILEDSUMMARY, detailedsummary);

        HashMap<String,Object> nologo = new HashMap<String,Object>();
        nologo.put("default-value", false);
        nologo.put("display-order", "12");
        nologo.put("display-name", "Start MSBuild without logo");
        nologo.put("required", false);
        config.put(NOLOGO, nologo);
        
        HashMap<String,Object> noautoresponse = new HashMap<String,Object>();
        noautoresponse.put("default-value", false);
        noautoresponse.put("display-order", "13");
        noautoresponse.put("display-name", "Don't automatically include MSBuild.rsp file");
        noautoresponse.put("required", false);
        config.put(NOAUTORESPONSE, noautoresponse);

        return createResponse(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE, config);
    }
	
    private GoPluginApiResponse createResponse(int responseCode, Map body) {
        final DefaultGoPluginApiResponse response = new DefaultGoPluginApiResponse(responseCode);
        response.setResponseBody(new GsonBuilder().serializeNulls().create().toJson(body));
        return response;
    }
    
    private GoPluginApiResponse handleValidation(GoPluginApiRequest request) {
        HashMap<String,Object> validationResult = new HashMap<String,Object>();
        int responseCode = DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE;
        
        Map configMap = (Map) new GsonBuilder().create().fromJson(request.requestBody(), Object.class);
        HashMap<String,Object> errorMap = new HashMap<String,Object>();

        
        Object customizemsbuildpath = configMap.get(CUSTOMIZEMSBUILDPATH);
        if(customizemsbuildpath != null && ((String)customizemsbuildpath).equals("true")) {
        	String msbuildpath = (String)configMap.get(MSBUILDPATH);
        	if(msbuildpath == null || StringUtils.isBlank(msbuildpath)) {
        		errorMap.put(MSBUILDPATH, "Path to MSBuild.exe must be specified");
        	}
        }

        String solutionfile = (String)configMap.get(SOLUTIONFILE);
        if (StringUtils.isBlank(solutionfile)) {
        	errorMap.put(SOLUTIONFILE, "A Solution file must be specified");
        }
        
        String properties = (String)configMap.get(PROPERTIES);
        if (!StringUtils.isBlank(properties) && !propertiesValid(properties)) {
        	errorMap.put(PROPERTIES, "Invalid entry for Properties - make sure one property per line formatted like propName=propValue");
        }
        
        String specifyTargets = (String)configMap.get(SPECIFYTARGETS);
        if(specifyTargets.equals("true")) {
        	String targets = (String)configMap.get(TARGETS);
        	if(StringUtils.isBlank(targets)) {
        		errorMap.put(TARGETS, "If SpecifyTargets is checked, Targets cannot be empty.");
        	}
        }
        
        String additionalParams = (String)configMap.get(ADDITIONALPARAMETERS);
    	if(additionalParams != null && !additionalParams.isEmpty()) {
	    	String splitParams[] = additionalParams.split("[\r\n]+"); 
	    	for(String param : splitParams){
	    		param = param.replaceAll("\\s+", "");
	    		if(!(param.charAt(0) == '/')) {
	    			errorMap.put(ADDITIONALPARAMETERS, "Parameters must start with forward slash: /parameter:value");
	    		}
	    	}
    	}
    	
    	if(errorMap.size() != 0)
    	{
    		responseCode = DefaultGoPluginApiResponse.VALIDATION_FAILED;
    	}
    	validationResult.put("errors", errorMap);
        return createResponse(responseCode, validationResult);
    }
    
    private GoPluginApiResponse handleTaskExecution(GoPluginApiRequest request) {
        MSBuildTaskExecutor executor = new MSBuildTaskExecutor();
        Map executionRequest = (Map) new GsonBuilder().create().fromJson(request.requestBody(), Object.class);
        Map config = (Map) executionRequest.get("config");
        Map context = (Map) executionRequest.get("context");

        Result result = executor.execute(new Config(config), new Context(context), JobConsoleLogger.getConsoleLogger());
        return createResponse(result.responseCode(), result.toMap());
    }
    
    private GoPluginApiResponse handleTaskView() {
        int responseCode = DefaultGoApiResponse.SUCCESS_RESPONSE_CODE;
        HashMap view = new HashMap();
        view.put("displayValue", "Curl");
        try {
            view.put("template", IOUtils.toString(getClass().getResourceAsStream("/views/task.template.html"), "UTF-8"));
        } catch (Exception e) {
            responseCode = DefaultGoApiResponse.INTERNAL_ERROR;
            String errorMessage = "Failed to find template: " + e.getMessage();
            view.put("exception", errorMessage);
            logger.error(errorMessage, e);
        }
        return createResponse(responseCode, view);
    }
    

}