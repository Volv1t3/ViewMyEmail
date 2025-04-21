package com.evolvlabs.ViewMyEmail.ViewModel;

import android.app.Application;
import android.graphics.Bitmap;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.evolvlabs.ViewMyEmail.Utils.GmailAPISubsystem;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.auth.oauth2.GoogleCredentials;
import org.jetbrains.annotations.NotNull;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;

public class SimpleUserDataViewModel extends AndroidViewModel {

    private MutableLiveData<String> clientUsernameMutableData = new MutableLiveData<>("no_data");
    public LiveData<String> clientPasswordImmutableData = clientUsernameMutableData;

    private MutableLiveData<String> clientEmailMutableData = new MutableLiveData<>("no_data");
    public LiveData<String> clientEmailImmutableData = clientEmailMutableData;

    private MutableLiveData<Boolean> clientLoggedInThroughNormalLogin =
            new MutableLiveData<>(false);
    public LiveData<Boolean> clientLoggedInThroughNormalLoginImmutable =
            clientLoggedInThroughNormalLogin;

    private MutableLiveData<Bitmap> clientProfileMutableData = new MutableLiveData<>();
    public LiveData<Bitmap> clientProfileImmutableData = clientProfileMutableData;
    private MutableLiveData<Boolean> clientLoggedInThroughGoogleSignIn =
            new MutableLiveData<>(false);
    public LiveData<Boolean> clientLoggedInThroughGoogleSignInImmutable =
            clientLoggedInThroughGoogleSignIn;

    private MutableLiveData<Boolean> clientLoggedOutOfApplication = new MutableLiveData<>(false);
    public LiveData<Boolean> clientLoggedOutOfApplicationImmutable = clientLoggedOutOfApplication;

    private MutableLiveData<GoogleSignInClient> signInClientMutableLiveData =
            new MutableLiveData<>(null);
    public LiveData<GoogleSignInClient> signInClientImmutableLiveData =
            signInClientMutableLiveData;

    private MutableLiveData<List<UserEmail>> clientRetrievedEmailMessages =
            new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<UserEmail>> clientRetrievedEmailMessagesImmutable =
            clientRetrievedEmailMessages;

    private Application applicationFromActivity;
    private MutableLiveData<GoogleAccountCredential> credentialsMutableData =
            new MutableLiveData<>(null);
    public LiveData<GoogleAccountCredential> credentialsImmutableData;

    public SimpleUserDataViewModel(@NotNull Application application) {
        super(application);
        this.applicationFromActivity = application;
    }

    /*! Metodos para actualizar los live data y sus valores*/

    public void setClientUsernameMutableData(String mutableStringObtainedFromClient) {
        if (mutableStringObtainedFromClient != null && !mutableStringObtainedFromClient.isEmpty()) {
            this.clientUsernameMutableData.setValue(mutableStringObtainedFromClient);
        } else {
            throw new NullPointerException("El valor de entrada no puede ser nulo o vacio");
        }
    }

    public MutableLiveData<String> getClientUsernameMutableData() {
        return this.clientUsernameMutableData;
    }

    public void clearClientUsernameMutableData() {
        if (this.clientUsernameMutableData.getValue() != null) {
            this.clientUsernameMutableData.setValue("");
        }
    }

    public void setClientEmailMutableData(String mutableStringObtainedFromClient) {
        if (mutableStringObtainedFromClient != null && !mutableStringObtainedFromClient.isEmpty()) {
            this.clientEmailMutableData.setValue(mutableStringObtainedFromClient);
        } else {
            throw new NullPointerException("El valor de entrada no puede ser nulo o vacio");
        }
    }

    public MutableLiveData<String> getClientEmailMutableData() {
        return this.clientEmailMutableData;
    }

    public void clearClientEmailMutableData(){
        if (this.clientEmailMutableData.getValue() != null) {
            this.clientEmailMutableData.setValue("");
        }
    }

    public void setClientLoggedInThroughNormalLogIn(Boolean clientLoggedInThroughNormalLogIn){
        if (clientLoggedInThroughNormalLogIn != null) {
            this.clientLoggedInThroughNormalLogin.setValue(clientLoggedInThroughNormalLogIn);
        } else {
            throw new NullPointerException("El valor de entrada no puede ser nulo");
        }
    }

    public MutableLiveData<Boolean> getClientLoggedInThroughNormalLogin() {
        return this.clientLoggedInThroughNormalLogin;
    }

    public void setClientProfileMutableData(Bitmap clientProfileMutableData){
       this.clientProfileMutableData.setValue(clientProfileMutableData);
    }

    public MutableLiveData<Bitmap> getClientProfileMutableData() {
        return this.clientProfileMutableData;
    }

    public void setClientLoggedInThroughGoogleSignIn(Boolean clientLoggedInThroughGoogleSignIn){
        if (clientLoggedInThroughGoogleSignIn != null) {
            this.clientLoggedInThroughGoogleSignIn.setValue(clientLoggedInThroughGoogleSignIn);
        } else {
            throw new NullPointerException("El valor de entrada no puede ser nulo");
        }
    }

    public MutableLiveData<Boolean> getClientLoggedInThroughGoogleSignIn() {
        return this.clientLoggedInThroughGoogleSignIn;
    }

    public void setClientLoggedOutOfApplication(Boolean clientLoggedOutOfApplication){
        if (clientLoggedOutOfApplication != null) {
            this.clientLoggedOutOfApplication.setValue(clientLoggedOutOfApplication);
        } else {
            throw new NullPointerException("El valor de entrada no puede ser nulo");
        }
    }

    public MutableLiveData<Boolean> getClientLoggedOutOfApplication() {
        return this.clientLoggedOutOfApplication;
    }


    /*Metodos para manejar el listado de correos que se obtienen del usuario*/
    public void setClientRetrievedEmailMessages(List<UserEmail> clientRetrievedEmailMessages) {
        if (clientRetrievedEmailMessages != null && !clientRetrievedEmailMessages.isEmpty()) {
            this.clientRetrievedEmailMessages.setValue(clientRetrievedEmailMessages);
        } else {
            throw new NullPointerException("El valor de entrada no puede ser nulo");
        }
    }

    public MutableLiveData<List<UserEmail>> getClientRetrievedEmailMessages() {
        return this.clientRetrievedEmailMessages;
    }

    public void clearClientRetrievedEmailMessagesMutableData(){
        if (this.clientRetrievedEmailMessages.getValue() != null) {
            this.clientRetrievedEmailMessages.setValue(new ArrayList<>());
        }
    }

    public void setSignInClientMutableLiveData(GoogleSignInClient signInClientMutableLiveData) {
        if (signInClientMutableLiveData != null) {
            this.signInClientMutableLiveData.setValue(signInClientMutableLiveData);
        } else {
            throw new NullPointerException("El valor de entrada no puede ser nulo");
        }
    }
    public MutableLiveData<GoogleSignInClient> getSignInClientMutableLiveData() {
        return this.signInClientMutableLiveData;
    }

    public MutableLiveData<GoogleAccountCredential> getGoogleCredentials() {
        return this.credentialsMutableData;
    }

    public void setClientCredentialsForApplication(GoogleAccountCredential credentials) {
        this.credentialsMutableData.setValue(credentials);
    }

    public void fetchEmailsUsingAPISystem(){
        if (this.getGoogleCredentials().getValue() != null){
            new GmailAPISubsystem(this).execute(this);
        }
    }


}
