package at.ac.tgm.hit.sew7.jdite.translationapplication;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.TranslateRemoteModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DownloadService extends Service {
    private final IBinder binder = new LocalBinder();
    private DownloadInterface downloadInterface;
    private RemoteModelManager modelManager;
    String [] enterYourLanguagesHere = {
            TranslateLanguage.ESTONIAN,
            TranslateLanguage.JAPANESE,
            TranslateLanguage.FRENCH,
            TranslateLanguage.GERMAN
    };
    ArrayList<Language> wantedLanguages, usedLanguages = new ArrayList();


    public class LocalBinder extends Binder {
        DownloadService getService() {
            // Return this instance of LocalService so clients can call public methods
            return DownloadService.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    public void setDownloadInterface(DownloadInterface downloadInterface) {
        this.downloadInterface = downloadInterface;
    }
    public void startDownloads() {
        wantedLanguages = new ArrayList();
        for(String languageCode: enterYourLanguagesHere) {
            wantedLanguages.add(new Language(languageCode));
        }
        Language english = new Language("en");
        if(!wantedLanguages.contains(english))
            wantedLanguages.add(english);
        modelManager = RemoteModelManager.getInstance();
        modelManager.getDownloadedModels(TranslateRemoteModel.class)
                .addOnSuccessListener(new OnSuccessListener<Set<TranslateRemoteModel>>() {
                    @Override
                    public void onSuccess(Set<TranslateRemoteModel> models) {
                        List<Language> notDownloadedLanguages = wantedLanguages;
                        for(TranslateRemoteModel t:models) {
                            Language language = new Language(t.getLanguage());
                            Log.i("myTAG", "test: "+new Locale(language.toString()).getDisplayLanguage());
                            if(!notDownloadedLanguages.remove(language))
                                modelManager.deleteDownloadedModel(t)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void v) {
                                                Log.i("myTAG", "deletus service");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.i("myTAG", "deletus fail");
                                            }
                                        });
                            else
                                usedLanguages.add(language);
                        }
                        downloadInterface.addLanguages(usedLanguages);
                        if(notDownloadedLanguages.isEmpty())
                            return;
                        downloadInterface.notifyDownload(notDownloadedLanguages.size());
                        for(Language language:notDownloadedLanguages) {
                            modelManager.download(new TranslateRemoteModel.Builder(language.code).build(), new DownloadConditions.Builder().requireWifi().build())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void v) {
                                            Log.i("myTAG", "fat load service");
                                            usedLanguages.add(language);
                                            downloadInterface.downloadedLanguage(language);
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
    public ArrayList<Language> getDownloadedLanguages() {
        return (ArrayList<Language>) usedLanguages.clone();
    }
}