package ch.uzh.ifi.imrg.patientapp.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import ch.uzh.ifi.imrg.patientapp.entity.Patient;
import ch.uzh.ifi.imrg.patientapp.repository.PatientRepository;
import ch.uzh.ifi.imrg.patientapp.utils.PasswordUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

@Component
public class CoachKeyFilter extends OncePerRequestFilter {

    @Autowired
    private PatientRepository patientRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // return true if you want to skip doFilterInternal()
        return "/coach/patients/register".equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // only intercept /coach/patients/{patientId}/**
        if (path.startsWith("/coach/patients/")) {
            // extract patientId from the path

            String[] parts = path.split("/");
            if (parts.length < 4) {
                response.sendError(HttpStatus.BAD_REQUEST.value(), "Invalid URL");
                return;
            }
            String patientId = parts[3]; // "", "coach", "patients", "{id}", ...

            // header must be present
            String rawKey = request.getHeader("X-Coach-Key");

            if (rawKey == null || rawKey.isBlank()) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Missing X-Coach-Key");
                return;
            }

            // load patient
            Patient patient = patientRepository.findById(patientId).orElse(null);
            if (patient == null) {
                response.sendError(HttpStatus.NOT_FOUND.value(), "Patient not found");
                return;
            }

            // verify matches stored hash
            if (!PasswordUtil.checkPassword(rawKey, patient.getCoachAccessKey())) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid access key");
                return;
            }

            // build Authentication and set in context
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    /* principal */ patient.getId(),
                    /* credentials */ rawKey,
                    /* authorities */ List.of(new SimpleGrantedAuthority("ROLE_COACH")));
            SecurityContextHolder.getContext().setAuthentication(auth);

        }

        chain.doFilter(request, response);
    }
}
