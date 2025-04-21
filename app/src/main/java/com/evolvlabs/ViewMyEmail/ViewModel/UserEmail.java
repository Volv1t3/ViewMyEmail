package com.evolvlabs.ViewMyEmail.ViewModel;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * @author : Santiago Arellano
 * @date : Apr-19-2025
 * @description : El presente archivo implementa una clase base para la definicion de una clase que
 * abstraiga el concepto de un correo cargado desde la GMAIL api
 */
public class UserEmail implements Comparable<UserEmail> {

    /*! Parametros internos de la clase*/
    private String emailID;
    private String emailThreadID;
    private String emailSubjectLine;
    private String emailSenderAccount;
    private String emailReceiverAccount;
    private String emailBody;
    private String emailReceptionDate;
    private long emailReceptionDateLong;
    private long emailInternalServerReceptionDate;



    /*! Constructores*/
    public UserEmail(Message emailMessageFromService) {
        this.setEmailID(emailMessageFromService.getId());
        this.setEmailThreadID(emailMessageFromService.getThreadId());
        this.setEmailInternalServerReceptionDate(emailMessageFromService.getInternalDate());
        this.parseMessageDetailsFromServiceMessage(emailMessageFromService);
    }

    public UserEmail() {
        ;
    }



    /*Setters y Getters*/

    public String getEmailID() {
        return emailID;
    }

    public void setEmailID(String emailID) throws IllegalArgumentException {
        if (!emailID.isEmpty()) {
            this.emailID = emailID;
        } else {
            throw new IllegalArgumentException("Email ID cannot be empty");
        }
    }

    public String getEmailThreadID() {
        return emailThreadID;
    }

    public void setEmailThreadID(String emailThreadID) {
        if (!emailThreadID.isEmpty()) {
            this.emailThreadID = emailThreadID;
        } else {
            throw new IllegalArgumentException("Email Thread ID cannot be empty");
        }
    }

    public String getEmailSubjectLine() {
        return emailSubjectLine;
    }

    public void setEmailSubjectLine(String emailSubjectLine) {
        if (!emailSubjectLine.isEmpty()) {
            this.emailSubjectLine = emailSubjectLine;
        } else {
            throw new IllegalArgumentException("Email Subject Line cannot be empty");
        }
    }

    public String getEmailSenderAccount() {
        return emailSenderAccount;
    }

    public void setEmailSenderAccount(String emailSenderAccount) {
        if (!emailSenderAccount.isEmpty()) {
            this.emailSenderAccount = emailSenderAccount;
        } else {
            throw new IllegalArgumentException("Email Sender Account cannot be empty");
        }
    }

    public void setEmailReceiverAccount(String emailReceiverAccount){
        if (!emailReceiverAccount.isEmpty()) {
            this.emailReceiverAccount = emailReceiverAccount;
        } else {
            throw new IllegalArgumentException("Email Receiver Account cannot be empty");
        }
    }

    public String getEmailReceiverAccount() {
         return this.emailReceiverAccount;
    }

    public String getEmailBody() {
        return emailBody;
    }

    public void setEmailBody(String emailBody) {
        if (!emailBody.isEmpty()) {
            this.emailBody = emailBody;
        } else {
            throw new IllegalArgumentException("Email Body cannot be empty");
        }
    }

    public String getEmailReceptionDate() {
        return new Date(this.emailInternalServerReceptionDate).toString();
    }

    public void setEmailReceptionDate(String emailReceptionDate) {
        if (!emailReceptionDate.isEmpty()) {
            this.emailReceptionDate = emailReceptionDate;
        } else {
            throw new IllegalArgumentException("Email Reception Date cannot be empty");
        }
    }

    public long getEmailInternalServerReceptionDate() {
        return emailInternalServerReceptionDate;
    }
    public void setEmailInternalServerReceptionDate(long emailInternalServerReceptionDate) {
        this.emailInternalServerReceptionDate = emailInternalServerReceptionDate;
    }


    /*Metodos internos*/
    private void parseMessageDetailsFromServiceMessage(Message externalMessageFromServiceContext) {
        if (externalMessageFromServiceContext.getPayload() != null
                && externalMessageFromServiceContext.getPayload().getHeaders() != null) {

            //> Al inicio, nosotros acabamos de revisar si el modelo de JSON basado en un HTTP
            // request a la google GMAIL api tiene datos, ahora tenemos que iterar sobre las
            // partes de los headers para obtener la informacion que requerimos del JSON
            for(MessagePartHeader header :
                    externalMessageFromServiceContext.getPayload().getHeaders()){
                switch(header.getName()){
                    case "To":
                        this.setEmailReceiverAccount(header.getValue());
                    case "Subject":
                        this.setEmailSubjectLine(header.getValue());
                        break;
                    case "From":
                        this.setEmailSenderAccount(header.getValue());
                        break;
                    case "Date":
                        this.interpretAndDecodeDate(header.getValue());
                }
            }

        }
    }

    /**
     * @description : El presente metodo se encarga de implementar un parsing del formato de
     * fecha de la aplicacion normal de GMAIL JSON, que utiliza la GMAIL api para manejar los datos
     */
    private void interpretAndDecodeDate(String dateString){
        try{
            //> 1. Creamos un tipo especial de format parser, como tenemnos que trabajar con el
            // formato de JSON, usamos el format especifico del estandar RFC 2822 que utiliza
            // google para la comunicacion interna.
            SimpleDateFormat parserForDateFormat = new SimpleDateFormat("EEE, d MMM yyyy " +
                                                                                "HH:mm:ss Z",
                                                                        Locale.US);
            Date dateFromParser = parserForDateFormat.parse(dateString);

            //> 2. cargamos ese tipo de fecha en nuestro sistema basado en un tipo especifico de
            // fecah interno
            SimpleDateFormat formatterForStorage = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss",
                                                                        Locale.US);
            this.setEmailReceptionDate(formatterForStorage.format(dateFromParser));
            this.setEmailReceptionDateLong(dateFromParser.getTime());
        } catch (ParseException e){
            Log.e("[UserEmailComms]", "Error parsing date from GMAIL JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setEmailReceptionDateLong(long time) {
        this.emailReceptionDateLong = time;
    }
    public long getEmailReceptionDateLong() {
        return emailReceptionDateLong;
    }


    /**
     * Indicates whether some other object is "equal to" this one.
     * <p>
     * The {@code equals} method implements an equivalence relation on non-null object references:
     * <ul>
     * <li>It is <i>reflexive</i>: for any non-null reference value
     *     {@code x}, {@code x.equals(x)} should return
     *     {@code true}.
     * <li>It is <i>symmetric</i>: for any non-null reference values
     *     {@code x} and {@code y}, {@code x.equals(y)}
     *     should return {@code true} if and only if
     *     {@code y.equals(x)} returns {@code true}.
     * <li>It is <i>transitive</i>: for any non-null reference values
     *     {@code x}, {@code y}, and {@code z}, if
     *     {@code x.equals(y)} returns {@code true} and
     *     {@code y.equals(z)} returns {@code true}, then
     *     {@code x.equals(z)} should return {@code true}.
     * <li>It is <i>consistent</i>: for any non-null reference values
     *     {@code x} and {@code y}, multiple invocations of
     *     {@code x.equals(y)} consistently return {@code true}
     *     or consistently return {@code false}, provided no
     *     information used in {@code equals} comparisons on the
     *     objects is modified.
     * <li>For any non-null reference value {@code x},
     *     {@code x.equals(null)} should return {@code false}.
     * </ul>
     *
     * <p>
     * An equivalence relation partitions the elements it operates on
     * into <i>equivalence classes</i>; all the members of an
     * equivalence class are equal to each other. Members of an
     * equivalence class are substitutable for each other, at least
     * for some purposes.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     * @implSpec The {@code equals} method for class {@code Object} implements the most
     * discriminating possible equivalence relation on objects; that is, for any non-null reference
     * values {@code x} and {@code y}, this method returns {@code true} if and only if {@code x} and
     * {@code y} refer to the same object ({@code x == y} has the value {@code true}).
     * <p>
     * In other words, under the reference equality equivalence relation, each equivalence class
     * only has a single element.
     * @apiNote It is generally necessary to override the {@link #hashCode hashCode} method whenever
     * this method is overridden, so as to maintain the general contract for the {@code hashCode}
     * method, which states that equal objects must have equal hash codes.
     * @see #hashCode()
     * @see HashMap
     */
    @Override
    public boolean equals(@Nullable @org.jetbrains.annotations.Nullable Object obj) {
        if (obj == null) {
            return false;
        } else if (obj.getClass() != this.getClass()) {
            return false;
        } else {
            UserEmail castedObj = (UserEmail) obj;
            return castedObj.getEmailID().equals(this.getEmailID());
        }
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     * @apiNote In general, the {@code toString} method returns a string that "textually represents"
     * this object. The result should be a concise but informative representation that is easy for a
     * person to read. It is recommended that all subclasses override this method. The string output
     * is not necessarily stable over time or across JVM invocations.
     * @implSpec The {@code toString} method for class {@code Object} returns a string consisting of
     * the name of the class of which the object is an instance, the at-sign character `{@code @}',
     * and the unsigned hexadecimal representation of the hash code of the object. In other words,
     * this method returns a string equal to the value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     */
    @NonNull
    @NotNull
    @Override
    public String toString() {
        return "UserEmail{" +
                "emailID='" + emailID + '\'' +
                ", emailThreadID='" + emailThreadID + '\'' +
                ", emailSubjectLine='" + emailSubjectLine + '\'' +
                ", emailSenderAccount='" + emailSenderAccount + '\'' +
                ", emailReceiverAccount='" + emailReceiverAccount + '\'' +
                ", emailBody='" + emailBody + '\'' +
                ", emailReceptionDate='" + emailReceptionDate + '\'' +
                ", emailReceptionDateLong='" + emailReceptionDateLong + '\'' +
                '}';
    }

    /**
     * Compares this object with the specified object for order.  Returns a negative integer, zero,
     * or a positive integer as this object is less than, equal to, or greater than the specified
     * object.
     *
     * <p>The implementor must ensure {@link Integer#signum
     * signum}{@code (x.compareTo(y)) == -signum(y.compareTo(x))} for all {@code x} and {@code y}.
     * (This implies that {@code x.compareTo(y)} must throw an exception if and only if
     * {@code y.compareTo(x)} throws an exception.)
     *
     * <p>The implementor must also ensure that the relation is transitive:
     * {@code (x.compareTo(y) > 0 && y.compareTo(z) > 0)} implies {@code x.compareTo(z) > 0}.
     *
     * <p>Finally, the implementor must ensure that {@code
     * x.compareTo(y)==0} implies that {@code signum(x.compareTo(z)) == signum(y.compareTo(z))}, for
     * all {@code z}.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is less than, equal
     * to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it from being compared
     *                              to this object.
     * @apiNote It is strongly recommended, but <i>not</i> strictly required that
     * {@code (x.compareTo(y)==0) == (x.equals(y))}.  Generally speaking, any class that implements
     * the {@code Comparable} interface and violates this condition should clearly indicate this
     * fact.  The recommended language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     */
    @Override
    public int compareTo(UserEmail o) {
        return Long.compare(o.getEmailReceptionDateLong(), this.getEmailReceptionDateLong());
    }
}
