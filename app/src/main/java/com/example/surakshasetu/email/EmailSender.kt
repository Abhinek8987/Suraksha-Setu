package com.example.surakshasetu.email

import android.util.Log
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailSender {

    // Credentials provided by the user
    private val fromEmail = "kumar12345abhinek@gmail.com"
    private val password = "rdqu acye debq gona" // Assuming App Password

    fun sendEmergencyEmail(toEmail: String, userName: String, locationLink: String, audioFilePath: String? = null) {
        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
        }

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(fromEmail, password)
            }
        })

        try {
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(fromEmail))
                addRecipient(Message.RecipientType.TO, InternetAddress(toEmail))
                subject = if (audioFilePath == null) "EMERGENCY ALERT - SURAKSHA SETU" else "EMERGENCY ALERT - AUDIO EVIDENCE"
                
                val multipart = javax.mail.internet.MimeMultipart()
                
                val textBodyPart = javax.mail.internet.MimeBodyPart()
                val body = if (audioFilePath == null) {
                    """
                    Emergency detected.
                    User $userName requires immediate help.
                    
                    Location: 
                    $locationLink
                    
                    Please respond immediately.
                    """.trimIndent()
                } else {
                    """
                    Emergency follow-up.
                    Please find the attached 30-second audio recording from the emergency situation for user $userName.
                    
                    Location: 
                    $locationLink
                    """.trimIndent()
                }
                textBodyPart.setText(body)
                multipart.addBodyPart(textBodyPart)
                
                if (audioFilePath != null) {
                    val attachmentBodyPart = javax.mail.internet.MimeBodyPart()
                    val source = javax.activation.FileDataSource(audioFilePath)
                    attachmentBodyPart.dataHandler = javax.activation.DataHandler(source)
                    attachmentBodyPart.fileName = java.io.File(audioFilePath).name
                    multipart.addBodyPart(attachmentBodyPart)
                }
                
                setContent(multipart)
            }

            Transport.send(message)
            Log.d("EmailSender", "Emergency email sent successfully to $toEmail")

        } catch (e: Exception) {
            Log.e("EmailSender", "Failed to send email", e)
        }
    }
}
