package com.evolvlabs.ViewMyEmail.Utils;

import android.os.AsyncTask;
import android.util.Log;
import com.evolvlabs.ViewMyEmail.ViewModel.SimpleUserDataViewModel;
import com.evolvlabs.ViewMyEmail.ViewModel.UserEmail;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author : Santiago Arellano
 * @date : 19-Apr-2025
 * @@description : El presente archivo implementa un metodo de ayuda para el trabajo de llamadas
 * a la API de google GMAIL para la carga de los correos del usuario.
 */
public class GmailAPISubsystem extends AsyncTask<SimpleUserDataViewModel, Void, List<UserEmail>> {

    private static final String APPLICATION_NAME_FOR_API = "ViewMyEmail";
    private static final long MAX_EMAILS_TO_QUERY = 10L;

    private final SimpleUserDataViewModel dataViewModel;

    public GmailAPISubsystem(SimpleUserDataViewModel dataViewModel){
        this.dataViewModel = dataViewModel;
    }

    /**
     * Runs on the UI thread before {@link #doInBackground}. Invoked directly by {@link #execute} or
     * {@link #executeOnExecutor}. The default version does nothing.
     *
     * @see #onPostExecute
     * @see #doInBackground
     */
    @Override
    protected void onPreExecute() {
        if (this.dataViewModel == null){
            cancel(true);
        }
    }

    /**
     * Override this method to perform a computation on a background thread. The specified
     * parameters are the parameters passed to {@link #execute} by the caller of this task.
     * <p>
     * This will normally run on a background thread. But to better support testing frameworks, it
     * is recommended that this also tolerates direct execution on the foreground thread, as part of
     * the {@link #execute} call.
     * <p>
     * This method can call {@link #publishProgress} to publish updates on the UI thread.
     *
     * @param simpleUserDataViewModels The parameters of the task.
     * @return A result, defined by the subclass of this task.
     * @see #onPreExecute()
     * @see #onPostExecute
     * @see #publishProgress
     */
    @Override
    protected List<UserEmail> doInBackground(SimpleUserDataViewModel... simpleUserDataViewModels) {
        //> 1. Creamos el objeto de retorno
        List<UserEmail> userEmails = new ArrayList<>();
        try{
            Gmail gmailServiceFromApplication = new Gmail.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    simpleUserDataViewModels[0].getGoogleCredentials().getValue())
                    .setApplicationName(APPLICATION_NAME_FOR_API)
                    .build();

            //> 2. Utilizamos la API para obtener los mensajes del usuario
            ListMessagesResponse messagesResponseFromAPI =
                    gmailServiceFromApplication
                            .users()
                            .messages()
                            .list("me")
                            .setMaxResults(MAX_EMAILS_TO_QUERY)
                            .execute();

            //> 3. Obtenemos los mensajes de la respuesta de la API a tipo message, que es una
            // clase base del sistema de GMAIL
            List< Message> messagesList = messagesResponseFromAPI.getMessages();
            System.out.println(messagesList);

            if (messagesList != null && !messagesList.isEmpty()){
                //> 3.1 Si tenemos mensajes, entonces los recorremos y los convertimos a UserEmail
                for (Message message : messagesList) {
                    //? 3.1.1 Obtenemos el mensaje y sus parametros completos
                    Message fullMessage = gmailServiceFromApplication.users().messages()
                            .get("me", message.getId())
                            .setFormat("full")
                            .execute();
                    UserEmail userEmail = new UserEmail(fullMessage);
                    if (userEmail != null){
                        userEmails.add(userEmail);
                    }
                }
            }

        } catch (GeneralSecurityException | IOException e) {
            Log.e("GmailAPISubsystem", "Error al crear el servicio de Gmail: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return userEmails;
    }

    /**
     * <p>Runs on the UI thread after {@link #doInBackground}. The
     * specified result is the value returned by {@link #doInBackground}. To better support testing
     * frameworks, it is recommended that this be written to tolerate direct execution as part of
     * the execute() call. The default version does nothing.</p>
     *
     * <p>This method won't be invoked if the task was cancelled.</p>
     *
     * @param userEmails The result of the operation computed by {@link #doInBackground}.
     * @see #onPreExecute
     * @see #doInBackground
     * @see #onCancelled(Object)
     */
    @Override
    protected void onPostExecute(List<UserEmail> userEmails) {
        super.onPostExecute(userEmails);
        if (userEmails != null && !userEmails.isEmpty()) {
            this.dataViewModel.setClientRetrievedEmailMessages(userEmails);
        }
    }
}
