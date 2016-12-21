package ch.ge.ve.protopoc.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import spock.lang.Specification

import static org.hamcrest.Matchers.equalTo
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Missing javadoc!
 */
@SpringBootTest
@AutoConfigureMockMvc
class HelloControllerTestSpec extends Specification {
    @Autowired
    private WebApplicationContext context

    MockMvc mvc

    @WithMockUser
    "index should display greeting"() {
        setup:
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build()

        expect: "Application should be mocked properly"
        mvc != null

        when: "call to the index page"
        def resultActions = mvc.perform(MockMvcRequestBuilders.get("/").accept(MediaType.APPLICATION_JSON))

        then: "status is ok"
        resultActions.andExpect(status().isOk())

        and: "the expected message is printed"
        resultActions.andExpect(content().string(equalTo("Greetings from Spring Boot!")))
    }
}
