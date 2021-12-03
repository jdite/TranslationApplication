package at.ac.tgm.hit.sew7.jdite.translationapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.icu.util.ULocale;
import android.net.http.SslCertificate;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentifier;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {
    LanguageIdentifier languageIdentifier;
    EditText inputField;
    TextView inputLang, outputField;
    String currentLang;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inputField = findViewById(R.id.input_field);
        inputLang = findViewById(R.id.input_language);
        outputField = findViewById(R.id.output_field);
        languageIdentifier = LanguageIdentification.getClient();
        inputField.addTextChangedListener(textWatcher);
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
                currentLang = "";
                return;
            }
            languageIdentifier.identifyLanguage(s)
                    .addOnSuccessListener(
                            new OnSuccessListener<String>() {
                                @Override
                                public void onSuccess(@Nullable String languageCode) {
                                    if (!languageCode.equals("und")) {
                                        inputLang.setText(ULocale.getDisplayLanguage(languageCode, ULocale.ENGLISH));
                                        currentLang = languageCode;
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
    boolean downloadError = false;
    public void translate(View view) {
        languageIdentifier.close();
        Translator translator = Translation.getClient(new TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.GERMAN)
            .setTargetLanguage(TranslateLanguage.ENGLISH)
            .build());
        translator.downloadModelIfNeeded(new DownloadConditions.Builder()
            .requireWifi()
            .build())
            .addOnSuccessListener(
                new OnSuccessListener() {
                    @Override
                    public void onSuccess(@NonNull Object o) {
                        downloadError = false;
                    }
            })
            .addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        downloadError = true;
                    }
                });
        Log.i("myTAG", downloadError+"");
        if(downloadError)
            return;
        translator.translate(inputField.getText().toString())
            .addOnSuccessListener(
                new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(@NonNull String translatedText) {
                        Log.i("myTAG", "succ "+translatedText);
                        outputField.setText(translatedText);
                    }
                })
            .addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("myTAG", "fail "+e.getMessage());
                        outputField.setText(e.getMessage());
                    }
                });
        translator.close();
    }
}