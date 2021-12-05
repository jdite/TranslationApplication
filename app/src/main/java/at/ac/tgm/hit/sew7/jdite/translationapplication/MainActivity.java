package at.ac.tgm.hit.sew7.jdite.translationapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.icu.util.ULocale;
import android.os.Bundle;
import android.os.PersistableBundle;
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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    LanguageIdentifier languageIdentifier;
    RemoteModelManager modelManager;
    EditText inputField;
    TextView inputLang, outputField;
    Spinner outputLang;
    String currentLang = "und";
    String [] enterYourLanguagesHere = {
            TranslateLanguage.ARABIC,
            TranslateLanguage.JAPANESE,
            TranslateLanguage.FRENCH
    };
    ArrayList<Language> wantedLanguages, usedLanguages;
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
        wantedLanguages = new ArrayList();
        for(String languageCode: enterYourLanguagesHere) {
            wantedLanguages.add(new Language(languageCode));
        }
        Language english = new Language("en");
        if(!wantedLanguages.contains(english))
            wantedLanguages.add(english);
        usedLanguages = new ArrayList<Language>();
        ArrayAdapter<Language> arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, usedLanguages);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        outputLang.setAdapter(arrayAdapter);
        modelManager = RemoteModelManager.getInstance();
        modelManager.getDownloadedModels(TranslateRemoteModel.class)
            .addOnSuccessListener(new OnSuccessListener<Set<TranslateRemoteModel>>() {
            @Override
            public void onSuccess(Set<TranslateRemoteModel> models) {
                List<Language> notDownloadedLanguages = wantedLanguages;
                for(TranslateRemoteModel t:models) {
                    String languageName = t.getLanguage();
                    Log.i("myTAG", "test: "+new Locale(languageName).getDisplayLanguage());
                    if(!notDownloadedLanguages.remove(new Language(languageName)))
                        modelManager.deleteDownloadedModel(t)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void v) {
                                    Log.i("myTAG", "deletus");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.i("myTAG", "deletus fail");
                                }
                            });
                    else
                        usedLanguages.add(new Language(languageName));
                }
                arrayAdapter.notifyDataSetChanged();
                if(notDownloadedLanguages.isEmpty())
                    return;
                if(savedInstanceState == null)
                    Toast.makeText(getApplicationContext(), notDownloadedLanguages.size()+" new language models are being downloaded. This may take several minutes.", Toast.LENGTH_LONG).show();
                for(Language language:notDownloadedLanguages) {
                    modelManager.download(new TranslateRemoteModel.Builder(language.code).build(), new DownloadConditions.Builder().requireWifi().build())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void v) {
                                    Log.i("myTAG", "fat load");
                                    usedLanguages.add(language);
                                    arrayAdapter.notifyDataSetChanged();
                                    if (currentLang.equals(language.toString()))
                                        inputLang.setTextColor(getResources().getColor(R.color.white));
                                    if(savedInstanceState == null)
                                        Toast.makeText(getApplicationContext(), "The " + language.toString() + " language model has been downloaded.", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.i("myTAG", "fat load fail");
                                }
                            });
                }
            }
        })
            .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("myTAG", "asdfghjklö");
            }
        });
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
                currentLang = "und";
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
                                        if(usedLanguages.contains(language)) {
                                            inputLang.setTextColor(getResources().getColor(R.color.white));
                                            currentLang = languageCode;
                                        } else {
                                            inputLang.setTextColor(getResources().getColor(R.color.red));
                                            currentLang = "und";
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
    public void translate(View view) {
        if(currentLang.equals("und")) {
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
        modelManager.getDownloadedModels(TranslateRemoteModel.class)
                .addOnSuccessListener(new OnSuccessListener<Set<TranslateRemoteModel>>() {
                    @Override
                    public void onSuccess(Set<TranslateRemoteModel> models) {
                        for(TranslateRemoteModel t:models)
                            Log.i("myTAG", "asderter: "+new Locale(t.getLanguage()).getDisplayLanguage());
                    }
                });
    }
}