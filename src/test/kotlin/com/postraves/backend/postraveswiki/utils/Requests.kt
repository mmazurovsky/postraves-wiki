package com.postraves.backend.postraveswiki.utils

import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers

object Requests {

    fun makePostRequest(mockMvc: MockMvc, endpoint: String, bodyJson: String, expectedStatus: ResultMatcher): String {
        val mvcResult: MvcResult = mockMvc.perform(
            MockMvcRequestBuilders.post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(bodyJson)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(expectedStatus)
            .andReturn()
        return mvcResult.response.contentAsString
    }

    fun makePutRequest(mockMvc: MockMvc, endpoint: String, bodyJson: String, expectedStatus: ResultMatcher): String {
        val mvcResult: MvcResult = mockMvc.perform(
            MockMvcRequestBuilders.put(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(bodyJson)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(expectedStatus)
            .andReturn()
        return mvcResult.response.contentAsString
    }

    fun makeGetRequest(mockMvc: MockMvc, endpoint: String, expectedStatus: ResultMatcher): String {
        val mvcResult: MvcResult = mockMvc.perform(
            MockMvcRequestBuilders.get(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(expectedStatus)
            .andReturn()
        return mvcResult.response.contentAsString
    }

    fun makeDeleteRequest(mockMvc: MockMvc, endpoint: String, expectedStatus: ResultMatcher): String {
        val mvcResult: MvcResult = mockMvc.perform(
            MockMvcRequestBuilders.delete(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(expectedStatus)
            .andReturn()
        return mvcResult.response.contentAsString
    }
}
