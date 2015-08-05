/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.web.controller;

import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.web.dao.AlarmDao;

/**
 * @author minwoo.jung
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:servlet-context.xml", "classpath:applicationContext-web.xml"})
public class AlarmControllerTest {
    private final static String APPLICATION_ID = "test-application";
    private final static String USER_GROUP_ID = "test-pinpoint_group";
    private final static String CHECKER_NAME = "ERROR_COUNT";
    private final static int THRESHOLD = 100;
    private final static boolean  SMS_SEND = false;
    private final static boolean  EMAIL_SEND = true;
    private final static String NOTES = "for unit test";
    
    @Autowired
    private WebApplicationContext wac;
    
    @Autowired
    private AlarmDao alarmDao;

    private MockMvc mockMvc;
    
    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.alarmDao.deleteRule(USER_GROUP_ID);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void insertRule() throws Exception {
        String jsonParm = "{" +
                            "\"applicationId\" : \"" + APPLICATION_ID + "\"," + 
                            "\"userGroupId\" : \"" + USER_GROUP_ID + "\"," + 
                            "\"checkerName\" : \"" + CHECKER_NAME + "\"," + 
                            "\"threshold\" : " + THRESHOLD + "," + 
                            "\"smsSend\" : " + SMS_SEND + "," + 
                            "\"emailSend\" : \"" + EMAIL_SEND  + "\"," + 
                            "\"notes\" : \"" + NOTES + "\"" + 
                          "}"; 
                           
        MvcResult result = this.mockMvc.perform(post("/alarmRule.pinpoint").contentType(MediaType.APPLICATION_JSON).content(jsonParm))
                                            .andExpect(status().isOk())
                                            .andExpect(content().contentType("application/json;charset=UTF-8"))
                                            .andReturn();
        String content = result.getResponse().getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> resultMap = objectMapper.readValue(content, HashMap.class);
        Assert.assertEquals(resultMap.get("result"), "SUCCESS");
        Assert.assertNotNull(resultMap.get("ruleId"));
        
        this.mockMvc.perform(delete("/alarmRule.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{\"ruleId\" : \"" + resultMap.get("ruleId") + "\"}"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType("application/json;charset=UTF-8"))
                        .andExpect(jsonPath("$", hasKey("result")))
                        .andExpect(jsonPath("$.result").value("SUCCESS"))
                        .andReturn();
    }
    
}
