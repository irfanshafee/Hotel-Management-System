package com.hotelbooking.hotel_booking.service;
import at.favre.lib.crypto.bcrypt.BCrypt;
import com.hotelbooking.hotel_booking.dto.*; import com.hotelbooking.hotel_booking.entity.*; import com.hotelbooking.hotel_booking.enums.UserRole; import com.hotelbooking.hotel_booking.exception.ApiException; import com.hotelbooking.hotel_booking.repository.*;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom; import java.time.LocalDateTime; import java.util.Locale;
@Service public class SignupVerificationService {
 private static final SecureRandom RANDOM=new SecureRandom(); private static final int COOLDOWN_SECONDS=60, MAX_ATTEMPTS=5;
 private final UserRepository users; private final SignupVerificationRepository verifications; private final SignupOtpDelivery delivery; private final SignupOtpConfigurationService configuration;
 public SignupVerificationService(UserRepository u,SignupVerificationRepository v,SignupOtpDelivery d,SignupOtpConfigurationService c){users=u;verifications=v;delivery=d;configuration=c;}
 @Transactional(noRollbackFor=ApiException.class) public void start(RegisterRequest r){
  String email=norm(r.email()); if(users.existsByEmail(email)) throw new ApiException("duplicate.email"); LocalDateTime now=LocalDateTime.now(); SignupVerification v=verifications.findByEmail(email).orElse(null);
  if(v!=null && v.getResendAvailableAt().isAfter(now)) throw new ApiException("signup.otp.cooldown");
  if(v==null){v=new SignupVerification();v.setEmail(email);} OtpIssue otp=populate(v,r,now); verifications.save(v); send(v,email,otp);
 }
 @Transactional(noRollbackFor=ApiException.class) public AuthResponse verify(VerifySignupRequest r){
  String email=norm(r.email()); SignupVerification v=verifications.findByEmail(email).orElseThrow(()->new ApiException("signup.verification.not-found")); LocalDateTime now=LocalDateTime.now();
  if(!v.getExpiresAt().isAfter(now)) throw new ApiException("signup.otp.expired"); if(v.getAttemptCount()>=MAX_ATTEMPTS) throw new ApiException("signup.otp.attempts.exceeded");
  if(!BCrypt.verifyer().verify(r.otp().toCharArray(),v.getOtpHash()).verified){v.setAttemptCount(v.getAttemptCount()+1);verifications.save(v);throw new ApiException(v.getAttemptCount()>=MAX_ATTEMPTS?"signup.otp.attempts.exceeded":"signup.otp.invalid");}
  if(users.existsByEmail(email)) throw new ApiException("duplicate.email"); User u=new User();u.setName(v.getName());u.setAge(v.getAge());u.setEmail(email);u.setPassword(v.getPasswordHash());u.setRole(UserRole.USER);u.setFailedLoginAttempts(0);u.setLockedUntil(null); User saved=users.save(u);verifications.delete(v); return new AuthResponse(null,saved.getId(),saved.getName(),saved.getEmail(),saved.getRole());
 }
 @Transactional(noRollbackFor=ApiException.class) public void resend(ResendSignupOtpRequest r){String email=norm(r.email());SignupVerification v=verifications.findByEmail(email).orElseThrow(()->new ApiException("signup.verification.not-found"));LocalDateTime now=LocalDateTime.now();if(v.getResendAvailableAt().isAfter(now))throw new ApiException("signup.otp.cooldown");OtpIssue otp=refreshOtp(v,now);verifications.save(v);send(v,email,otp);}
 private OtpIssue populate(SignupVerification v,RegisterRequest r,LocalDateTime now){v.setName(r.name().trim());v.setAge(r.age());v.setPasswordHash(BCrypt.withDefaults().hashToString(12,r.password().toCharArray()));return refreshOtp(v,now);}
 private OtpIssue refreshOtp(SignupVerification v,LocalDateTime now){String otp="%06d".formatted(RANDOM.nextInt(1_000_000));int validityMinutes=Math.toIntExact(configuration.getOtpValidity().toMinutes());v.setOtpHash(BCrypt.withDefaults().hashToString(12,otp.toCharArray()));v.setAttemptCount(0);v.setExpiresAt(now.plusMinutes(validityMinutes));v.setResendAvailableAt(now.plusSeconds(COOLDOWN_SECONDS));return new OtpIssue(otp,validityMinutes);}
 private void send(SignupVerification v,String email,OtpIssue otp){try{delivery.deliver(email,v.getName(),otp.value(),otp.validityMinutes());}catch(RuntimeException e){throw new ApiException("signup.email.delivery.failed");}}
 private String norm(String e){return e.trim().toLowerCase(Locale.ROOT);}
 private record OtpIssue(String value,int validityMinutes){}
}
