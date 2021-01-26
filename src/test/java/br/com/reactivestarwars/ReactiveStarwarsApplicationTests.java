package br.com.reactivestarwars;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ExtendWith(LocalDynamoExtension.class)
class ReactiveStarwarsApplicationTests {

	@Test
	void contextLoads() {
	}

}
