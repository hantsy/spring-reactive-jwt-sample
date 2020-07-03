package com.example.demo;

import com.example.demo.web.CurrentUserController;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(controllers = CurrentUserController.class)
@Slf4j
public class CurrentUserControllerTest {

	@Autowired
	private WebTestClient client;

	@Test
	@WithMockUser()
	public void testCurrentUser() {
		this.client.get().uri("/me").exchange().expectBody().jsonPath("$.name").isEqualTo("user").jsonPath("$.roles")
				.isArray().jsonPath("$.roles[0]").isEqualTo("ROLE_USER");
	}

}
