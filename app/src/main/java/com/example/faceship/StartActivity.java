package com.example.faceship;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class StartActivity extends AppCompatActivity {
    private TextInputEditText ed_mail,ed_pas;
    private TextView text_forget,text_sign_up;
    private Button b_sign_in;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth auth;
    private AlertDialog alertDialog;
    private final int REQ_GOOGLE_SIGN=25;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        init();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(auth.getCurrentUser()!=null){
            Toast.makeText(this, "Вы вошли как:"+auth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(StartActivity.this,MainActivity.class));
        }
    }

    private void GoogleAccountManager()
    {
        GoogleSignInOptions googleSignInOptions=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();
        googleSignInClient= GoogleSignIn.getClient(this,googleSignInOptions);
    }
    public void SignIn_Google()
    {
        Intent i =googleSignInClient.getSignInIntent();
        startActivityForResult(i,REQ_GOOGLE_SIGN);
    }
    private void restoreDialog()
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        View view=getLayoutInflater().inflate(R.layout.restore_layout,null);
        builder.setView(view);

        EditText mail=view.findViewById(R.id.r_mail);
        Button button=view.findViewById(R.id.r_b);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setEnabled(false);
                if(TextUtils.isEmpty(mail.getText().toString()))
                {
                    auth.sendPasswordResetEmail(mail.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(StartActivity.this, "Мы отправили вам письмо для подтверждения", Toast.LENGTH_SHORT).show();
                                alertDialog.dismiss();
                            }
                            else
                            {
                                button.setEnabled(true);
                                Toast.makeText(StartActivity.this, "Вы вели неверный e-mail", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else
                {
                    button.setEnabled(true);
                    Toast.makeText(StartActivity.this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                }
            }
        });

        alertDialog=builder.create();
        if(alertDialog.getWindow()!=null)
        {
            alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        alertDialog.show();

    }
    private void init()
    {
        firebaseDatabase=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();

        ed_mail=findViewById(R.id.start_mail);
        ed_pas=findViewById(R.id.start_pas);
        text_forget=findViewById(R.id.start_f_pas);
        text_sign_up=findViewById(R.id.start_up);
        b_sign_in=findViewById(R.id.start_b_in);
        b_sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                b_sign_in.setEnabled(false);
                if(!TextUtils.isEmpty(ed_mail.getText().toString()) && !TextUtils.isEmpty(ed_pas.getText().toString()))
                {
                    signIn(ed_mail.getText().toString(),ed_pas.getText().toString());
                }
                else
                {
                    b_sign_in.setEnabled(true);
                    Toast.makeText(StartActivity.this, "Все поля должны быть заполнены", Toast.LENGTH_SHORT).show();
                }
            }
        });
        text_forget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restoreDialog();
            }
        });
        text_sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transition();
            }
        });
    }

    private void transition()
    {
        Bundle bundle=null;
        View view=findViewById(R.id.start_mail);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            if(view!=null)
            {
                ActivityOptions options=ActivityOptions.makeSceneTransitionAnimation(this,view, this.getString(R.string.transition_act));
                bundle=options.toBundle();
            }
        }
        Intent i=new Intent(StartActivity.this,SignUpActivity.class);
        if(bundle==null)
        {
            startActivity(i);
        }
        else
        {
            startActivity(i,bundle);
        }


    }
    private  void signIn(String mail,String pas)
    {
        auth.signInWithEmailAndPassword(mail,pas).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(StartActivity.this, "Вы вошли как:"+auth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(StartActivity.this,MainActivity.class));
                }
                else
                {
                    b_sign_in.setEnabled(true);
                    Toast.makeText(StartActivity.this, "Вы ввели неверный e-mail или пароль", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}