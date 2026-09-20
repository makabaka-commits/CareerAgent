package dev.careeragent.auth;
import dev.careeragent.infrastructure.InMemoryStore;import org.junit.jupiter.api.*;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.boot.test.context.SpringBootTest;import org.springframework.http.MediaType;import org.springframework.jdbc.core.simple.JdbcClient;import org.springframework.test.web.servlet.*;import org.springframework.web.context.WebApplicationContext;import tools.jackson.databind.ObjectMapper;import java.util.Map;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:auth;MODE=PostgreSQL;DB_CLOSE_DELAY=-1","app.persistence.flush-ms=600000","app.rate-limit.mode=local","spring.ai.model.chat=none"})
class AuthIntegrationTest{
 MockMvc mvc;@Autowired WebApplicationContext context;@Autowired ObjectMapper json;@Autowired InMemoryStore store;@Autowired JdbcClient jdbc;
 @BeforeEach void setup(){mvc=webAppContextSetup(context).apply(springSecurity()).build();}
 @Test void protectsApiAndAllowsRegisteredUser()throws Exception{
  int landingStatus=mvc.perform(get("/")).andReturn().getResponse().getStatus();Assertions.assertNotEquals(401,landingStatus);
  mvc.perform(get("/api/v1/profiles/me")).andExpect(status().isUnauthorized());
  String response=mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"tester\",\"email\":\"tester@example.com\",\"password\":\"secret12\"}")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
  Map<?,?> root=json.readValue(response,Map.class);String token=(String)((Map<?,?>)root.get("data")).get("token");
  mvc.perform(get("/api/v1/profiles/me").header("Authorization","Bearer "+token)).andExpect(status().isOk()).andExpect(jsonPath("$.data.major").value("计算机科学与技术"));
  store.flush();Assertions.assertEquals(1L,jdbc.sql("SELECT COUNT(*) FROM user_account WHERE username='tester'").query(Long.class).single());
  mvc.perform(post("/api/v1/auth/logout").header("Authorization","Bearer "+token)).andExpect(status().isOk());
  mvc.perform(get("/api/v1/profiles/me").header("Authorization","Bearer "+token)).andExpect(status().isUnauthorized());
 }
}
