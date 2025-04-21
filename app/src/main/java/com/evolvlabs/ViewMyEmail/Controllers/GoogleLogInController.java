package com.evolvlabs.ViewMyEmail.Controllers;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.UrlRequest;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.evolvlabs.ViewMyEmail.Utils.GmailAPISubsystem;
import com.evolvlabs.ViewMyEmail.ViewModel.SimpleUserDataViewModel;
import com.evolvlabs.ViewMyEmail.R;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.gmail.GmailScopes;
import org.jetbrains.annotations.NotNull;
import org.jsoup.helper.HttpConnection;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


public class GoogleLogInController extends Fragment {

    private SimpleUserDataViewModel dataViewModel;
    private EditText inGoogleLogInView_EmailEditText;
    private EditText inGoogleLogInView_PasswordEditText;
    private Button inGoogleLogInView_NormalLogInButton;
    private SignInButton inGoogleLogInView_GoogleSignInButton;
    private GoogleSignInClient internalGoogleSignInClient;
    private Map<String, String> internalLastSignInMap;
    private ActivityResultLauncher<Intent> signInLauncher;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //> 1. Importante: para que la aplicacion pueda pedir los accessos a google, debemos
        // primero cargar en el on create de la actividad de registro, una conexion con la API
        // de google y el sistema, a traves de esto logramos
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail() // Aqui solicitamos el email de la persona que se esta loggeando
                // en la app
                // Aqui solicitamos la informacion basica del perfil, es decir su nombre y su
                // foto de perfil
                .requestProfile()
                // Aqui solicitamos el ID de usuario, esto lo hacemos pero no lo usamos
                .requestId()
                // Esta es importante dado que nos permite solicitar especificamente el Google
                // Email de esta persona que se esta loggeando, lo que nos permite leer su inbox
                .requestScopes(new Scope("https://www.googleapis.com/auth/gmail.readonly"))
                .build();


        //> 2. Inicializamos el cliente de google
        this.internalGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        //> 3. Revisamos si antes de entrar ya tenemos una instancia interna de los datos, es
        // decir si ya teniamos un log in hecho
        GoogleSignInAccount accountPrev = GoogleSignIn.getLastSignedInAccount(requireActivity());
        if (accountPrev != null) {
            //> 3.1 Si tenemos una instancia interna de los datos, entonces podemos actualizar
            // los datos del usuario
            if (!accountPrev.isExpired()) {
                this.internalLastSignInMap = new HashMap<>();
                this.internalLastSignInMap.put("email", accountPrev.getEmail());
                this.internalLastSignInMap.put("name", accountPrev.getDisplayName());
                this.internalLastSignInMap.put("id", accountPrev.getId());
                if (accountPrev.getPhotoUrl() != null) {
                    this.internalLastSignInMap.put("photo", accountPrev.getPhotoUrl().toString());
                }
            } else {
                Log.e("GoogleLogInController", "El token de google esta expirado");
            }
        } else {
            Log.e("GoogleLogInController", "No hay una instancia interna de los datos");
        }

        //> 4. Inicializamos el sign in activity launcher
        this.signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        handleOnActivityResultAccountInformationPulling(result.getData());
                    } else {
                        Log.e("GoogleLogInController", "No se pudo obtener la informacion del usuario");
                        Log.e("GoogleLogInController", "Error: " + result.getResultCode());
                    }
                });

    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater,
                             @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        //> 1, Alzamos el layout
        return inflater.inflate(R.layout.fragment_google_log_in_view,
                                container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view,
                              @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //> 1. Inicializamos el applicationData para esta vista
        this.dataViewModel =
                new ViewModelProvider(requireActivity()).get(SimpleUserDataViewModel.class);

        //> 1.1 Si tenemos datos en el mapa entonces habia un usuario anterior, y si esto sucede
        // debemos actualizar las constantes para dejar pasar por log in de google
        this.updateUIWithUserInfo(this.internalLastSignInMap, null);
        this.dataViewModel.setSignInClientMutableLiveData(this.internalGoogleSignInClient);

        //> 2. Inicializamos los campos de entradas de datos, tanto para el email como para el 
        // password, si tenemos su email entonces podemos cargar sus datos de correo, pero no
        // necesariamente el resto de datos
        this.initializeAndSetUpEditTextInputsFromUser();

        //> 2.1 Inicializamos la conexion con el click del boton general de log in, como esta
        // aplicacion es un demo entonces no trabajamos para validar entradas mas alla de dejarle
        // al usuario pasar, claro esta poniendo como desactivados las funcionalidades internas.
        this.initializeAndSetUpNormalLogInButton();

        //> 3. Inicializamos el google sign in button para poder manejar un intent directo desde
        // esta seccion de la app hacia el resto de la app. Con esto me refiero a que debemos
        // tener un modelo de datos que permita movernos cuando el sign in sea valido
        this.initializeAndSetUpGoogleSignInButtonAction();

        //> 4. Para evitar que el usuario pueda avanzar en el stack, interceptamos el movimiento
        // de las flechas de la UI y pasamos
        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {            @Override
            public void handleOnBackPressed() {
                return;
            }
        });
    }


    private void initializeAndSetUpEditTextInputsFromUser() {
        //> 1. Tomamos las referencias de la vista principal
        this.inGoogleLogInView_EmailEditText =
                getView().findViewById(R.id.inGoogleLogInView_EmailEditText);
        this.inGoogleLogInView_PasswordEditText =
                getView().findViewById(R.id.inGoogleLogInView_PasswordEditText);
        this.inGoogleLogInView_NormalLogInButton =
                getView().findViewById(R.id.inGoogleLogInView_NormalLogInButton);
        this.inGoogleLogInView_GoogleSignInButton =
                getView().findViewById(R.id.inGoogleLogInView_GoogleSignInButton);

        //> 2. Conectamos un text watcher para los dos con el mismo formato, si no tienen ambos
        // texto entonces el boton grande no se inicializa
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //pass
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //pass
            }

            @Override
            public void afterTextChanged(Editable s) {
                this.updateSignInButtonStateDependingOnTheirState();
            }

            private void updateSignInButtonStateDependingOnTheirState() {
                //? 1. Revisamos el texto el email y del password
                String email = inGoogleLogInView_EmailEditText.getText().toString().trim();
                String password = inGoogleLogInView_PasswordEditText.getText().toString().trim();

                //? 2. Tomamos estos valores y los comparamos con las utilidades de android
                boolean emailGood = Patterns.EMAIL_ADDRESS.matcher(email).matches();
                boolean passwordGood = password.length() >= 5;
                boolean shouldAllowButtonOn = emailGood && passwordGood;

                inGoogleLogInView_NormalLogInButton.setEnabled(shouldAllowButtonOn);
                //? 3. Actualizamos los campos con errores si es necesario
                if (!emailGood) {
                    inGoogleLogInView_EmailEditText.setError("Email invalido");
                } else {
                    inGoogleLogInView_EmailEditText.setError(null);
                }
                if (!passwordGood) {
                    inGoogleLogInView_PasswordEditText.setError("Password invalido. Longitud " +
                                                                        "mayor a cinco necesaria");
                } else {
                    inGoogleLogInView_PasswordEditText.setError(null);
                }
            }
        };

        this.inGoogleLogInView_PasswordEditText.addTextChangedListener(textWatcher);
        this.inGoogleLogInView_EmailEditText.addTextChangedListener(textWatcher);

        //> 3. Clavamos el estado inicial como desactivado
        this.inGoogleLogInView_NormalLogInButton.setEnabled(false);
    }

    private void initializeAndSetUpNormalLogInButton() {
        //> 1. Cargamos el componente visual si no esta cargado
        if (this.inGoogleLogInView_NormalLogInButton == null) {
            this.inGoogleLogInView_NormalLogInButton =
                    getView().findViewById(R.id.inGoogleLogInView_NormalLogInButton);
        }

        //> 2. Conectamos un listener para que cuando este se haga click, guarde los datos e
        // inicialize la navegacion hacia el otro segmento de la app
        this.inGoogleLogInView_NormalLogInButton.setOnClickListener(view -> {
            //> 2.1. Guardamos los datos del usuario
            String email = inGoogleLogInView_EmailEditText.getText().toString().trim();
            String password = inGoogleLogInView_PasswordEditText.getText().toString().trim();

            //? 2.1.1 Validamos los datos para retornar si no son validos aunque haya contenido
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                inGoogleLogInView_EmailEditText.setError("Email invalido");
                return;
            }

            //> 2.2. Guardamos los datos del usuario
            this.dataViewModel.setClientEmailMutableData(email);
            this.dataViewModel.setClientUsernameMutableData(email);

            //> 2.3. Marcamos la flag de loggeado como true para indicar que ya debemos pasar de
            // esta vista
            this.dataViewModel.setClientLoggedInThroughNormalLogIn(true);
            this.dataViewModel.setClientLoggedInThroughGoogleSignIn(false);

            //> 2.4 Realizamos la navegacion hacia el segundo fragmento
            try {
                View currentView = getView();
                if (currentView != null) {
                    if (isAdded() && getActivity() != null) {
                        NavController navController = Navigation.findNavController(
                                getActivity(), R.id.nav_host_fragment);
                        if (navController.getCurrentDestination().getId() == R.id.user_login_fragment) {
                            navController.navigate(R.id.action_transition_from_login_to_content_view);
                        }
                    }
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        });
    }

    private void initializeAndSetUpGoogleSignInButtonAction() {
        //> 1. Sacamos una referencia al boton si es que todavia no existe
        if (this.inGoogleLogInView_GoogleSignInButton == null) {
            this.inGoogleLogInView_GoogleSignInButton =
                    getView().findViewById(R.id.inGoogleLogInView_GoogleSignInButton);
        }

        //> 2. Cargamos un intent en el momento que el usuario haga click en el boton, la idea es
        // que este intent tiene que abrir un intent que se resuelve por google y no por
        // nosotros, nosotros solo esperamos los datos de retorno para ver si hay una cuenta o
        // no, si no hay una cuenta activa entonces no realizamos un movimiento a la otra vista
        if (this.inGoogleLogInView_GoogleSignInButton != null) {
            this.inGoogleLogInView_GoogleSignInButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //? 1. Este metood de aqui nos da el launcher que vamos a usar para
                            // trabajar con el sistema de google. EL unico problema que tenemos
                            // es que al ser un fragment, los metodos que tenemos para mandar un
                            // intent estan deprecados...
                            Intent signInIntentByGoogle =
                                    internalGoogleSignInClient.getSignInIntent();

                            //? 2. Si estamos en fragmentos, segun varias fuentes online, se
                            // tiene que usar el ActivityResultLauncher, una nueva API que se
                            // maneja mejor en el caso de tener un fragmento
                            signInLauncher.launch(signInIntentByGoogle);
                        }
                    });
        }
    }


    private void handleOnActivityResultAccountInformationPulling(Intent data) {
        try {
            //> 1. Abrimos un task que es el quivalente del AsyncTask que usabamos, dado que este
            // tambien permite realizar y esperar a operaciones asyncronas
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            GoogleSignInAccount account = task.getResult(ApiException.class);

            //> 2. Como tenemos que manejar el correo electronico necesitamos trabajar con
            // credenciales de google para tener accesso al correo del usuario
            GoogleAccountCredential credentials =
                    GoogleAccountCredential.usingOAuth2(requireContext(), /*Equivale a enviar el
                    contexto de nuestra aplicacion hacia el sistema*/
                    Arrays.asList(GmailScopes.GMAIL_READONLY) /*Equivale a solicitar solo read
                    only al sistema del usuario*/)
                            .setSelectedAccount(account.getAccount());
            this.dataViewModel.setClientCredentialsForApplication(credentials);

            //> 2. Manejo de los datos si hay un Sign in correcto
            updateUIWithUserInfo(null, account);

            //> 3 Aqui realizamos la navegacion hacia el otro fragmento si es correcto el inicio
            // de sesion
            if (this.dataViewModel.getClientLoggedInThroughGoogleSignIn().getValue() != null) {
                if (this.dataViewModel.getClientLoggedInThroughGoogleSignIn().getValue()) {
                    try {
                        View currentView = getView();
                        if (currentView != null) {
                            if (isAdded() && getActivity() != null) {
                                NavController navController = Navigation.findNavController(
                                        getActivity(), R.id.nav_host_fragment);
                                if (navController.getCurrentDestination().getId() == R.id.user_login_fragment) {
                                    navController.navigate(R.id.action_transition_from_login_to_content_view);
                                }
                            }
                        }
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (ApiException e) {
            // Sign in failed
            Log.e("GoogleSignIn", "Sign in failed", e);
        } catch (Exception e) {
            // Handle other unexpected errors
            Log.e("GoogleSignIn", "Unexpected error during sign-in", e);
        }

    }

    private void updateUIWithUserInfo(@Nullable Map<String, String> previusUserInformaiton,
                                      @Nullable GoogleSignInAccount account) {
        //> 1. Revisamos si alguno de los parametros no es nulo
        if (previusUserInformaiton == null && (account == null || account.isExpired())) {
            Log.w("GoogleSignIn", "No user information available");
            return;
        }

        //> 2. Revisamos todos los parametros para sacar la informacion necesaria
        try {
            String username = previusUserInformaiton != null ? previusUserInformaiton.get("name")
                    : account.getDisplayName();
            String email = previusUserInformaiton != null ? previusUserInformaiton.get("email")
                    : account.getEmail();
            String profileImageUrl = previusUserInformaiton != null ? previusUserInformaiton.get("photo")
                    : account.getPhotoUrl().toString();

            //? 2.1 Actualizamos el modelo de datos solo si hay datos
            if (username != null && !username.isEmpty()) {
                this.dataViewModel.setClientUsernameMutableData(username);
            }
            if (email != null && !email.isEmpty()) {
                this.dataViewModel.setClientEmailMutableData(email);
            }
            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                //> 2.2. Descargamos la imagen de perfil
                new DownloadProfileImageAsyncTask().execute(profileImageUrl);
                //> 2.3. Actualizamos el modelo de datos
                this.dataViewModel.setClientLoggedInThroughGoogleSignIn(true);
            }

            //? 2.2
            this.dataViewModel.setClientLoggedInThroughNormalLogIn(false);
            this.dataViewModel.setClientLoggedInThroughGoogleSignIn(true);

        } catch (Exception e) {
            Log.e("GoogleSignIn", "Error al actualizar la informacion del usuario", e);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.dataViewModel.fetchEmailsUsingAPISystem();
    }

    public class DownloadProfileImageAsyncTask extends AsyncTask<String, Void, Bitmap> {

        /**
         * Override this method to perform a computation on a background thread. The specified
         * parameters are the parameters passed to {@link #execute} by the caller of this task.
         * <p>
         * This will normally run on a background thread. But to better support testing frameworks,
         * it is recommended that this also tolerates direct execution on the foreground thread, as
         * part of the {@link #execute} call.
         * <p>
         * This method can call {@link #publishProgress} to publish updates on the UI thread.
         *
         * @param strings The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected Bitmap doInBackground(String... strings) {
            Integer retryCount = 0;
            Bitmap bitmapFromTheInternet = null;

            while (retryCount < 2) {
                HttpURLConnection connection = null;
                if (strings[0] != null) {
                    //> 1. Init online connection
                    try {
                        URL urlFromString = new URL(strings[0]);
                        connection = (HttpURLConnection) urlFromString.openConnection();

                        //> 2. Con la conexion tomamos el stream y descargamos
                        connection.setConnectTimeout(15000);
                        connection.setReadTimeout(15000);

                        try (InputStream inputStream = connection.getInputStream()) {
                            bitmapFromTheInternet = BitmapFactory.decodeStream(inputStream);
                        }
                        if (bitmapFromTheInternet != null) {
                            return bitmapFromTheInternet;
                        }
                    } catch (MalformedURLException e) {
                        Log.e("DownloadProfileImageAsyncTask",
                              "Error al crear la URL: " + e.getMessage());
                        return bitmapFromTheInternet;
                    } catch (IOException e) {
                        Log.e("DownloadProfileImageAsyncTask",
                              "Error al crear la conexion: " + e.getMessage());
                        return bitmapFromTheInternet;
                    } catch (Exception e) {
                        Log.e("DownloadProfileImageAsyncTask",
                              "Error al descargar la imagen: " + e.getMessage());
                        if (retryCount < 2) {
                            try {
                                Thread.sleep(1000 * 2);
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                return null;
                            }

                        }
                    } finally {
                        if (connection != null) {
                            connection.disconnect();
                        }
                    }
                    retryCount++;
                }
            }
            return bitmapFromTheInternet;
        }

        /**
         * <p>Runs on the UI thread after {@link #doInBackground}. The
         * specified result is the value returned by {@link #doInBackground}. To better support
         * testing frameworks, it is recommended that this be written to tolerate direct execution
         * as part of the execute() call. The default version does nothing.</p>
         *
         * <p>This method won't be invoked if the task was cancelled.</p>
         *
         * @param bitmap The result of the operation computed by {@link #doInBackground}.
         * @see #onPreExecute
         * @see #doInBackground
         * @see #onCancelled(Object)
         */
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null) {
                Log.d("DownloadProfileImageAsyncTask", "Imagen descargada");
                dataViewModel.setClientProfileMutableData(bitmap);
            } else {
                Log.e("DownloadProfileImageAsyncTask", "No se pudo descargar la imagen");
                dataViewModel.setClientProfileMutableData(null);
            }
        }


    }
}