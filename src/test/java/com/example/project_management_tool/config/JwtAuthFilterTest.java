package com.example.project_management_tool.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    private final JwtService jwtService = mock(JwtService.class);
    private final UserDetailsService userDetailsService = mock(UserDetailsService.class);

    private final JwtAuthFilter filter = new JwtAuthFilter(jwtService, userDetailsService);

    @AfterEach
    void tearDown() {

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldContinueChain_whenNoAuthorizationHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldContinueChain_whenAuthorizationHeaderIsNotBearer() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic abc.def.ghi");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldContinueChain_whenTokenParsingThrowsException() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid.token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(jwtService.extractEmail("invalid.token")).thenThrow(new RuntimeException("boom"));

        filter.doFilter(request, response, chain);

        verify(jwtService, times(1)).extractEmail("invalid.token");
        verifyNoInteractions(userDetailsService);
        verify(chain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldAuthenticateAndContinueChain_whenValidBearerTokenAndNoExistingAuth() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer good.token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(jwtService.extractEmail("good.token")).thenReturn("john@example.com");

        UserDetails userDetails = User.withUsername("john@example.com")
                .password("N/A")
                .authorities("ROLE_USER")
                .build();

        when(userDetailsService.loadUserByUsername("john@example.com")).thenReturn(userDetails);

        filter.doFilter(request, response, chain);

        // auth bien posée
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("john@example.com", SecurityContextHolder.getContext().getAuthentication().getName());

        verify(jwtService, times(1)).extractEmail("good.token");
        verify(userDetailsService, times(1)).loadUserByUsername("john@example.com");
        verify(chain, times(1)).doFilter(request, response);
    }
}
