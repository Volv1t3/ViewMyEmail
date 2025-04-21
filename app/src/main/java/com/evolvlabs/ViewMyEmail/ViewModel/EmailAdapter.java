package com.evolvlabs.ViewMyEmail.ViewModel;

import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.evolvlabs.ViewMyEmail.R;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EmailAdapter extends RecyclerView.Adapter<EmailAdapter.EmailAdapterView> {

    private SimpleUserDataViewModel dataViewModel;

    public EmailAdapter(SimpleUserDataViewModel dataViewModel){
        this.dataViewModel = dataViewModel;
    }

    @NonNull
    @NotNull
    @Override
    public EmailAdapterView onCreateViewHolder(@NonNull @NotNull ViewGroup viewGroup, int i) {
        //> 1. Creamos el view holder de la actividad
        View inflatedLayout = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_recycler_view_holder_for_emails,
                         viewGroup,
                         false);
        return new EmailAdapterView(inflatedLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull EmailAdapterView viewHolder, int i) {
        //> 1. Unimos hacia el modelo de datos
        if (this.dataViewModel != null){
            if (this.dataViewModel.getClientRetrievedEmailMessages().getValue() != null){
                UserEmail emailFromUser =
                        this.dataViewModel.getClientRetrievedEmailMessages().getValue().get(i);
                if (emailFromUser != null){
                    viewHolder.bindExternalData(emailFromUser);
                    return;
                }
                else {
                    Log.e("[EmailAdapterComms]", "Email from user is null");
                }
            } else {
                Log.e("[EmailAdapterComms]", "Emails from user is null");
            }
        } else {
            Log.e("[EmailAdapterComms]", "DataViewModel is null");
        }
    }

    @Override
    public int getItemCount() {
        if (this.dataViewModel != null){
            if (this.dataViewModel.getClientRetrievedEmailMessages().getValue() != null){
                return this.dataViewModel.getClientRetrievedEmailMessages().getValue().size();
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public static class EmailAdapterView extends RecyclerView.ViewHolder {

        private TextView inCustomRecyclerHolder_FromWhomComponent;
        private TextView inCustomRecyclerHolder_EmailIDComponent;
        private TextView inCustomRecyclerHolder_ToWhomComponent;
        private TextView inCustomRecyclerHolder_EmailThreadComponent;
        private TextView inCustomRecyclerHolder_EmailSubjectComponent;
        private TextView inCustomRecyclerHolder_EmailDateComponent;
        public EmailAdapterView(@NonNull @NotNull View itemView) {
            super(itemView);

            //> 1. Cargamos todos los componentes
            inCustomRecyclerHolder_FromWhomComponent =
                    itemView.findViewById(R.id.inCustomRecyclerHolder_FromWhomComponent);
            inCustomRecyclerHolder_EmailIDComponent =
                    itemView.findViewById(R.id.inCustomRecyclerHolder_EmailIDComponent);
            inCustomRecyclerHolder_ToWhomComponent =
                    itemView.findViewById(R.id.inCustomRecyclerHolder_ToWhomComponent);
            inCustomRecyclerHolder_EmailThreadComponent =
                    itemView.findViewById(R.id.inCustomRecyclerHolder_EmailThreadComponent);
            inCustomRecyclerHolder_EmailSubjectComponent =
                    itemView.findViewById(R.id.inCustomRecyclerHolder_EmailSubjectComponent);
            inCustomRecyclerHolder_EmailDateComponent =
                    itemView.findViewById(R.id.inCustomRecyclerHolder_EmailDateComponent);
        }

        public void bindExternalData(UserEmail emailInformation){
            inCustomRecyclerHolder_FromWhomComponent
                    .setText(emailInformation.getEmailSenderAccount());
            inCustomRecyclerHolder_EmailIDComponent
                    .setText(emailInformation.getEmailID());
            inCustomRecyclerHolder_ToWhomComponent
                    .setText(emailInformation.getEmailReceiverAccount());
            inCustomRecyclerHolder_EmailThreadComponent
                    .setText(emailInformation.getEmailThreadID());
            inCustomRecyclerHolder_EmailSubjectComponent
                    .setText(emailInformation.getEmailSubjectLine());
            inCustomRecyclerHolder_EmailDateComponent
                    .setText(emailInformation.getEmailReceptionDate());
        }
    }
}
