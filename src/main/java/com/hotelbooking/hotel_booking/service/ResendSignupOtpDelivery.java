package com.hotelbooking.hotel_booking.service;
import org.springframework.stereotype.Service; import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
@Service @ConditionalOnProperty(name="app.password-reset.delivery",havingValue="resend")
class ResendSignupOtpDelivery implements SignupOtpDelivery {
 private final ResendEmailSender sender; ResendSignupOtpDelivery(ResendEmailSender sender){this.sender=sender;}
 public void deliver(String email,String name,String otp,int validityMinutes){
  String html="<p>Hello "+esc(name)+",</p><p>Use this code to verify your Hotel Booking account:</p><h1 style=\"letter-spacing:6px\">"+otp+"</h1><p>This code expires in "+validityMinutes+" minutes.</p><p>If you did not create this account, you can ignore this email.</p>";
  sender.send(email,"Verify your Hotel Booking account",html,"Hello "+name+",\n\nYour verification code is: "+otp+"\n\nIt expires in "+validityMinutes+" minutes.");
 }
 private String esc(String v){return v.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");}
}
