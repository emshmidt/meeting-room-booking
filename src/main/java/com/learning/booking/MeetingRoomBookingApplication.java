package com.learning.booking;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
		info = @Info(
				title = "API бронирования комнат",
				version = "v1",
				description = "При запуске сервер выбирает демонстрационного пользователя "
						+ "по настройке app.demo-user. Данные хранятся в памяти "
						+ "и сбрасываются при перезапуске. Аутентификация запланирована."
		)
)
@SpringBootApplication
public class MeetingRoomBookingApplication {

	public static void main(String[] args) {
		SpringApplication.run(MeetingRoomBookingApplication.class, args);
	}

}
