package com.evolvlabs.ViewMyEmail;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.evolvlabs.ViewMyEmail.ViewModel.SimpleUserDataViewModel;

/**
 * @author : Santiago Arellano
 * @date : 18-Apr-2025
 * @description : El presente archivo implementa la clase principal para la carga de los datos de
 * la aplicacion y la carga de la vista de la aplicacion. Para este sistema se usa un menu de
 * navegacion diferente y especial. Asimismo, se implementa navegacion con botones y menus, asi
 * como diferentes fragmentos
 */
public class ViewMyEmail extends AppCompatActivity {

    /*! Parametros de la Aplicacion*/
    private SimpleUserDataViewModel dataViewModel;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //> 1. Inicializamos el dataViewModel para toda la app
        this.dataViewModel = new ViewModelProvider(this).get(SimpleUserDataViewModel.class);

        //> 2. Cargamos la vista
        setContentView(R.layout.view_my_email_loader);
    }


}
