package ch.uzh.ifi.imrg.patientapp.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class JwtUtil {

  private static SecretKey getSecretKey() {
    byte[] keyBytes = Decoders.BASE64.decode(EnvironmentVariables.getJwtSecretKey());
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public static String createJWT(String email) {
    long nowMillis = System.currentTimeMillis();
    long expMillis = nowMillis + 30L * 24 * 60 * 60 * 1000; // 30 days
    Date now = new Date(nowMillis);
    Date exp = new Date(expMillis);

    SecretKey key = getSecretKey();

    String jwt = Jwts.builder().subject(email).issuedAt(now).expiration(exp).signWith(key).compact();
    return jwt;
  }

  public static void addJwtCookie(
      HttpServletResponse response, HttpServletRequest request, String jwt) {
    Cookie cookie = new Cookie("auth", jwt);
    cookie.setHttpOnly(true);
    cookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
    cookie.setPath("/");
    String appCookieDomain = EnvironmentVariables.getAppCookieDomain();
    if (appCookieDomain != null && !appCookieDomain.isEmpty()) {
        cookie.setDomain(appCookieDomain);
    }
    cookie.setSecure(request.isSecure());
    cookie.setAttribute("SameSite", "Lax");
    response.addCookie(cookie);
  }

  public static void removeJwtCookie(HttpServletResponse response) {
    Cookie cookie = new Cookie("auth", null);
    cookie.setHttpOnly(true);
    cookie.setMaxAge(0);
    cookie.setPath("/");
    String appCookieDomain = EnvironmentVariables.getAppCookieDomain();
    if (appCookieDomain != null && !appCookieDomain.isEmpty()) {
        cookie.setDomain(appCookieDomain);
    }
    cookie.setSecure(false);
    cookie.setAttribute("SameSite", "Lax");
    response.addCookie(cookie);

  }

  public static String validateJWTAndExtractEmail(HttpServletRequest request) {

    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Cookie could not be found");
    }

    for (Cookie cookie : cookies) {
      if ("auth".equals(cookie.getName())) {
        try {
          String jwt = cookie.getValue();
          SecretKey key = getSecretKey();

          Jws<Claims> claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(jwt);

          String jwtEmail = claims.getPayload().getSubject();

          Date expiration = claims.getPayload().getExpiration();
          if (expiration.before(new Date())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The JWT has expired");
          }
          return jwtEmail;

        } catch (Exception e) {
          throw new ResponseStatusException(
              HttpStatus.UNAUTHORIZED, "There was a problem with the cookie");
        }
      }
    }
    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Cookie 'auth' could not be found");
  }
}
