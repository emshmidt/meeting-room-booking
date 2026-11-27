package com.learning.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("memory")
class MeetingRoomBookingApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@ParameterizedTest
	@CsvSource(delimiter = '|', value = {
			"null {} | MALFORMED_JSON",
			"{\"slotId\":\"00000000-0000-0000-0000-000000000002\"} {} | MALFORMED_JSON",
			"null | VALIDATION_ERROR",
			"[] | VALIDATION_ERROR"
	})
	void shouldClassifyBookingJsonRootErrors(String body, String expectedCode) throws Exception {
		mockMvc.perform(post("/api/v1/bookings")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value(expectedCode))
				.andExpect(jsonPath("$.fieldErrors").isArray())
				.andExpect(jsonPath("$.fieldErrors").isEmpty());
	}

}
