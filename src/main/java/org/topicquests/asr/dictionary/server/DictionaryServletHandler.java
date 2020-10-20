/**
 * Copyright 2019, TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.asr.dictionary.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.topicquests.asr.dictionary.DictionaryServerEnvironment;
import org.topicquests.asr.dictionary.server.api.IDictionaryServerModel;
import org.topicquests.support.api.IResult;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

/**
 * @author jackpark
 *
 */
public class DictionaryServletHandler extends HttpServlet {
	private static final long serialVersionUID = 1L;
	/**
	 * 200
	 */
	public static final int RESPONSE_OK = HttpServletResponse.SC_OK;
	/**
	 * 400
	 */
	public static final int RESPONSE_BAD = HttpServletResponse.SC_BAD_REQUEST;
	/**
	 * 401
	 */
	public static final int RESPONSE_UNAUTHORIZED = HttpServletResponse.SC_UNAUTHORIZED;
	/**
	 * 403
	 */
	public static final int RESPONSE_FORBIDDEN = HttpServletResponse.SC_FORBIDDEN;
	/**
	 * 404
	 */
	public static final int RESPONSE_NOT_FOUND = HttpServletResponse.SC_NOT_FOUND;
	/**
	 * 407
	 */
	public static final int RESPONSE_AUTHENTICATION_REQUIRED = HttpServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED;
	/**
	 * 500
	 */
	public static final int RESPONSE_INTERNAL_SERVER_ERROR = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
	
	private DictionaryServerEnvironment environment;
	private IDictionaryServerModel model = null;

	public DictionaryServletHandler(DictionaryServerEnvironment env) {
		environment = env;
		model = environment.getPostgresModel();
		environment.logDebug("BootingServletHandler "+model);
	}
	
	public void executeGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject x = processRequest(request);
		environment.logDebug("DSH-1 "+x);
		if (x == null) {
			JSONObject msg = new JSONObject();
			msg.put("msg", "missing query");
			sendJSON(msg.toJSONString(), RESPONSE_BAD, response);
	
		} else {
			IResult r = model.handleRequest(x);
			JSONObject jo = (JSONObject)r.getResultObject();
			//Two cases:
			environment.logDebug("DictServer.executeGet "+jo);
			if (jo == null) {
				JSONObject msg = new JSONObject();
				if (r.hasError()) {
					msg.put("msg", r.getErrorString());
					sendJSON(msg.toJSONString(), RESPONSE_BAD, response);
				} else {
					msg.put("msg", "ok");
					sendJSON(msg.toJSONString(), RESPONSE_OK, response);
				}
				
			} else {
				sendJSON(jo.toJSONString(), RESPONSE_OK, response);
			} 
		}
	}

	public void executePost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		executeGet(request, response);
	}
	
	void sendJSON(String json, int statusCode, HttpServletResponse response) throws ServletException {
    	System.out.println("SENDJSON "+statusCode+" "+json);
    	try {
	    	response.setContentType("application/json; charset=UTF-8");
	    	response.setStatus(statusCode);
	        PrintWriter out = response.getWriter();
	    	out.write(json);
	    	out.close();
    	} catch (Exception e) {
    		throw new ServletException(e);
    	}
    }
	
	/**
	 * Can return {@code null}
	 * @param request
	 * @return
	 * @throws ServletException
	 */
	JSONObject processRequest(HttpServletRequest request) throws ServletException {
		JSONObject result = null;
		String pt = getPath(request);
		environment.logDebug("DictionaryServletHandler.processRequest "+pt);
		if (pt == null || pt.contentEquals(""))
			return result;
		if (!pt.startsWith("{")) {
			int where = pt.indexOf('/');
			if (where > -1)  {
				pt = pt.substring(where+1);
			}
		}
		result = jsonFromString(pt);
		
		return result;
	}
	
	JSONObject jsonFromString(String jsonString) throws ServletException {
		environment.logDebug("JSONFROMSTRING "+jsonString);
		//NOTE: there are edge conditions:
		//  jsonString == ""  can happen
		environment.logDebug("DictionaryServletHandler.jsonFromString "+jsonString);
		JSONParser p = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
		try {
			return (JSONObject)p.parse(jsonString);
		} catch (Exception e) {
			environment.logError(jsonString, e);
			throw new ServletException(e);
		}
	}
	
	String getPath(HttpServletRequest request) throws ServletException {
    	String pi = request.getPathInfo().trim();
    	environment.logDebug("DictionaryServletHandler.getPath "+pi);
		String path = notNullString(pi);
		StringBuilder buf = null;
    	try {
    		InputStream ins = request.getInputStream();
    		if (ins != null) {
    			buf = new StringBuilder();
    			int c;
    			while ((c = ins.read()) > -1)
    				buf.append((char)c);
 //       		System.out.println("GP "+buf.toString());
    			
    		}
    	} catch (Exception x) {
    		environment.logError("DictionaryServletHandler.getPath booboo "+x.getMessage(), x);
    	}
//		System.out.println("GP-2 "+path);
		if (buf != null)
	    	path = buf.toString();
		else if (!path.equals("")) {
	    	if (path.startsWith("/"))
	    		path = path.substring(1);
	    	if (path.endsWith("/"))
	    		path = path.substring(0,path.length()-1);
	    } 
    	try {
    		path = URLDecoder.decode(path, "UTF8");
    	} catch (Exception e) {
    		throw new ServletException(e);
    	}
    	if (path != null && path.startsWith("/"))
    		path = path.substring(1);
 
	
 //   	System.out.println("GP+ "+path);
    	return path;
    }
	
	String notNullString(String in) {
    	if (in == null) return "";
    	return in;
    }
	
	public void shutDown() throws Exception {
		System.out.println("DictionaryServletHandler.shutDown");
	}
}
