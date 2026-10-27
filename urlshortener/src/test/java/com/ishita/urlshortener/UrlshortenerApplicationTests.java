package com.ishita.urlshortener;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Full-context smoke test. It needs live PostgreSQL, Redis and Kafka, so it is
// disabled by default and `./mvnw test` runs standalone. Remove @Disabled to run it
// against a local stack.
@Disabled("Requires running PostgreSQL, Redis and Kafka")
@SpringBootTest
class UrlshortenerApplicationTests {

	@Test
	void contextLoads() {
	}

}
