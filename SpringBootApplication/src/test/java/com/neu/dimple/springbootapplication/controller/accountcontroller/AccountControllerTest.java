package com.neu.dimple.springbootapplication.controller.accountcontroller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neu.dimple.springbootapplication.persistance.accountpersistance.AccountPersistance;
import com.neu.dimple.springbootapplication.repository.accountrepository.AccountRepository;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.junit.Assert;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Dimpleben Kanjibhai Patel
 */
@SpringBootTest(classes = AccountControllerTest.class)
@AutoConfigureMockMvc
public class AccountControllerTest {


    @MockBean
    private AccountRepository accountRepository;
    @Autowired
    private MockMvc mockMvc;

//    @Autowired
    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void accountCreateTest() throws Exception{
//        UUID uuid = UUID.randomUUID();
//
//        AccountPersistance account = new AccountPersistance(uuid, "Dimple", "Patel", "dimplepatel@gmail.com",
//                "Dimple12345", new Date(), new Date());
//
////        ArrayList<AccountPersistance> list = new ArrayList<>(Arrays.asList(account));
//
//        JSONObject accountJson = new JSONObject();
//        accountJson.put("first_name" , "Dimple");
//        accountJson.put("last_name" , "Patel");
//        accountJson.put("username" , "dimplepatel@gmail.com");
//        accountJson.put("password" , "Dimple12345");
//
//
//        Mockito.when(accountRepository.save(account)).thenReturn(account);
//
//        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders
//                .post("/v1/account")
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaType.ALL)
//                .content(this.mapper.writeValueAsString(account));
//
//        mockMvc.perform(mockRequest)
//                .andExpect(status().isOk());

//        RequestBuilder requestBuilder = MockMvcRequestBuilders
//                .post("/v1/account")
//                .accept(MediaType.APPLICATION_JSON)
//                .content(String.valueOf(accountJson))
//                .contentType(MediaType.APPLICATION_JSON);
//
//        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
//
//        MockHttpServletResponse response = result.getResponse();
//
//        Assert.assertEquals(HttpStatus.CREATED.value(), response.getStatus());

    }


}
