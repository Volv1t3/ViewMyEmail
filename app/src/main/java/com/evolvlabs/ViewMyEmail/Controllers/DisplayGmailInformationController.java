package com.evolvlabs.ViewMyEmail.Controllers;

import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.evolvlabs.ViewMyEmail.R;
import com.evolvlabs.ViewMyEmail.ViewModel.EmailAdapter;
import com.evolvlabs.ViewMyEmail.ViewModel.SimpleUserDataViewModel;
import com.evolvlabs.ViewMyEmail.ViewModel.UserEmail;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class DisplayGmailInformationController extends Fragment {

    private static final Log log = LogFactory.getLog(DisplayGmailInformationController.class);
    private SimpleUserDataViewModel dataViewModel;
    private MaterialButton menuButtonForNavigation;
    private RecyclerView inDisplayGmailInformation_RecyclerViewForData;
    private DrawerLayout inDemoGoogleGmailView_DrawerLayout;
    private NavigationView navigationView;
    private EmailAdapter adapterForRecyclerView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //> 1. Cargamos la vista original aqui
        return inflater.inflate(R.layout.fragment_display_gmail_information_view,
                                container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //> 1. Cargamos los datos del AndroidViewModel afuera (aplicacion en general) hacia
        // nuestro dispositivo
        dataViewModel = new ViewModelProvider(requireActivity()).get(SimpleUserDataViewModel.class);

        //> 2. Buscamos todos los componentes y los cargamos al sistema
        this.findAndLoadAllComponentUUIDIntoSystem();

        //> 3. Buscamos y cargamos el componente del boton de navegacion, parte principal de la
        // aplicacion
        this.findAndActivateNagivationButtonOnHeader();

        //> 4. Cargamos el comportamiento general del adapter para la lista de datos
        this.findAndActivateRecyclerViewAdapter();

        //> 5. En base al drawer layout, buscamos dentro de el los componentes importantes de la
        // vista del usuario y los conectamos a los valores generales que tenemos en la
        // aplicacion, es decir, al AndroidViewModel
        this.locateInnerComponentsAndCommunicateWithViewModel();

        //> 6. Para evitar que el usuario pueda avanzar en el stack, interceptamos el movimiento
        // de las flechas de la UI y pasamos
        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {            @Override
        public void handleOnBackPressed() {
            return;
        }
        });
    }

    private void findAndLoadAllComponentUUIDIntoSystem() {
        //> 1. Buscamos el menu de navegacion
        this.menuButtonForNavigation = getView().findViewById(R.id.menuButtonForNavigation);
        //> 2. Buscamos el recycler view
        this.inDisplayGmailInformation_RecyclerViewForData =
                getView().findViewById(R.id.inDisplayGmailInformation_RecyclerViewForData);
        //> 3. Buscamos el drawer layout
        this.inDemoGoogleGmailView_DrawerLayout =
                getView().findViewById(R.id.inDemoGoogleGmailView_DrawerLayout);

        //> 4. Cargamos el menu de navegacion
        this.navigationView =
                getView().findViewById(R.id.navigationView);
        NavController navController = NavHostFragment.findNavController(this);
        NavigationUI.setupWithNavController(navigationView, navController);


    }

    private void findAndActivateNagivationButtonOnHeader() {
        //> 1. Como ya tenemos cargado el menu, entonces lo que hacemos es simplemente cargar el
        // listener para que los elementos dentro de esta vista se carguen y salgan como un menu
        // lateral
        menuButtonForNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inDemoGoogleGmailView_DrawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    private void findAndActivateRecyclerViewAdapter() {
        //> 1. Como ya tenemos la vista cargada, entonces nosotros trabajamos para lograr que
        // ahora se conecte a un adapter creado por nuestro lado en base al objeto Email del
        // AndroidViewModel
        this.inDisplayGmailInformation_RecyclerViewForData
                .setLayoutManager(new LinearLayoutManager(requireContext()));

        this.adapterForRecyclerView = new EmailAdapter(this.dataViewModel);

        //> 2. Conectamos el adapter a nuestro recycler view y mandamos una llamda directa a un
        // update del contenido
        this.inDisplayGmailInformation_RecyclerViewForData.setAdapter(this.adapterForRecyclerView);
        this.adapterForRecyclerView.notifyDataSetChanged();

        //> 3. Conectamos el adapter a un observable del AndroidViewModel para que cuando haya
        // cambios este sistema se recarge
        this.dataViewModel.getClientRetrievedEmailMessages().observe(getViewLifecycleOwner(),
                                                                     new Observer<List<UserEmail>>() {
                                                                         @Override
                                                                         public void onChanged(List<UserEmail> userEmails) {
                                                                             adapterForRecyclerView.notifyDataSetChanged();
                                                                         }
                                                                     });


    }

    private void locateInnerComponentsAndCommunicateWithViewModel() {
        //> 1. Conectamos con el drawer layout para buscar sus componentes internos
        View headerView = this.navigationView.getHeaderView(0);
        ShapeableImageView localImageViewForUserPhoto =
                headerView
                        .findViewById(R.id.inCustomUserView_UserProfileIcon);
        TextView localTextViewForUsername = headerView
                .findViewById(R.id.inCustomUserView_UserUsername);
        TextView localTextViewForUserEmail = headerView
                .findViewById(R.id.inCustomUserView_UserEmail);

        //> 2. Conectamos los listeners y el manejo de eventos a estos componentes
        if (this.dataViewModel.getClientUsernameMutableData().getValue() != null &&
                this.dataViewModel.getClientEmailMutableData().getValue() != null) {

            //? 2.1 Conectamos el nombre de usuario si existe, si no entonces tomamos simplemente
            // dos veces el correo
            this.dataViewModel
                    .getClientUsernameMutableData()
                    .observe(getViewLifecycleOwner(),
                             new Observer<String>() {
                                 @Override
                                 public void onChanged(String s) {
                                     if (s == null || s.isEmpty()) {
                                         localTextViewForUsername.setText(dataViewModel
                                                                                  .getClientEmailMutableData()
                                                                                  .getValue());
                                     } else {
                                         localTextViewForUsername.setText(s);
                                     }
                                 }
                             });
            this.dataViewModel
                    .getClientEmailMutableData()
                    .observe(getViewLifecycleOwner(),
                             new Observer<String>() {
                                 @Override
                                 public void onChanged(String s) {
                                     localTextViewForUserEmail.setText(s);
                                 }
                             });
            this.dataViewModel
                    .getClientProfileMutableData()
                    .observe(getViewLifecycleOwner(),
                             new Observer<Bitmap>() {
                                 @Override
                                 public void onChanged(Bitmap bitmap) {
                                     if (bitmap != null) {
                                         localImageViewForUserPhoto.setImageBitmap(bitmap);
                                     } else {
                                         localImageViewForUserPhoto
                                                 .setImageIcon(Icon.createWithResource(requireContext(),
                                                                                       R.drawable.user_image_icon));
                                     }
                                 }
                             });

            //> 3. Conectamos el manejador de eventos para el boton de salida de la aplicacion
            this.navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem menuItem) {
                    if (menuItem.getItemId() == R.id.transition_towards_menu) {
                        handleSignOutInternally();
                        return true;
                    }
                    return false;
                }
            });

        }
    }

    private void handleSignOutInternally() {

        if (dataViewModel.getSignInClientMutableLiveData().getValue() != null &&
                dataViewModel.getClientLoggedInThroughGoogleSignIn().getValue() != null) {
            if (dataViewModel.getClientLoggedInThroughGoogleSignIn().getValue()) {
                GoogleSignInClient signInModel =
                        dataViewModel.getSignInClientMutableLiveData().getValue();
                signInModel.signOut().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        dataViewModel.setClientLoggedInThroughGoogleSignIn(false);
                        dataViewModel.setClientLoggedOutOfApplication(true);
                        dataViewModel.setClientLoggedInThroughNormalLogIn(false);
                        //? Dejamos de un lado toda la informacion del cliente
                        dataViewModel.setClientUsernameMutableData("no_data");
                        dataViewModel.setClientEmailMutableData("no_data");
                        dataViewModel.setClientProfileMutableData(null);
                        //? Dejamos de un lado toda la informacion de los emails
                        dataViewModel.clearClientRetrievedEmailMessagesMutableData();
                        dataViewModel.setClientCredentialsForApplication(null);
                        //? Navegamos a la vista de login
                        try {
                            View currentView = getView();
                            if (currentView != null) {
                                if (isAdded() && getActivity() != null) {
                                    NavController navController = Navigation.findNavController(
                                            getActivity(), R.id.nav_host_fragment);
                                    inDemoGoogleGmailView_DrawerLayout.closeDrawers();
                                    navController.navigate(R.id.action_transition_from_content_to_login_on_sign_out);
                                }
                            }
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                dataViewModel.setClientLoggedInThroughNormalLogIn(false);
                dataViewModel.setClientLoggedOutOfApplication(true);
                //? Dropeamos la info del usuario tambien
                dataViewModel.setClientUsernameMutableData("no_data");
                dataViewModel.setClientEmailMutableData("no_data");
                dataViewModel.setClientProfileMutableData(null);
                //? Dropeamos la info de los emails tambien
                dataViewModel.clearClientRetrievedEmailMessagesMutableData();
                //? Navegamos a la vista de login
                try {
                    View currentView = getView();
                    if (currentView != null) {
                        if (isAdded() && getActivity() != null) {
                            NavController navController = Navigation.findNavController(
                                    getActivity(), R.id.nav_host_fragment);
                            inDemoGoogleGmailView_DrawerLayout.closeDrawers();
                            navController.navigate(R.id.action_transition_from_content_to_login_on_sign_out);
                        }
                    }
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}