package com.neu.dimple.springbootapplication.controller.accountcontroller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.neu.dimple.springbootapplication.persistance.accountpersistance.AccountPersistance;
import com.neu.dimple.springbootapplication.repository.accountrepository.AccountRepository;
import org.checkerframework.checker.units.qual.A;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcExtensionsKt;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Dimpleben Kanjibhai Patel
 */
@SpringBootTest(classes = AccountControllerTest.class)
//@AutoConfigureMockMvc
@RunWith(MockitoJUnitRunner.class)
public class AccountControllerTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountController accountController;

//    @Autowired
    private MockMvc mockMvc;

    ObjectMapper mapper = new ObjectMapper();
    ObjectWriter objectWriter = mapper.writer();

    AccountPersistance accountPersistance = new AccountPersistance("Dimple", "Patel", "dimplepatel@gmail.com", "Dimple12345");

    @Before
    public void setUp() throws Exception{
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();
    }

    @Test
    public void accountCreateTest() throws Exception{
        UUID uuid = UUID.randomUUID();

        AccountPersistance account = new AccountPersistance(uuid, "Dimple", "Patel", "dimplepatel@gmail.com",
                "Dimple12345", new Date(), new Date());

        Mockito.when(accountRepository.save(accountPersistance)).thenReturn(account);
        Mockito.when(accountRepository.findByUsername("dimplepate@gmail.com")).thenReturn(null);

        MockHttpServletRequestBuilder mockReqeust = MockMvcRequestBuilders
                .post("/v1/account")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .accept(MediaType.APPLICATION_JSON)
                .content(objectWriter.writeValueAsString(accountPersistance));

        mockMvc.perform(mockReqeust)
                .andExpect(status().isBadRequest());

    }


}
