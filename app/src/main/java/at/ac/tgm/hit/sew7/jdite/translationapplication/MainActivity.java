package at.ac.tgm.hit.sew7.jdite.translationapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentifier;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.TranslateRemoteModel;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements DownloadInterface {
    LanguageIdentifier languageIdentifier;
    RemoteModelManager modelManager;
    EditText inputField;
    TextView inputLang, outputField;
    Spinner outputLang;
    String currentLang = "";
    ArrayList<Language> usedLanguages;
    ArrayAdapter<Language> arrayAdapter;
    DownloadService mService;
    boolean mBound = false, first = false, currentLangDownloaded = false;
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("myTAG", "tttttttttt");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inputField = findViewById(R.id.input_field);
        inputLang = findViewById(R.id.input_language);
        outputField = findViewById(R.id.output_field);
        outputLang = findViewById(R.id.output_language);
        languageIdentifier = LanguageIdentification.getClient();
        inputField.addTextChangedListener(textWatcher);
        if(savedInstanceState == null)
            first = true;
        Intent intent = new Intent(this, DownloadService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        usedLanguages = new ArrayList<Language>();
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, usedLanguages);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        outputLang.setAdapter(arrayAdapter);
    }
    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        public void afterTextChanged(Editable editable) {
            String s = editable.toString();
            if(s.length() < 3) {
                inputLang.setText("Detect Language");
                inputLang.setTextColor(getResources().getColor(R.color.white));
                currentLang = "";
                return;
            }
            languageIdentifier.identifyLanguage(s)
                    .addOnSuccessListener(
                            new OnSuccessListener<String>() {
                                @Override
                                public void onSuccess(@Nullable String languageCode) {
                                    if (!languageCode.equals("und")) {
                                        Language language = new Language(languageCode);
                                        inputLang.setText(language.toString());
                                        currentLang = languageCode;
                                        if(usedLanguages.contains(language)) {
                                            inputLang.setTextColor(getResources().getColor(R.color.white));
                                            currentLangDownloaded = true;
                                        } else {
                                            inputLang.setTextColor(getResources().getColor(R.color.red));
                                            currentLangDownloaded = false;
                                        }
                                    }
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.i("myTAG", "fail");
                                }
                            });
        }
    };
    @Override
    public void addLanguages(List<Language> languages) {
        for(Language language:languages) {
            usedLanguages.add(language);
        }
        if(!languages.isEmpty())
            arrayAdapter.notifyDataSetChanged();
    }
    @Override
    public void downloadedLanguage(Language language) {
        usedLanguages.add(language);
        arrayAdapter.notifyDataSetChanged();
        if (!currentLangDownloaded && currentLang.equals(language.code)) {
            inputLang.setTextColor(getResources().getColor(R.color.white));
            currentLangDownloaded = true;
        }
        Toast.makeText(getApplicationContext(), "The " + language.toString() + " language model has been downloaded.", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void notifyDownload(int numberOfLangs) {
        String message;
        if(numberOfLangs == 1)
            message = "1 new language model is being downloaded. This may take several minutes.";
        else message = numberOfLangs+" new language models are being downloaded. This may take several minutes.";
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    public void translate(View view) {
        if(currentLang.equals("")) {
            Toast.makeText(getApplicationContext(), "Enter some text into the input field to translate.", Toast.LENGTH_LONG).show();
            return;
        }
        if(!currentLangDownloaded) {
            Toast.makeText(getApplicationContext(), "The model for this input-language is not installed. You can add it in the code.", Toast.LENGTH_LONG).show();
            return;
        }
        Translator translator = Translation.getClient(new TranslatorOptions.Builder()
            .setSourceLanguage(currentLang)
            .setTargetLanguage(((Language) outputLang.getSelectedItem()).code)
            .build());
        translator.translate(inputField.getText().toString())
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(@NonNull String translatedText) {
                                outputField.setText(translatedText);
                                translator.close();
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                outputField.setText(e.getMessage());
                                translator.close();
                            }
                        });
    }
    public void reset (View view) {
        inputField.setText("");
        for(Language l:usedLanguages)
            Log.i("myTAG", "czuerit: "+l.toString());
    }
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            DownloadService.LocalBinder binder = (DownloadService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            mService.setDownloadInterface(MainActivity.this);
            Log.i("myTAG", first+"steve");
            if(first)
                mService.startDownloads();
            else
                addLanguages(mService.getDownloadedLanguages());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}